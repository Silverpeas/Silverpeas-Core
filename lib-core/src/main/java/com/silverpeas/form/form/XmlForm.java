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

package com.silverpeas.form.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

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
import com.silverpeas.form.fieldType.JdbcRefField;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A Form is an object which can display in HTML the content of a DataRecord to a end user and can
 * retrieve via HTTP any updated values.
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public class XmlForm extends AbstractForm {
  public XmlForm(RecordTemplate template) throws FormException {
    super(template);
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>a field is unknown by the template.</li>
   * <li>a field has not the required type.</li>
   * </ul>
   * @param pagesContext
   * @param record
   * @return  
   */
  @Override
  public String toString(PagesContext pagesContext, DataRecord record) {
    SilverTrace.info("form", "XmlForm.toString", "root.MSG_GEN_ENTER_METHOD");
    StringWriter sw = new StringWriter();
    String language = pagesContext.getLanguage();
    PrintWriter out = new PrintWriter(sw, true);
    if (pagesContext.getPrintTitle() && StringUtil.isDefined(getTitle())) {
      out.println("<table cellpadding=\"0\" cellspacing=\"2\" border=\"0\" width=\"98%\" class=\"intfdcolor\">");
      out.println("<tr>");
      out.println("<td class=\"intfdcolor4\" nowrap=\"nowrap\">");
      out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
      out.println("<tr>");
      out.println("<td class=\"intfdcolor\" nowrap=\"nowrap\" width=\"100%\">");
      out.println("<img border=\"0\" src=\"" + Util.getIcon("px") +
          "\" width=\"5\" alt=\"\"/><span class=\"txtNav\">" + getTitle() + "</span>");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
    }

    Iterator<FieldTemplate> itFields = null;
    List<FieldTemplate> listField = getFieldTemplates();
    if (listField != null) {
      itFields = listField.iterator();
    }
    boolean mandatory = false;
    if (itFields != null && itFields.hasNext()) {
      out.println("<input type=\"hidden\" name=\"id\" value=\"" + record.getId() + "\"/>");
      if (pagesContext.isBorderPrinted()) {
        out
            .println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"intfdcolor4\">");
        out.println("<tr>");
        out.println("<td nowrap=\"nowrap\">");
        out
            .println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\">");
      } else {
        out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
      }
      PagesContext pc = new PagesContext(pagesContext);
      pc.setNbFields(listField.size());
      pc.incCurrentFieldIndex(1);

      // calcul lastFieldIndex
      int lastFieldIndex = -1;
      lastFieldIndex += new Integer(pc.getCurrentFieldIndex()).intValue();
      FieldTemplate fieldTemplate;
      String fieldName;
      Field field = null;
      String fieldType;
      String fieldDisplayerName;
      FieldDisplayer fieldDisplayer = null;

      while (itFields.hasNext()) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null) {
          fieldName = fieldTemplate.getFieldName();
          fieldType = fieldTemplate.getTypeName();
          fieldDisplayerName = fieldTemplate.getDisplayerName();

          field = null;
          if (record != null) {
            try {
              field = record.getField(fieldName);
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlForm.toString", "form.EXP_UNKNOWN_FIELD", null, fe);
            }
          }

          if (record == null || (record != null && field != null)) {
            try {
              if (!StringUtil.isDefined(fieldDisplayerName)) {
                fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
              }

              fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
              if (fieldDisplayer != null) {
                lastFieldIndex += fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc);
              }
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlForm.toString", "form.EXP_UNKNOWN_DISPLAYER", null, fe);
            }
          }
        }
      }
      pc.setLastFieldIndex(lastFieldIndex);

      String fieldLabel;
      boolean isMandatory;
      boolean isDisabled;
      boolean isReadOnly;
      boolean isHidden;
      itFields = listField.iterator();
      while (itFields.hasNext()) {
        fieldTemplate = itFields.next();
        if (fieldTemplate != null) {
          fieldName = fieldTemplate.getFieldName();
          fieldLabel = fieldTemplate.getLabel(language);
          fieldType = fieldTemplate.getTypeName();
          fieldDisplayerName = fieldTemplate.getDisplayerName();
          isMandatory = fieldTemplate.isMandatory();
          isDisabled = fieldTemplate.isDisabled();
          isReadOnly = fieldTemplate.isReadOnly();
          isHidden = fieldTemplate.isHidden();

          field = null;
          try {
            field = record.getField(fieldName);
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlForm.toString", "form.EXP_UNKNOWN_FIELD", null, fe);
          }

          if (field != null) {
            try {
              if (!StringUtil.isDefined(fieldDisplayerName)) {
                fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
              }
              fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlForm.toString", "form.EXP_UNKNOWN_DISPLAYER", null, fe);
            }

            if (fieldDisplayer != null) {
              out.println("<tr align=\"center\">");
              if (fieldLabel != null && !fieldLabel.equals("")) {
                out
                    .println("<td class=\"intfdcolor4\" valign=\"top\" align=\"left\" nowrap=\"nowrap\">");
                out.println("<span class=\"txtlibform\">" + fieldLabel + " :</span>");
                out.println("</td>");
              }

              out.println("<td class=\"intfdcolor4\" valign=\"baseline\" align=\"left\">");
              if (field == null) {
                try {
                  field = fieldTemplate.getEmptyField();
                } catch (FormException fe) {
                  SilverTrace.error("form", "XmlForm.toString", "form.EXP_UNKNOWN_FIELD", null, fe);
                }
              }

              try {
                fieldDisplayer.display(out, record.getField(fieldName), fieldTemplate, pc);
              } catch (FormException fe) {
                SilverTrace.error("form", "XmlForm.toString", "form.EX_CANT_GET_FORM", null, fe);
              }

              if (isMandatory && !isDisabled && !isReadOnly && !isHidden &&
                  fieldDisplayer.isDisplayedMandatory()) {
                mandatory = true;
              }
              out.println("</td>");
              out.println("</tr>");
              pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc));
            }
          }
        }
      }
      if (mandatory) {
        out.println("<tr align=\"left\">");
        out.println("<td colspan=\"2\">");
        out.println("(<img border=\"0\" src=\"" + Util.getIcon("mandatoryField") +
            "\" width=\"5\" height=\"5\" alt=\"" + Util.getString("GML.requiredField", language) +
            "\"/>&nbsp;:&nbsp;" + Util.getString("GML.requiredField", language) + ")");
        out.println("</td>");
        out.println("</tr>");
      }
      if (pagesContext.isBorderPrinted()) {
        out.println("</table>");
        out.println("</td>");
        out.println("</tr>");
      }
      out.println("</table>");     
    }
    return sw.getBuffer().toString();
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>a field is unknown by the template.</li>
   * <li>a field has not the required type.</li>
   * </ul>
   */
  @Override
  public void display(JspWriter jw, PagesContext pagesContext, DataRecord record) {
    SilverTrace.info("form", "XmlForm.display", "root.MSG_GEN_ENTER_METHOD");
    try {
      String language = pagesContext.getLanguage();
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw, true);

      if (record != null) {
        out.println("<input type=\"hidden\" name=\"id\" value=\"" + record.getId() + "\"/>");
      }

      if (pagesContext.getPrintTitle() && getTitle() != null && getTitle().length() > 0) {
        out
            .println("<table cellpadding=\"0\" cellspacing=\"2\" border=\"0\" width=\"98%\" class=\"intfdcolor\">");
        out.println("<tr>");
        out.println("<td class=\"intfdcolor4\" nowrap=\"nowrap\">");
        out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
        out.println("<tr>");
        out.println("<td class=\"intfdcolor\" nowrap=\"nowrap\" width=\"100%\">");
        out.println("<img border=\"0\" src=\"" + Util.getIcon("px") +
            "\" width=\"5\" alt=\"\"/><span class=\"txtNav\">" + getTitle() + "</span>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
      }

      Iterator<FieldTemplate> itFields = null;
      List<FieldTemplate> listField = getFieldTemplates();
      if (listField != null) {
        itFields = listField.iterator();
      }
      boolean mandatory = false;
      if ((itFields != null) && (itFields.hasNext())) {
        if (pagesContext.isBorderPrinted()) {
          out.println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"intfdcolor4\">");
          out.println("<tr>");
          out.println("<td nowrap=\"nowrap\">");
          out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\">");
        } else {
          out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
        }

        out.flush();
        jw.write(sw.toString());

        PagesContext pc = new PagesContext(pagesContext);
        pc.setNbFields(listField.size());
        if (record != null) {
          pc.incCurrentFieldIndex(1);
        }

        // calcul lastFieldIndex
        int lastFieldIndex = -1;
        lastFieldIndex += new Integer(pc.getCurrentFieldIndex()).intValue();
        FieldTemplate fieldTemplate;
        String fieldName;
        Field field = null;
        String fieldType;
        String fieldDisplayerName;
        FieldDisplayer fieldDisplayer = null;

        while (itFields.hasNext()) {
          fieldTemplate = itFields.next();
          if (fieldTemplate != null) {
            fieldName = fieldTemplate.getFieldName();
            fieldType = fieldTemplate.getTypeName();
            fieldDisplayerName = fieldTemplate.getDisplayerName();

            field = null;
            if (record != null) {
              try {
                field = record.getField(fieldName);
              } catch (FormException fe) {
                SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
              }
            }

            if (record == null || (record != null && field != null)) {
              try {
                if (fieldDisplayerName == null || fieldDisplayerName.equals("")) {
                  fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
                }

                fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
                if (fieldDisplayer != null) {
                  lastFieldIndex += fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc);
                }
              } catch (FormException fe) {
                SilverTrace
                    .error("form", "XmlForm.display", "form.EXP_UNKNOWN_DISPLAYER", null, fe);
              }
            }
          }
        }
        pc.setLastFieldIndex(lastFieldIndex);

        String fieldLabel;
        boolean isMandatory;
        boolean isDisabled;
        boolean isReadOnly;
        boolean isHidden;
        String fieldClass = null;
        itFields = listField.iterator();
        while (itFields.hasNext()) {
          fieldTemplate = itFields.next();
          Map<String, String> parameters = fieldTemplate.getParameters(language);
          fieldName = fieldTemplate.getFieldName();
          fieldLabel = fieldTemplate.getLabel(language);
          fieldType = fieldTemplate.getTypeName();
          fieldDisplayerName = fieldTemplate.getDisplayerName();
          isMandatory = fieldTemplate.isMandatory();
          isDisabled = fieldTemplate.isDisabled();
          isReadOnly = fieldTemplate.isReadOnly();
          isHidden = fieldTemplate.isHidden();
          fieldClass = "";
          if (parameters.containsKey("classLabel")) {
            fieldClass = parameters.get("classLabel");
          }

          field = null;
          if (record != null) {
            try {
              field = record.getField(fieldName);
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
            }
          }

          if (record == null || (record != null && field != null)) {
            try {
              if (!StringUtil.isDefined(fieldDisplayerName)) {
                fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
              }

              fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
            } catch (FormException fe) {
              SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_DISPLAYER", null, fe);
            }

            if (fieldDisplayer != null) {
              sw = new StringWriter();
              out = new PrintWriter(sw, true);
              out.println("<tr align=\"center\">");
              out.println("<td class=\"intfdcolor4\" valign=\"top\" align=\"left\">");
              if (fieldLabel != null && !fieldLabel.equals("")) {
                if (StringUtil.isDefined(fieldClass)) {
                  out.println("<span class=\"" + fieldClass + "\">" + fieldLabel + " :</span>");
                } else {
                  out.println("<span class=\"txtlibform\">" + fieldLabel + " :</span>");
                }
              } else {
                out.println("<span class=\"txtlibform\">&nbsp;</span>");
              }
              out.println("</td>");
              out.println("<td class=\"intfdcolor4\" valign=\"baseline\" align=\"left\">");
              if (field == null) {
                try {
                  field = fieldTemplate.getEmptyField();
                } catch (FormException fe) {
                  SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
                }
              }
              try {
                fieldDisplayer.display(out, field, fieldTemplate, pc);
              } catch (FormException fe) {
                SilverTrace.error("form", "XmlForm.display", "form.EX_CANT_GET_FORM", null, fe);
              }
              if (isMandatory && !isDisabled && !isHidden
                  && fieldDisplayer.isDisplayedMandatory()
                  && (!isReadOnly || fieldType.equals(JdbcRefField.TYPE))) {
                mandatory = true;
              }
              out.println("</td>");
              out.println("</tr>");
              out.flush();
              jw.write(sw.toString());
              pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc));
            }
          }
        }
        sw = new StringWriter();
        out = new PrintWriter(sw, true);
        if (mandatory) {
          out.println("<tr align=\"left\">");
          out.println("<td colspan=\"2\">");
          out.println("(<img border=\"0\" src=\"" + Util.getIcon("mandatoryField") +
              "\" width=\"5\" height=\"5\" alt=\"\"/>&nbsp;:&nbsp;" +
              Util.getString("GML.requiredField", language) + ")");
          out.println("</td>");
          out.println("</tr>");
        }
        if (pagesContext.isBorderPrinted()) {
          out.println("</table>");
          out.println("</td>");
          out.println("</tr>");
        }
        out.println("</table>");
        out.flush();
        jw.write(sw.toString());
      }
    } catch (java.io.IOException fe) {
      SilverTrace.error("form", "XmlForm.display", "form.EXP_CANT_WRITE", null, fe);
    }
  }
  
  private TypeManager getTypeManager() {
    return TypeManager.getInstance();
  }
}