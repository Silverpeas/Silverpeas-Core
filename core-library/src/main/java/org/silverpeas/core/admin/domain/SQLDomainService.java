/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain;

import org.silverpeas.core.admin.domain.driver.sqldriver.SQLSettings;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.exception.DomainAuthenticationPropertiesAlreadyExistsException;
import org.silverpeas.core.admin.domain.exception.DomainConflictException;
import org.silverpeas.core.admin.domain.exception.DomainCreationException;
import org.silverpeas.core.admin.domain.exception.DomainDeletionException;
import org.silverpeas.core.admin.domain.exception.DomainPropertiesAlreadyExistsException;
import org.silverpeas.core.admin.domain.repository.SQLDomainRepository;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;

@Singleton
@Named("sqlDomainService")
public class SQLDomainService extends AbstractDomainService {
  SettingBundle templateSettings;
  SettingBundle adminSettings;

  private final static String DATABASE_TABLE_NAME_DOMAIN_PREFIX = "Domain";
  private final static String DATABASE_TABLE_NAME_DOMAIN_USER_SUFFIX = "_User";
  private final static String DATABASE_TABLE_NAME_DOMAIN_GROUP_SUFFIX = "_Group";
  private final static String DATABASE_TABLE_NAME_DOMAIN_USER_GROUP_SUFFIX = "_Group_User_Rel";

  @Inject
  private SQLDomainRepository dao;

  @PostConstruct
  void init() {
    templateSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.domains.templateDomainSQL");
    adminSettings = ResourceLocator.getSettingBundle("org.silverpeas.admin.admin");
  }

  private void checkFileName(String fileDomainName)
      throws DomainAuthenticationPropertiesAlreadyExistsException,
      DomainPropertiesAlreadyExistsException {

    // Check properties files availability
    // org.silverpeas.domains.domain<domainName>.properties
    // org.silverpeas.authentication.autDomain<domainName>.properties
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(fileDomainName);
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(fileDomainName);

    if (new File(authenticationPropertiesPath).exists()) {
      SilverTrace.error("admin", "SQLDomainService.checkFileName",
          "admin.MSG_ERR_DOMAIN_ALREADY_EXIST_DOMAIN_PROPERTIES", fileDomainName);
      throw new DomainAuthenticationPropertiesAlreadyExistsException(fileDomainName);
    }

    if (new File(domainPropertiesPath).exists()) {
      SilverTrace.error("admin", "SQLDomainService.checkFileName",
          "admin.MSG_ERR_DOMAIN_ALREADY_EXIST_DOMAIN_PROPERTIES", fileDomainName);
      throw new DomainPropertiesAlreadyExistsException(fileDomainName);
    }
  }

  /**
   * Gets a file name without special characters and without accentued characters in the aim to
   * create domain property files safely on file system.
   * @param domain with a name that may contain some special characters and/or accentued characters
   * @return
   */
  protected String getTechnicalDomainName(Domain domain) {

    // Normalizing the name (accents, puissance, ...)
    String fileDomainName = FileServerUtils.replaceAccentChars(domain.getName());
    fileDomainName = Normalizer.normalize(fileDomainName, Normalizer.Form.NFKD);

    // Replacing of each sequence of special characters by nothing
    fileDomainName = fileDomainName.replaceAll("[^\\p{Alnum}]+", "");

    // Limitations of some databases on length of table or column names : compute max length
    int maxTableNameSuffixLength = Math.max(DATABASE_TABLE_NAME_DOMAIN_USER_SUFFIX.length(),
        DATABASE_TABLE_NAME_DOMAIN_GROUP_SUFFIX.length());
    maxTableNameSuffixLength =
        Math.max(maxTableNameSuffixLength, DATABASE_TABLE_NAME_DOMAIN_USER_GROUP_SUFFIX.length());
    int maxLength =
        SQLSettings.DATABASE_TABLE_NAME_MAX_LENGTH - DATABASE_TABLE_NAME_DOMAIN_PREFIX.length() -
            maxTableNameSuffixLength - domain.getId().length();

    // The technical name is the addition of the part of the domain id and the part of the
    // normalized (and resized) domain name. The domain name is unique by this way
    return domain.getId() + StringUtil.left(fileDomainName, maxLength);
  }

  @Transactional
  @Override
  public String createDomain(Domain domainToCreate)
      throws DomainConflictException, DomainCreationException {

    // Check domain name
    String initialDomainName = domainToCreate.getName();
    try {
      checkDomainName(initialDomainName);
    } catch (AdminException e) {
      throw new DomainConflictException("SQLDomainService.createDomain", domainToCreate.toString(),
          e);
    }

    // Get the next domain identifier to work on it
    String domainId = getNextDomainId();
    domainToCreate.setId(domainId);

    // Get the technical name of the domain
    String technicalDomainName = getTechnicalDomainName(domainToCreate);

    // Check that it doesn't exist a file with the computed technical name
    checkFileName(technicalDomainName);

    try {

      // Set the technical name to the domain for technical treatments
      domainToCreate.setName(technicalDomainName);

      // Generates domain properties file
      generateDomainPropertiesFile(domainToCreate);

      // Generates domain authentication properties file
      generateDomainAuthenticationPropertiesFile(domainToCreate);

      // Create storage
      dao.createDomainStorage(domainToCreate);

      // SQL Driver might be override for some purpose
      if (!StringUtil.isDefined(domainToCreate.getDriverClassName())) {
        domainToCreate.setDriverClassName("org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver");
      }
      domainToCreate.setPropFileName("org.silverpeas.domains.domain" + technicalDomainName);
      domainToCreate.setAuthenticationServer("autDomain" + technicalDomainName);
      domainToCreate.setTheTimeStamp("0");

      // Enregistre le nom initial dans la table st_domain
      domainToCreate.setName(initialDomainName);
      registerDomain(domainToCreate);

    } catch (Exception e) {

      /*
      Roll back all things that have been created
       */

      try {
        removePropertiesFiles(technicalDomainName);
      } catch (Exception anyE) {
        // Nothing to do ...
      }

      try {
        domainToCreate.setName(technicalDomainName);
        dao.deleteDomainStorage(domainToCreate);
      } catch (Exception anyE) {
        // Nothing to do ...
      }

      try {
        domainToCreate.setName(initialDomainName);
        unRegisterDomain(domainToCreate);
      } catch (Exception anyE) {
        // Nothing to do ...
      }

      if (e instanceof DomainCreationException) {
        throw (DomainCreationException) e;
      }
      throw new DomainCreationException("SQLDomainService.createDomain", domainToCreate.toString(),
          e);
    }

    return domainId;
  }

  @Transactional
  @Override
  public String deleteDomain(Domain domainToRemove) throws DomainDeletionException {

    // Retrieve the prefix of a domain property file name
    String separator = "#@#@#@#@#";
    String domainPropertyPrefix = new File(
        FileRepositoryManager.getDomainPropertiesPath(separator).replaceAll(separator + ".*$", ""))
        .getName();
    // Get the domain property file name without the package
    String domainPropertyFileName =
        domainToRemove.getPropFileName().replaceAll("[\\p{Alnum}]+\\.+", "");
    // Compute the common property file name by removing the prefix of a domain property file name
    String fileDomainName = domainPropertyFileName.replaceFirst(domainPropertyPrefix, "");
    domainToRemove.setName(fileDomainName);

    // unregister new Domain dans st_domain
    String domainId = unRegisterDomain(domainToRemove);
    if (!StringUtil.isDefined(domainId)) {
      throw new DomainDeletionException("SQLDomainService.deleteDomain");
    }

    // Remove storage
    try {
      dao.deleteDomainStorage(domainToRemove);
    } catch (Exception e) {
      throw new DomainDeletionException("SQLDomainService.deleteDomain", e);
    }

    // Remove domain authentication properties file
    removeDomainPropertiesFile(domainToRemove);

    return domainId;
  }

  /**
   * Delete phisycally domain and authentication properties files
   * @param domainName domain name concerned
   */
  private void removePropertiesFiles(String domainName) {
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainName);
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);
    new File(authenticationPropertiesPath).delete();
    new File(domainPropertiesPath).delete();
  }

  /**
   * Generates domain properties file
   * @param domainToCreate domain to create
   * @throws DomainCreationException
   */
  private void generateDomainPropertiesFile(Domain domainToCreate) throws DomainCreationException {


    String domainName = domainToCreate.getName();

    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);

    SilverpeasTemplate template = getNewTemplate();
    template.setAttribute("SQLDataSourceJNDIName", adminSettings.getString("DefaultDataSourceJNDIName"));
    template.setAttribute("SQLUserTableName",
        DATABASE_TABLE_NAME_DOMAIN_PREFIX + domainName + DATABASE_TABLE_NAME_DOMAIN_USER_SUFFIX);
    template.setAttribute("SQLGroupTableName",
        DATABASE_TABLE_NAME_DOMAIN_PREFIX + domainName + DATABASE_TABLE_NAME_DOMAIN_GROUP_SUFFIX);
    template.setAttribute("SQLUserGroupTableName", DATABASE_TABLE_NAME_DOMAIN_PREFIX + domainName +
        DATABASE_TABLE_NAME_DOMAIN_USER_GROUP_SUFFIX);

    File domainPropertiesFile = new File(domainPropertiesPath);
    PrintWriter out = null;
    try {
      out = new PrintWriter(new FileWriter(domainPropertiesFile));
      out.print(template.applyFileTemplate("templateDomainSQL"));
    } catch (IOException e) {
      domainPropertiesFile.delete();
      throw new DomainCreationException("SQLDomainService.generateDomainPropertiesFile()",
          domainToCreate.toString(), e);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Generates domain authentication properties file
   * @param domainToCreate domain to create
   * @throws DomainCreationException
   */
  private void generateDomainAuthenticationPropertiesFile(Domain domainToCreate)
      throws DomainCreationException {


    String domainName = domainToCreate.getName();
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainName);

    boolean allowPasswordChange = templateSettings.getBoolean("allowPasswordChange", true);

    SilverpeasTemplate template = getNewTemplate();
    template.setAttribute("allowPasswordChange", allowPasswordChange);

    template.setAttribute("SQLDataSourceJNDIName", adminSettings.getString("DefaultDataSourceJNDIName"));
    template.setAttribute("SQLUserTableName",
        DATABASE_TABLE_NAME_DOMAIN_PREFIX + domainName + DATABASE_TABLE_NAME_DOMAIN_USER_SUFFIX);

    File domainPropertiesFile = new File(domainPropertiesPath);
    File authenticationPropertiesFile = new File(authenticationPropertiesPath);
    PrintWriter out = null;
    try {
      out = new PrintWriter(new FileWriter(authenticationPropertiesFile));
      out.print(template.applyFileTemplate("templateDomainAuthenticationSQL"));
    } catch (IOException e) {
      domainPropertiesFile.delete();
      authenticationPropertiesFile.delete();
      throw new DomainCreationException(
          "SQLDomainService.generateDomainAuthenticationPropertiesFile()", domainToCreate
          .toString(), e);
    } finally {
      if (out != null) {
      out.close();
    }
  }
  }

  /**
   * Remove domain authentication and settings properties file
   * @param domainToRemove domain to remove
   * @throws DomainDeletionException
   */
  private void removeDomainPropertiesFile(Domain domainToRemove) {


    String domainName = domainToRemove.getName();
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainName);

    File domainPropertiesFile = new File(domainPropertiesPath);
    File authenticationPropertiesFile = new File(authenticationPropertiesPath);

    boolean domainPropertiesFileDeleted = domainPropertiesFile.delete();
    boolean authenticationPropertiesFileDeleted = authenticationPropertiesFile.delete();

    if (!(domainPropertiesFileDeleted && authenticationPropertiesFileDeleted)) {
      SilverTrace.warn("admin", "SQLDomainService.removeDomainAuthenticationPropertiesFile()",
          "admin.EX_DELETE_DOMAIN_PROPERTIES", "domainPropertiesFileDeleted:" +
          domainPropertiesFileDeleted + ", authenticationPropertiesFileDeleted:" +
          authenticationPropertiesFileDeleted);
    }
  }

  /**
   * Return SilverpeasTemplate
   * @return
   */
  private SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("admin/sqlDomain");
  }

}
