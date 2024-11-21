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

import com.novell.ldap.LDAPConnection;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.LdapField;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A LDAPFieldDisplayer is an object which can display a listbox in HTML the content of a listbox to
 * a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class LdapFieldDisplayer extends AbstractFieldDisplayer<LdapField> {

  private static final String[] MANAGED_TYPES = new String[]{LdapField.TYPE};
  private static final String MANDATORY_IMG = Util.getIcon("mandatoryField");
  private static final String THIS_O_AUTO_COMP = " this.oAutoComp";


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
  public void display(PrintWriter out, LdapField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    String value = "";

    String currentUserId = pagesContext.getUserId();
    String language = pagesContext.getLanguage();

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);
    LdapField ldapField;

    if (!field.getTypeName().equals(LdapField.TYPE)) {

      ldapField = new LdapField();
    } else {
      ldapField = field;
    }

    if (!field.isNull()) {
      value = field.getValue(language);
    }
    Collection<String> listRes;

    // Parameters
    Params params = getParams(parameters);
    listRes = requestLDAPService(ldapField, params, currentUserId);
    StringBuilder html = new StringBuilder(10000);
    if (listRes != null && !listRes.isEmpty()) {
      generateHTMLFields(template, pagesContext, html, fieldName, value, listRes, params);
    } else {
      generateEmptyHTMLFields(template, params, html, fieldName);
    }
    out.println(html);
  }

  private static void generateEmptyHTMLFields(FieldTemplate template, Params params, StringBuilder html, String fieldName) {
    if ("1".equals(params.valueFieldType)) {// valeurs possibles 1 = choix restreint à la liste
      // ou 2 =
      // saisie libre, par défaut 1
      html.append("<select name=\"").append(fieldName).append("\"");
      if (template.isDisabled() || template.isReadOnly()) {
        html.append(" disabled=\"disabled\"");
      }
      html.append(" >\n");
      html.append("</select>\n");

      if ((template.isMandatory()) && (!template.isDisabled()) && (!template.isReadOnly())
          && (!template.isHidden())) {
        html.append("&nbsp;<img src=\"").append(MANDATORY_IMG).append(
            "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\"/>&nbsp;\n");
      }
    } else {
      html.append("<input type=\"text\" name=\"").append(fieldName).append("\"");
      if (template.isDisabled() || template.isReadOnly()) {
        html.append(" disabled=\"disabled\"");
      }
      html.append(" />\n");
      if ((template.isMandatory()) && (!template.isDisabled()) && (!template.isReadOnly())
          && (!template.isHidden())) {
        html.append("&nbsp;<img src=\"").append(MANDATORY_IMG);
        html.append("\" width=\"5\" height=\"5\" border=\"0\" alt=\"\"/>&nbsp;\n");
      }
    }
  }

  private static void generateHTMLFields(FieldTemplate template, PagesContext pagesContext, StringBuilder html, String fieldName, String value, Collection<String> listRes, Params params) {
    int zindex = 100;
    html.append("<style type=\"text/css\">\n").append(" #listAutocomplete").append(fieldName);
    html.append(" {\n ").append("  width:15em;\n").append("  padding-bottom:2em;\n");
    html.append(" }\n").append(" #container").append(fieldName).append(" {\n");
    html.append("  z-index:").append(zindex);
    html
        .append("; /* z-index needed on top instance for ie & sf absolute inside relative issue" +
            " ");
    html.append("*/\n").append(" }\n").append(" #").append(fieldName).append(" {\n");
    html.append("  _position:absolute; /* abs pos needed for ie quirks */\n").append(" }\n");
    html.append("</style>\n").append("<div id=\"listAutocomplete").append(fieldName).append(
        "\">\n");
    html.append("<input id=\"").append(fieldName).append("\" name=\"").append(fieldName);
    html.append("\" type=\"text\"");
    if (value != null) {
      html.append(" value=\"").append(value).append("\"");
    }
    if (template.isDisabled() || template.isReadOnly()) {
      html.append(" disabled");
    }
    html.append("/>\n").append("<div id=\"container").append(fieldName).append("\"/>\n");
    html.append("</div>\n");

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && pagesContext.useMandatory()) {
      html.append("<img src=\"").append(MANDATORY_IMG).append("\" width=\"5\" height=\"5\" ");
      html.append("border=\"0\" alt=\"\" style=\"position:absolute;left:16em;top:5px\"/>\n");
    }

    JdbcFieldDisplayer.generateFragmentHTML(listRes, html, fieldName);
    html.append(" this.oACDS").append(fieldName);
    html.append(" = new YAHOO.util.LocalDataSource(listArray").append(fieldName).append(");\n");
    html.append(THIS_O_AUTO_COMP).append(fieldName).append(" = new YAHOO.widget.AutoComplete('");
    html.append(fieldName).append("','container").append(fieldName).append("', this.oACDS");
    html.append(fieldName).append(");\n").append(THIS_O_AUTO_COMP).append(fieldName);
    html.append(".prehighlightClassName = \"yui-ac-prehighlight\";\n");
    generateAutoComp(fieldName, params.valueFieldType, html, THIS_O_AUTO_COMP);
    generateYahooCode(fieldName, html);
  }

  private static Collection<String> requestLDAPService(LdapField ldapField, Params params, String currentUserId) throws FormException {
    Collection<String> listRes;
    LDAPConnection ldapConnection = null;
    try {
      ldapConnection = ldapField.connectLdap(params.host, params.port);

      // Bind LDAP
      byte[] tabPassword = null;
      if (params.password != null) {
        tabPassword = params.password.getBytes();
      }
      ldapField.bindLdap(ldapConnection, params.version, params.baseDN, tabPassword);

      // Set max result displayed
      if (params.maxResultDisplayed != null) {
        ldapField.setConstraintLdap(ldapConnection, params.maxResultDisplayed);
      }

      // LDAP request
      boolean boolSearchTypeOnly = "true".equals(params.searchTypeOnly);
      listRes = ldapField.searchLdap(ldapConnection, params.searchBase, params.searchScope,
          params.searchFilter,
          params.searchAttribute, boolSearchTypeOnly, currentUserId);
    } finally {
      ldapField.disconnectLdap(ldapConnection);
    }
    return listRes;
  }

  private static Params getParams(Map<String, String> parameters) {
    String host = null;
    String port = null;
    String version = null;
    String baseDN = null;
    String password = null;
    String searchBase = null;
    String searchScope = null;
    String searchFilter = null;
    String searchAttribute = null;
    String searchTypeOnly = null;
    String maxResultDisplayed = null;
    String valueFieldType = "1"; // valeurs possibles 1 = choix restreint à la liste ou 2 = saisie
    // libre, par défaut 1
    if (parameters.containsKey("host")) {
      host = parameters.get("host");
    }
    if (parameters.containsKey("port")) {
      port = parameters.get("port");
    }
    if (parameters.containsKey("version")) {
      version = parameters.get("version");
    }
    if (parameters.containsKey("baseDN")) {
      baseDN = parameters.get("baseDN");
    }
    if (parameters.containsKey("password")) {
      password = parameters.get("password");
    }
    if (parameters.containsKey("searchBase")) {
      searchBase = parameters.get("searchBase");
    }
    if (parameters.containsKey("searchScope")) {
      searchScope = parameters.get("searchScope");
    }
    if (parameters.containsKey("searchFilter")) {
      searchFilter = parameters.get("searchFilter");
    }
    if (parameters.containsKey("searchAttribute")) {
      searchAttribute = parameters.get("searchAttribute");
    }
    if (parameters.containsKey("searchTypeOnly")) {
      searchTypeOnly = parameters.get("searchTypeOnly");
    }
    if (parameters.containsKey("maxResultDisplayed")) {
      maxResultDisplayed = parameters.get("maxResultDisplayed");
    }
    if (parameters.containsKey("valueFieldType")) {
      valueFieldType = parameters.get("valueFieldType"); // valeurs possibles 1 = choix
      // restreint à la liste ou 2 =
      // saisie libre, par défaut 1
    }
    Params params = new Params();
    params.host = host;
    params.port = port;
    params.version = version;
    params.baseDN = baseDN;
    params.password = password;
    params.searchBase = searchBase;
    params.searchScope = searchScope;
    params.searchFilter = searchFilter;
    params.searchAttribute = searchAttribute;
    params.searchTypeOnly = searchTypeOnly;
    params.maxResultDisplayed = maxResultDisplayed;
    params.valueFieldType = valueFieldType;
    return params;
  }

  private static class Params {
    String host;
    String port;
    String version;
    String baseDN;
    String password;
    String searchBase;
    String searchScope;
    String searchFilter;
    String searchAttribute;
    String searchTypeOnly;
    String maxResultDisplayed;
    String valueFieldType;
  }

  static void generateAutoComp(String fieldName, String valueFieldType, StringBuilder html,
      String thisOAutoComp) {
    html.append(thisOAutoComp).append(fieldName).append(".typeAhead = true;\n");
    html.append(thisOAutoComp).append(fieldName).append(".useShadow = true;\n");
    html.append(thisOAutoComp).append(fieldName).append(".minQueryLength = 0;\n");

    if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint à la liste ou 2 =
      // saisie libre, par défaut 1
      html.append(thisOAutoComp).append(fieldName).append(".forceSelection = true;\n");
    }

    html.append(thisOAutoComp).append(fieldName).append(
        ".textboxFocusEvent.subscribe(function(){\n");
  }

  static void generateYahooCode(String fieldName, StringBuilder html) {
    html.append("  var sInputValue = YAHOO.util.Dom.get('").append(fieldName).append(
        "').value;\n");
    html.append("  if(sInputValue.length == 0) {\n");
    html.append("   var oSelf = this;\n");
    html.append("   setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n");
    html.append("  }\n");
    html.append(" });\n");
    html.append("</script>\n");
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   *
   * @throws FormException if the field type is not a managed type or if the field doesn't accept
   * the new value.
   */
  @Override
  public List<String> update(String newValue, LdapField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {

    if (!LdapField.TYPE.equals(field.getTypeName())) {
      throw new FormException("Incorrect field type '{0}', expected; {0}", LdapField.TYPE);
    }

    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("Incorrect field value type. Expected {0}", LdapField.TYPE);
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
