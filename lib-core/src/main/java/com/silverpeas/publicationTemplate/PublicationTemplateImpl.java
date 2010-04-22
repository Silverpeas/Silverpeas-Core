/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.publicationTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.HtmlForm;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.form.XmlSearchForm;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * A PublicationTemplate describes a set of publication records built on a same template. A
 * PublicationTemplate groups :
 * <OL>
 * <LI>a RecordTemplate which describes the built records.
 * <LI>a RecordSet of records built on this template,
 * <LI>an update Form used to create and update the publication items
 * <LI>a view Form used to show the publications.
 * </OL>
 */
public class PublicationTemplateImpl implements PublicationTemplate {
  private String name = "";
  private String description = "";
  private String thumbnail = "";
  private String fileName = "";
  private boolean visible = false;
  private String viewFileName = "";
  private String updateFileName = "";
  private String searchFileName = "";
  private String dataFileName = "";
  private String viewTypeFile = "";
  private String updateTypeFile = "";
  private String externalId = "";
  private RecordTemplate template = null;
  private RecordTemplate searchTemplate = null;
  private RecordTemplate viewTemplate = null;
  private RecordTemplate updateTemplate = null;
  private RecordSet recordSet = null;
  private Form updateForm = null;
  private Form viewForm = null;
  private Form searchForm = null;
  private ArrayList<TemplateFile> templateFiles = new ArrayList<TemplateFile>();

  /**
   * Returns the RecordTemplate of the publication data item.
   */
  public RecordTemplate getRecordTemplate(boolean loadIfNull)
      throws PublicationTemplateException {
    if (template == null && loadIfNull)
      template = loadRecordTemplate(dataFileName);
    return template;
  }

  public RecordTemplate getRecordTemplate() throws PublicationTemplateException {
    return getRecordTemplate(true);
  }

  /**
   * Returns the RecordSet of all the records built from this template.
   */
  public RecordSet getRecordSet() throws PublicationTemplateException {
    try {
      if (recordSet == null)
        recordSet = GenericRecordSetManager.getRecordSet(this.externalId);
      return recordSet;
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.getUpdateForm",
          "form.EX_CANT_GET_RECORDSET", null, e);
    }
  }

  /**
   * Returns the Form used to create and update the records built from this template.
   */
  public Form getUpdateForm() throws PublicationTemplateException {
    if (updateForm == null)
      updateForm = getForm(updateFileName, updateTypeFile);
    return updateForm;
  }

  /**
   * Returns the Form used to view the records built from this template.
   */
  public Form getViewForm() throws PublicationTemplateException {
    if (viewForm == null)
      viewForm = getForm(viewFileName, viewTypeFile);
    return viewForm;

  }

  /**
   * Returns the RecordTemplate of the publication search item.
   */
  public RecordTemplate getSearchTemplate(boolean loadIfNull)
      throws PublicationTemplateException {
    if (searchTemplate == null && loadIfNull)
      searchTemplate = loadRecordTemplate(searchFileName);
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
  public RecordTemplate getViewTemplate() throws PublicationTemplateException {
    return viewTemplate;
  }

  /**
   * Returns the RecordTemplate of the publication update item.
   */
  public RecordTemplate getUpdateTemplate() throws PublicationTemplateException {
    return updateTemplate;
  }

  public RecordTemplate getDataTemplate() throws PublicationTemplateException {
    return getRecordTemplate(false);
  }

  public Form getSearchForm() throws PublicationTemplateException {
    if (searchForm == null) {
      if (isSearchable()) {
        RecordTemplate templateForm = loadRecordTemplate(searchFileName);

        try {
          searchForm = new XmlSearchForm(templateForm);
        } catch (FormException e) {
          throw new PublicationTemplateException(
              "PublicationTemplateImpl.getUpdateForm", "form.EX_CANT_GET_FORM",
              null, e);
        }
      }
    }
    return searchForm;
  }

  /**
   * Returns the Form witch name is name parameter the records built from this template.
   */
  public Form getEditForm(String name) throws PublicationTemplateException {
    SilverTrace.info("form", "PublicationTemplateImpl.getEditForm",
        "root.MSG_GEN_PARAM_VALUE", "name=" + name);
    Form form = null;
    if (templateFiles != null) {
      Iterator<TemplateFile> files = templateFiles.iterator();
      while (files.hasNext()) {
        TemplateFile file = files.next();
        if (file.getName().compareToIgnoreCase(name) == 0) {
          form = getForm(file.getFileName(), file.getTypeName());
          return form;
        }
      }
    }
    if (form == null)
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.getEditForm", "form.EX_CANT_GET_FORM",
          "name=" + name, null);
    return form;
  }

  private Form getForm(String fileName, String fileType)
      throws PublicationTemplateException {
    Form form = null;
    RecordTemplate templateForm = null;

    String typeFile = FileRepositoryManager.getFileExtension(fileName);
    if (fileType != null && !fileType.equals(""))
      typeFile = fileType.trim().toLowerCase();

    if (fileName != null && !fileName.equals("") && typeFile.equals("xml")) {
      templateForm = loadRecordTemplate(fileName);
      mergeTemplate(templateForm);

      try {
        form = (Form) new XmlForm(templateForm);
      } catch (FormException e) {
        throw new PublicationTemplateException(
            "PublicationTemplateImpl.getForm", "form.EX_CANT_GET_FORM", null, e);
      }
    } else {
      String htmlFileName = PublicationTemplateManager.makePath(
          PublicationTemplateManager.templateDir, fileName);

      fileName = fileName.replaceAll(".html", ".xml");

      templateForm = loadRecordTemplate(fileName);
      mergeTemplate(templateForm);

      try {
        HtmlForm viewFormHtml = new HtmlForm(templateForm);
        viewFormHtml.setFileName(htmlFileName);
        form = (Form) viewFormHtml;
      } catch (FormException e) {
        throw new PublicationTemplateException(
            "PublicationTemplateImpl.getForm", "form.EX_CANT_GET_FORM", null, e);
      }
    }

    return form;
  }

  /**
   * merge the data template with the form template
   */
  private void mergeTemplate(RecordTemplate formTemplate)
      throws PublicationTemplateException {
    RecordTemplate dataTemplate = getRecordTemplate();
    if (formTemplate != null && dataTemplate != null) {
      String fieldNames[] = formTemplate.getFieldNames();
      int size = fieldNames.length;
      String fieldName;
      GenericFieldTemplate formFieldTemplate;
      GenericFieldTemplate dataFieldTemplate;
      for (int i = 0; i < size; i++) {
        fieldName = fieldNames[i];
        SilverTrace.info("form", "PublicationTemplateImpl.mergeTemplates",
            "root.MSG_GEN_PARAM_VALUE", "fieldName = " + fieldName);
        try {
          formFieldTemplate = (GenericFieldTemplate) formTemplate
              .getFieldTemplate(fieldName);
          dataFieldTemplate = (GenericFieldTemplate) dataTemplate
              .getFieldTemplate(fieldName);

          formFieldTemplate.setTypeName(dataFieldTemplate.getTypeName());
          // formFieldTemplate.setLabelsObj(formFieldTemplate.getLabelsObj());
          formFieldTemplate.setParametersObj(dataFieldTemplate
              .getParametersObj());
        } catch (FormException e) {
          SilverTrace.error("form", "PublicationTemplateImpl.mergeTemplates",
              "form.EXP_UNKNOWN_FIELD", null, e);
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
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   *
   */
  public String getExternalId() {
    return externalId;
  }

  public void setTemplatesObj(ArrayList<TemplateFile> templatesObj) {
    this.templateFiles = templatesObj;
  }

  /**
   *
   */
  public ArrayList<TemplateFile> getTemplatesObj() {
    return templateFiles;
  }

  /**
   * load a recordTemplate definition from xml file to java objects
   * @param xmlFileName the xml file name that contains process model definition
   * @return a RecordTemplate object
   */
  private RecordTemplate loadRecordTemplate(String xmlFileName)
      throws PublicationTemplateException {
    if (!StringUtil.isDefined(xmlFileName))
      return null;

    Mapping mapping = new Mapping();

    try {
      // Format these url
      xmlFileName = PublicationTemplateManager.makePath(
          PublicationTemplateManager.templateDir, xmlFileName);

      // Load mapping and instantiate a Marshaller
      mapping
          .loadMapping(PublicationTemplateManager.mappingRecordTemplateFilePath);
      Unmarshaller unmar = new Unmarshaller(mapping);

      // Unmarshall the process model
      GenericRecordTemplate recordTemplate = (GenericRecordTemplate) unmar
          .unmarshal(new InputSource(new FileInputStream(xmlFileName)));
      recordTemplate.setTemplateName(fileName.substring(0, fileName
          .lastIndexOf(".")));

      return (RecordTemplate) recordTemplate;
    } catch (MappingException me) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_XML_MAPPING",
          "Publication Template FileName : " + xmlFileName, me);
    } catch (MarshalException me) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_UNMARSHALL_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, me);
    } catch (ValidationException ve) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_INVALID_XML_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, ve);
    } catch (IOException ioe) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, ioe);
    }
  }

  public void saveRecordTemplates() throws PublicationTemplateException {
    String subDir = fileName.substring(0, fileName.lastIndexOf("."))
        + File.separator;
    if (template != null)
      saveRecordTemplate(template, subDir, "data.xml");
    if (viewTemplate != null)
      saveRecordTemplate(viewTemplate, subDir, "view.xml");
    if (updateTemplate != null)
      saveRecordTemplate(updateTemplate, subDir, "update.xml");
    if (searchTemplate != null)
      saveRecordTemplate(searchTemplate, subDir, "search.xml");
  }

  /**
   * Save a recordTemplate to xml file
   * @param recordTemplate the object to save as xml File
   */
  private void saveRecordTemplate(RecordTemplate recordTemplate, String subDir,
      String xmlFileName) throws PublicationTemplateException {
    // FileWriter writer = null;
    Mapping mapping = new Mapping();

    try {
      // Format these url
      String xmlDirPath = PublicationTemplateManager.makePath(
          PublicationTemplateManager.templateDir, subDir);
      File dir = new File(xmlDirPath);
      if (!dir.exists())
        try {
          FileFolderManager.createFolder(dir);
        } catch (UtilException e) {
          throw new PublicationTemplateException(
              "PublicationTemplateImpl.saveRecordTemplate",
              "form.EX_ERR_CASTOR_SAVE_PUBLICATION_TEMPLATE", "xmlDirPath = "
              + xmlDirPath, e);
        }

      String xmlFilePath = PublicationTemplateManager.makePath(
          PublicationTemplateManager.templateDir, subDir + xmlFileName);

      // Load mapping and instantiate a Marshaller
      mapping
          .loadMapping(PublicationTemplateManager.mappingRecordTemplateFilePath);

      String encoding = "ISO-8859-1";

      FileOutputStream fos = new FileOutputStream(xmlFilePath);
      OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);

      // writer = new FileWriter(xmlFilePath);

      Marshaller mar = new Marshaller(osw);
      mar.setEncoding(encoding);
      mar.setMapping(mapping);

      // Marshall the template
      mar.marshal(recordTemplate);
    } catch (MappingException me) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_XML_MAPPING",
          "Publication Template FileName : " + xmlFileName, me);
    } catch (MarshalException me) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_UNMARSHALL_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, me);
    } catch (ValidationException ve) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_INVALID_XML_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, ve);
    } catch (IOException ioe) {
      throw new PublicationTemplateException(
          "PublicationTemplateImpl.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, ioe);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
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

  public boolean isSearchable() {
    return (searchFileName != null && searchFileName.trim().length() > 0);
  }

  public PublicationTemplateImpl basicClone() {
    PublicationTemplateImpl template = new PublicationTemplateImpl();
    template.setName(getName());
    template.setDescription(getDescription());
    template.setThumbnail(getThumbnail());
    template.setFileName(getFileName());
    template.setVisible(isVisible());
    template.setViewFileName(getViewFileName());
    template.setUpdateFileName(getUpdateFileName());
    template.setSearchFileName(getSearchFileName());
    template.setDataFileName(getDataFileName());
    template.setViewTypeFile(getViewTypeFile());
    template.setUpdateTypeFile(getUpdateTypeFile());
    template.setExternalId(getExternalId());
    return template;
  }
}