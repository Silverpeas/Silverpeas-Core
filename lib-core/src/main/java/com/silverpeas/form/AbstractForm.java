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

package com.silverpeas.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;

import java.util.Arrays;

/**
 * This abstract class implements the form interface and provides for all concretes classes a
 * default implementation of some displaying methods.
 */
public abstract class AbstractForm implements Form {

  private List<FieldTemplate> fieldTemplates;
  private String title = "";
  private String name = "";
  public static final String CONTEXT_FORM_FILE = "Images";
  public static final String CONTEXT_FORM_IMAGE = "XMLFormImages";

  /**
   * Creates a new form from the specified template of records.
   * @param template the record template.
   * @throws FormException if an error occurs while setting up the form.
   */
  public AbstractForm(final RecordTemplate template) throws FormException {
    if (template != null) {
      fieldTemplates = Arrays.asList(template.getFieldTemplates());
    } else {
      fieldTemplates = new ArrayList<FieldTemplate>();
    }
  }

  /**
   * Gets the template of all of the fields that made this form.
   * @return
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
        ComponentInstLight component =
            new OrganizationController().getComponentInstLight(pagesContext.getComponentId());
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
          FieldDisplayer fieldDisplayer = null;
          try {
            if (fieldDisplayerName == null || fieldDisplayerName.isEmpty()) {
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            }
            fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);

            if (fieldDisplayer != null) {
              out.append("	field = document.getElementById(\"")
                  .append(fieldTemplate.getFieldName()).append("\");\n");
              out.append("	if (field == null) {\n");
              // try to find field by name
              out.append("  field = $(\"input[name=").append(fieldTemplate.getFieldName()).append(
                  "]\");\n");
              out.println("}");
              out.append(" if (field != null) {\n");
              fieldDisplayer.displayScripts(out, fieldTemplate, pc);
              out.println("}");
              pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate,
                  pc));
            }
          } catch (FormException fe) {
            SilverTrace.error("form", "AbstractForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
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
          .append("\" + errorNb + \" ")
          .append(Util.getString("GML.errors", language))
          .append(" :\\n \" + errorMsg;\n")
          .append("		window.alert(errorMsg);\n")
          .append("		result = false;\n")
          .append("		break;\n")
          .append("	}\n")
          .append("	return result;\n")
          .append("}\n")
          .append("	\n\n")
          .append("</script>\n");
      out.flush();
      jw.write(sw.toString());
    } catch (java.io.IOException fe) {
      SilverTrace.error("form", "AbstractForm.display", "form.EXP_CANT_WRITE", null, fe);
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
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @param items the item of a form in which is embbeded multipart data.
   * @param record the record of data.
   * @param pagesContext the page context.
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
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
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  @Override
  public List<String> update(List<FileItem> items, DataRecord record, PagesContext pagesContext,
      boolean updateWysiwyg) {
    List<String> attachmentIds = new ArrayList<String>();

    for (FieldTemplate fieldTemplate : fieldTemplates) {
      FieldDisplayer fieldDisplayer = null;

      // Have to check if field is not readonly, if so no need to update
      if (!fieldTemplate.isReadOnly()) {
        if (fieldTemplate != null) {
          String fieldName = fieldTemplate.getFieldName();
          String fieldType = fieldTemplate.getTypeName();
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if ((fieldDisplayerName == null) || (fieldDisplayerName.isEmpty())) {
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            }
            if ((!"wysiwyg".equals(fieldDisplayerName) || updateWysiwyg)) {
              fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
              if (fieldDisplayer != null) {
                attachmentIds.addAll(fieldDisplayer.update(items, record.getField(fieldName),
                    fieldTemplate, pagesContext));
              }
            }
          } catch (FormException fe) {
            SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, fe);
          } catch (Exception e) {
            SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, e);
          }
        }
      } else {
        SilverTrace.info("form", "AbstractForm.update", "root.MSG_GEN_PARAM_VALUE", fieldTemplate
            .getFieldName() +
            " : field value is ignored as field is read only");
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
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  @Override
  public List<String> updateWysiwyg(List<FileItem> items, DataRecord record,
      PagesContext pagesContext) {
    List<String> attachmentIds = new ArrayList<String>();
    for (FieldTemplate fieldTemplate : fieldTemplates) {
      FieldDisplayer fieldDisplayer = null;
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
        } catch (FormException fe) {
          SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, fe);
        } catch (Exception e) {
          SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, e);
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
      FieldDisplayer fieldDisplayer = null;
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
        } catch (FormException fe) {
          SilverTrace.error("form", "AbstractForm.isEmpty", "form.EXP_UNKNOWN_FIELD", null, fe);
        } catch (Exception e) {
          SilverTrace.error("form", "AbstractForm.isEmpty", "form.EXP_UNKNOWN_FIELD", null, e);
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
    SilverTrace.debug("form", "AbstractForm.getParameterValue", "root.MSG_GEN_ENTER_METHOD",
        "parameterName = " + parameterName);
    FileItem item = getParameter(items, parameterName);
    if (item != null && item.isFormField()) {
      SilverTrace.debug("form", "AbstractForm.getParameterValue", "root.MSG_GEN_EXIT_METHOD",
          "parameterValue = " + item.getString());
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
}
