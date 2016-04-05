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

import com.novell.ldap.LDAPConnection;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.LdapField;
import org.silverpeas.core.util.EncodeHelper;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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

  private final static String[] MANAGED_TYPES = new String[]{LdapField.TYPE};
  private final static String mandatoryImg = Util.getIcon("mandatoryField");

  /**
   * Constructeur
   */
  public LdapFieldDisplayer() {
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

    if (!template.getTypeName().equals(LdapField.TYPE)) {

    }

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
    Collection<String> listRes = null;

    // Parameters
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

    if (ldapField != null) {
      LDAPConnection ldapConnection = null;
      try {
        ldapConnection = ldapField.connectLdap(host, port);

        // Bind LDAP
        byte[] tabPassword = null;
        if (password != null) {
          tabPassword = password.getBytes();
        }
        ldapField.bindLdap(ldapConnection, version, baseDN, tabPassword);

        // Set max result displayed
        if (maxResultDisplayed != null) {
          ldapField.setConstraintLdap(ldapConnection, maxResultDisplayed);
        }

        // Requête LDAP
        boolean boolSearchTypeOnly = "true".equals(searchTypeOnly);
        listRes = ldapField.searchLdap(ldapConnection, searchBase, searchScope, searchFilter,
            searchAttribute, boolSearchTypeOnly, currentUserId);
      } finally {
        ldapField.disconnectLdap(ldapConnection);
      }
    }
    StringBuilder html = new StringBuilder(10000);
    if (listRes != null && !listRes.isEmpty()) {
      int zindex = 100;
      html.append("<style type=\"text/css\">\n").append("	#listAutocomplete").append(fieldName);
      html.append(" {\n").append("		width:15em;\n").append("		padding-bottom:2em;\n");
      html.append("	}\n").append("	#container").append(fieldName).append(" {\n");
      html.append("		z-index:").append(zindex);
      html
          .append("; /* z-index needed on top instance for ie & sf absolute inside relative issue ");
      html.append("*/\n").append("	}\n").append("	#").append(fieldName).append(" {\n");
      html.append("		_position:absolute; /* abs pos needed for ie quirks */\n").append("	}\n");
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
        html.append("<img src=\"").append(mandatoryImg).append("\" width=\"5\" height=\"5\" ");
        html.append("border=\"0\" alt=\"\" style=\"position:absolute;left:16em;top:5px\"/>\n");
      }

      html.append("<script type=\"text/javascript\">\n");
      html.append("listArray").append(fieldName).append(" = [\n");

      Iterator<String> itRes = listRes.iterator();
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
      html.append(" this.oACDS").append(fieldName);
      html.append(" = new YAHOO.util.LocalDataSource(listArray").append(fieldName).append(");\n");
      html.append("	this.oAutoComp").append(fieldName).append(" = new YAHOO.widget.AutoComplete('");
      html.append(fieldName).append("','container").append(fieldName).append("', this.oACDS");
      html.append(fieldName).append(");\n").append("	this.oAutoComp").append(fieldName);
      html.append(".prehighlightClassName = \"yui-ac-prehighlight\");\n");
      html.append("	this.oAutoComp").append(fieldName).append(".typeAhead = true;\n");
      html.append("	this.oAutoComp").append(fieldName).append(".useShadow = true;\n");
      html.append("	this.oAutoComp").append(fieldName).append(".minQueryLength = 0;\n");

      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint à la liste ou 2 =
        // saisie libre, par défaut 1
        html.append("	this.oAutoComp").append(fieldName).append(".forceSelection = true;\n");
      }

      html.append("	this.oAutoComp").append(fieldName).append(
          ".textboxFocusEvent.subscribe(function(){\n");
      html.append("		var sInputValue = YAHOO.util.Dom.get('").append(fieldName).append(
          "').value;\n");
      html.append("		if(sInputValue.length == 0) {\n");
      html.append("			var oSelf = this;\n");
      html.append("			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n");
      html.append("		}\n");
      html.append("	});\n");
      html.append("</script>\n");

    } else {

      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint à la liste ou 2 =
        // saisie libre, par défaut 1
        html.append("<select name=\"").append(fieldName).append("\"");
        if (template.isDisabled() || template.isReadOnly()) {
          html.append(" disabled=\"disabled\"");
        }
        html.append(" >\n");
        html.append("</select>\n");

        if ((template.isMandatory()) && (!template.isDisabled()) && (!template.isReadOnly())
            && (!template.isHidden())) {
          html.append("&nbsp;<img src=\"").append(mandatoryImg).append(
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
          html.append("&nbsp;<img src=\"").append(mandatoryImg);
          html.append("\" width=\"5\" height=\"5\" border=\"0\" alt=\"\"/>&nbsp;\n");
        }
      }
    }

    out.println(html.toString());
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
      throw new FormException("LdapFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          LdapField.TYPE);
    }

    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("LdapFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          LdapField.TYPE);
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
