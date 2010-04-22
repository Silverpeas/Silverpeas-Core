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

package com.silverpeas.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.fieldType.UserField;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public abstract class AbstractForm implements Form {

  private List<FieldTemplate> fieldTemplates = new ArrayList<FieldTemplate>();
  private String title = "";
  public static final String CONTEXT_FORM_FILE = "Images";
  public static final String CONTEXT_FORM_IMAGE = "XMLFormImages";

  public AbstractForm(RecordTemplate template) throws FormException {
    if (template != null) {
      FieldTemplate fields[] = template.getFieldTemplates();
      int size = fields.length;
      FieldTemplate fieldTemplate;
      for (int i = 0; i < size; i++) {
        fieldTemplate = fields[i];
        this.fieldTemplates.add(fieldTemplate);
      }
    }
  }

  public List<FieldTemplate> getFieldTemplates() {
    return fieldTemplates;
  }

  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Set the form title
   */
  public void setTitle(String title) {
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
   */
  @Override
  public void displayScripts(JspWriter jw, PagesContext pagesContext) {
    try {
      String language = pagesContext.getLanguage();
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);
      Iterator<FieldTemplate> itFields = null;

      if (fieldTemplates != null) {
        itFields = this.fieldTemplates.iterator();
      }

      FieldTemplate fieldTemplate = null;
      if (itFields != null && itFields.hasNext()) {
        fieldTemplate = (FieldTemplate) itFields.next();

        out.println(
            "<script type=\"text/javascript\" src=\"/weblib/xmlforms/" +
            fieldTemplate.getTemplateName() + ".js\"></script>");
      }

      out.println(Util.getJavascriptIncludes());
      out.println("<script type=\"text/javascript\">");
      out.println("	var errorNb = 0;");
      out.println("	var errorMsg = \"\";");
      out.println("function addXMLError(message) {");
      out.println("	errorMsg+=\"  - \"+message+\"\\n\";");
      out.println("	errorNb++;");
      out.println("}");
      out.println("function getXMLField(fieldName) {");
      out.println("	return document.getElementById(fieldName);");
      out.println("}");
      out.println("function isCorrectForm() {");
      out.println("	errorMsg = \"\";");
      out.println("	errorNb = 0;");
      out.println("	var field;");
      out.println("	\n");
      if (fieldTemplates != null) {
        itFields = this.fieldTemplates.iterator();
      }
      if ((itFields != null) && (itFields.hasNext())) {
        PagesContext pc = new PagesContext(pagesContext);
        pc.incCurrentFieldIndex(1);
        while (itFields.hasNext()) {
          fieldTemplate = (FieldTemplate) itFields.next();
          if (fieldTemplate != null) {
            String fieldDisplayerName = fieldTemplate.getDisplayerName();
            String fieldType = fieldTemplate.getTypeName();
            FieldDisplayer fieldDisplayer = null;
            try {
              if ((fieldDisplayerName == null) || (fieldDisplayerName.equals(""))) {
                fieldDisplayerName = TypeManager.getDisplayerName(fieldType);
              }

              fieldDisplayer = TypeManager.getDisplayer(fieldType, fieldDisplayerName);

              if (fieldDisplayer != null) {
                out.println("	field = document.getElementById(\"" + fieldTemplate.getFieldName() +
                    "\");");
                out.println("	if (field != null) {");
                fieldDisplayer.displayScripts(out, fieldTemplate, pc);
                out.println("}\n");
                pc
                    .incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate,
                    pc));
              }
            } catch (FormException fe) {
              SilverTrace.error("form", "AbstractForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
            }
          }
        }
      }
      out.println("	\n");
      out.println("	switch(errorNb)");
      out.println("	{");
      out.println("	case 0 :");
      out.println("		result = true;");
      out.println("		break;");
      out.println("	case 1 :");
      out.println("		errorMsg = \"" + Util.getString("GML.ThisFormContains", language) + " 1 " +
          Util.getString(
          "GML.error", language) + " : \\n \" + errorMsg;");
      out.println("		window.alert(errorMsg);");
      out.println("		result = false;");
      out.println("		break;");
      out.println("	default :");
      out.println("		errorMsg = \"" + Util.getString("GML.ThisFormContains", language) +
          "\" + errorNb + \" " + Util.
          getString("GML.errors", language) + " :\\n \" + errorMsg;");
      out.println("		window.alert(errorMsg);");
      out.println("		result = false;");
      out.println("		break;");
      out.println("	}");
      out.println("	return result;");
      out.println("}");
      out.println("	\n");
      out.println("</script>");
      out.flush();
      jw.write(sw.toString());
    } catch (java.io.IOException fe) {
      SilverTrace.error("form", "AbstractForm.display", "form.EXP_CANT_WRITE", null, fe);
    }
  }

  @Override
  public abstract void display(JspWriter out, PagesContext pagesContext, DataRecord record);

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public List<String> update(List<FileItem> items, DataRecord record, PagesContext pagesContext) {
    List<String> attachmentIds = new ArrayList<String>();
    Iterator<FieldTemplate> itFields = null;
    if (fieldTemplates != null) {
      itFields = this.fieldTemplates.iterator();
    }
    if ((itFields != null) && (itFields.hasNext())) {
      FieldDisplayer fieldDisplayer = null;
      FieldTemplate fieldTemplate = null;
      while (itFields.hasNext()) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null) {
          String fieldName = fieldTemplate.getFieldName();
          String fieldType = fieldTemplate.getTypeName();
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if ((fieldDisplayerName == null) || (fieldDisplayerName.equals(""))) {
              fieldDisplayerName = TypeManager.getDisplayerName(fieldType);
            }
            fieldDisplayer = TypeManager.getDisplayer(fieldType, fieldDisplayerName);
            if (fieldDisplayer != null) {
              attachmentIds.addAll(fieldDisplayer.update(items, record
                  .getField(fieldName), fieldTemplate, pagesContext));
            }
          } catch (FormException fe) {
            SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, fe);
          } catch (Exception e) {
            SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, e);
          }
        }
      }
    }
    return attachmentIds;
  }

  @Override
  public boolean isEmpty(List<FileItem> items, DataRecord record, PagesContext pagesContext) {
    boolean isEmpty = true;
    Iterator<FieldTemplate> itFields = null;
    if (fieldTemplates != null) {
      itFields = this.fieldTemplates.iterator();
    }
    if (itFields != null && itFields.hasNext()) {
      FieldDisplayer fieldDisplayer = null;
      FieldTemplate fieldTemplate = null;
      while (itFields.hasNext() && isEmpty) {
        fieldTemplate = (FieldTemplate) itFields.next();
        if (fieldTemplate != null) {
          String fieldType = fieldTemplate.getTypeName();
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if (!StringUtil.isDefined(fieldDisplayerName)) {
              fieldDisplayerName = TypeManager.getDisplayerName(fieldType);
            }
            fieldDisplayer = TypeManager.getDisplayer(fieldType, fieldDisplayerName);
            if (fieldDisplayer != null) {
              String itemName = fieldTemplate.getFieldName();
              if (fieldType.equals(UserField.TYPE)) {
                itemName = itemName + UserField.PARAM_NAME_SUFFIX;
              }
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
      }
    }
    return isEmpty;
  }

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

  private FileItem getParameter(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    FileItem item = null;
    while (iter.hasNext()) {
      item = iter.next();
      if (parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }
}