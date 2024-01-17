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
package org.silverpeas.core.contribution.content.form.field;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A TextFieldImpl stores use a String attribute to store its value.
 */
public class TextFieldImpl extends TextField {

  private static final long serialVersionUID = 1L;
  private String value = "";
  private static final String SUGGESTIONS_QUERY = "select distinct(f.fieldValue)"
      + " from sb_formtemplate_template t, sb_formtemplate_record r, sb_formtemplate_textfield f"
      + " where t.templateId = r.templateId"
      + " and r.recordId = f.recordId"
      + " and f.fieldName = ?"
      + " and t.externalId = ?"
      + " order by f.fieldValue";

  /**
   * Returns the string value of this field.
   */
  @Override
  public String getStringValue() {
    return value;
  }

  /**
   * Set the string value of this field.
   */
  @Override
  public void setStringValue(String value) {
    this.value = value;
  }

  /**
   * Returns true if the value is read only.
   */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  public List<String> getSuggestions(String fieldName, String templateName,
      String componentId) {
    List<String> suggestions = new ArrayList<>();

    PreparedStatement statement = null;
    ResultSet rs = null;
    try(Connection connection = DBUtil.openConnection()) {
      statement = connection.prepareStatement(SUGGESTIONS_QUERY);
      statement.setString(1, fieldName);
      statement.setString(2, componentId + ":" + templateName);

      rs = statement.executeQuery();

      String oneSuggestion;
      while (rs.next()) {
        oneSuggestion = rs.getString(1);
        if (StringUtil.isDefined(oneSuggestion)) {
          suggestions.add(oneSuggestion);
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    } finally {
      DBUtil.close(rs, statement);
    }
    return suggestions;
  }

  public static void printSuggestionsIncludes(String fieldName, PrintWriter out) {
    int zindex = 100;
    out.println("<style type=\"text/css\">\n");
    out.println("	#container" + fieldName + " {\n");
    out
        .println("		z-index:"
        + zindex
        + "; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n");
    out.println("	}\n");
    out.println("	#" + fieldName + " {\n");
    out.println("		_position:absolute; /* abs pos needed for ie quirks */\n");
    out.println("	}\n");
    out.println("</style>\n");
  }

  public static void printSuggestionsScripts(String fieldName, List<String> suggestions,
      PrintWriter out) {
    out.println("<script type=\"text/javascript\">\n");
    out.println("listArray" + fieldName + " = [\n");

    Iterator<String> itRes = suggestions.iterator();
    String val;
    while (itRes.hasNext()) {
      val = itRes.next();

      out.println("\"" + WebEncodeHelper.javaStringToJsString(val) + "\"");

      if (itRes.hasNext()) {
        out.println(",");
      }
    }

    out.println("];\n");
    out.println("</script>\n");

    out.println("<script type=\"text/javascript\">\n");
    out.println(" this.oACDS" + fieldName + " = new YAHOO.util.LocalDataSource(listArray"
        + fieldName + ");\n");
    out.println("	this.oAutoComp" + fieldName
        + " = new YAHOO.widget.AutoComplete('" + fieldName + "','container"
        + fieldName + "', this.oACDS" + fieldName + ");\n");
    out.println("	this.oAutoComp" + fieldName
        + ".prehighlightClassName = \"yui-ac-prehighlight\";\n");
    out.println("	this.oAutoComp" + fieldName + ".typeAhead = true;\n");
    out.println("	this.oAutoComp" + fieldName + ".useShadow = true;\n");
    out.println("	this.oAutoComp" + fieldName + ".minQueryLength = 0;\n");

    out.println("	this.oAutoComp" + fieldName + ".textboxFocusEvent.subscribe(function(){\n");
    out.println("		var sInputValue = YAHOO.util.Dom.get('" + fieldName + "').value;\n");
    out.println("		if(sInputValue.length == 0) {\n");
    out.println("			var oSelf = this;\n");
    out.println("			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n");
    out.println("		}\n");
    out.println("	});\n");
    out.println("</script>\n");
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
