/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.templatedesigner.control;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.component.model.LocalizedComponent;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.web.templatedesigner.model.TemplateDesignerException;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.ContentEncryptionServiceProvider;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

public class TemplateDesignerSessionController extends AbstractComponentSessionController {

  PublicationTemplateImpl template = null;
  boolean updateInProgress = false;
  private final static int SCOPE_DATA = 0;
  private final static int SCOPE_VIEW = 1;
  private final static int SCOPE_UPDATE = 2;
  private final static int SCOPE_SEARCH = 3;
  private final static int SCOPE_SEARCHRESULT = 4;
  private List<String> languages = null;
  private final AdminController adminController;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public TemplateDesignerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.templatedesigner.multilang.templateDesignerBundle",
        "org.silverpeas.templatedesigner.settings.templateDesignerIcons");
    adminController = ServiceProvider.getService(AdminController.class);
  }

  public List<String> getLanguages() {
    if (languages == null) {
      languages = DisplayI18NHelper.getLanguages();
    }
    return languages;
  }

  public List<PublicationTemplate> getTemplates() throws TemplateDesignerException {
    try {
      updateInProgress = false;
      template = null;

      return getPublicationTemplateManager().getPublicationTemplates(false);
    } catch (PublicationTemplateException e) {
      throw new TemplateDesignerException("TemplateDesignerSessionController.getTemplates",
          SilverpeasException.ERROR, "templateManager.GETTING_TEMPLATES_FAILED", e);
    }
  }

  public PublicationTemplate reloadCurrentTemplate() throws TemplateDesignerException {
    return setTemplate(template.getFileName());
  }

  public PublicationTemplate setTemplate(String fileName) throws TemplateDesignerException {
    try {
      template = (PublicationTemplateImpl) getPublicationTemplateManager()
          .loadPublicationTemplate(fileName);

      // load data.xml
      template.getRecordTemplate();

      // load search.xml
      template.getSearchTemplate();

      List<FieldTemplate> templates = getRecordTemplate(SCOPE_DATA).getFieldList();
      for (FieldTemplate field : templates) {
        if (getRecordTemplate(SCOPE_SEARCH).getFieldList().contains(field)) {
          ((GenericFieldTemplate) field).setSearchable(true);
        }
      }

      return template;
    } catch (PublicationTemplateException e) {
      throw new TemplateDesignerException("TemplateDesignerSessionController.getTemplate",
          SilverpeasException.ERROR, "templateManager.GETTING_TEMPLATE_FAILED", e);
    }
  }

  public void createTemplate(PublicationTemplate template)
      throws TemplateDesignerException, CryptoException {
    this.template = (PublicationTemplateImpl) template;
    String fileName = string2fileName(template.getName());

    String templateDirPath =
        PublicationTemplateManager.makePath(PublicationTemplateManager.templateDir, fileName);
    File templateDir = new File(templateDirPath);
    if (!templateDir.exists()) {
      try {
        FileFolderManager.createFolder(templateDir);
      } catch (UtilException e) {
        throw new TemplateDesignerException(getClass().getSimpleName() + ".createTemplate",
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
      }
    }

    this.template.setFileName(fileName + ".xml");
    this.template.setDataFileName(fileName + File.separator + "data.xml");

    String viewFileName = "view.xml";
    if (this.template.isViewLayerDefined()) {
      viewFileName = "view.html";
    }
    this.template.setViewFileName(fileName + File.separator + viewFileName);

    String updateFileName = "update.xml";
    if (this.template.isUpdateLayerDefined()) {
      updateFileName = "update.html";
    }
    this.template.setUpdateFileName(fileName + File.separator + updateFileName);

    this.template.setSearchResultFileName(fileName + File.separator + "searchresult.xml");
    if (template.isSearchable()) {
      this.template.setSearchFileName(fileName + File.separator + "search.xml");
    } else {
      this.template.setSearchFileName(null);
    }

    this.template.setVisible(true);

    updateInProgress = true;

    saveTemplate();
  }

  private static String string2fileName(String text) {
    String newText = text.toLowerCase();
    newText = newText.replace('é', 'e');
    newText = newText.replace('\'', '_');
    newText = newText.replace(' ', '_');
    newText = newText.replace('è', 'e');
    newText = newText.replace('ë', 'e');
    newText = newText.replace('ê', 'e');
    newText = newText.replace('ö', 'o');
    newText = newText.replace('ô', 'o');
    newText = newText.replace('õ', 'o');
    newText = newText.replace('ò', 'o');
    newText = newText.replace('ï', 'i');
    newText = newText.replace('î', 'i');
    newText = newText.replace('ì', 'i');
    newText = newText.replace('ñ', 'n');
    newText = newText.replace('ü', 'u');
    newText = newText.replace('û', 'u');
    newText = newText.replace('ù', 'u');
    newText = newText.replace('ç', 'c');
    newText = newText.replace('à', 'a');
    newText = newText.replace('ä', 'a');
    newText = newText.replace('ã', 'a');
    newText = newText.replace('â', 'a');
    newText = newText.replace('°', '_');
    return newText;
  }

  public void updateTemplate(PublicationTemplateImpl updatedTemplate)
      throws TemplateDesignerException, CryptoException {
    this.template.setName(updatedTemplate.getName());
    this.template.setDescription(updatedTemplate.getDescription());
    this.template.setThumbnail(updatedTemplate.getThumbnail());
    this.template.setVisible(updatedTemplate.isVisible());
    this.template.setDataEncrypted(updatedTemplate.isDataEncrypted());
    this.template.setViewLayerFileName(updatedTemplate.getViewLayerFileName());
    this.template.setViewLayerAction(updatedTemplate.getViewLayerAction());
    this.template.setUpdateLayerFileName(updatedTemplate.getUpdateLayerFileName());
    this.template.setUpdateLayerAction(updatedTemplate.getUpdateLayerAction());

    String subdir = getSubdir(this.template.getFileName());

    if (this.template.getViewLayerAction() == PublicationTemplateImpl.LAYER_ACTION_ADD) {
      this.template.setViewFileName(subdir + File.separator + "view.html");
    } else if (updatedTemplate.getViewLayerAction() ==
        PublicationTemplateImpl.LAYER_ACTION_REMOVE) {
      this.template.setViewFileName(subdir + File.separator + "view.xml");
    }

    if (this.template.getUpdateLayerAction() == PublicationTemplateImpl.LAYER_ACTION_ADD) {
      this.template.setUpdateFileName(subdir + File.separator + "update.html");
    } else if (updatedTemplate.getUpdateLayerAction() ==
        PublicationTemplateImpl.LAYER_ACTION_REMOVE) {
      this.template.setUpdateFileName(subdir + File.separator + "update.xml");
    }

    if (updatedTemplate.isSearchable()) {
      this.template.setSearchFileName(subdir + File.separator + "search.xml");
    } else {
      this.template.setSearchFileName(null);
    }

    this.template.setSpaces(updatedTemplate.getSpaces());
    this.template.setApplications(updatedTemplate.getApplications());
    this.template.setInstances(updatedTemplate.getInstances());

    updateInProgress = true;

    saveTemplateHeader();
  }

  private String getSubdir(String fileName) {
    return FilenameUtils.getBaseName(fileName);
  }

  public void addField(FieldTemplate field) throws TemplateDesignerException {
    addField(field, -1);
  }

  public void addField(FieldTemplate field, int index) throws TemplateDesignerException {
    if (index == -1 || index == getRecordTemplate(SCOPE_DATA).getFieldList().size()) {
      getRecordTemplate(SCOPE_DATA).getFieldList().add(field);
    } else {
      getRecordTemplate(SCOPE_DATA).getFieldList().add(index, field);
    }

    updateInProgress = true;
    saveTemplateFields(true);
  }

  public void removeField(String fieldName) throws TemplateDesignerException {
    FieldTemplate field = getField(fieldName);

    getRecordTemplate(SCOPE_DATA).getFieldList().remove(field);

    updateInProgress = true;
    saveTemplateFields(true);
  }

  public void sortFields(String[] fieldNames) throws TemplateDesignerException {
    List<FieldTemplate> sortedFieldTemplates = new ArrayList<>();
    List<FieldTemplate> fieldTemplates = getRecordTemplate(SCOPE_DATA).getFieldList();
    for (String fieldName : fieldNames) {
      FieldTemplate field = getField(fieldName, fieldTemplates);
      if (field != null) {
        sortedFieldTemplates.add(field);
      }
    }
    getRecordTemplate(SCOPE_DATA).getFieldList().clear();
    getRecordTemplate(SCOPE_DATA).getFieldList().addAll(sortedFieldTemplates);

    updateInProgress = true;
    saveTemplateFields(true);
  }

  public void updateField(FieldTemplate field) throws TemplateDesignerException {
    int index = getRecordTemplate(SCOPE_DATA).getFieldList().indexOf(field);
    List<FieldTemplate> fieldTemplates = getRecordTemplate(SCOPE_DATA).getFieldList();
    fieldTemplates.remove(index);
    addField(field, index);

    updateInProgress = true;
  }

  public Iterator<FieldTemplate> getFields() {
    if (getRecordTemplate(SCOPE_DATA).getFieldList() != null) {
      return getRecordTemplate(SCOPE_DATA).getFieldList().iterator();
    }
    return null;
  }

  public FieldTemplate getField(String fieldName) throws TemplateDesignerException {
    Iterator<FieldTemplate> fields = getFields();
    while (fields != null && fields.hasNext()) {
      FieldTemplate field = fields.next();
      if (field.getFieldName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }
    return null;
  }

  private FieldTemplate getField(String fieldName, List<FieldTemplate> fields) {
    for (FieldTemplate field : fields) {
      if (field.getFieldName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }
    return null;
  }

  private GenericRecordTemplate getRecordTemplate(int scope) {
    GenericRecordTemplate recordTemplate = null;
    try {
      if (scope == SCOPE_VIEW) {
        recordTemplate = (GenericRecordTemplate) template.getViewTemplate();
        if (recordTemplate == null) {
          recordTemplate = new GenericRecordTemplate();
          template.setViewTemplate(recordTemplate);
        }
      } else if (scope == SCOPE_UPDATE) {
        recordTemplate = (GenericRecordTemplate) template.getUpdateTemplate();
        if (recordTemplate == null) {
          recordTemplate = new GenericRecordTemplate();
          template.setUpdateTemplate(recordTemplate);
        }
      } else if (scope == SCOPE_SEARCH) {
        recordTemplate = (GenericRecordTemplate) template.getSearchTemplate(false);
        if (recordTemplate == null) {
          recordTemplate = new GenericRecordTemplate();
          template.setSearchTemplate(recordTemplate);
        }
      } else if (scope == SCOPE_SEARCHRESULT) {
        recordTemplate = (GenericRecordTemplate) template.getSearchResultTemplate();
        if (recordTemplate == null) {
          recordTemplate = new GenericRecordTemplate();
          template.setSearchResultTemplate(recordTemplate);
        }
      } else {
        recordTemplate = (GenericRecordTemplate) template.getDataTemplate();
        if (recordTemplate == null) {
          recordTemplate = new GenericRecordTemplate();
          template.setTemplate(recordTemplate);
        }
      }
    } catch (PublicationTemplateException e) {
      // Do nothing
      SilverTrace.error("templateDesigner", "TemplateDesignerSessionController.getRecordTemplate()",
          "root.EX_NO_MESSAGE", e);
    }
    return recordTemplate;
  }

  public void saveTemplate() throws TemplateDesignerException, CryptoException {
    saveTemplateHeader();
    saveTemplateFields(true);
  }

  private void saveTemplateFields(boolean resetCache) throws TemplateDesignerException {
    try {
      List<FieldTemplate> fields = getRecordTemplate(SCOPE_DATA).getFieldList();

      getRecordTemplate(SCOPE_UPDATE).getFieldList().clear();
      getRecordTemplate(SCOPE_VIEW).getFieldList().clear();
      getRecordTemplate(SCOPE_SEARCH).getFieldList().clear();
      getRecordTemplate(SCOPE_SEARCHRESULT).getFieldList().clear();

      getRecordTemplate(SCOPE_UPDATE).getFieldList().addAll(fields);

      for (FieldTemplate field : fields) {
        // process search.xml
        if (field.isSearchable()) {
          getRecordTemplate(SCOPE_SEARCH).getFieldList().add(field);
        } else {
          getRecordTemplate(SCOPE_SEARCH).getFieldList().remove(field);
        }

        // process view.xml (set field to simpletext)
        GenericFieldTemplate cloneField = ((GenericFieldTemplate) field).clone();
        String cloneDisplayer = cloneField.getDisplayerName();
        if (isAReadOnlyField(cloneDisplayer)) {
          cloneField.setReadOnly(true);
        } else {
          cloneField.setDisplayerName("simpletext");
        }
        getRecordTemplate(SCOPE_VIEW).getFieldList().add(cloneField);
      }

      // Using same content as view to search result extra information
      getRecordTemplate(SCOPE_SEARCHRESULT).getFieldList()
          .addAll(getRecordTemplate(SCOPE_VIEW).getFieldList());

      // Save others xml files (data.xml, view.xml, update.xml
      template.saveRecordTemplates();

      if (resetCache) {
        // reset caches partially
        getPublicationTemplateManager().removePublicationTemplateFromCaches(template.getFileName());
      }

      updateInProgress = false;
    } catch (PublicationTemplateException e) {
      throw new TemplateDesignerException("TemplateDesignerSessionController.saveTemplate",
          SilverpeasException.ERROR, "templateManager.TEMPLATE_SAVING_FAILED",
          "template = " + template.getName(), e);
    }
  }

  private void saveTemplateHeader() throws TemplateDesignerException, CryptoException {
    try {
      // Save main xml File
      try {
        getPublicationTemplateManager().savePublicationTemplate(template);
      } catch (CryptoException e) {
        // reload current template as it was before saving
        reloadCurrentTemplate();
        throw e;
      }

      String dir = PublicationTemplateManager
          .makePath(PublicationTemplateManager.templateDir, getSubdir(template.getFileName()));

      if (template.isViewLayerDefined() &&
          template.getViewLayerAction() == PublicationTemplateImpl.LAYER_ACTION_ADD) {
        saveLayer(template.getViewLayerFileName(), dir);
      } else if (template.getViewLayerAction() == PublicationTemplateImpl.LAYER_ACTION_REMOVE) {
        removeLayer(new File(dir, "view.html"));
      }

      if (template.isUpdateLayerDefined() &&
          template.getUpdateLayerAction() == PublicationTemplateImpl.LAYER_ACTION_ADD) {
        saveLayer(template.getUpdateLayerFileName(), dir);
      } else if (template.getUpdateLayerAction() == PublicationTemplateImpl.LAYER_ACTION_REMOVE) {
        removeLayer(new File(dir, "update.html"));
      }

      // reset caches partially
      getPublicationTemplateManager().removePublicationTemplateFromCaches(template.getFileName());
    } catch (PublicationTemplateException e) {
      throw new TemplateDesignerException("TemplateDesignerSessionController.saveTemplate",
          SilverpeasException.ERROR, "templateManager.TEMPLATE_SAVING_FAILED",
          "template = " + template.getName(), e);
    }
  }

  private void saveLayer(String filePath, String dir) throws PublicationTemplateException {
    try {
      File file = new File(dir, FilenameUtils.getName(filePath));
      if (file.exists()) {
        file.delete();
      }
      FileUtils.moveFileToDirectory(new File(filePath), new File(dir), false);
    } catch (IOException ioe) {
      throw new PublicationTemplateException("PublicationTemplateImpl.saveLayer()",
          "form.EX_ERR_CANT_SAVE_LAYER", "Publication Template FileName : " + filePath, ioe);
    }
  }

  private void removeLayer(File file) throws PublicationTemplateException {
    try {
      FileUtils.forceDelete(file);
    } catch (IOException ioe) {
      throw new PublicationTemplateException("PublicationTemplateImpl.removeLayer()",
          "form.EX_ERR_CANT_REMOVE_LAYER",
          "Publication Template FileName : " + file.getAbsolutePath(), ioe);
    }
  }

  public PublicationTemplate getCurrentTemplate() {
    return this.template;
  }

  public boolean isUpdateInProgress() {
    return updateInProgress;
  }

  /**
   * Gets a PublicationTemplateManager instance.
   * @return a PublicationTemplateManager instance.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  /**
   * Is the specified field should be a read only one in view?
   * @param fieldName the name of the field.
   * @return true if the field should be read only when it is only printed as such and no in a form
   * for change.
   */
  private boolean isAReadOnlyField(final String fieldName) {
    return Arrays.asList("wysiwyg", "url", "image", "file", "video", "map", "email")
        .contains(fieldName);
  }

  public List<LocalizedComponent> getComponentsUsingForms() {
    Map<String, WAComponent> components = adminController.getAllComponents();
    String[] names =
        {"kmelia", "kmax", "classifieds", "gallery", "formsOnline", "resourcesManager", "webPages",
            "yellowpages"};
    List<LocalizedComponent> result = new ArrayList<>();
    for (String name : names) {
      WAComponent component = components.get(name);
      if (component != null) {
        result.add(new LocalizedComponent(component, getLanguage()));
      }
    }
    if (!result.isEmpty()) {
      Collections.sort(result, new Comparator<LocalizedComponent>() {
        @Override
        public int compare(LocalizedComponent o1, LocalizedComponent o2) {
          String valcomp1 = o1.getSuite() + o1.getLabel();
          String valcomp2 = o2.getSuite() + o2.getLabel();
          return valcomp1.toUpperCase().compareTo(valcomp2.toUpperCase());
        }
      });
    }
    return result;
  }

  public boolean isEncryptionAvailable() {
    ContentEncryptionService encryptionService =
        ContentEncryptionServiceProvider.getContentEncryptionService();
    return encryptionService.isCipherKeyDefined();
  }
}
