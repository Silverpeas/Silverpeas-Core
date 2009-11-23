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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.JdbcField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import java.util.ArrayList;
import java.util.List;

/**
 * A JdbcFieldDisplayer is an object which can display a listbox in HTML the content of a listbox to
 * a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class JdbcFieldDisplayer extends AbstractFieldDisplayer {

  /**
   * Constructeur
   */
  public JdbcFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[0];
    s[0] = JdbcField.TYPE;
    return s;
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  public void displayScripts(PrintWriter out,
      FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException {

    String language = PagesContext.getLanguage();

    if (!template.getTypeName().equals(JdbcField.TYPE))
      SilverTrace.info("form", "JdbcFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          JdbcField.TYPE);

    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " +
          Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("		errorNb++;");
      out.println("	}");
    }

    Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  public void display(PrintWriter out,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    String value = "";
    String html = "";
    String currentUserId = PagesContext.getUserId();
    String language = PagesContext.getLanguage();

    String mandatoryImg = Util.getIcon("mandatoryField");

    String fieldName = template.getFieldName();
    Map parameters = template.getParameters(language);
    JdbcField jdbcField = null;

    if (!field.getTypeName().equals(JdbcField.TYPE)) {
      SilverTrace.info("form", "JdbcFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          JdbcField.TYPE);
    } else {
      jdbcField = (JdbcField) field;
    }

    if (!field.isNull()) {
      value = field.getValue(language);
    }
    Collection listRes = null; // liste de valeurs String

    // Parameters
    String driverName = null;
    String url = null;
    String login = null;
    String password = null;
    String query = null;
    String valueFieldType = "1"; // valeurs possibles 1 = choix restreint a la liste ou 2 = saisie
    // libre, par defaut 1
    if (parameters.containsKey("driverName")) {
      driverName = (String) parameters.get("driverName");
    }
    if (parameters.containsKey("url")) {
      url = (String) parameters.get("url");
    }
    if (parameters.containsKey("login")) {
      login = (String) parameters.get("login");
    }
    if (parameters.containsKey("password")) {
      password = (String) parameters.get("password");
    }
    if (parameters.containsKey("query")) {
      query = (String) parameters.get("query");
    }
    if (parameters.containsKey("valueFieldType")) {
      valueFieldType = (String) parameters.get("valueFieldType"); // valeurs possibles 1 = choix
      // restreint a la liste ou 2 =
      // saisie libre, par defaut 1
    }

    if (jdbcField != null) {
      Connection jdbcConnection = null;
      try {
        jdbcConnection = jdbcField.connectJdbc(driverName, url, login, password);
        listRes = jdbcField.selectSql(jdbcConnection, query, currentUserId);
      } finally {
        try {
          if (jdbcConnection != null)
            jdbcConnection.close();
        } catch (SQLException e) {
          SilverTrace.error("formTemplate", "JdbcFieldDisplayer.selectSql",
              "root.EX_CONNECTION_CLOSE_FAILED", e);
        }
      }
    }

    if (listRes != null && listRes.size() > 0) {
      String m_context =
          GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
      int zindex =
          (PagesContext.getLastFieldIndex() - new Integer(PagesContext.getCurrentFieldIndex())
          .intValue()) * 9000;
      html +=
          "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + m_context +
          "/util/yui/fonts/fonts-min.css\" />\n";
      html +=
          "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + m_context +
          "/util/yui/autocomplete/assets/skins/sam/autocomplete.css\" />\n";
      html +=
          "<script type=\"text/javascript\" src=\"" + m_context +
          "/util/yui/yahoo-dom-event/yahoo-dom-event.js\"></script>\n";
      html +=
          "<script type=\"text/javascript\" src=\"" + m_context +
          "/util/yui/animation/animation-min.js\"></script>\n";
      html +=
          "<script type=\"text/javascript\" src=\"" + m_context +
          "/util/yui/autocomplete/autocomplete-min.js\"></script>\n";
      html += "<style type=\"text/css\">\n";
      html += "	#listAutocomplete" + fieldName + " {\n";
      html += "		width:15em;\n";
      html += "		padding-bottom:2em;\n";
      html += "	}\n";
      html += "	#listAutocomplete" + fieldName + " {\n";
      html +=
          "		z-index:" + zindex +
          "; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n";
      html += "	}\n";
      html += "	#" + fieldName + " {\n";
      html += "		_position:absolute; /* abs pos needed for ie quirks */\n";
      html += "	}\n";
      html += "</style>\n";

      html += "<div id=\"listAutocomplete" + fieldName + "\">\n";
      html += "<input id=\"" + fieldName + "\" name=\"" + fieldName + "\" type=\"text\"";
      if (value != null) {
        html += " value=\"" + value + "\"";
      }
      if (template.isDisabled() || template.isReadOnly()) {
        html += " disabled";
      }
      html += "/>\n";
      html += "<div id=\"container" + fieldName + "\"/>\n";
      html += "</div>\n";

      if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
          !template.isHidden() && PagesContext.useMandatory()) {
        html +=
            "<img src=\"" +
                mandatoryImg +
                "\" width=\"5\" height=\"5\" border=\"0\" style=\"position:absolute;left:16em;top:5px\">\n";
      }

      html += "<script type=\"text/javascript\">\n";
      html += "listArray" + fieldName + " = [\n";

      Iterator itRes = listRes.iterator();
      String val;
      while (itRes.hasNext()) {
        val = (String) itRes.next();

        html += "\"" + EncodeHelper.javaStringToJsString(val) + "\",\n";

      }

      // supprime derniere virgule inutile
      html = html.substring(0, html.length() - 1);

      html += "];\n";
      html += "</script>\n";

      html += "<script type=\"text/javascript\">\n";
      html +=
          "	this.oACDS" + fieldName + " = new YAHOO.widget.DS_JSArray(listArray" + fieldName +
          ");\n";
      html +=
          "	this.oAutoComp" + fieldName + " = new YAHOO.widget.AutoComplete('" + fieldName +
          "','container" + fieldName + "', this.oACDS" + fieldName + ");\n";
      html += "	this.oAutoComp" + fieldName + ".prehighlightClassName = \"yui-ac-prehighlight\";\n";
      html += "	this.oAutoComp" + fieldName + ".typeAhead = true;\n";
      html += "	this.oAutoComp" + fieldName + ".useShadow = true;\n";
      html += "	this.oAutoComp" + fieldName + ".minQueryLength = 0;\n";

      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint a la liste ou 2 =
        // saisie libre, par defaut 1
        html += "	this.oAutoComp" + fieldName + ".forceSelection = true;\n";
      }

      html += "	this.oAutoComp" + fieldName + ".textboxFocusEvent.subscribe(function(){\n";
      html += "		var sInputValue = YAHOO.util.Dom.get('" + fieldName + "').value;\n";
      html += "		if(sInputValue.length == 0) {\n";
      html += "			var oSelf = this;\n";
      html += "			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n";
      html += "		}\n";
      html += "	});\n";
      html += "</script>\n";

    } else {

      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint a la liste ou 2 =
        // saisie libre, par defaut 1

        html += "<SELECT name=\"" + fieldName + "\"";

        if (template.isDisabled() || template.isReadOnly()) {
          html += " disabled";
        }
        html += " >\n";
        html += "</SELECT>\n";

      } else {
        html += "<input type=\"text\" name=\"" + fieldName + "\"";

        if (template.isDisabled() || template.isReadOnly()) {
          html += " disabled";
        }
        html += " >\n";
      }
      if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
          !template.isHidden()) {
        html +=
            "&nbsp;<img src=\"" + mandatoryImg +
            "\" width=\"5\" height=\"5\" border=\"0\">&nbsp;\n";
      }
    }

    out.println(html);
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public List<String> update(String newValue,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext)
      throws FormException {

    if (!field.getTypeName().equals(JdbcField.TYPE))
      throw new FormException("JdbcFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          JdbcField.TYPE);

    if (field.acceptValue(newValue, PagesContext.getLanguage()))
      field.setValue(newValue, PagesContext.getLanguage());
    else
      throw new FormException("JdbcFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          JdbcField.TYPE);
    return new ArrayList<String>();
  }

  public boolean isDisplayedMandatory() {
    return true;
  }

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

}
