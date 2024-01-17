/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.form;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.contribution.content.form.AbstractForm;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.contribution.content.form.record.ParameterValue;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.jsp.JspWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A Form is an object which can display in HTML the content of a DataRecord to a end user and can
 * retrieve via HTTP any updated values.
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public class XmlSearchForm extends AbstractForm {

  private static final String DIV_TAG_END = "</div>";
  public static final String EXTRA_FIELD_PERIOD = "extraSearchFieldUpdatedFor";
  public static final String EXTRA_FIELD_SPACE = "extraSearchFieldSpace";

  public XmlSearchForm(RecordTemplate template) throws FormException {
    super(template);
  }

  /**
   * Prints the javascripts which will be used to control the new values given to the data record
   * fields. The error messages may be adapted to a local language. The RecordTemplate gives the
   * field type and constraints. The RecordTemplate gives the local label too. Never throws an
   * Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>a field is unknown by the template.</li>
   * <li>a field has not the required type.</li>
   * </ul>
   */
  @Override
  public void displayScripts(JspWriter jw, PagesContext pagesContext) {
    PrintWriter out = new PrintWriter(jw, true);
    out.append(getJavascriptSnippet());
    out.flush();
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

  @Override
  public void display(JspWriter jw, PagesContext pageContext, DataRecord record) {
    PrintWriter out = new PrintWriter(jw, true);
    display(out, pageContext, record);
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
  private void display(PrintWriter jw, PagesContext pagesContext, DataRecord record) {
    String language = pagesContext.getLanguage();
    PrintWriter out = new PrintWriter(jw, true);

    out.println("<div class=\"forms " + getFormName() + " mode-search\">");

    if (pagesContext.getPrintTitle() && StringUtil.isDefined(getTitle())) {
      out.println("<h2 class=\"form-title\">");
      out.println(getTitle());
      out.println("</h2>");
    }

    List<FieldTemplate> listFields = new ArrayList<>(getFieldTemplates());

    if (CollectionUtil.isNotEmpty(listFields)) {
      if (pagesContext.isBorderPrinted()) {
        out.println("<ul class=\"fields form-border\">");
      } else {
        out.println("<ul class=\"fields\">");
      }

      out.flush();
      PagesContext pc = new PagesContext(pagesContext);
      pc.setUseMandatory(false);
      pc.setIgnoreDefaultValues(true);

      //adding extra fields
      if (pagesContext.isExtraSearchFieldPeriod()) {
        GenericFieldTemplate fieldDate =
            generateListboxFieldFromProperties(EXTRA_FIELD_PERIOD, "form.search.field.period",
                pagesContext);
        listFields.add(fieldDate);
      }
      if (pagesContext.isExtraSearchFieldSpace()) {
        GenericFieldTemplate fieldSpace =
            generateListboxFieldFromMainSpaces(EXTRA_FIELD_SPACE, "form.search.field.space",
                pagesContext);
        listFields.add(fieldSpace);
      }

      displayFields(out, record, listFields, language, pc);

      out.println("</ul>");
      out.println(DIV_TAG_END);

      out.flush();
    }
  }

  private void displayFields(final PrintWriter out, final DataRecord record,
      final List<FieldTemplate> listFields, final String language, final PagesContext pc) {
    for (FieldTemplate fieldTemplate : listFields) {
      String fieldName = fieldTemplate.getFieldName();
      String fieldLabel = fieldTemplate.getLabel(language);

      FieldDisplayer fieldDisplayer = getFieldDisplayer(fieldTemplate);

      if (fieldDisplayer != null) {

        boolean checkbox = "checkbox".equalsIgnoreCase(fieldTemplate.getDisplayerName());

        out.println("<li class=\"field field_" + fieldName + "\" id=\"form-row-" + fieldName
            + "\">");
        out.println("<div>");
        out.println("<label for=\"" + fieldName + "\">" + fieldLabel + "</label>");
        if(checkbox) {
          out.println(getOperatorsSnippet(fieldName, pc));
        }
        out.println(DIV_TAG_END);
        out.println("<div class=\"fieldInput\">");

        try {
          fieldDisplayer.display(out, record.getField(fieldName), fieldTemplate, pc);
        } catch (FormException fe) {
          SilverLogger.getLogger(this).error(fe.getMessage(), fe);
        }
        out.println(DIV_TAG_END);
        out.println("</li>");
        out.flush();
      }
    }
  }

  private String getOperatorsSnippet(String fieldName, PagesContext pc) {
    StringBuilder sb = new StringBuilder();
    String classAND = "";
    String classOR = "";
    String currentOperator = "";
    String searchOperator = pc.getSearchOperator(fieldName, Util.getDefaultOperator());
    if (searchOperator.equals(PagesContext.OPERATOR_AND)) {
      classAND = "active";
      currentOperator = PagesContext.OPERATOR_AND;
    } else if (searchOperator.equals(PagesContext.OPERATOR_OR)) {
      classOR = "active";
      currentOperator = PagesContext.OPERATOR_OR;
    }
    sb.append("<div class=\"operators\">");
    if (Util.isOperatorsChoiceEnabled()) {
      sb.append("<a href=\"#\" id=\"").append(fieldName).append("OperatorAND\" onclick=\"javascript:$('#").append(fieldName)
          .append("Operator').val('").append(PagesContext.OPERATOR_AND).append("');$(this).attr('class','active');$('#").append(fieldName)
          .append("OperatorOR').attr('class','');return false;\" class=\"").append(classAND)
          .append("\"/>").append(Util.getString("Operator.AND", pc.getLanguage())).append("</a>");
      sb.append(" / ");
      sb.append("<a href=\"#\" id=\"").append(fieldName).append("OperatorOR\" onclick=\"javascript:$('#").append(fieldName)
          .append("Operator').val('").append(PagesContext.OPERATOR_OR).append("');$(this).attr('class','active');$('#").append(fieldName)
          .append("OperatorAND').attr('class','');return false;\" class=\"").append(classOR)
          .append("\"/>").append(Util.getString("Operator.OR", pc.getLanguage())).append("</a>");
    }
    sb.append("<input type=\"hidden\" name=\"").append(fieldName).append("Operator\" id=\"")
        .append(fieldName).append("Operator\" value=\"").append(currentOperator).append("\"/>");
    sb.append(DIV_TAG_END);
    return sb.toString();
  }

  private GenericFieldTemplate generateListboxFieldFromMainSpaces(String fieldName, String prefix,
      final PagesContext pc) {

    String label = Util.getString(prefix + ".label", pc.getLanguage());

    String[] mainSpaceIds = OrganizationController.get().getAllRootSpaceIds(pc.getUserId());
    StringBuilder keys = new StringBuilder();
    StringBuilder values = new StringBuilder();
    for (String spaceId : mainSpaceIds) {
      SpaceInstLight space = OrganizationController.get().getSpaceInstLightById(spaceId);
      if (keys.length() > 0) {
        keys.append("##");
        values.append("##");
      }
      keys.append(space.getId());
      values.append(space.getName(pc.getLanguage()));
    }

    return generateListboxField(fieldName, label, keys.toString(), values.toString());
  }

  private GenericFieldTemplate generateListboxFieldFromProperties(String fieldName, String prefix,
      final PagesContext pc) {

    String label = Util.getString(prefix + ".label", pc.getLanguage());
    String keys = Util.getSetting(prefix + ".keys");
    String values = Util.getString(prefix + ".values", pc.getLanguage());

    return generateListboxField(fieldName, label, keys, values);
  }

  private GenericFieldTemplate generateListboxField(String fieldName, String label, String keys,
      String values) {
    GenericFieldTemplate fieldDate = new GenericFieldTemplate();
    fieldDate.setFieldName(fieldName);
    try {
      fieldDate.setTypeName("text");
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    fieldDate.setDisplayerName("listbox");
    fieldDate.setSearchable(true);
    fieldDate.setLabel(label);
    List<Parameter> parameters = new ArrayList<>();

    Parameter paramKeys = new Parameter();
    paramKeys.setName("keys");
    List<ParameterValue> parameterValues = new ArrayList<>();
    ParameterValue paramValue = new ParameterValue();
    paramValue.setLang("fr");
    paramValue.setValue(keys);
    parameterValues.add(paramValue);
    paramKeys.setParameterValuesObj(parameterValues);

    Parameter paramValues = new Parameter();
    paramValues.setName("values");
    parameterValues = new ArrayList<>();
    paramValue = new ParameterValue();
    paramValue.setLang("fr");
    paramValue.setValue(values);
    parameterValues.add(paramValue);
    paramValues.setParameterValuesObj(parameterValues);

    parameters.add(paramKeys);
    parameters.add(paramValues);

    fieldDate.setParametersObj(parameters);

    return fieldDate;
  }


}
