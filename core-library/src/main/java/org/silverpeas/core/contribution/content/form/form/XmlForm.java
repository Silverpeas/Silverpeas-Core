/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.form;

import org.silverpeas.core.contribution.content.form.AbstractForm;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.jsp.JspWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnRendering;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * A Form is an object which can display in HTML the content of a DataRecord to a end user and can
 * retrieve via HTTP any updated values.
 *
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public class XmlForm extends AbstractForm {

  public XmlForm(RecordTemplate template) throws FormException {
    super(template);
  }

  public XmlForm(RecordTemplate template, boolean viewForm) throws FormException {
    super(template);
    setViewForm(viewForm);
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
  public String toString(PagesContext pagesContext, DataRecord record) {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    display(pw, pagesContext, record);
    return sw.toString();
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
  private void display(PrintWriter out, PagesContext pageContext, DataRecord record) {

    String language = pageContext.getLanguage();

    // content language is the one of the record
    pageContext.setContentLanguage(record.getLanguage());

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
            SilverLogger.getLogger(this).error(unknown("fieldName", fieldName), fe);
          }
        }

        boolean displayField = true;
        if (isViewForm() && !Util.isEmptyFieldsDisplayed()) {
          displayField = StringUtil.isDefined(field.getStringValue());
          if (displayField && field.getStringValue().startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
            // special case about WYSIWYG field
            String fileName = field.getStringValue().substring(WysiwygFCKFieldDisplayer.DB_KEY.length());
            displayField = isWYSIWYGFieldDefined(fileName, pageContext);
          }
        }

        if (displayField && record != null && field != null) {
          FieldDisplayer fieldDisplayer = getFieldDisplayer(fieldTemplate);
          if (fieldDisplayer != null) {
            String aClass = "class=\"txtlibform\"";
            if (StringUtil.isDefined(fieldClass)) {
              aClass = "class=\"txtlibform " + fieldClass + "\"";
            }

            String technicalNameHelp = "";
            if (pageContext.isDesignMode()) {
              technicalNameHelp = " title=\"" + fieldName + "\"";
            }

            out.println("<li class=\"field field_" + fieldName + "\" id=\"form-row-" + fieldName
                + "\">");
            out.println("<label for=\"" + fieldName + "\" " + aClass + technicalNameHelp + ">"
                + fieldLabel + "</label>");
            out.println("<div class=\"fieldInput\">");
            if (!fieldTemplate.isRepeatable()) {
              field = getSureField(fieldTemplate, record, 0);
              try {
                fieldDisplayer.display(out, field, fieldTemplate, pc);
              } catch (FormException fe) {
                SilverLogger.getLogger(this)
                    .error(failureOnRendering("field", field.getName()), fe);
              }
            } else {
              boolean isWriting = !"simpletext".equals(fieldTemplate.getDisplayerName())
                  && !fieldTemplate.isReadOnly();
              String currentVisibility = AbstractForm.REPEATED_FIELD_CSS_SHOW;
              int maxOccurrences = fieldTemplate.getMaximumNumberOfOccurrences();
              Field lastNotEmptyField = getLastNotEmptyField(record, fieldName, maxOccurrences);
              out.println("<ul class=\"repeatable-field-list\">");
              for (int occ = 0; occ < maxOccurrences; occ++) {
                field = getSureField(fieldTemplate, record, occ);
                if (occ > 0) {
                  ((GenericFieldTemplate) fieldTemplate).setMandatory(false);
                  if (!isWriting) {
                    currentVisibility = field.isNull() ? AbstractForm.REPEATED_FIELD_CSS_HIDE
                        : AbstractForm.REPEATED_FIELD_CSS_SHOW;
                  } else {
                    currentVisibility = (lastNotEmptyField == null || (occ > lastNotEmptyField.
                        getOccurrence())) ? AbstractForm.REPEATED_FIELD_CSS_HIDE
                        : AbstractForm.REPEATED_FIELD_CSS_SHOW;
                  }
                }
                out.println("<li class=\"" + currentVisibility + " repeatable-field-list-element"
                    + occ + "\">");
                try {
                  fieldDisplayer.display(out, field, fieldTemplate, pc);
                } catch (FormException fe) {
                  SilverLogger.getLogger(this)
                      .error(failureOnRendering("field", field.getName()), fe);
                }
                out.println("</li>");
              }
              out.println("</ul>");
              if (isWriting && (lastNotEmptyField == null || (lastNotEmptyField.getOccurrence()
                  < maxOccurrences - 1))) {
                Util.printOneMoreInputSnippet(fieldName, pc, out);
              }
            }
            if (pageContext.isDesignMode()) {
              out.println("<span class=\"actions\">");
              out.println("<a title=\"" + Util.getString("GML.modify", language)
                  + "\" href=\"#\" onclick=\"editField('" + fieldName + "','" + fieldDisplayerName
                  + "');return false;\"><img alt=\"" + Util.getString("GML.modify", language)
                  + "\" src=\"/silverpeas/util/icons/update.gif\"/></a>");
              out.println("<a title=\"" + Util.getString("GML.delete", language)
                  + "\" href=\"#\" onclick=\"deleteField('" + fieldName
                  + "');return false;\"><img alt=\"" + Util.getString("GML.delete", language)
                  + "\" src=\"/silverpeas/util/icons/delete.gif\"/></a>");
              out.println("</span>");
            }
            out.println("</div>");
            out.println("</li>");
          }

          if (isMandatory && !isDisabled && !isHidden && fieldDisplayer.isDisplayedMandatory() &&
              !isReadOnly) {
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
      out.println(Util.getMandatorySnippet() + "&nbsp;:&nbsp;" + Util.getString("GML.requiredField",
          language));
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
        Field field = null;
        if (record != null) {
          try {
            field = record.getField(fieldName);
          } catch (FormException fe) {
            SilverLogger.getLogger(this).error(unknown("fieldName", fieldName), fe);
          }
        }

        if (record == null || field != null) {
          FieldDisplayer fieldDisplayer = getFieldDisplayer(fieldTemplate);
          if (fieldDisplayer != null) {
            lastFieldIndex += fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc);
          }
        }
      }
    }
    return lastFieldIndex;
  }

  private Field getLastNotEmptyField(DataRecord record, String fieldName, int nbOccurrences) {
    Field lastNotEmptyField = null;
    for (int occ = 0; occ < nbOccurrences; occ++) {
      Field field = record.getField(fieldName, occ);
      if (field != null && !field.isNull()) {
        lastNotEmptyField = field;
      }
    }
    return lastNotEmptyField;
  }

  private boolean isWYSIWYGFieldDefined(String fileName, PagesContext pc) {
    String content = WysiwygFCKFieldDisplayer.getContentFromFile(pc.getComponentId(), fileName);
    return StringUtil.isDefined(content);
  }
}