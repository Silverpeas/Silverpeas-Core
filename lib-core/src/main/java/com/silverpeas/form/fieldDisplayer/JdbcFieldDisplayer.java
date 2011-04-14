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
package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import com.stratelia.webactiv.util.DBUtil;

/**
 * A JdbcFieldDisplayer is an object which can display a listbox in HTML the content of a listbox to
 * a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class JdbcFieldDisplayer extends AbstractFieldDisplayer {

  private final static String[] MANAGED_TYPES = new String[]{JdbcField.TYPE};
  private final static String mandatoryImg = Util.getIcon("mandatoryField");

  /**
   * Constructeur
   */
  public JdbcFieldDisplayer() {
    super();
  }

  /**
   * Returns the name of the managed types.
   * @return 
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
   *
   * @param out
   * @param template
   * @param pagesContext
   * @throws java.io.IOException 
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws java.io.IOException {
    String language = pagesContext.getLanguage();
    if (!JdbcField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "JdbcFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          JdbcField.TYPE);
    }
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' "
          + Util.getString("GML.MustBeFilled", language) + "\\n \";");
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
   *
   * @param out
   * @param field
   * @param template
   * @param pagesContext
   * @throws FormException 
   */
  @Override
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    String currentUserId = pagesContext.getUserId();
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);
    JdbcField jdbcField = null;

    if (!field.getTypeName().equals(JdbcField.TYPE)) {
      SilverTrace.info("form", "JdbcFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          JdbcField.TYPE);
    } else {
      jdbcField = (JdbcField) field;
    }
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
    if (jdbcField != null) {
      Connection jdbcConnection = null;
      try {
        jdbcConnection = jdbcField.connectJdbc(parameters.get("driverName"), parameters.get("url"), parameters.
            get("login"), parameters.get("password"));
        listRes = jdbcField.selectSql(jdbcConnection, parameters.get("query"), currentUserId);
      } finally {
        DBUtil.close(jdbcConnection);
      }
    }
    StringBuilder html = new StringBuilder(10000);
    if (listRes != null && !listRes.isEmpty()) {
      int zindex =
          (pagesContext.getLastFieldIndex() - Integer.parseInt(pagesContext.getCurrentFieldIndex())) * 9000;
      html.append("<style type=\"text/css\">\n").append("	#listAutocomplete").append(fieldName).
          append(" {\n");
      html.append("		width:15em;\n");
      html.append("		padding-bottom:2em;\n");
      html.append("	}\n");
      html.append("	#listAutocomplete").append(fieldName).append(" {\n");
      html.append("		z-index:").append(zindex).append(
          "; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n");
      html.append("	}\n");
      html.append("	#").append(fieldName).append(" {\n");
      html.append("		_position:absolute; /* abs pos needed for ie quirks */\n");
      html.append("	}\n");
      html.append("</style>\n");

      html.append("<div id=\"listAutocomplete").append(fieldName).append("\">\n");
      html.append("<input id=\"").append(fieldName).append("\" name=\"").append(fieldName).append(
          "\" type=\"text\"");
      if (value != null) {
        html.append(" value=\"").append(value).append("\"");
      }
      if (template.isDisabled() || template.isReadOnly()) {
        html.append(" disabled");
      }
      html.append("/>\n");
      html.append("<div id=\"container").append(fieldName).append("\"/>\n");
      html.append("</div>\n");

      if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
          && !template.isHidden() && pagesContext.useMandatory()) {
        html.append("<img src=\"").append(mandatoryImg).append(
            "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\" style=\"position:absolute;left:16em;top:5px\"/>\n");
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
      html.append(" this.oACDS").append(fieldName).
          append(" = new YAHOO.util.LocalDataSource(listArray").append(fieldName).append(
          ");\n");
      html.append("	this.oAutoComp").append(fieldName).append(" = new YAHOO.widget.AutoComplete('").
          append(fieldName).append("','container").append(fieldName).append("', this.oACDS").
          append(fieldName).append(
          ");\n");
      html.append("	this.oAutoComp").append(fieldName).append(
          ".prehighlightClassName = \"yui-ac-prehighlight\";\n");
      html.append("	this.oAutoComp").append(fieldName).append(".typeAhead = true;\n");
      html.append("	this.oAutoComp").append(fieldName).append(".useShadow = true;\n");
      html.append("	this.oAutoComp").append(fieldName).append(".minQueryLength = 0;\n");

      if ("1".equals(valueFieldType)) {// valeurs possibles 1 = choix restreint a la liste ou 2 =
        // saisie libre, par defaut 1
        html.append("	this.oAutoComp").append(fieldName).append(".forceSelection = true;\n");
      }

      html.append("	this.oAutoComp").append(fieldName).append(
          ".textboxFocusEvent.subscribe(function(){\n");
      html.append("		var sInputValue = YAHOO.util.Dom.get('").append(fieldName).append("').value;\n");
      html.append("		if(sInputValue.length == 0) {\n");
      html.append("			var oSelf = this;\n");
      html.append("			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n");
      html.append("		}\n");
      html.append("	});\n");
      html.append("</script>\n");

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

  

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.  
   * @param newValue
   * @param field
   * @param template
   * @param pagesContext
   * @return
   * @throws FormException if the field type is not a managed type or if the field doesn't accept the new value.
   */
  public List<String> update(String newValue, Field field, FieldTemplate template,
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
    return new ArrayList<String>();
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
