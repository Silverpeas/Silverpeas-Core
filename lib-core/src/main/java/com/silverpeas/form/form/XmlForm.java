/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.form.form;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.silverpeas.form.record.GenericFieldTemplate;
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
    PrintWriter pw = new PrintWriter(sw, true);
    display(pw, pagesContext, record);
    return pw.toString();
  }

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>a field is unknown by the template.</li>
   * <li>a field has not the required type.</li>
   * </ul>
   * @param jw
   */
  private void display(PrintWriter out, PagesContext pageContext, DataRecord record) {
    SilverTrace.info("form", "XmlForm.display", "root.MSG_GEN_ENTER_METHOD");
    String language = pageContext.getLanguage();
    
    String mode = "";
    if (pageContext.isDesignMode()) {
      mode = "mode-design";
    }

    out.println("<div class=\"forms " + getFormName() + " " + mode + "\">");

    if (record != null) {
      out.println("<input type=\"hidden\" name=\"id\" value=\"" + record.getId() + "\"/>");
    }

    if (pageContext.getPrintTitle() && StringUtil.isDefined(getTitle())) {
      out.println("<h2 class=\"form-title\">");
      out.println(getTitle());
      out.println("</h2>");
    }

    List<FieldTemplate> listFields = getFieldTemplates();

    boolean mandatory = false;
    if (listFields != null && !listFields.isEmpty()) {
      if (pageContext.isBorderPrinted()) {
        out.println("<ul class=\"fields form-border\">");
      } else {
        out.println("<ul class=\"fields\">");
      }

      out.flush();

      PagesContext pc = new PagesContext(pageContext);
      pc.setNbFields(listFields.size());
      if (record != null) {
        pc.incCurrentFieldIndex(1);
      }

      // calcul lastFieldIndex
      pc.setLastFieldIndex(getLastFieldIndex(pageContext, record, listFields));

      boolean isMandatory;
      for (FieldTemplate fieldTemplate : listFields) {
        Map<String, String> parameters = fieldTemplate.getParameters(language);
        String fieldName = fieldTemplate.getFieldName();
        String fieldLabel = fieldTemplate.getLabel(language);
        String fieldType = fieldTemplate.getTypeName();
        String fieldDisplayerName = fieldTemplate.getDisplayerName();
        isMandatory = fieldTemplate.isMandatory();
        boolean isDisabled = fieldTemplate.isDisabled();
        boolean isReadOnly = fieldTemplate.isReadOnly();
        boolean isHidden = fieldTemplate.isHidden();
        String fieldClass = "";
        if (parameters.containsKey("classLabel")) {
          fieldClass = parameters.get("classLabel");
        }

        Field field = null;
        if (record != null) {
          try {
            field = record.getField(fieldName);
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
          }
        }

        if (record == null || (record != null && field != null)) {
          FieldDisplayer fieldDisplayer = null;
          try {
            if (!StringUtil.isDefined(fieldDisplayerName)) {
              fieldDisplayerName = getTypeManager().getDisplayerName(fieldType);
            }

            fieldDisplayer = getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_DISPLAYER", null, fe);
          }

          if (fieldDisplayer != null) {
            String aClass = "class=\"txtlibform\"";
            if (StringUtil.isDefined(fieldClass)) {
              aClass = "class=\"txtlibform " + fieldClass + "\"";
            }

            out.println("<li class=\"field field_" + fieldName + "\" id=\"form-row-" + fieldName +
                "\">");
            out.println("<label for=\"" + fieldName + "\" " + aClass + ">" + fieldLabel +
                "</label>");
            out.println("<div class=\"fieldInput\">");
            if (!fieldTemplate.isRepeatable()) {
              field = getSureField(fieldTemplate, record, 0);
              try {
                fieldDisplayer.display(out, field, fieldTemplate, pc);
              } catch (FormException fe) {
                SilverTrace.error("form", "XmlForm.display", "form.EX_CANT_GET_FORM", null, fe);
              }
            } else {
              String currentVisibility = AbstractForm.REPEATED_FIELD_CSS_SHOW;
              int maxOccurrences = fieldTemplate.getMaximumNumberOfOccurrences();
              Field lastNotEmptyField = getLastNotEmptyField(record, fieldName, maxOccurrences);
              out.println("<ul class=\"repeatable-field-list\">");
              for (int occ = 0; occ < maxOccurrences; occ++) {
                field = getSureField(fieldTemplate, record, occ);
                if (occ > 0) {
                  ((GenericFieldTemplate) fieldTemplate).setMandatory(false);
                  if (field.isNull()) {
                    currentVisibility = AbstractForm.REPEATED_FIELD_CSS_HIDE;
                  }
                }
                out.println("<li class=\"" + currentVisibility + " repeatable-field-list-element"+occ+"\">");
                try {
                  fieldDisplayer.display(out, field, fieldTemplate, pc);
                } catch (FormException fe) {
                  SilverTrace.error("form", "XmlForm.display", "form.EX_CANT_GET_FORM", null, fe);
                }
                out.println("</li>");
              }
              out.println("</ul>");
              if (!"simpletext".equals(fieldTemplate.getDisplayerName()) &&
                  !fieldTemplate.isReadOnly() && fieldTemplate.isRepeatable()) {
                if (lastNotEmptyField == null ||
                    (lastNotEmptyField != null && lastNotEmptyField.getOccurrence() < maxOccurrences - 1)) {
                  Util.printOneMoreInputSnippet(fieldName, pc, out);
                }
              }
            }
            if (pageContext.isDesignMode()) {
              out.println("<span class=\"actions\">");
              out.println("<a title=\"" + Util.getString("GML.modify", language) +
                    "\" href=\"#\" onclick=\"editField('" + fieldName + "','" +
                  fieldDisplayerName +
                    "');return false;\"><img alt=\"" + Util.getString("GML.modify", language) +
                    "\" src=\"/silverpeas/util/icons/update.gif\"/></a>");
              out.println("<a title=\"" + Util.getString("GML.delete", language) +
                    "\" href=\"#\" onclick=\"deleteField('" + fieldName +
                  "');return false;\"><img alt=\"" +
                    Util.getString("GML.delete", language) +
                    "\" src=\"/silverpeas/util/icons/delete.gif\"/></a>");
              out.println("</span>");
            }
            out.println("</div>");
            out.println("</li>");
          }

          if (isMandatory && !isDisabled && !isHidden
                && fieldDisplayer.isDisplayedMandatory()
                && (!isReadOnly || JdbcRefField.TYPE.equals(fieldType))) {
            mandatory = true;
          }
          out.flush();
          pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc));
        }
      }
    }
    out.println("</ul>");
    if (mandatory) {
      out.println("<div class=\"legend\">");
      out.println(Util.getMandatorySnippet() + "&nbsp;:&nbsp;" +
          Util.getString("GML.requiredField", language));
      out.println("</div>");
    }
    out.println("</div>");
    out.flush();
  }
  
  @Override
  public void display(JspWriter jw, PagesContext pageContext, DataRecord record) {
    PrintWriter out = new PrintWriter(jw, true);
    display(out, pageContext, record);
  }
  
  private int getLastFieldIndex(PagesContext pc, DataRecord record, List<FieldTemplate> listFields) {
    int lastFieldIndex = -1;
    lastFieldIndex += Integer.parseInt(pc.getCurrentFieldIndex());
    for (FieldTemplate fieldTemplate : listFields) {
      if (fieldTemplate != null) {
        String fieldName = fieldTemplate.getFieldName();
        String fieldType = fieldTemplate.getTypeName();
        String fieldDisplayerName = fieldTemplate.getDisplayerName();

        Field field = null;
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

            FieldDisplayer fieldDisplayer =
                getTypeManager().getDisplayer(fieldType, fieldDisplayerName);
            if (fieldDisplayer != null) {
              lastFieldIndex += fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc);
            }
          } catch (FormException fe) {
            SilverTrace.error("form", "XmlForm.getLastFieldIndex", "form.EXP_UNKNOWN_DISPLAYER",
                null, fe);
          }
        }
      }
    }
    return lastFieldIndex;
  }
  
  private Field getLastNotEmptyField(DataRecord record, String fieldName, int nbOccurrences) {
    Field lastNotEmptyField = null;
    for (int occ=0; occ<nbOccurrences; occ++) {
      Field field = record.getField(fieldName, occ);
      if (field != null && !field.isNull()) {
        lastNotEmptyField = field;
      }
    }
    return lastNotEmptyField;
  }

  private TypeManager getTypeManager() {
    return TypeManager.getInstance();
  }
}