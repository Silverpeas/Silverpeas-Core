/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.form.form;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.TypeManager;
import com.silverpeas.form.Util;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.jsp.JspWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Form is an object which can display in HTML the content of a DataRecord to a end user and can
 * retrieve via HTTP any updated values.
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public class XmlSearchForm extends AbstractForm {
  private List<FieldTemplate> fieldTemplates = new ArrayList<FieldTemplate>();
  private String title = "";

  public XmlSearchForm(RecordTemplate template) throws FormException {
    super(template);
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
  public void displayScripts(JspWriter jw, PagesContext PagesContext) {
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>a field is unknown by the template.
   * <LI>a field has not the required type.
   * </UL>
   */
  @Override
  public String toString(PagesContext pagesContext, DataRecord record) {
    SilverTrace.info("form", "XmlSearchForm.toString",
        "root.MSG_GEN_ENTER_METHOD");
    StringWriter sw = new StringWriter();
    /*
     * try {
     */
    String language = pagesContext.getLanguage();
    // StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw, true);

    if (pagesContext.getPrintTitle() && title != null && title.length() > 0) {
      out
          .println("<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
      out.println("<tr>");
      out.println("<td CLASS=intfdcolor4 NOWRAP>");
      out
          .println("<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH=\"100%\">");
      out.println("<tr>");
      out.println("<td class=\"intfdcolor\" nowrap width=\"100%\">");
      out.println("<img border=\"0\" src=\"" + Util.getIcon("px")
          + "\" width=5><span class=txtNav>" + title + "</span>");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
    }

    Iterator<FieldTemplate> itFields = null;
    if (fieldTemplates != null)
      itFields = this.fieldTemplates.iterator();

    if (itFields != null && itFields.hasNext()) {
      if (pagesContext.isBorderPrinted()) {
        out
            .println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=intfdcolor4>");
        out.println("<tr>");
        out.println("<td nowrap>");
        out
            .println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\">");
      } else {
        out
            .println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
      }
      out.println("<INPUT TYPE=\"hidden\" NAME=id VALUE=\"" + record.getId()
          + "\">");

      // out.flush();
      // jw.write(sw.toString());
      PagesContext pc = new PagesContext(pagesContext);
      pc.setNbFields(fieldTemplates.size());
      pc.incCurrentFieldIndex(1);

      // calcul lastFieldIndex
      int lastFieldIndex = -1;
      lastFieldIndex += new Integer(pc.getCurrentFieldIndex()).intValue();
      FieldTemplate fieldTemplate;
      String fieldName;
      String fieldType;
      String fieldDisplayerName;
      FieldDisplayer fieldDisplayer = null;

      while (itFields.hasNext()) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null) {
          fieldName = fieldTemplate.getFieldName();
          fieldType = fieldTemplate.getTypeName();
          fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if (fieldDisplayerName == null || fieldDisplayerName.equals("")) {
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            }

            fieldDisplayer = getTypeManager().getDisplayer(fieldType,
                fieldDisplayerName);
            if (fieldDisplayer != null) {
              lastFieldIndex += fieldDisplayer.getNbHtmlObjectsDisplayed(
                  fieldTemplate, pc);
            }
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlSearchForm.toString",
                "form.EXP_UNKNOWN_DISPLAYER", null, fe);
          }
        }
      }
      pc.setLastFieldIndex(lastFieldIndex);

      String fieldLabel;
      itFields = this.fieldTemplates.iterator();
      while (itFields.hasNext()) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null) {
          fieldName = fieldTemplate.getFieldName();
          fieldLabel = fieldTemplate.getLabel(language);
          fieldType = fieldTemplate.getTypeName();
          fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if (fieldDisplayerName == null || fieldDisplayerName.equals(""))
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);

            fieldDisplayer = getTypeManager().getDisplayer(fieldType,
                fieldDisplayerName);
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlSearchForm.toString",
                "form.EXP_UNKNOWN_DISPLAYER", null, fe);
          }

          if (fieldDisplayer != null) {
            // sw = new StringWriter();
            // out = new PrintWriter(sw, true);
            out.println("<tr align=center>");
            if (fieldLabel != null && !fieldLabel.equals("")) {
              out.println("<td class=\"txtlibform\" align=left width=\"200\">");
              out.println(fieldLabel);
              out.println("</TD>");
            }

            out.println("<td valign=\"baseline\" align=left>");
            try {
              fieldDisplayer.display(out, record.getField(fieldName),
                  fieldTemplate, pc);
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlSearchForm.toString",
                  "form.EX_CANT_GET_FORM", null, fe);
            }
            out.println("</TD>");
            out.println("</TR>");
            // out.flush();
            // jw.write(sw.toString());
            pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(
                fieldTemplate, pc));
          }
        }
      }
      // sw = new StringWriter();
      // out = new PrintWriter(sw, true);
      if (pagesContext.isBorderPrinted()) {
        out.println("</TABLE>");
        out.println("</TD>");
        out.println("</TR>");
      }
      out.println("</TABLE>");
      // out.flush();
      // jw.write(sw.toString());
    }
    /*
     * } catch (java.io.IOException fe) { SilverTrace.error("form", "XmlSearchForm.toString",
     * "form.EXP_CANT_WRITE", null, fe); }
     */
    return sw.getBuffer().toString();
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>a field is unknown by the template.
   * <LI>a field has not the required type.
   * </UL>
   */
  @Override
  public void display(JspWriter jw, PagesContext pagesContext, DataRecord record) {
    SilverTrace.info("form", "XmlSearchForm.display",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      String language = pagesContext.getLanguage();
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      if (pagesContext.getPrintTitle() && title != null && title.length() > 0) {
        out
            .println("<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
        out.println("<tr>");
        out.println("<td CLASS=intfdcolor4 NOWRAP>");
        out
            .println("<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH=\"100%\">");
        out.println("<tr>");
        out.println("<td class=\"intfdcolor\" nowrap width=\"100%\">");
        out.println("<img border=\"0\" src=\"" + Util.getIcon("px")
            + "\" width=5><span class=txtNav>" + title + "</span>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
      }

      Iterator<FieldTemplate> itFields = null;
      if (fieldTemplates != null)
        itFields = this.fieldTemplates.iterator();

      if (itFields != null && itFields.hasNext()) {
        if (pagesContext.isBorderPrinted()) {
          out
              .println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=intfdcolor4>");
          out.println("<tr>");
          out.println("<td nowrap>");
          out
              .println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\">");
        } else {
          out
              .println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
        }
        out.println("<INPUT TYPE=\"hidden\" NAME=id VALUE=\"" + record.getId()
            + "\">");

        out.flush();
        jw.write(sw.toString());
        PagesContext pc = new PagesContext(pagesContext);
        pc.setNbFields(fieldTemplates.size());
        pc.incCurrentFieldIndex(1);

        // calcul lastFieldIndex
        int lastFieldIndex = -1;
        lastFieldIndex += new Integer(pc.getCurrentFieldIndex()).intValue();
        FieldTemplate fieldTemplate;
        String fieldName;
        String fieldType;
        String fieldDisplayerName;
        FieldDisplayer fieldDisplayer = null;

        while (itFields.hasNext()) {
          fieldTemplate = itFields.next();
          if (fieldTemplate != null) {
            fieldName = fieldTemplate.getFieldName();
            fieldType = fieldTemplate.getTypeName();
            fieldDisplayerName = fieldTemplate.getDisplayerName();
            try {
              if (fieldDisplayerName == null || fieldDisplayerName.equals("")) {
                fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
              }

              fieldDisplayer = getTypeManager().getDisplayer(fieldType,
                  fieldDisplayerName);
              if (fieldDisplayer != null) {
                lastFieldIndex += fieldDisplayer.getNbHtmlObjectsDisplayed(
                    fieldTemplate, pc);
              }
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlSearchForm.display",
                  "form.EXP_UNKNOWN_DISPLAYER", null, fe);
            }
          }
        }
        pc.setLastFieldIndex(lastFieldIndex);

        String fieldLabel;
        itFields = this.fieldTemplates.iterator();
        while (itFields.hasNext()) {
          fieldTemplate = itFields.next();
          if (fieldTemplate != null) {
            fieldName = fieldTemplate.getFieldName();
            fieldLabel = fieldTemplate.getLabel(language);
            fieldType = fieldTemplate.getTypeName();
            fieldDisplayerName = fieldTemplate.getDisplayerName();
            try {
              if (fieldDisplayerName == null || fieldDisplayerName.equals(""))
                fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);

              fieldDisplayer = getTypeManager().getDisplayer(fieldType,
                  fieldDisplayerName);
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlSearchForm.display",
                  "form.EXP_UNKNOWN_DISPLAYER", null, fe);
            }

            if (fieldDisplayer != null) {
              sw = new StringWriter();
              out = new PrintWriter(sw, true);
              out.println("<tr align=center>");
              out.println("<td class=\"txtlibform\" align=left width=\"200\">");
              if (fieldLabel != null && !fieldLabel.equals(""))
                out.println(fieldLabel);
              else
                out.println("&nbsp;");
              out.println("</TD>");
              out.println("<td valign=\"baseline\" align=left>");
              try {
                fieldDisplayer.display(out, record.getField(fieldName),
                    fieldTemplate, pc);
              } catch (FormException fe) {
                SilverTrace.error("form", "XmlSearchForm.display",
                    "form.EX_CANT_GET_FORM", null, fe);
              }
              out.println("</TD>");
              out.println("</TR>");
              out.flush();
              jw.write(sw.toString());
              pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(
                  fieldTemplate, pc));
            }
          }
        }
        sw = new StringWriter();
        out = new PrintWriter(sw, true);
        if (pagesContext.isBorderPrinted()) {
          out.println("</TABLE>");
          out.println("</TD>");
          out.println("</TR>");
        }
        out.println("</TABLE>");
        out.flush();
        jw.write(sw.toString());
      }
    } catch (java.io.IOException fe) {
      SilverTrace.error("form", "XmlSearchForm.display", "form.EXP_CANT_WRITE",
          null, fe);
    }
  }

  private String getParameterValue(List<FileItem> items, String parameterName) {
    SilverTrace.debug("form", "XmlSearchForm.getParameterValue",
        "root.MSG_GEN_ENTER_METHOD", "parameterName = " + parameterName);
    FileItem item = getParameter(items, parameterName);
    if (item != null && item.isFormField()) {
      SilverTrace.debug("form", "XmlSearchForm.getParameterValue",
          "root.MSG_GEN_EXIT_METHOD", "parameterValue = " + item.getString());
      return item.getString();
    }
    return null;
  }

  private String getParameterValues(List<FileItem> items, String parameterName) {
    SilverTrace.debug("form", "XmlSearchForm.getParameterValues",
        "root.MSG_GEN_ENTER_METHOD", "parameterName = " + parameterName);
    String values = "";
    List<FileItem> params = getParameters(items, parameterName);
    FileItem item = null;
    for (int p = 0; p < params.size(); p++) {
      item = params.get(p);
      values += item.getString();
      if (p < params.size() - 1) {
        values += "##";
      }
    }
    SilverTrace.debug("form", "XmlSearchForm.getParameterValues",
        "root.MSG_GEN_EXIT_METHOD", "parameterValue = " + values);
    return values;
  }

  private FileItem getParameter(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  // for multi-values parameter (like checkbox)
  private List<FileItem> getParameters(List<FileItem> items, String parameterName) {
    List<FileItem> parameters = new ArrayList<FileItem>();
    for (FileItem item : items) {
      if (parameterName.equals(item.getFieldName())) {
        parameters.add(item);
      }
    }
    return parameters;
  }

  /**
   * Get the form title
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Set the form title
   */
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public boolean isEmpty(List<FileItem> items, DataRecord record,
      PagesContext pagesContext) {
    boolean isEmpty = true;
    Iterator<FieldTemplate> itFields = null;
    if (fieldTemplates != null)
      itFields = this.fieldTemplates.iterator();
    if (itFields != null && itFields.hasNext()) {
      FieldDisplayer fieldDisplayer = null;
      FieldTemplate fieldTemplate = null;
      while (itFields.hasNext() || !isEmpty) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null) {
          String fieldType = fieldTemplate.getTypeName();
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if (!StringUtil.isDefined(fieldDisplayerName))
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            fieldDisplayer = getTypeManager().getDisplayer(fieldType,
                fieldDisplayerName);
            if (fieldDisplayer != null) {
              String itemName = fieldTemplate.getFieldName();
              String itemValue = null;

              if (Field.TYPE_FILE.equals(fieldType)) {
                FileItem image = getParameter(items, itemName);
                if (image != null && !image.isFormField()
                    && StringUtil.isDefined(image.getName()))
                  isEmpty = false;
              } else {
                if (fieldDisplayerName.equals("checkbox")) {
                  itemValue = getParameterValues(items, itemName);
                } else {
                  itemValue = getParameterValue(items, itemName);
                }
                if (StringUtil.isDefined(itemValue))
                  isEmpty = false;
              }
            }
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlSearchForm.isEmpty",
                "form.EXP_UNKNOWN_FIELD", null, fe);
          } catch (Exception e) {
            SilverTrace.error("form", "XmlSearchForm.isEmpty",
                "form.EXP_UNKNOWN_FIELD", null, e);
          }
        }
      }
    }
    return isEmpty;
  }
  
  private TypeManager getTypeManager() {
    return TypeManager.getInstance();
  }
     
}