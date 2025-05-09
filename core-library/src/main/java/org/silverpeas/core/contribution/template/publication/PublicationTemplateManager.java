/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.template.publication;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.FormEncryptionContentIterator;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSet;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.contribution.content.form.record.IdentifiedRecordTemplate;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.security.encryption.ContentEncryptionServiceProvider;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.annotation.Nullable;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.kernel.util.SystemWrapper;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The PublicationTemplateManager manages all the PublicationTemplate for all the Job'Peas. It is a
 * singleton.
 */
@Service
@Singleton
public class PublicationTemplateManager implements ComponentInstanceDeletion {

  // PublicationTemplates instances associated to silverpeas components. Theses templates should
  // already exist and be loaded.
  // map externalId -> PublicationTemplate
  private final Map<String, PublicationTemplate> externalTemplates = new HashMap<>();
  // All of the PublicationTemplates loaded in silverpeas and identified by their XML file.
  // map templateFileName -> PublicationTemplate to avoid multiple marshalling
  private final Map<String, PublicationTemplateImpl> templates = new HashMap<>();
  private String templateDir;
  private String defaultTemplateDir;
  private JAXBContext jaxbContext = null;

  @PostConstruct
  private void setup() {
    final SettingBundle templateSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.publicationTemplate.settings.template");
    templateDir = templateSettings.getString("templateDir");
    defaultTemplateDir =
        SystemWrapper.getInstance().getenv("SILVERPEAS_HOME") + "/data/templateRepository/";
    try {
      jaxbContext = JAXBContext.newInstance(PublicationTemplateImpl.class);
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

  /**
   * Makes the path denoted by the specified file name relative to the template directory.
   * @param fileName the path of a file or a directory
   * @return the absolute path of the specified file name in the template directory.
   */
  public String makePath(String fileName) {
    if (!StringUtil.isDefined(templateDir)) {
      return fileName;
    }
    if (!StringUtil.isDefined(fileName)) {
      return templateDir;
    }
    return Paths.get(templateDir, fileName).toString().replace('\\', '/');
  }

  private String makeDefaultPath(String fileName) {
    if (!StringUtil.isDefined(defaultTemplateDir)) {
      return fileName;
    }
    if (!StringUtil.isDefined(fileName)) {
      return defaultTemplateDir;
    }
    return Paths.get(defaultTemplateDir, fileName).toString().replace('\\', '/');
  }

  public String getTemplateDirectoryPath() {
    return this.templateDir;
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
      return getGenericRecordSetManager()
          .createRecordSet(externalId, recordTemplate, fileName, thePubTemplate.isDataEncrypted());
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "Fail to add dynamic publication template " + templateFileName + " for " + externalId, e);
    }
  }

  public PublicationTemplate getPublicationTemplate(String externalId)
      throws PublicationTemplateException {
    return getPublicationTemplate(externalId, null);
  }

  /**
   * Returns the PublicationTemplate having the given externalId.
   * @param externalId the identifier of an instance of the asked publication template. The
   * external identifier is the identifier of a template instance used for a resource in an external
   * Silverpeas component.
   * @param templateFileName the name of the file in which is stored the template.
   * @return the publication template used to generate the specified template instance.
   * @throws PublicationTemplateException if an error occurs while getting the template.
   */
  public PublicationTemplate getPublicationTemplate(String externalId, String templateFileName)
      throws PublicationTemplateException {
    String currentTemplateFileName = templateFileName;
    PublicationTemplate thePubTemplate = externalTemplates.get(externalId);
    if (thePubTemplate == null) {
      if (templateFileName == null) {
        try {
          RecordSet set = getGenericRecordSetManager().getRecordSet(externalId);
          RecordTemplate template = set.getRecordTemplate();
          if (template instanceof IdentifiedRecordTemplate) {
            currentTemplateFileName = ((IdentifiedRecordTemplate) template).getTemplateName();
          } else {
            throw new PublicationTemplateException("Unexpected type of data record template: " +
                template.getClass().getSimpleName());
          }
        } catch (Exception e) {
          throw new PublicationTemplateException(
              "Fail to get publication template for " + externalId, e);
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
   * @param externalId the identifier of an instance of the publication template to remove. The
   * external identifier is the identifier of a template instance used for a resource in an external
   * Silverpeas component.
   * @throws PublicationTemplateException if the deletion fails.
   */
  public void removePublicationTemplate(String externalId) throws PublicationTemplateException {
    try {
      getGenericRecordSetManager().removeRecordSet(externalId);
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "Fail to remove publication template for " + externalId, e);
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
   * @throws PublicationTemplateException if the loading fails.
   */
  public PublicationTemplate loadPublicationTemplate(String xmlFileName)
      throws PublicationTemplateException {

    try {
      PublicationTemplateImpl publicationTemplate = templates.get(xmlFileName);
      if (publicationTemplate != null) {
        return publicationTemplate.basicClone();
      }
      String xmlFilePath = makePath(xmlFileName);

      File xmlFile = new File(xmlFilePath);
      if (!xmlFile.exists()) {
        // file does not exist in directory, try to locate it in default one
        xmlFilePath = makeDefaultPath(xmlFileName);
        xmlFile = new File(xmlFilePath);
      }

      if (!xmlFile.exists()) {
        return null;
      }

      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      publicationTemplate = (PublicationTemplateImpl) unmarshaller.unmarshal(xmlFile);
      publicationTemplate.setFileName(xmlFileName);

      templates.put(xmlFileName, publicationTemplate);

      return publicationTemplate.basicClone();
    } catch (JAXBException e) {
      throw new PublicationTemplateException("Fail to load publication template " + xmlFileName, e);
    }
  }

  /**
   * Save a publicationTemplate definition from java objects to xml file
   * @param template the PublicationTemplate to save
   * @throws PublicationTemplateException if the saving of the publication template fails.
   * @throws CryptoException if the encryption of the template data fails.
   */
  public void savePublicationTemplate(PublicationTemplate template)
      throws PublicationTemplateException, CryptoException {


    String xmlFileName = template.getFileName();

    PublicationTemplate previousTemplate = loadPublicationTemplate(xmlFileName);
    boolean encryptionChanged =
        previousTemplate != null && template.isDataEncrypted() != previousTemplate.isDataEncrypted();

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
      String xmlFilePath = makePath(xmlFileName);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(template, new File(xmlFilePath));

    } catch (JAXBException e) {
      throw new PublicationTemplateException("Fail to save publication template " + xmlFileName, e);
    }
  }

  /**
   * Retrieve Publication Templates
   * @param onlyVisible only visible templates boolean
   * @return only visible PublicationTemplates if onlyVisible is true, all the publication templates
   * else if
   * @throws PublicationTemplateException if the getting of the templates fails
   */
  public List<PublicationTemplate> getPublicationTemplates(boolean onlyVisible)
      throws PublicationTemplateException {
    List<PublicationTemplate> publicationTemplates = new ArrayList<>();
    Collection<File> templateNames;
    try {
      templateNames = FileFolderManager.getAllFile(templateDir);
    } catch (UtilException e1) {
      throw new PublicationTemplateException("Fail to get publication templates", e1);
    }
    for (File templateFile : templateNames) {
      String fileName = templateFile.getName();
      try {
        String extension = FileRepositoryManager.getFileExtension(fileName);
        if ("xml".equalsIgnoreCase(extension)) {
          PublicationTemplate template = loadPublicationTemplate(
              fileName.substring(fileName.lastIndexOf(File.separator) + 1));
          if (onlyVisible) {
            if (template.isVisible()) {
              publicationTemplates.add(template);
            }
          } else {
            publicationTemplates.add(template);
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }

    return publicationTemplates;
  }

  /**
   * @return only the visible PublicationTemplates
   * @throws PublicationTemplateException if the getting of the templates fails.
   */
  public List<PublicationTemplate> getPublicationTemplates() throws PublicationTemplateException {
    return getPublicationTemplates(true);
  }

  /**
   * @param globalContext the global context in which is indicated the components in which the
   * templates are used. It can be null to get all the publication templates.
   * @return a list of publication templates matching the specified global context.
   * @throws PublicationTemplateException if an error occurs while getting the templates
   */
  public List<PublicationTemplate> getPublicationTemplates(@Nullable GlobalContext globalContext)
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

  public List<PublicationTemplate> getDirectoryTemplates() {
    List<PublicationTemplate> directoryTemplates = new ArrayList<>();
    try {
      List<PublicationTemplate> theTemplates = getPublicationTemplates(true);
      for (PublicationTemplate template : theTemplates) {
        if (template.isDirectoryUsage()) {
          directoryTemplates.add(template);
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return directoryTemplates;
  }

  public boolean isPublicationTemplateVisible(String templateName, GlobalContext globalContext)
      throws PublicationTemplateException {
    PublicationTemplate template = loadPublicationTemplate(templateName);
    return isPublicationTemplateVisible(template, globalContext);
  }

  private boolean isPublicationTemplateVisible(PublicationTemplate template, GlobalContext globalContext) {
    if (template.isDirectoryUsage()) {
      // this template is not available to components
      return false;
    }

    if (!template.isRestrictedVisibility()) {
      return true;
    }

    // template is restricted
    // check it according to current space and component
    if (template.isRestrictedVisibilityToInstance()) {
      return isTemplateVisibleAccordingToInstance(template, globalContext);
    } else {
      return isAllowed(template, globalContext);
    }
  }

  private boolean isAllowed(final PublicationTemplate template, final GlobalContext globalContext) {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    boolean allowed = !template.isRestrictedVisibilityToApplication() ||
        isTemplateVisibleAccordingToApplication(template, globalContext, oc);
    if (allowed) {
      if (!template.isRestrictedVisibilityToSpace()) {
        return true;
      } else {
        return isTemplateVisibleAccordingToSpace(template, globalContext, oc);
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
    List<String> restrictedToSpaceIds = template.getSpaces();
    List<SpaceInstLight> spacePath = oc.getPathToSpace(context.getSpaceId());
    for (SpaceInstLight space : spacePath) {
      String spaceId = space.getId();
      if (restrictedToSpaceIds.contains(spaceId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the list of PublicationTemplate which contains a search form
   * @throws PublicationTemplateException if the templates cannot be got.
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
        SilverLogger.getLogger(this).warn(e);
      }
    }

    return searchableTemplates;
  }

  /**
   * @return the list of PublicationTemplate which are encrypted
   * @throws PublicationTemplateException if the getting of the templates fails.
   */
  public List<PublicationTemplate> getEncryptedPublicationTemplates()
      throws PublicationTemplateException {
    List<PublicationTemplate> encryptedTemplates = new ArrayList<>();

    List<PublicationTemplate> publicationTemplates = getPublicationTemplates();
    for (PublicationTemplate template : publicationTemplates) {
      if (template.isDataEncrypted()) {
        encryptedTemplates.add(template);
      }
    }

    return encryptedTemplates;
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

  @SuppressWarnings("unused")
  protected void registerForRenewingContentCipher() {
    EncryptionContentIterator contentIterator = new FormEncryptionContentIterator();
    ContentEncryptionServiceProvider.getContentEncryptionService()
        .registerForContentCiphering(contentIterator);
  }

  public void saveData(String xmlFormName, PagesContext context, List<FileItem> items)
      throws SilverpeasException {
    if (context == null) {
      throw new SilverpeasException("Context must be defined !");
    }
    if (StringUtil.isNotDefined(context.getComponentId()) ||
        StringUtil.isNotDefined(context.getObjectId()) ||
        StringUtil.isNotDefined(context.getLanguage()) ||
        StringUtil.isNotDefined(context.getUserId())) {
      throw new SilverpeasException(
          "Context is not complete ! ComponentId, ObjectId, Language and UserId must be " +
              "defined...");
    }

    String ignoreThisForm = FileUploadUtil.getParameter(items, "ignoreThisForm");
    if (StringUtil.isDefined(xmlFormName) && !StringUtil.getBooleanValue(ignoreThisForm)) {
      PublicationTemplate pub = getTemplateAndRegisterIt(xmlFormName, context);
      try {
        RecordSet set = pub.getRecordSet();
        Form form = pub.getUpdateForm();
        DataRecord data = set.getRecord(context.getObjectId());
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId(context.getObjectId());
        }

        // sauvegarde des données du formulaire
        form.update(items, data, context);
        set.save(data);
      } catch (Exception e) {
        throw new SilverpeasException("Unable to save data", e);
      }
    }
  }

  private PublicationTemplate getTemplateAndRegisterIt(String xmlFormName, PagesContext context)
      throws SilverpeasException {
    String shortName = getShortName(xmlFormName);
    String externalId = context.getComponentId() + ":" + shortName;
    PublicationTemplate pub;
    try {
      // récupération des données du formulaire (via le DataRecord)
      pub = getPublicationTemplate(externalId);
      if (pub == null) {
        SilverLogger.getLogger(this)
            .warn("This template is not yet registered");
        pub = registerTemplate(externalId, shortName);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("This template is not yet registered. Error: " + e.getMessage());
      pub = registerTemplate(externalId, shortName);
    }
    return pub;
  }

  private PublicationTemplate registerTemplate(final String externalId, final String shortName)
      throws SilverpeasException {
    PublicationTemplate pub;
    try {
      addDynamicPublicationTemplate(externalId, shortName);
      pub = getPublicationTemplate(externalId);
    } catch (Exception templateException) {
      throw new SilverpeasException("Unable to register template", templateException);
    }
    return pub;
  }

  public Form getFormAndData(String xmlFormName, PagesContext context, boolean readOnly)
      throws SilverpeasException {
    if (StringUtil.isNotDefined(xmlFormName)) {
      return null;
    }
    PublicationTemplate pub = getTemplateAndRegisterIt(xmlFormName, context);
    try {
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      if (readOnly) {
        form = pub.getViewForm();
      }
      DataRecord data = set.getRecord(context.getObjectId());
      if (data == null) {
        if (readOnly) {
          return null;
        }
        data = set.getEmptyRecord();
        data.setId(context.getObjectId());
      }
      form.setData(data);
      return form;
    } catch (Exception e) {
      throw new SilverpeasException("Unable to get data", e);
    }
  }

  public void setDataIntoIndex(String xmlFormName, String componentId, String userId,
      FullIndexEntry indexEntry) {
    try {
      String shortName = getShortName(xmlFormName);
      PublicationTemplate usedTemplate = getPublicationTemplate(componentId + ":" + shortName);
      RecordSet set = usedTemplate.getRecordSet();
      set.indexRecord(userId, shortName, indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public PublicationTemplate getDirectoryTemplate() {
    List<PublicationTemplate> directoryTemplates = getDirectoryTemplates();
    if (CollectionUtil.isNotEmpty(directoryTemplates)) {
      PublicationTemplate template = directoryTemplates.get(0);
      setExternalId(template);
      return template;
    }
    return null;
  }

  public PublicationTemplate getDirectoryTemplate(PagesContext context) {
    String domainId = context.getDomainId();
    String userId = context.getObjectId();
    if (StringUtil.isDefined(userId)) {
      domainId = User.getById(userId).getDomainId();
    }
    List<PublicationTemplate> directoryTemplates = getDirectoryTemplates();
    for (PublicationTemplate template : directoryTemplates) {
      if ((StringUtil.isDefined(userId) && template.isVisibleToUser(userId)) ||
          (StringUtil.isNotDefined(userId) && template.isVisibleToDomain(domainId))) {
        setExternalId(template);
        return template;
      }
    }
    return null;
  }

  private void setExternalId(PublicationTemplate template) {
    String shortName = getShortName(template.getFileName());
    String externalId = UserDetail.USER_COMPONENT + ":" + shortName;
    template.setExternalId(externalId);
  }

  public Form getDirectoryForm(PagesContext context, boolean viewMode) {
    PublicationTemplate template = getDirectoryTemplate(context);
    if (template != null) {
      try {
        return getFormAndData(template.getFileName(), context, viewMode);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  public void deleteDirectoryData(String userId) {
    PublicationTemplate template = getDirectoryTemplate();
    if (template != null) {
      try {
        template.getRecordSet().delete(userId);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  private String getShortName(String name) {
    String shortName = name;
    if (shortName.contains(".")) {
      shortName = name.substring(name.indexOf("/") + 1, name.indexOf("."));
    }
    return shortName;
  }

  public Map<String, String> getDirectoryFormValues(String userId, String domainId,
      String language) {
    PagesContext pageContext = getFormContext(userId, domainId, language);
    PublicationTemplate template = getDirectoryTemplate(pageContext);
    if (template != null) {
      try {
        DataRecord data = template.getRecordSet().getRecord(userId);
        if (data != null) {
          return data.getValues(language);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return Collections.emptyMap();
  }

  public Map<String, String> getDirectoryFormLabels(String userId, String domainId,
      String language) {
    Map<String, String> labels = new HashMap<>();
    PagesContext pageContext = getFormContext(userId, domainId, language);
    PublicationTemplate template = getDirectoryTemplate(pageContext);
    if (template != null) {
      try {
        FieldTemplate[] fields = template.getRecordTemplate().getFieldTemplates();
        for (FieldTemplate field : fields) {
          labels.put(field.getFieldName(), field.getLabel(language));
        }
        return labels;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return Collections.emptyMap();
  }

  private PagesContext getFormContext(String userId, String domainId, String language) {
    PagesContext pageContext = new PagesContext();
    pageContext.setDomainId(domainId);
    pageContext.setObjectId(userId);
    pageContext.setLanguage(language);
    return pageContext;
  }

  public Map<String, Integer> getNumberOfRecordsByTemplateAndComponents(String templateName)
      throws FormException {
    return getGenericRecordSetManager().getNumberOfRecordsByTemplateAndComponents(templateName);
  }

}
