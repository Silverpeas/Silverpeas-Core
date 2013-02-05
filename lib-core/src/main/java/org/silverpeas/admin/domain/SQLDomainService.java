/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package org.silverpeas.admin.domain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.admin.domain.exception.DomainAuthenticationPropertiesAlreadyExistsException;
import org.silverpeas.admin.domain.exception.DomainConflictException;
import org.silverpeas.admin.domain.exception.DomainCreationException;
import org.silverpeas.admin.domain.exception.DomainDeletionException;
import org.silverpeas.admin.domain.exception.DomainPropertiesAlreadyExistsException;
import org.silverpeas.admin.domain.repository.SQLDomainRepository;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

@Named("sqlDomainService")
public class SQLDomainService extends AbstractDomainService {
  ResourceLocator templateSettings;
  ResourceLocator adminSettings;

  @Inject
  @Named("sqlInternalDomainRepository")
  SQLDomainRepository dao;

  @PostConstruct
  void init() {
    templateSettings =
        new ResourceLocator("org.silverpeas.domains.templateDomainSQL", "");
    adminSettings = new ResourceLocator("org.silverpeas.beans.admin.admin", "");
  }

  @Override
  protected void checkDomainName(String domainName) throws DomainConflictException, AdminException {

    // Commons checks
    super.checkDomainName(domainName);

    // Check properties files availability
    // org.silverpeas.domains.domain<domainName>.properties
    // org.silverpeas.authentication.autDomain<domainName>.properties
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainName);
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);

    if (new File(authenticationPropertiesPath).exists()) {
      throw new DomainAuthenticationPropertiesAlreadyExistsException(domainName);
    }

    if (new File(domainPropertiesPath).exists()) {
      throw new DomainPropertiesAlreadyExistsException(domainName);
    }
  }

  @Override
  public String createDomain(Domain domainToCreate) throws DomainConflictException,
      DomainCreationException {

    // Check domain name
    String domainName = domainToCreate.getName();
    try {
      checkDomainName(domainName);
    } catch (AdminException e) {
      throw new DomainCreationException("SQLDomainService.createDomain", domainToCreate.toString(),
          e);
    }

    // Generates domain properties file
    generateDomainPropertiesFile(domainToCreate);

    // Generates domain authentication properties file
    generateDomainAuthenticationPropertiesFile(domainToCreate);

    // Create storage
    try {
      dao.createDomainStorage(domainToCreate);
    } catch (Exception e) {
      removePropertiesFiles(domainName);
      throw new DomainCreationException("SQLDomainService.createDomain", domainToCreate.toString(),
          e);
    }

    // register new Domain
    // SQL Driver might be override for some purpose
    if (!StringUtil.isDefined(domainToCreate.getDriverClassName())) {
      domainToCreate.setDriverClassName("com.stratelia.silverpeas.domains.sqldriver.SQLDriver");
    }
    domainToCreate.setPropFileName("org.silverpeas.domains.domain" + domainName);
    domainToCreate.setAuthenticationServer("autDomain" + domainName);
    domainToCreate.setTheTimeStamp("0");
    String domainId = registerDomain(domainToCreate);
    if (!StringUtil.isDefined(domainId)) {
      try {
        dao.deleteDomainStorage(domainToCreate);
      } catch (Exception e) {
        removePropertiesFiles(domainName);
      }
      removePropertiesFiles(domainName);
    }

    return domainId;
  }

  @Override
  public String deleteDomain(Domain domainToRemove) throws DomainDeletionException {

    // unregister new Domain
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
    SilverTrace
        .info(
        "admin",
        "SQLDomainService.generateDomainPropertiesFile()",
        "root.MSG_GEN_ENTER_METHOD");

    String domainName = domainToCreate.getName();
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);

    SilverpeasTemplate template = getNewTemplate();
    template.setAttribute("SQLClassName", adminSettings.getString("AdminDBDriver"));
    template.setAttribute("SQLJDBCUrl", adminSettings.getString("WaProductionDb"));
    template.setAttribute("SQLAccessLogin", adminSettings.getString("WaProductionUser"));
    template.setAttribute("SQLAccessPasswd", adminSettings.getString("WaProductionPswd"));
    template.setAttribute("SQLUserTableName", "Domain" + domainName + "_User");
    template.setAttribute("SQLGroupTableName", "Domain" + domainName + "_Group");
    template.setAttribute("SQLUserGroupTableName", "Domain" + domainName + "_Group_User_Rel");

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
    SilverTrace
        .info(
        "admin",
        "SQLDomainService.generateDomainAuthenticationPropertiesFile()",
        "root.MSG_GEN_ENTER_METHOD");

    String domainName = domainToCreate.getName();
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainName);

    boolean allowPasswordChange = templateSettings.getBoolean("allowPasswordChange", true);

    SilverpeasTemplate template = getNewTemplate();
    template.setAttribute("allowPasswordChange", allowPasswordChange);

    template.setAttribute("SQLDriverClass", adminSettings.getString("AdminDBDriver"));
    template.setAttribute("SQLJDBCUrl", adminSettings.getString("WaProductionDb"));
    template.setAttribute("SQLAccessLogin", adminSettings.getString("WaProductionUser"));
    template.setAttribute("SQLAccessPasswd", adminSettings.getString("WaProductionPswd"));
    template.setAttribute("SQLUserTableName", "Domain" + domainName + "_User");

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
      out.close();
    }
  }

  /**
   * Remove domain authentication and settings properties file
   * @param domainToRemove domain to remove
   * @throws DomainDeletionException
   */
  private void removeDomainPropertiesFile(Domain domainToRemove) throws DomainDeletionException {
    SilverTrace
        .info(
        "admin",
        "SQLDomainService.removeDomainAuthenticationPropertiesFile()",
        "root.MSG_GEN_ENTER_METHOD");

    String domainName = domainToRemove.getName();
    String domainPropertiesPath = FileRepositoryManager.getDomainPropertiesPath(domainName);
    String authenticationPropertiesPath =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainName);

    File domainPropertiesFile = new File(domainPropertiesPath);
    File authenticationPropertiesFile = new File(authenticationPropertiesPath);

    boolean domainPropertiesFileDeleted = domainPropertiesFile.delete();
    boolean authenticationPropertiesFileDeleted = authenticationPropertiesFile.delete();

    if ((!domainPropertiesFileDeleted) || (!authenticationPropertiesFileDeleted)) {
      SilverTrace
          .warn(
          "admin",
          "SQLDomainService.removeDomainAuthenticationPropertiesFile()",
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
