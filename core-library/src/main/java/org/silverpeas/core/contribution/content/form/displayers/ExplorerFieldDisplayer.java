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
import org.silverpeas.core.contribution.content.form.field.ExplorerField;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An ExplorerFieldDisplayer is an object which allow to browse Silverpeas treeview (nodes) and to
 * select one of it
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class ExplorerFieldDisplayer extends AbstractFieldDisplayer<ExplorerField> {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{ExplorerField.TYPE};
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
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pageContext)
      throws IOException {
    String language = pageContext.getLanguage();

    if (!ExplorerField.TYPE.equals(template.getTypeName())) {
      SilverTrace
          .info("form", "ExplorerFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
              ExplorerField.TYPE);
    }
    if (template.isMandatory() && pageContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("      errorMsg+=\"  - '" +
          EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " +
          Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("      errorNb++;");
      out.println("   }");
    }

    Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
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
  public void display(PrintWriter out, ExplorerField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String language = pageContext.getLanguage();
    String selectImg = Util.getIcon("explorer");
    String selectLabel = Util.getString("field.explorer.browse", language);
    String deleteImg = Util.getIcon("delete");
    String deleteLabel = Util.getString("GML.delete", language);

    String path = "";
    String nodePK = "";
    String html = "";

    String fieldName = template.getFieldName();

    if (!field.getTypeName().equals(ExplorerField.TYPE)) {

    } else {
      nodePK = field.getNodePK();
    }
    if (!field.isNull()) {
      path = field.getValue(language);
    }
    html +=
        "<input type=\"hidden\"" + " id=\"" + fieldName + "\" name=\"" + fieldName + "\" value=\"" +
            EncodeHelper.javaStringToHtmlString(nodePK) + "\"/>";

    if (!template.isHidden()) {
      html += "<input type=\"text\" disabled=\"disabled\" size=\"50\" " + "id=\"" + fieldName +
          "_path\" name=\"" + fieldName + "$$path\" value=\"" +
          EncodeHelper.javaStringToHtmlString(path) + "\"/>";
    }

    if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {

      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      String scope = parameters.get("scope");

      html += "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('" +
          URLUtil.getApplicationURL() + "/explorer/jsp/explorer.jsp" + "?elementHidden=" +
          fieldName + "&elementVisible=" + fieldName + "_path" + "&dedicatedToWriters=true" +
          "&scope=" + scope;
      html += "','explorer',400,600,'scrollbars=yes');\" >";
      html += "<img src=\"" + selectImg + "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" +
          selectLabel + "\" align=\"top\" title=\"" + selectLabel + "\"/></a>";
      html +=
          "&nbsp;<a href=\"#\" onclick=\"javascript:" + "document." + pageContext.getFormName() +
              "." + fieldName + ".value='';" + "document." + pageContext.getFormName() + "." +
              fieldName + "$$path" + ".value='';" + "\">";
      html += "<img src=\"" + deleteImg + "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" +
          deleteLabel + "\" align=\"top\" title=\"" + deleteLabel + "\"/></a>";

      if (template.isMandatory() && pageContext.useMandatory()) {
        html += Util.getMandatorySnippet();
      }
    }

    out.println(html);
  }

  @Override
  public List<String> update(String newId, ExplorerField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    if (ExplorerField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(newId)) {
        field.setNull();
      } else {
        field.setNodePK(newId);
      }
    } else {
      throw new FormException("ExplorerFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          ExplorerField.TYPE);
    }
    return Collections.emptyList();
  }

  /**
   * Method declaration
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pageContext) {
    return 2;
  }

  @Override
  public List<String> update(List<FileItem> items, ExplorerField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES &&
        !StringUtil.isDefined(value)) {
      return new ArrayList<>();
    }
    return update(value, field, template, pageContext);
  }

}
