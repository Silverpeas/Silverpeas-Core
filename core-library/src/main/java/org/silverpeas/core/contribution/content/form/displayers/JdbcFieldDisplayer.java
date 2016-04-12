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
package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.JdbcField;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A JdbcFieldDisplayer is an object which can display a listbox in HTML the content of a listbox to
 * a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class JdbcFieldDisplayer extends AbstractFieldDisplayer<JdbcField> {

  private final static String[] MANAGED_TYPES = new String[]{JdbcField.TYPE};
  private final static String mandatoryImg = Util.getIcon("mandatoryField");

  /**
   * Constructor
   */
  public JdbcFieldDisplayer() {
    super();
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return MANAGED_TYPES;
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the fieldName is unknown by the template.</li>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws java.io.IOException
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws java.io.IOException {
    String language = pagesContext.getLanguage();
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' "
          + Util.getString("GML.MustBeFilled", language) + "\\n\";");
      out.println("		errorNb++;");
      out.println("	}");
    }
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, JdbcField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    String currentUserId = pagesContext.getUserId();
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);
    String value = "";
    if (!field.isNull()) {
      value = field.getValue(language);
    }
    // valeurs possibles 1 = choix restreint a la liste ou 2 = saisie libre
    String valueFieldType = "1";
    if (parameters.containsKey("valueFieldType")) {
      valueFieldType = parameters.get("valueFieldType");
    }
    Collection<String> listRes = null;
    Connection jdbcConnection = null;
    try {
      jdbcConnection =
          field.connect(parameters.get("dataSourceName"), parameters.get("login"),
              parameters.get("password"));
      listRes = field.selectSql(jdbcConnection, parameters.get("query"), currentUserId);
    } finally {
      DBUtil.close(jdbcConnection);
    }
    StringBuilder html = new StringBuilder(10000);
    if (listRes != null && !listRes.isEmpty()) {
      String displayer = parameters.get("displayer");
      if (!StringUtil.isDefined(displayer)) {
        displayer = "autocomplete";
      }
      if ("autocomplete".equals(displayer)) {
        getAutocompleteFragment(template, value, valueFieldType, listRes, pagesContext, html);
      } else {
        getListboxFragment(template, value, listRes, pagesContext, html);
      }
    } else {
      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint a la liste ou 2 =
        // saisie libre, par defaut 1

        html.append("<select name=\"").append(fieldName).append("\"");

        if (template.isDisabled() || template.isReadOnly()) {
          html.append(" disabled");
        }
        html.append(" >\n");
        html.append("</select>\n");

      } else {
        html.append("<input type=\"text\" name=\"").append(fieldName).append("\"");

        if (template.isDisabled() || template.isReadOnly()) {
          html.append(" disabled=\"disabled\"");
        }
        html.append(" />\n");
      }
      if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
          && !template.isHidden()) {
        html.append("&nbsp;<img src=\"").append(mandatoryImg).append(
            "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\"/>&nbsp;\n");
      }

    }
    out.println(html.toString());

  }

  private void getAutocompleteFragment(FieldTemplate template, String fieldValue,
      String valueFieldType, Collection<String> entries, PagesContext pageContext,
      StringBuilder html) {
    String fieldName = template.getFieldName();
    int zindex = 100;
    html.append("<style type=\"text/css\">\n").append(" #listAutocomplete").append(fieldName).
        append(" {\n");
    html.append("  width:15em;\n");
    html.append("  padding-bottom:2em;\n");
    html.append(" }\n");
    html.append(" #container").append(fieldName).append(" {\n");
    html.append("  z-index:").append(zindex).append(
        "; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n");
    html.append(" }\n");
    html.append(" #").append(fieldName).append(" {\n");
    html.append("  _position:absolute; /* abs pos needed for ie quirks */\n");
    html.append(" }\n");
    html.append("</style>\n");

    html.append("<div id=\"listAutocomplete").append(fieldName).append("\">\n");
    html.append("<input id=\"").append(fieldName).append("\" name=\"").append(fieldName).append(
        "\" type=\"text\"");
    if (fieldValue != null) {
      html.append(" value=\"").append(fieldValue).append("\"");
    }
    if (template.isDisabled() || template.isReadOnly()) {
      html.append(" disabled");
    }
    html.append("/>\n");
    html.append("<div id=\"container").append(fieldName).append("\"/>\n");
    html.append("</div>\n");

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && pageContext.useMandatory()) {
      html
          .append("<img src=\"").append(mandatoryImg)
          .append(
              "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\" style=\"position:absolute;" +
                  "left:16em;top:5px\"/>\n");
    }

    html.append("<script type=\"text/javascript\">\n");
    html.append("listArray").append(fieldName).append(" = [\n");

    Iterator<String> itRes = entries.iterator();
    while (itRes.hasNext()) {
      html.append("\"").
          append(EncodeHelper.javaStringToJsString(itRes.next())).append("\"");
      if (itRes.hasNext()) {
        html.append(",\n");
      }
    }

    html.append("];\n");
    html.append("</script>\n");

    html.append("<script type=\"text/javascript\">\n");
    html.append(" this.oACDS").append(fieldName).
        append(" = new YAHOO.util.LocalDataSource(listArray").append(fieldName).append(");\n");
    html.append(" this.oAutoComp").append(fieldName).append(" = new YAHOO.widget.AutoComplete('")
        .
        append(fieldName).append("','container").append(fieldName).append("', this.oACDS").
        append(fieldName).append(");\n");
    html.append(" this.oAutoComp").append(fieldName).append(
        ".prehighlightClassName = \"yui-ac-prehighlight\";\n");
    html.append(" this.oAutoComp").append(fieldName).append(".typeAhead = true;\n");
    html.append(" this.oAutoComp").append(fieldName).append(".useShadow = true;\n");
    html.append(" this.oAutoComp").append(fieldName).append(".minQueryLength = 0;\n");

    if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint a la liste ou 2 =
      // saisie libre, par defaut 1
      html.append(" this.oAutoComp").append(fieldName).append(".forceSelection = true;\n");
    }

    html.append(" this.oAutoComp").append(fieldName).append(
        ".textboxFocusEvent.subscribe(function(){\n");
    html.append("  var sInputValue = YAHOO.util.Dom.get('").append(fieldName).append(
        "').value;\n");
    html.append("  if(sInputValue.length == 0) {\n");
    html.append("   var oSelf = this;\n");
    html.append("   setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n");
    html.append("  }\n");
    html.append(" });\n");
    html.append("</script>\n");
  }

  private void getListboxFragment(FieldTemplate template, String fieldValue,
      Collection<String> entries, PagesContext pageContext, StringBuilder html) {
    html.append("<select name=\"").append(template.getFieldName()).append("\"");
    html.append(" id=\"").append(template.getFieldName()).append("\"");
    if (template.isDisabled() || template.isReadOnly()) {
      html.append(" disabled=\"disabled\"");
    }
    html.append(" >\n");
    html.append("<option value=\"\"></option>");
    for (String entry : entries) {
      html.append("<option");
      if (entry.equals(fieldValue)) {
        html.append(" selected=\"selected\"");
      }
      html.append(" value=\"").append(entry).append("\">").append(entry).append("</option>\n");
    }
    html.append("</select>\n");

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && pageContext.useMandatory()) {
      html.append(Util.getMandatorySnippet());
    }
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throws FormException if the field type is not a managed type or if the field doesn't accept
   * the new value.
   */
  @Override
  public List<String> update(String newValue, JdbcField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (!JdbcField.TYPE.equals(field.getTypeName())) {
      throw new FormException("JdbcFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          JdbcField.TYPE);
    }

    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("JdbcFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          JdbcField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }
}
