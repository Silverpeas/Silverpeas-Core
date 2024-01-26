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

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.form.AbstractForm;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.dummy.DummyRecordSet;
import org.silverpeas.core.contribution.content.form.dummy.DummyRecordTemplate;
import org.silverpeas.core.contribution.content.form.form.HtmlForm;
import org.silverpeas.core.contribution.content.form.form.XmlForm;
import org.silverpeas.core.contribution.content.form.form.XmlSearchForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.contribution.content.form.record.Label;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.contribution.content.form.record.ParameterValue;
import org.silverpeas.core.contribution.content.form.record.Repeatable;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A PublicationTemplate describes a set of publication records built on a same template. A
 * PublicationTemplate groups :
 * <ol>
 * <li>a RecordTemplate which describes the built records.</li>
 * <li>a RecordSet of records built on this template,</li>
 * <li>an update Form used to create and update the publication items</li>
 * <li>a view Form used to show the publications.</li>
 * </ol>
 */
@XmlRootElement(name = "publicationTemplate")
@XmlAccessorType(XmlAccessType.NONE)
public class PublicationTemplateImpl implements PublicationTemplate {

  private static final String HTML_EXTENSION = ".html";
  @XmlElement(required = true)
  private String name = "";
  @XmlElement
  private String description = "";
  @XmlElement(name = "image")
  private String thumbnail = "";
  @XmlElement(required = true, defaultValue = "false")
  private boolean directoryUsage = false;
  @XmlElement(required = true, defaultValue = "false")
  private boolean visible = false;
  @XmlElement(defaultValue = "false")
  private boolean locked = false;
  @XmlElement(required = true, defaultValue = "false")
  private boolean dataEncrypted = false;
  @XmlElementWrapper(name = "spaces")
  @XmlElement(name = "space")
  private List<String> spaces;
  @XmlElementWrapper(name = "applications")
  @XmlElement(name = "application")
  private List<String> applications;
  @XmlElementWrapper(name = "instances")
  @XmlElement(name = "instance")
  private List<String> instances;
  @XmlElementWrapper(name = "domains")
  @XmlElement(name = "domain")
  private List<String> domains;
  @XmlElementWrapper(name = "groups")
  @XmlElement(name = "group")
  private List<String> groups;
  @XmlElement
  private String viewFileName = "";
  @XmlElement
  private String updateFileName = "";
  @XmlElement
  private String searchFileName = "";
  @XmlElement
  private String dataFileName = "";
  @XmlElement
  private String viewTypeFile = "";
  @XmlElement
  private String updateTypeFile = "";
  private String fileName = "";
  private String externalId = "";
  @XmlElement
  private String searchResultFileName = "";
  private RecordTemplate template = null;
  private RecordTemplate searchTemplate = null;
  private RecordTemplate viewTemplate = null;
  private RecordTemplate updateTemplate = null;
  private RecordTemplate searchResultTemplate = null;
  private RecordSet recordSet = null;
  private Form updateForm = null;
  private Form viewForm = null;
  private Form searchResultForm = null;
  public static final int LAYER_ACTION_NONE = 0;
  public static final int LAYER_ACTION_ADD = 1;
  public static final int LAYER_ACTION_REMOVE = 2;
  private int viewLayerAction = LAYER_ACTION_NONE;
  private int updateLayerAction = LAYER_ACTION_NONE;
  private String viewLayerFileName = "";
  private String updateLayerFileName = "";

  private static JAXBContext jaxbContext = null;
  static {
    try {
      jaxbContext =
          JAXBContext.newInstance(GenericRecordTemplate.class,
              GenericFieldTemplate.class,
              Label.class, Parameter.class,
              ParameterValue.class,
              Repeatable.class);
    } catch (JAXBException e) {
      SilverLogger.getLogger(PublicationTemplateImpl.class).error(e.getMessage(), e);
    }
  }

  /**
   * Return the RecordTemplate of the publication data item.
   *
   * @param loadIfNull
   * @return the record template, or a dummy record template if not found (never return
   * <code>null</code> if <code>loadIfNull</code> is <code>true</code>), or <code>null</code> if not
   * loaded and <code>loadIfNull</code> is <code>false</code>.
   * @throws PublicationTemplateException
   */
  public RecordTemplate getRecordTemplate(boolean loadIfNull) throws PublicationTemplateException {
    if ((template != null) || !loadIfNull) {
      return template;
    }
    RecordTemplate tmpl = loadRecordTemplate(dataFileName);
    if ((tmpl != null) && !(tmpl instanceof DummyRecordTemplate)) {
      template = tmpl;
    }
    return tmpl;
  }

  @Override
  public RecordTemplate getRecordTemplate() throws PublicationTemplateException {
    return getRecordTemplate(true);
  }

  /**
   * Return the RecordSet of all the records built from this template.
   *
   * @return the record set or a dummy record set if not found (never return <code>null</code>).
   * @throws PublicationTemplateException
   */
  @Override
  public RecordSet getRecordSet() throws PublicationTemplateException {
    try {
      if (recordSet != null) {
        return recordSet;
      }
      RecordSet rs = getGenericRecordSetManager().getRecordSet(this.externalId);
      if ((rs != null) && !(rs instanceof DummyRecordSet)) {
        recordSet = rs;
      }
      if (template != null && rs instanceof DummyRecordSet) {
        rs = new DummyRecordSet(template);
      }
      return rs;
    } catch (FormException e) {
      throw new PublicationTemplateException(e);
    }
  }

  /**
   * Returns the Form used to create and update the records built from this template.
   */
  @Override
  public Form getUpdateForm() throws PublicationTemplateException {
    if (updateForm == null) {
      updateForm = getForm(updateFileName, updateTypeFile);
    }
    return updateForm;
  }


  /* (non-Javadoc)
   * @see org.silverpeas.core.contribution.template.publication.PublicationTemplate#getUpdateFormAsXMLOne()
   */
  @Override
  public XmlForm getUpdateFormAsXMLOne() throws PublicationTemplateException {
    return (XmlForm) getForm(updateFileName.replaceAll(HTML_EXTENSION, ".xml"), "xml");
  }

  /**
   * Returns the Form used to view the records built from this template.
   */
  @Override
  public Form getViewForm() throws PublicationTemplateException {
    if (viewForm == null) {
      viewForm = getForm(viewFileName, viewTypeFile, true);
    }
    return viewForm;
  }

  /**
   * Returns the RecordTemplate of the publication search item.
   */
  public RecordTemplate getSearchTemplate(boolean loadIfNull)
      throws PublicationTemplateException {
    if (searchTemplate == null && loadIfNull) {
      searchTemplate = loadRecordTemplate(searchFileName);
    }
    return searchTemplate;
  }

  /**
   * Returns the RecordTemplate of the publication search item.
   */
  public RecordTemplate getSearchTemplate() throws PublicationTemplateException {
    return getSearchTemplate(true);
  }

  /**
   * Returns the RecordTemplate of the publication view item.
   */
  public RecordTemplate getViewTemplate() {
    return viewTemplate;
  }

  /**
   * Returns the RecordTemplate of the publication update item.
   */
  public RecordTemplate getUpdateTemplate() {
    return updateTemplate;
  }

  public RecordTemplate getDataTemplate() throws PublicationTemplateException {
    return getRecordTemplate(false);
  }

  @Override
  public Form getSearchForm() throws PublicationTemplateException {
    Form searchForm = null;
    if (isSearchable()) {
      RecordTemplate templateForm = loadRecordTemplate(searchFileName);
      try {
        searchForm = new XmlSearchForm(templateForm);
        searchForm.setFormName(FilenameUtils.getBaseName(this.fileName));
      } catch (FormException e) {
        throw new PublicationTemplateException(e);
      }
    }
    return searchForm;
  }

  private Form getForm(String fileName, String fileType) throws PublicationTemplateException {
    return getForm(fileName, fileType, false);
  }

  private Form getForm(String fileName, String fileType, boolean viewForm)
      throws PublicationTemplateException {
    AbstractForm form;
    RecordTemplate templateForm;
    String currentFileName = fileName;

    String typeFile = FileRepositoryManager.getFileExtension(currentFileName);
    if (StringUtil.isDefined(fileType)) {
      typeFile = fileType.trim().toLowerCase();
    }

    if (StringUtil.isDefined(currentFileName) && "xml".equals(typeFile)) {
      templateForm = loadRecordTemplate(currentFileName);
      mergeTemplate(templateForm);
      try {
        form = new XmlForm(templateForm, viewForm);
      } catch (FormException e) {
        throw new PublicationTemplateException(e);
      }
    } else {
      PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
      String htmlFileName = templateManager.makePath(currentFileName);
      currentFileName = currentFileName.replaceAll(HTML_EXTENSION, ".xml");
      templateForm = loadRecordTemplate(currentFileName);
      mergeTemplate(templateForm);
      try {
        HtmlForm viewFormHtml = new HtmlForm(templateForm);
        viewFormHtml.setFileName(htmlFileName);
        form = viewFormHtml;
      } catch (FormException e) {
        throw new PublicationTemplateException(e);
      }
    }
    form.setFormName(FilenameUtils.getBaseName(this.fileName));
    form.setTitle(getName());
    return form;
  }

  /**
   * Merge the data template with the form template
   *
   * @param formTemplate
   * @throws PublicationTemplateException
   */
  private void mergeTemplate(RecordTemplate formTemplate) throws PublicationTemplateException {
    RecordTemplate dataTemplate = getRecordTemplate();
    if (formTemplate != null && dataTemplate != null) {
      String[] fieldNames = formTemplate.getFieldNames();
      GenericFieldTemplate formFieldTemplate;
      GenericFieldTemplate dataFieldTemplate;
      for (String fieldName : fieldNames) {
        try {
          formFieldTemplate = (GenericFieldTemplate) formTemplate.getFieldTemplate(fieldName);
          dataFieldTemplate = (GenericFieldTemplate) dataTemplate.getFieldTemplate(fieldName);
          formFieldTemplate.setTypeName(dataFieldTemplate.getTypeName());
          formFieldTemplate.setParametersObj(dataFieldTemplate.getParametersObj());
          formFieldTemplate.setLabelsObj(dataFieldTemplate.getLabelsObj());
        } catch (FormException e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
  }

  /**
   *
   */
  public void setViewFileName(String viewFileName) {
    this.viewFileName = viewFileName;
  }

  /**
   *
   */
  public String getViewFileName() {
    return viewFileName;
  }

  /**
   *
   */
  public void setUpdateFileName(String updateFileName) {
    this.updateFileName = updateFileName;
  }

  /**
   *
   */
  public String getUpdateTypeFile() {
    return updateTypeFile;
  }

  /**
   *
   */
  public void setUpdateTypeFile(String updateTypeFile) {
    this.updateTypeFile = updateTypeFile;
  }

  /**
   *
   */
  public String getViewTypeFile() {
    return viewTypeFile;
  }

  /**
   *
   */
  public void setViewTypeFile(String viewTypeFile) {
    this.viewTypeFile = viewTypeFile;
  }

  /**
   *
   */
  public String getUpdateFileName() {
    return updateFileName;
  }

  /**
   *
   */
  public void setDataFileName(String dataFileName) {
    this.dataFileName = dataFileName;
  }

  /**
   *
   */
  public String getDataFileName() {
    return dataFileName;
  }

  /**
   *
   */
  @Override
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   *
   */
  @Override
  public String getExternalId() {
    return externalId;
  }

  /**
   * load a recordTemplate definition from xml file to java objects
   *
   * @param xmlFileName the xml file name that contains process model definition
   * @return a RecordTemplate object
   */
  public RecordTemplate loadRecordTemplate(String xmlFileName) throws PublicationTemplateException {
    if (!StringUtil.isDefined(xmlFileName)) {
      return null;
    }
    final PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    String filePath = templateManager.makePath(xmlFileName);

    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      GenericRecordTemplate recordTemplate = (GenericRecordTemplate) unmarshaller.unmarshal(new File(filePath));
      recordTemplate.setTemplateName(fileName.substring(0, fileName.lastIndexOf('.')));
      return recordTemplate;
    } catch (JAXBException e) {
      throw new PublicationTemplateException(
          "Fail to unmarshal publication template " + xmlFileName, e);
    }


  }

  /**
   * This method saves current templates inside a sub directory
   *
   * @throws PublicationTemplateException
   */
  public void saveRecordTemplates() throws PublicationTemplateException {
    String subDir = fileName.substring(0, fileName.lastIndexOf('.'))
        + File.separator;
    if (template != null) {
      saveRecordTemplate(template, subDir, "data.xml");
    }
    if (viewTemplate != null) {
      saveRecordTemplate(viewTemplate, subDir, "view.xml");
    }
    if (updateTemplate != null) {
      saveRecordTemplate(updateTemplate, subDir, "update.xml");
    }
    if (searchTemplate != null) {
      saveRecordTemplate(searchTemplate, subDir, "search.xml");
    }
    if (searchResultTemplate != null) {
      saveRecordTemplate(searchResultTemplate, subDir, "searchresult.xml");
    }
  }

  /**
   * Save a recordTemplate to xml file
   *
   * @param recordTemplate the object to save as xml File
   * @param subDir the sub directory where saving the xml file
   * @param xmlFileName the xml file name
   * @throws PublicationTemplateException
   */
  private void saveRecordTemplate(RecordTemplate recordTemplate, String subDir,
      String xmlFileName) throws PublicationTemplateException {

    // save record into XML file
    try {
      // Format this URL
      PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
      String xmlFilePath = templateManager.makePath(subDir + xmlFileName);

      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(recordTemplate, new File(xmlFilePath));

    } catch (JAXBException e) {
      throw new PublicationTemplateException(
          "Fail to marshall publication template " + xmlFileName, e);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public boolean isLocked() {
    return locked;
  }

  public void setLocked(final boolean locked) {
    this.locked = locked;
  }

  @Override
  public boolean isDirectoryUsage() {
    return directoryUsage;
  }

  public void setDirectoryUsage(final boolean directoryUsage) {
    this.directoryUsage = directoryUsage;
  }

  public String getSearchFileName() {
    return searchFileName;
  }

  public void setSearchFileName(String searchFileName) {
    this.searchFileName = searchFileName;
  }

  public void setSearchTemplate(RecordTemplate searchTemplate) {
    this.searchTemplate = searchTemplate;
  }

  public void setTemplate(RecordTemplate template) {
    this.template = template;
  }

  public void setUpdateTemplate(RecordTemplate updateTemplate) {
    this.updateTemplate = updateTemplate;
  }

  public void setViewTemplate(RecordTemplate viewTemplate) {
    this.viewTemplate = viewTemplate;
  }

  /**
   * @return the searchResultTemplateFileName
   */
  public String getSearchResultFileName() {
    return searchResultFileName;
  }

  /**
   * @param searchResultFileName the search result template file name to set
   */
  public void setSearchResultFileName(String searchResultFileName) {
    this.searchResultFileName = searchResultFileName;
  }

  @Override
  public boolean isSearchable() {
    return (searchFileName != null && searchFileName.trim().length() > 0);
  }

  /**
   * @return the searchResultTemplate
   */
  public RecordTemplate getSearchResultTemplate() {
    return searchResultTemplate;
  }

  /**
   * @param searchResultTemplate the searchResultTemplate to set
   */
  public void setSearchResultTemplate(RecordTemplate searchResultTemplate) {
    this.searchResultTemplate = searchResultTemplate;
  }

  /**
   * @return a copy of the current PublicationTemplate implementation
   */
  public PublicationTemplateImpl basicClone() {
    PublicationTemplateImpl cloneTemplate = new PublicationTemplateImpl();
    cloneTemplate.setName(getName());
    cloneTemplate.setDescription(getDescription());
    cloneTemplate.setThumbnail(getThumbnail());
    cloneTemplate.setFileName(getFileName());
    cloneTemplate.setVisible(isVisible());
    cloneTemplate.setLocked(isLocked());
    cloneTemplate.setDataEncrypted(isDataEncrypted());
    cloneTemplate.setViewFileName(getViewFileName());
    cloneTemplate.setUpdateFileName(getUpdateFileName());
    cloneTemplate.setSearchFileName(getSearchFileName());
    cloneTemplate.setDataFileName(getDataFileName());
    cloneTemplate.setViewTypeFile(getViewTypeFile());
    cloneTemplate.setUpdateTypeFile(getUpdateTypeFile());
    cloneTemplate.setExternalId(getExternalId());
    cloneTemplate.setSearchResultFileName(getSearchResultFileName());
    cloneTemplate.setSpaces(getSpaces());
    cloneTemplate.setApplications(getApplications());
    cloneTemplate.setInstances(getInstances());
    cloneTemplate.setDomains(getDomains());
    cloneTemplate.setGroups(getGroups());
    cloneTemplate.setDirectoryUsage(isDirectoryUsage());
    cloneTemplate.setViewLayerFileName(getViewLayerFileName());
    cloneTemplate.setUpdateLayerFileName(getUpdateLayerFileName());
    return cloneTemplate;
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   *
   * @return a GenericRecordSetManager instance.
   */
  protected GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }

  @Override
  public Form getSearchResultForm() throws PublicationTemplateException {
    if (searchResultForm == null) {
      searchResultForm = getForm(searchResultFileName, null);
    }
    return searchResultForm;
  }

  @Override
  public List<String> getFieldsForFacets() {
    List<String> fieldNames = new ArrayList<>();
    try {
      FieldTemplate[] fieldTemplates = getRecordTemplate().getFieldTemplates();
      for (FieldTemplate fieldTemplate : fieldTemplates) {
        if (fieldTemplate.isUsedAsFacet()) {
          fieldNames.add(fieldTemplate.getFieldName());
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return fieldNames;
  }

  @Override
  public List<String> getSpaces() {
    return spaces;
  }

  @Override
  public boolean isRestrictedVisibilityToSpace() {
    return getSpaces() != null && !getSpaces().isEmpty();
  }

  @Override
  public boolean isRestrictedVisibilityToApplication() {
    return getApplications() != null && !getApplications().isEmpty();
  }

  @Override
  public boolean isRestrictedVisibilityToInstance() {
    return getInstances() != null && !getInstances().isEmpty();
  }

  public void setSpaces(List<String> spaces) {
    this.spaces = spaces;
  }

  @Override
  public List<String> getApplications() {
    return applications;
  }

  public void setApplications(List<String> applications) {
    this.applications = applications;
  }

  @Override
  public List<String> getInstances() {
    return instances;
  }

  public void setInstances(List<String> instances) {
    this.instances = instances;
  }

  public void setDomains(List<String> domains) {
    this.domains = domains;
  }

  @Override
  public List<String> getDomains() {
    return domains;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  @Override
  public List<String> getGroups() {
    return groups;
  }

  @Override
  public boolean isVisibleToDomain(String domainId) {
    return CollectionUtil.isEmpty(getDomains()) || getDomains().contains(domainId) ;
  }

  @Override
  public boolean isVisibleToUser(String userId) {
    if (CollectionUtil.isEmpty(getGroups())) {
      return isVisibleToDomain(User.getById(userId).getDomainId());
    }
    String[] groupIds = OrganizationController.get().getAllGroupIdsOfUser(userId);
    return !CollectionUtil.intersection(getGroups(), Arrays.asList(groupIds)).isEmpty();
  }

  @Override
  public boolean isRestrictedVisibility() {
    return isRestrictedVisibilityToSpace() || isRestrictedVisibilityToApplication()
        || isRestrictedVisibilityToInstance();
  }

  public void setDataEncrypted(boolean dataEncrypted) {
    this.dataEncrypted = dataEncrypted;
  }

  @Override
  public boolean isDataEncrypted() {
    return dataEncrypted;
  }

  public int getViewLayerAction() {
    return viewLayerAction;
  }

  public void setViewLayerAction(int viewLayerAction) {
    this.viewLayerAction = viewLayerAction;
  }

  public int getUpdateLayerAction() {
    return updateLayerAction;
  }

  public void setUpdateLayerAction(int updateLayerAction) {
    this.updateLayerAction = updateLayerAction;
  }

  public String getViewLayerFileName() {
    return viewLayerFileName;
  }

  public void setViewLayerFileName(String viewLayerFileName) {
    this.viewLayerFileName = viewLayerFileName;
  }

  public String getUpdateLayerFileName() {
    return updateLayerFileName;
  }

  public void setUpdateLayerFileName(String updateLayerFileName) {
    this.updateLayerFileName = updateLayerFileName;
  }

  public boolean isViewLayerDefined() {
    return StringUtil.isDefined(viewLayerFileName);
  }

  public boolean isUpdateLayerDefined() {
    return StringUtil.isDefined(updateLayerFileName);
  }

  @Override
  public boolean isViewLayerExist() {
    return getViewFileName().endsWith(HTML_EXTENSION);
  }

  @Override
  public boolean isUpdateLayerExist() {
    return getUpdateFileName().endsWith(HTML_EXTENSION);
  }
}
