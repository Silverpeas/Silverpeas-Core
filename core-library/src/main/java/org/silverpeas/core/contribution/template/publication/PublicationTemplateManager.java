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

package org.silverpeas.core.contribution.template.publication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.FormEncryptionContentIterator;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSet;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.contribution.content.form.record.IdentifiedRecordTemplate;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.security.encryption.ContentEncryptionServiceProvider;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.admin.service.OrganizationController;

/**
 * The PublicationTemplateManager manages all the PublicationTemplate for all the Job'Peas. It is a
 * singleton.
 */
@Singleton
public class PublicationTemplateManager implements ComponentInstanceDeletion {

  // PublicationTemplates instances associated to silverpeas components. Theses templates should
  // already exist and be loaded.
  // map externalId -> PublicationTemplate
  private final Map<String, PublicationTemplate> externalTemplates = new HashMap<>();
  // All of the PublicationTemplates loaded in silverpeas and identified by their XML file.
  // map templateFileName -> PublicationTemplate to avoid multiple marshalling
  private final Map<String, PublicationTemplateImpl> templates = new HashMap<>();
  public static String templateDir = null;
  public static String defaultTemplateDir = null;
  private JAXBContext JAXB_CONTEXT = null;

  static {
    SettingBundle templateSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.publicationTemplate.settings.template");

    templateDir = templateSettings.getString("templateDir");
    defaultTemplateDir =
        SystemWrapper.get().getenv("SILVERPEAS_HOME") + "/data/templateRepository/";
  }

  @PostConstruct
  private void setup() {
    try {
      JAXB_CONTEXT =
          JAXBContext.newInstance(PublicationTemplateImpl.class);
    } catch (JAXBException e) {
      SilverLogger.getLogger(this).error("can not initialize JAXB_CONTEXT", e);
    }
  }

  /**
   * Gets the single instance of this manager.
   * @return the single instance of PublicationTemplateManager.
   */
  public static PublicationTemplateManager getInstance() {
    return ServiceProvider.getService(PublicationTemplateManager.class);
  }

  public static String makePath(String dirName, String fileName) {
    if (!StringUtil.isDefined(dirName)) {
      return fileName;
    }
    if (!StringUtil.isDefined(fileName)) {
      return dirName;
    }

    if (dirName.charAt(dirName.length() - 1) == '/' || dirName.charAt(dirName.length() - 1) == '\\') {
      return dirName.replace('\\', '/') + fileName.replace('\\', '/');
    }
    return dirName.replace('\\', '/') + "/" + fileName.replace('\\', '/');
  }

  public GenericRecordSet addDynamicPublicationTemplate(String externalId, String templateFileName)
      throws PublicationTemplateException {
    String fileName = templateFileName;
    try {
      if (!fileName.endsWith(".xml")) {
        fileName += ".xml";
      }
      PublicationTemplate thePubTemplate = loadPublicationTemplate(fileName);
      RecordTemplate recordTemplate = thePubTemplate.getRecordTemplate();
      return getGenericRecordSetManager().createRecordSet(externalId, recordTemplate, fileName,
          thePubTemplate.isDataEncrypted());
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "PublicationTemplateManager.addDynamicPublicationTemplate", "form.EXP_INSERT_FAILED",
          "externalId=" + externalId + ", templateFileName=" + templateFileName, e);
    }
  }

  public PublicationTemplate getPublicationTemplate(String externalId)
      throws PublicationTemplateException {
    return getPublicationTemplate(externalId, null);
  }

  /**
   * Returns the PublicationTemplate having the given externalId.
   * @param externalId
   * @param templateFileName
   * @return
   * @throws PublicationTemplateException
   */
  public PublicationTemplate getPublicationTemplate(String externalId,
      String templateFileName) throws PublicationTemplateException {
    String currentTemplateFileName = templateFileName;
    PublicationTemplate thePubTemplate = externalTemplates.get(externalId);
    if (thePubTemplate == null) {
      if (templateFileName == null) {
        try {
          RecordSet set = getGenericRecordSetManager().getRecordSet(externalId);
          IdentifiedRecordTemplate template = (IdentifiedRecordTemplate) set.getRecordTemplate();
          currentTemplateFileName = template.getTemplateName();
        } catch (Exception e) {
          throw new PublicationTemplateException(
              "PublicationTemplateManager.getPublicationTemplate", "form.EXP_INSERT_FAILED",
              "externalId=" + externalId + ", templateFileName=" + templateFileName, e);
        }
      }
      thePubTemplate = loadPublicationTemplate(currentTemplateFileName);
      thePubTemplate.setExternalId(externalId);
      externalTemplates.put(externalId, thePubTemplate);
    }

    return thePubTemplate;
  }

  /**
   * Removes the PublicationTemplate having the given externalId.
   * @param externalId
   * @throws PublicationTemplateException
   */
  public void removePublicationTemplate(String externalId) throws PublicationTemplateException {
    try {
      getGenericRecordSetManager().removeRecordSet(externalId);
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "PublicationTemplateManager.removePublicationTemplate",
          "form.EXP_DELETE_FAILED", "externalId=" + externalId, e);
    }
  }

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      List<String> externalIds =
          getGenericRecordSetManager().getExternalIdOfComponentInstanceId(componentInstanceId);
      for (String externalId : externalIds) {
        removePublicationTemplate(externalId);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(
          "error during deletion of data of component instance identifier " + componentInstanceId,
          e);
    }
  }

  /**
   * load a publicationTemplate definition from xml file to java objects
   * @param xmlFileName the xml file name that contains publication template definition
   * @return a PublicationTemplate object
   * @throws PublicationTemplateException
   */
  public PublicationTemplate loadPublicationTemplate(String xmlFileName) throws
      PublicationTemplateException {

    try {
      PublicationTemplateImpl publicationTemplate = templates.get(xmlFileName);
      if (publicationTemplate != null) {
        return publicationTemplate.basicClone();
      }
      String xmlFilePath = makePath(templateDir, xmlFileName);

      File xmlFile = new File(xmlFilePath);
      if (!xmlFile.exists()) {
        // file does not exist in directory, try to locate it in default one
        xmlFilePath = makePath(defaultTemplateDir, xmlFileName);
        xmlFile = new File(xmlFilePath);
      }

      if (!xmlFile.exists()) {
        return null;
      }

      Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
      publicationTemplate = (PublicationTemplateImpl) unmarshaller.unmarshal(xmlFile);
      publicationTemplate.setFileName(xmlFileName);

      templates.put(xmlFileName, publicationTemplate);

      return publicationTemplate.basicClone();
    } catch (JAXBException e) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_UNMARSHALL_PUBLICATION_TEMPLATE", "Publication Template FileName : "
          + xmlFileName, e);
    }
  }

  /**
   * Save a publicationTemplate definition from java objects to xml file
   * @param template the PublicationTemplate to save
   * @throws PublicationTemplateException
   * @throws CryptoException
   */
  public void savePublicationTemplate(PublicationTemplate template) throws
      PublicationTemplateException, CryptoException {


    String xmlFileName = template.getFileName();

    PublicationTemplate previousTemplate = loadPublicationTemplate(xmlFileName);
    boolean encryptionChanged =
        previousTemplate != null &&
            template.isDataEncrypted() != previousTemplate.isDataEncrypted();

    if (encryptionChanged) {
      if (template.isDataEncrypted()) {
        getGenericRecordSetManager().encryptData(xmlFileName);
      } else {
        getGenericRecordSetManager().decryptData(xmlFileName);
      }
    }

    // save template into XML file
    try {
      // Format this URL
      String xmlFilePath = makePath(templateDir, xmlFileName);

      Marshaller marshaller = JAXB_CONTEXT.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(template, new File(xmlFilePath));

    } catch (JAXBException e) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_UNMARSHALL_PUBLICATION_TEMPLATE", "Publication Template FileName : "
          + xmlFileName, e);
    }
  }

  /**
   * Retrieve Publication Templates
   * @param onlyVisibles only visible templates boolean
   * @return only visible PublicationTemplates if onlyVisible is true, all the publication templates
   * else if
   * @throws PublicationTemplateException
   */
  public List<PublicationTemplate> getPublicationTemplates(boolean onlyVisibles)
      throws PublicationTemplateException {
    List<PublicationTemplate> publicationTemplates = new ArrayList<>();
    Collection<File> templateNames;
    try {
      templateNames = FileFolderManager.getAllFile(templateDir);
    } catch (UtilException e1) {
      throw new PublicationTemplateException("PublicationTemplateManager.getPublicationTemplates",
          "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATES", e1);
    }
    for (File templateFile : templateNames) {
      String fileName = templateFile.getName();
      try {
        String extension = FileRepositoryManager.getFileExtension(fileName);
        if ("xml".equalsIgnoreCase(extension)) {
          PublicationTemplate template =
              loadPublicationTemplate(fileName.substring(fileName.lastIndexOf(File.separator) + 1,
              fileName.length()));
          if (onlyVisibles) {
            if (template.isVisible()) {
              publicationTemplates.add(template);
            }
          } else {
            publicationTemplates.add(template);
          }
        }
      } catch (Exception e) {
        SilverTrace.error("form", "PublicationTemplateManager.getPublicationTemplates",
            "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATE", "fileName = " + fileName);
      }
    }

    return publicationTemplates;
  }

  /**
   * @return only the visible PublicationTemplates
   * @throws PublicationTemplateException
   */
  public List<PublicationTemplate> getPublicationTemplates()
      throws PublicationTemplateException {
    return getPublicationTemplates(true);
  }

  /**
   * @param globalContext componentName It can be null. It is usefull when componentId is not defined.
   * @return
   * @throws PublicationTemplateException
   */
  public List<PublicationTemplate> getPublicationTemplates(GlobalContext globalContext)
      throws PublicationTemplateException {
    List<PublicationTemplate> theTemplates = getPublicationTemplates(true);
    if (globalContext == null) {
      return theTemplates;
    }
    List<PublicationTemplate> allowedTemplates = new ArrayList<>();
    for (PublicationTemplate template : theTemplates) {
      if (isPublicationTemplateVisible(template, globalContext)) {
        allowedTemplates.add(template);
      }
    }
    return allowedTemplates;
  }

  public boolean isPublicationTemplateVisible(String templateName, GlobalContext globalContext)
      throws PublicationTemplateException {
    PublicationTemplate template = loadPublicationTemplate(templateName);
    return isPublicationTemplateVisible(template, globalContext);
  }

  private boolean isPublicationTemplateVisible(PublicationTemplate template, GlobalContext globalContext) {
    if (!template.isRestrictedVisibility()) {
      return true;
    } else {
      // template is restricted
      // check it according to current space and component
      if (template.isRestrictedVisibilityToInstance()) {
        if (isTemplateVisibleAccordingToInstance(template, globalContext)) {
          return true;
        }
      } else {
        OrganizationController oc =
            OrganizationControllerProvider.getOrganisationController();
        boolean allowed = true;
        if (template.isRestrictedVisibilityToApplication()) {
          if (!isTemplateVisibleAccordingToApplication(template, globalContext, oc)) {
            allowed = false;
          }
        }
        if (allowed) {
          if (!template.isRestrictedVisibilityToSpace()) {
            return true;
          } else {
            if (isTemplateVisibleAccordingToSpace(template, globalContext, oc)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean isTemplateVisibleAccordingToInstance(PublicationTemplate template,
      GlobalContext context) {
    List<String> restrictedInstanceIds = template.getInstances();
    return restrictedInstanceIds.contains(context.getComponentId());
  }

  private boolean isTemplateVisibleAccordingToApplication(PublicationTemplate template,
      GlobalContext context, OrganizationController oc) {
    List<String> restrictedApplications = template.getApplications();
    String componentName = context.getComponentName();
    if (StringUtil.isDefined(context.getComponentId())) {
      ComponentInstLight component = oc.getComponentInstLight(context.getComponentId());
      componentName = component.getName();
    }
    return restrictedApplications.contains(componentName);
  }

  private boolean isTemplateVisibleAccordingToSpace(PublicationTemplate template,
      GlobalContext context, OrganizationController oc) {
    List<String> restrictedSpaceIds = template.getSpaces();
    List<SpaceInst> spacePath = oc.getSpacePath(context.getSpaceId());
    for (SpaceInst space : spacePath) {
      String spaceId = space.getId();
      if (restrictedSpaceIds.contains(spaceId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the list of PublicationTemplate which contains a search form
   * @throws PublicationTemplateException
   */
  public List<PublicationTemplate> getSearchablePublicationTemplates()
      throws PublicationTemplateException {
    List<PublicationTemplate> searchableTemplates = new ArrayList<>();

    List<PublicationTemplate> publicationTemplates = getPublicationTemplates();
    for (PublicationTemplate template : publicationTemplates) {
      try {
        if (template.getSearchForm() != null) {
          searchableTemplates.add(template);
        }
      } catch (PublicationTemplateException e) {
        // Catch exception here in case of one of searchable form is malformed
        // Valid forms must be displayed in search screen
        SilverTrace.warn("form", "PublicationTemplateManager.getSearchablePublicationTemplates",
            "form.ERROR_ONE_ILL_FORM", template.getName() + " is malformed");
      }
    }

    return searchableTemplates;
  }

  /**
   * @return the list of PublicationTemplate which are crypted
   * @throws PublicationTemplateException
   */
  public List<PublicationTemplate> getCryptedPublicationTemplates()
      throws PublicationTemplateException {
    List<PublicationTemplate> cryptedTemplates = new ArrayList<>();

    List<PublicationTemplate> publicationTemplates = getPublicationTemplates();
    for (PublicationTemplate template : publicationTemplates) {
      if (template.isDataEncrypted()) {
        cryptedTemplates.add(template);
      }
    }

    return cryptedTemplates;
  }

  /**
   * @param fileName the file name of the template to remove from cache
   */
  public void removePublicationTemplateFromCaches(String fileName) {

    List<String> externalIdsToRemove = new ArrayList<>();
    Collection<PublicationTemplate> publicationTemplates = externalTemplates.values();
    for (PublicationTemplate template : publicationTemplates) {
      if (template.getFileName().equals(fileName)) {
        externalIdsToRemove.add(template.getExternalId());
      }
    }
    for (String externalId : externalIdsToRemove) {
      externalTemplates.remove(externalId);
    }

    templates.remove(fileName);

    getGenericRecordSetManager().removeTemplateFromCache(fileName);
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  private static GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }

  protected void registerForRenewingContentCipher() {
    EncryptionContentIterator contentIterator = new FormEncryptionContentIterator();
    ContentEncryptionServiceProvider.getContentEncryptionService()
        .registerForRenewingContentCipher(contentIterator);
  }
}