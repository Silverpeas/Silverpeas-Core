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
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.util.StringUtil;

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

  private static final String[] MANAGED_TYPES = new String[]{JdbcField.TYPE};
  private static final String MANDATORY_IMG = Util.getIcon("mandatoryField");
  private static final String THIS_O_AUTO_COMP = " this.oAutoComp";

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

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    produceMandatoryCheck(out, template, pagesContext);
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

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
    Collection<String> listRes = getResponses(currentUserId, field, parameters);
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
      generateDefaultView(html, template, fieldName, valueFieldType);

    }
    out.println(html);

  }

  private void generateDefaultView(final StringBuilder html, final FieldTemplate template,
      final String fieldName, final String valueFieldType) {
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
      html.append("&nbsp;<img src=\"").append(MANDATORY_IMG).append(
          "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\"/>&nbsp;\n");
    }
  }

  private Collection<String> getResponses(final String currentUserId, final JdbcField field,
      final Map<String, String> parameters) throws FormException {
    Collection<String> listRes;
    Connection jdbcConnection = null;
    try {
      jdbcConnection =
          field.connect(parameters.get("dataSourceName"), parameters.get("login"),
              parameters.get("password"));
      listRes = field.selectSql(jdbcConnection, parameters.get("query"), currentUserId);
    } finally {
      DBUtil.close(jdbcConnection);
    }
    return listRes;
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
          .append("<img src=\"").append(MANDATORY_IMG)
          .append(
              "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\" style=\"position:absolute;" +
                  "left:16em;top:5px\"/>\n");
    }

    generateFragmentHTML(entries, html, fieldName);
    html.append(" this.oACDS").append(fieldName).
        append(" = new YAHOO.util.LocalDataSource(listArray").append(fieldName).append(");\n");
    html.append(THIS_O_AUTO_COMP).append(fieldName).append(" = new YAHOO.widget.AutoComplete('")
        .
        append(fieldName).append("','container").append(fieldName).append("', this.oACDS").
        append(fieldName).append(");\n");
    html.append(THIS_O_AUTO_COMP).append(fieldName).append(
        ".prehighlightClassName = \"yui-ac-prehighlight\";\n");
    LdapFieldDisplayer.generateAutoComp(fieldName, valueFieldType, html, THIS_O_AUTO_COMP);
    LdapFieldDisplayer.generateYahooCode(fieldName, html);
  }

  static void generateFragmentHTML(Collection<String> entries, StringBuilder html, String fieldName) {
    html.append("<script type=\"text/javascript\">\n");
    html.append("listArray").append(fieldName).append(" = [\n");

    Iterator<String> itRes = entries.iterator();
    while (itRes.hasNext()) {
      html.append("\"").
          append(WebEncodeHelper.javaStringToJsString(itRes.next())).append("\"");
      if (itRes.hasNext()) {
        html.append(",\n");
      }
    }

    html.append("];\n");
    html.append("</script>\n");

    html.append("<script type=\"text/javascript\">\n");
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
      throw new FormException("Incorrect field type '{0}', expected; {0}", JdbcField.TYPE);
    }

    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("Incorrect field value type. Expected {0}", JdbcField.TYPE);
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
