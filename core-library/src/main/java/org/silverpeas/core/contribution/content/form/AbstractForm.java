/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.jsp.JspWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This abstract class implements the form interface and provides for all concretes classes a
 * default implementation of some displaying methods.
 */
public abstract class AbstractForm implements Form {

  private List<FieldTemplate> fieldTemplates;
  private String title = "";
  private String name = "";
  private String formName = "";
  private DataRecord data;
  private boolean viewForm = false;

  public static final String REPEATED_FIELD_CSS_SHOW = "field-occurrence-shown";
  public static final String REPEATED_FIELD_CSS_HIDE = "field-occurrence-hidden";
  public static final String REPEATED_FIELD_SEPARATOR = "__SSPP__";


  /**
   * Creates a new form from the specified template of records.
   * @param template the record template.
   * @throws FormException if an error occurs while setting up the form.
   */
  public AbstractForm(final RecordTemplate template) throws FormException {
    if (template != null) {
      fieldTemplates = Arrays.asList(template.getFieldTemplates());
    } else {
      fieldTemplates = new ArrayList<>();
    }
  }

  @Override
  public void setFormName(String name) {
    formName = name;
  }

  public String getFormName() {
    return formName;
  }

  /**
   * Gets the template of all of the fields that made this form.
   */
  public List<FieldTemplate> getFieldTemplates() {
    return fieldTemplates;
  }

  /**
   * Gets the title of this form.
   * @return the title of this form or an empty string if it isn't set.
   */
  @Override
  public String getTitle() {
    return (title == null ? "" : title);
  }

  /**
   * Sets the form title.
   * @param title the new title of the form.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Prints the javascripts which will be used to control the new values given to the data record
   * fields. The error messages may be adapted to a local language. The RecordTemplate gives the
   * field type and constraints. The RecordTemplate gives the local label too. Never throws an
   * Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>a field is unknown by the template.
   * <LI>a field has not the required type.
   * </UL>
   * @param jw the JSP writer into which the javascript is written.
   * @param pagesContext the JSP page context.
   */
  @Override
  public void displayScripts(final JspWriter jw, final PagesContext pagesContext) {
    try {
      String language = pagesContext.getLanguage();
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      boolean jsAdded = false;
      if (StringUtil.isDefined(pagesContext.getComponentId()) && StringUtil.isDefined(getName())) {
        ComponentInstLight component =  OrganizationControllerProvider.getOrganisationController()
            .getComponentInstLight(pagesContext.getComponentId());
        if (component != null && component.isWorkflow()) {
          out.append("<script type=\"text/javascript\" src=\"/weblib/workflows/")
              .append(component.getName()).append("/").append(getName())
              .append(".js\"></script>\n");
          jsAdded = true;
        }
      }

      if (!jsAdded) {
        if (!fieldTemplates.isEmpty()) {
          FieldTemplate fieldTemplate = fieldTemplates.get(0);
          if (StringUtil.isDefined(fieldTemplate.getTemplateName())) {
            out.append("<script type=\"text/javascript\" src=\"/weblib/xmlForms/")
                .append(fieldTemplate.getTemplateName()).append(".js\"></script>\n");
          }
        }
      }

      PagesContext pc = new PagesContext(pagesContext);
      pc.incCurrentFieldIndex(1);

      out.append(Util.getJavascriptIncludes(language))
          .append("\n<script type=\"text/javascript\">\n")
          .append("	var errorNb = 0;\n")
          .append("	var errorMsg = \"\";\n")
          .append("function addXMLError(message) {\n")
          .append("	errorMsg+=\"  - \"+message+\"\\n\";\n")
          .append("	errorNb++;\n")
          .append("}\n")
          .append("function getXMLField(fieldName) {\n")
          .append("	return document.getElementById(fieldName);\n")
          .append("}\n")
          .append("function isCorrectForm() {\n")
          .append("	errorMsg = \"\";\n")
          .append("	errorNb = 0;\n")
          .append("	var field;\n")
          .append("	\n\n");

      for (FieldTemplate fieldTemplate : fieldTemplates) {
        if (fieldTemplate != null) {
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          String fieldType = fieldTemplate.getTypeName();
          String fieldName = fieldTemplate.getFieldName();
          boolean mandatory = fieldTemplate.isMandatory();
          FieldDisplayer fieldDisplayer;
          try {
            if (fieldDisplayerName == null || fieldDisplayerName.isEmpty()) {
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            }
            fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);

            if (fieldDisplayer != null) {
              int nbFieldsToDisplay = fieldTemplate.getMaximumNumberOfOccurrences();
              for (int i=0; i<nbFieldsToDisplay; i++) {
                String currentFieldName = Util.getFieldOccurrenceName(fieldName, i);
                ((GenericFieldTemplate) fieldTemplate).setFieldName(currentFieldName);
                if (i > 0) {
                  ((GenericFieldTemplate) fieldTemplate).setMandatory(false);
                }
                out.append("	field = document.getElementById(\"").append(currentFieldName).append("\");\n");
                out.append("	if (field == null) {\n");
                // try to find field by name
                out.append("  field = $(\"input[name=").append(currentFieldName).append("]\");\n");
                out.println("}");
                out.append(" if (field != null) {\n");
                fieldDisplayer.displayScripts(out, fieldTemplate, pc);
                out.println("}");
                pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc));
              }

              // set original data
              ((GenericFieldTemplate) fieldTemplate).setFieldName(fieldName);
              ((GenericFieldTemplate) fieldTemplate).setMandatory(mandatory);
            }
          } catch (FormException e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        }
      }

      out.append("	\n\n")
          .append("	switch(errorNb)\n")
          .append("	{\n")
          .append("	case 0 :\n")
          .append("		result = true;\n")
          .append("		break;\n")
          .append("	case 1 :\n")
          .append("		errorMsg = \"")
          .append(Util.getString("GML.ThisFormContains", language))
          .append(" 1 ")
          .append(Util.getString("GML.error", language))
          .append(" : \\n \" + errorMsg;\n")
          .append("		window.alert(errorMsg);\n")
          .append("		result = false;\n")
          .append("		break;\n")
          .append("	default :\n")
          .append("		errorMsg = \"")
          .append(Util.getString("GML.ThisFormContains", language))
          .append(" \" + errorNb + \" ")
          .append(Util.getString("GML.errors", language))
          .append(" :\\n \" + errorMsg;\n")
          .append("		window.alert(errorMsg);\n")
          .append("		result = false;\n")
          .append("		break;\n")
          .append("	}\n")
          .append("	return result;\n")
          .append("}\n")
          .append("	\n\n");

      out.append("function showOneMoreField(fieldName) {\n");
      out.append("$('.field_'+fieldName+' ." + REPEATED_FIELD_CSS_HIDE + ":first').removeClass('" +
          REPEATED_FIELD_CSS_HIDE + "').addClass('" + REPEATED_FIELD_CSS_SHOW + "');\n");
      out.append("if ($('.field_'+fieldName+' ." + REPEATED_FIELD_CSS_HIDE + "').length == 0) {\n");
      out.append(" $('#form-row-'+fieldName+' #moreField-'+fieldName).hide();\n");
      out.append("}\n");
      out.append("}\n");

      out.append("</script>\n");
      out.flush();
      jw.write(sw.toString());
    } catch (java.io.IOException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * Prints this form into the specified JSP writer according to the specified records of data that
   * populate the form fields.
   * @param out the JSP writer.
   * @param pagesContext the JSP page context.
   * @param record the record the data records embbed the form fields.
   */
  @Override
  public abstract void display(JspWriter out, PagesContext pagesContext, DataRecord record);

  /**
   * Prints this form into the specified JSP writer according to the specified records of data that
   * populate the form fields.
   * @param out the JSP writer.
   * @param pagesContext the JSP page context.
   */
  @Override
  public void display(JspWriter out, PagesContext pagesContext) {
    display(out, pagesContext, getData());
  }

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @param items the item of a form in which is embedded multipart data.
   * @param record the record of data.
   * @param pagesContext the page context.
   */
  @Override
  public List<String> update(List<FileItem> items, DataRecord record, PagesContext pagesContext) {
    return update(items, record, pagesContext, true);
  }

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @param items the item of a form in which is embbeded multipart data.
   * @param record the record of data.
   * @param pagesContext the page context.
   * @param updateWysiwyg flag indicating if all of WYSIWYG data can be updated.
   */
  @Override
  public List<String> update(List<FileItem> items, DataRecord record, PagesContext pagesContext,
      boolean updateWysiwyg) {
    List<String> attachmentIds = new ArrayList<>();

    for (FieldTemplate fieldTemplate : fieldTemplates) {
      // Have to check if field is not readonly, if so no need to update
      if (!fieldTemplate.isReadOnly()) {
        if (fieldTemplate != null) {
          String fieldName = fieldTemplate.getFieldName();
          String fieldType = fieldTemplate.getTypeName();
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if (fieldDisplayerName == null || fieldDisplayerName.isEmpty()) {
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            }
            if ((!"wysiwyg".equals(fieldDisplayerName) || updateWysiwyg)) {
              FieldDisplayer fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
              if (fieldDisplayer != null) {
                for (int occ=0; occ<fieldTemplate.getMaximumNumberOfOccurrences(); occ++) {
                  attachmentIds.addAll(fieldDisplayer.update(items, record.getField(fieldName, occ),
                      fieldTemplate, pagesContext));
                }
              }
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        }
      } else {
        SilverLogger.getLogger(this).info("Field {0} is ignored as it is read only",
            fieldTemplate.getFieldName());
      }

    }
    return attachmentIds;
  }

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @param items the item of a form in which is embbeded multipart data.
   * @param record the record of data.
   * @param pagesContext the page context.
   */
  @Override
  public List<String> updateWysiwyg(List<FileItem> items, DataRecord record,
      PagesContext pagesContext) {
    List<String> attachmentIds = new ArrayList<>();
    for (FieldTemplate fieldTemplate : fieldTemplates) {
      FieldDisplayer fieldDisplayer;
      if (fieldTemplate != null) {
        String fieldName = fieldTemplate.getFieldName();
        String fieldType = fieldTemplate.getTypeName();
        String fieldDisplayerName = fieldTemplate.getDisplayerName();
        try {
          if ((fieldDisplayerName == null) || (fieldDisplayerName.isEmpty())) {
            fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
          }
          if ("wysiwyg".equals(fieldDisplayerName)) {
            fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
            if (fieldDisplayer != null) {
              attachmentIds.addAll(fieldDisplayer.update(items, record.getField(fieldName),
                  fieldTemplate, pagesContext));
            }
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      }
    }
    return attachmentIds;
  }

  /**
   * Is the form is empty? A form is empty if all of its fields aren't valued (no data associated
   * with them).
   * @param items the items embbeding multipart data in the form.
   * @param record the record of data.
   * @param pagesContext the page context.
   * @return true if one of the form field has no data.
   */
  @Override
  public boolean isEmpty(List<FileItem> items, DataRecord record, PagesContext pagesContext) {
    boolean isEmpty = true;
    for (FieldTemplate fieldTemplate : fieldTemplates) {
      FieldDisplayer fieldDisplayer;
      if (fieldTemplate != null) {
        String fieldType = fieldTemplate.getTypeName();
        String fieldDisplayerName = fieldTemplate.getDisplayerName();
        try {
          if (!StringUtil.isDefined(fieldDisplayerName)) {
            fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
          }
          fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
          if (fieldDisplayer != null) {
            String itemName = fieldTemplate.getFieldName();
            FileItem item = getParameter(items, itemName);
            if (item != null && !item.isFormField() && StringUtil.isDefined(item.getName())) {
              isEmpty = false;
            } else {
              String itemValue = getParameterValue(items, itemName, pagesContext.getEncoding());
              isEmpty = !StringUtil.isDefined(itemValue);
            }
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      }
      if (!isEmpty) {
        break;
      }
    }
    return isEmpty;
  }

  /**
   * Gets the value of the specified parameter from the specified items.
   * @param items the items of the form embbeding multipart data.
   * @param parameterName the name of the parameter.
   * @param encoding the encoding at which the value must be in.
   * @return the value of the specified parameter in the given encoding. or null if no such
   * parameter is defined in this form.
   * @throws UnsupportedEncodingException if the encoding at which the value should be in isn't
   * supported.
   */
  private String getParameterValue(List<FileItem> items, String parameterName, String encoding)
      throws UnsupportedEncodingException {
    FileItem item = getParameter(items, parameterName);
    if (item != null && item.isFormField()) {
      return item.getString(encoding);
    }
    return null;
  }

  /**
   * Gets the multipart data of the specified parameter.
   * @param items the items of the form with all of the multipart data.
   * @param parameterName the name of the parameter.
   * @return the item corresponding to the specified parameter.
   */
  private FileItem getParameter(final List<FileItem> items, final String parameterName) {
    FileItem fileItem = null;
    for (FileItem item : items) {
      if (parameterName.equals(item.getFieldName())) {
        fileItem = item;
        break;
      }
    }
    return fileItem;
  }

  private TypeManager getTypeManager() {
    return TypeManager.getInstance();
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  protected Field getSureField(FieldTemplate fieldTemplate, DataRecord record, int occurrence) {
    Field field = null;
    try {
      field = record.getField(fieldTemplate.getFieldName(), occurrence);
      if (field == null) {
        field = fieldTemplate.getEmptyField(occurrence);
      }
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return field;
  }

  public DataRecord getData() {
    return data;
  }

  public void setData(DataRecord data) {
    this.data = data;
  }

  public void setViewForm(boolean viewForm) {
    this.viewForm = viewForm;
  }

  public boolean isViewForm() {
    return this.viewForm;
  }

}
