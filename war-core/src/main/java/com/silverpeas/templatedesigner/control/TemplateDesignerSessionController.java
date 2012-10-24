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

package com.silverpeas.templatedesigner.control;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.templatedesigner.model.TemplateDesignerException;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Arrays;

public class TemplateDesignerSessionController extends AbstractComponentSessionController {

  PublicationTemplateImpl template = null;
  boolean updateInProgress = false;
  private final static int SCOPE_DATA = 0;
  private final static int SCOPE_VIEW = 1;
  private final static int SCOPE_UPDATE = 2;
  private final static int SCOPE_SEARCH = 3;
  private final static int SCOPE_SEARCHRESULT = 4;
  private List<String> languages = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public TemplateDesignerSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.templatedesigner.multilang.templateDesignerBundle",
        "com.silverpeas.templatedesigner.settings.templateDesignerIcons");
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
      throw new TemplateDesignerException(
          "TemplateDesignerSessionController.getTemplates",
          SilverpeasException.ERROR,
          "templateManager.GETTING_TEMPLATES_FAILED", e);
    }
  }

  public PublicationTemplate reloadCurrentTemplate()
      throws TemplateDesignerException {
    return setTemplate(template.getFileName());
  }

  public PublicationTemplate setTemplate(String fileName)
      throws TemplateDesignerException {
    try {
      template = (PublicationTemplateImpl) getPublicationTemplateManager().loadPublicationTemplate(
          fileName);

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
      throw new TemplateDesignerException(
          "TemplateDesignerSessionController.getTemplate",
          SilverpeasException.ERROR, "templateManager.GETTING_TEMPLATE_FAILED",
          e);
    }
  }

  public void createTemplate(PublicationTemplate template)
      throws TemplateDesignerException {
    this.template = (PublicationTemplateImpl) template;

    String fileName = string2fileName(template.getName());

    this.template.setFileName(fileName + ".xml");
    this.template.setDataFileName(fileName + File.separator + "data.xml");
    this.template.setViewFileName(fileName + File.separator + "view.xml");
    this.template.setUpdateFileName(fileName + File.separator + "update.xml");
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
      throws TemplateDesignerException {
    this.template.setName(updatedTemplate.getName());
    this.template.setDescription(updatedTemplate.getDescription());
    this.template.setThumbnail(updatedTemplate.getThumbnail());
    this.template.setVisible(updatedTemplate.isVisible());

    if (updatedTemplate.isSearchable()) {
      this.template.setSearchFileName(getSubdir(template.getFileName())
          + File.separator + "search.xml");
    } else {
      this.template.setSearchFileName(null);
    }

    updateInProgress = true;

    saveTemplateHeader();
  }

  private String getSubdir(String fileName) {
    return fileName.substring(0, fileName.indexOf("."));
  }

  public void addField(FieldTemplate field) throws TemplateDesignerException {
    addField(field, -1);
  }

  public void addField(FieldTemplate field, int index)
      throws TemplateDesignerException {
    if (index == -1
        || index == getRecordTemplate(SCOPE_DATA).getFieldList().size()) {
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

  public void moveField(String fieldName, int direction)
      throws TemplateDesignerException {
    FieldTemplate field = getField(fieldName);
    List<FieldTemplate> fieldTemplates = getRecordTemplate(SCOPE_DATA).getFieldList();

    int index = getRecordTemplate(SCOPE_DATA).getFieldList().indexOf(field);
    field = fieldTemplates.remove(index);

    addField(field, index + direction);

    updateInProgress = true;
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

  public FieldTemplate getField(String fieldName)
      throws TemplateDesignerException {
    Iterator<FieldTemplate> fields = getFields();
    while (fields != null && fields.hasNext()) {
      FieldTemplate field = fields.next();
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
      SilverTrace.error("templateDesigner",
          "TemplateDesignerSessionController.getRecordTemplate()",
          "root.EX_NO_MESSAGE", e);
    }
    return recordTemplate;
  }

  public void saveTemplate() throws TemplateDesignerException {
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
      getRecordTemplate(SCOPE_SEARCHRESULT).getFieldList().addAll(
          getRecordTemplate(SCOPE_VIEW).getFieldList());

      // Save others xml files (data.xml, view.xml, update.xml
      ((PublicationTemplateImpl) template).saveRecordTemplates();

      if (resetCache) {
        // reset caches partially
        getPublicationTemplateManager().removePublicationTemplateFromCaches(template.getFileName());
      }

      updateInProgress = false;
    } catch (PublicationTemplateException e) {
      throw new TemplateDesignerException(
          "TemplateDesignerSessionController.saveTemplate",
          SilverpeasException.ERROR, "templateManager.TEMPLATE_SAVING_FAILED",
          "template = " + template.getName(), e);
    }
  }

  private void saveTemplateHeader() throws TemplateDesignerException {
    try {
      // Save main xml File
      getPublicationTemplateManager().savePublicationTemplate(template);

      // reset caches partially
      getPublicationTemplateManager().removePublicationTemplateFromCaches(template.getFileName());
    } catch (PublicationTemplateException e) {
      throw new TemplateDesignerException(
          "TemplateDesignerSessionController.saveTemplate",
          SilverpeasException.ERROR, "templateManager.TEMPLATE_SAVING_FAILED",
          "template = " + template.getName(), e);
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
   * @return true if the field shlould be read only when it is only printed as such and no in a form
   * for change.
   */
  private boolean isAReadOnlyField(final String fieldName) {
    return Arrays.asList("wysiwyg", "url", "image", "file", "video").contains(fieldName);
  }
}
