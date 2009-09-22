package com.silverpeas.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.fileupload.FileItem;


import com.silverpeas.form.fieldType.UserField;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public abstract class AbstractForm implements Form {

  private List fieldTemplates = new ArrayList();
  private String title = "";

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

  public List getFieldTemplates() {
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
   * Prints the javascripts which will be used to control
   * the new values given to the data record fields.
   *
   * The error messages may be adapted to a local language.
   * The RecordTemplate gives the field type and constraints.
   * The RecordTemplate gives the local label too.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> a field is unknown by the template.
   * <LI> a field has not the required type.
   * </UL>
   */
  @Override
  public void displayScripts(JspWriter jw, PagesContext PagesContext) {
    try {
      String language = PagesContext.getLanguage();
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);
      Iterator itFields = null;

      if (fieldTemplates != null) {
        itFields = this.fieldTemplates.iterator();
      }

      FieldTemplate fieldTemplate = null;
      if (itFields != null && itFields.hasNext()) {
        //while (itFields.hasNext())
        //{
        fieldTemplate = (FieldTemplate) itFields.next();

        //out.println("<script type=\"text/javascript\" src=\"/weblib/xmlforms/"+fieldTemplate.getTemplateName()+"/"+fieldTemplate.getFieldName()+".js\"></script>");
        out.println(
            "<script type=\"text/javascript\" src=\"/weblib/xmlforms/" + fieldTemplate.getTemplateName() + ".js\"></script>");
        //}
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
        PagesContext pc = new PagesContext(PagesContext);
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
                //out.println("	field = document.forms[" + pc.getFormIndex() + "].elements[\"" + fieldTemplate.getFieldName() + "\"];");
                out.println("	field = document.getElementById(\"" + fieldTemplate.getFieldName() + "\");");
                out.println("	if (field != null) {");
                fieldDisplayer.displayScripts(out, fieldTemplate, pc);
                out.println("}\n");
                pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc));
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
      out.println("		errorMsg = \"" + Util.getString("GML.ThisFormContains", language) + " 1 " + Util.getString(
          "GML.error", language) + " : \\n \" + errorMsg;");
      out.println("		window.alert(errorMsg);");
      out.println("		result = false;");
      out.println("		break;");
      out.println("	default :");
      out.println("		errorMsg = \"" + Util.getString("GML.ThisFormContains", language) + "\" + errorNb + \" " + Util.
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
  public abstract void display(JspWriter out, PagesContext PagesContext, DataRecord record);

  /**
   * Updates the values of the dataRecord using the RecordTemplate
   * to extra control information (readOnly or mandatory status).
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public List<String> update(List items, DataRecord record, PagesContext pagesContext) {
    List<String> attachmentIds = new ArrayList<String>();
    Iterator itFields = null;
    if (fieldTemplates != null) {
      itFields = this.fieldTemplates.iterator();
    }
    if ((itFields != null) && (itFields.hasNext())) {
      FieldDisplayer fieldDisplayer = null;
      FieldTemplate fieldTemplate = null;
      while (itFields.hasNext()) {
        fieldTemplate = (FieldTemplate) itFields.next();
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
              attachmentIds.addAll(fieldDisplayer.update((List<FileItem>)items, record.getField(fieldName),
                  fieldTemplate, pagesContext));
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
  public boolean isEmpty(List items, DataRecord record, PagesContext pagesContext) {
    boolean isEmpty = true;
    Iterator itFields = null;
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
                String itemValue = getParameterValue(items, itemName);
                isEmpty = !StringUtil.isDefined(itemValue);
              }
            }
          }catch  (FormException fe) {
            SilverTrace.error("form", "AbstractForm.isEmpty", "form.EXP_UNKNOWN_FIELD", null, fe);
          } catch (Exception e) {
            SilverTrace.error("form", "AbstractForm.isEmpty", "form.EXP_UNKNOWN_FIELD", null, e);
          }
        }
      }
    }
    return isEmpty;
  }

  private boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private String getParameterValue(List items, String parameterName) {
    SilverTrace.debug("form", "AbstractForm.getParameterValue", "root.MSG_GEN_ENTER_METHOD",
        "parameterName = " + parameterName);
    FileItem item = getParameter(items, parameterName);
    if (item != null && item.isFormField()) {
      SilverTrace.debug("form", "AbstractForm.getParameterValue", "root.MSG_GEN_EXIT_METHOD",
          "parameterValue = " + item.getString());
      return item.getString();
    }
    return null;
  }

  private String getParameterValues(List items, String parameterName) {
    SilverTrace.debug("form", "AbstractForm.getParameterValues", "root.MSG_GEN_ENTER_METHOD",
        "parameterName = " + parameterName);
    String values = "";
    List params = getParameters(items, parameterName);
    FileItem item = null;
    for (int p = 0; p < params.size(); p++) {
      item = (FileItem) params.get(p);
      values += item.getString();
      if (p < params.size() - 1) {
        values += "##";
      }
    }
    SilverTrace.debug("form", "AbstractForm.getParameterValues", "root.MSG_GEN_EXIT_METHOD",
        "parameterValue = " + values);
    return values;
  }

  private FileItem getParameter(List items, String parameterName) {
    Iterator iter = items.iterator();
    FileItem item = null;
    while (iter.hasNext()) {
      item = (FileItem) iter.next();
      if (parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  //for multi-values parameter (like checkbox)
  private List getParameters(List items, String parameterName) {
    List parameters = new ArrayList();
    Iterator iter = items.iterator();
    FileItem item = null;
    while (iter.hasNext()) {
      item = (FileItem) iter.next();
      if (parameterName.equals(item.getFieldName())) {
        parameters.add(item);
      }
    }
    return parameters;
  }

  private boolean runOnUnix() {
    ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
    return settings.getBoolean("runOnSolaris", false);
  }
}
