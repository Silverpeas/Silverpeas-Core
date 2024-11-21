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

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.ExplorerField;
import org.silverpeas.core.contribution.content.form.field.PublicationsPickerField;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An PublicationsPickerFieldDisplayer is an object which allow to browse Silverpeas treeview (nodes) and to
 * select publications
 */
@SuppressWarnings("unused")
public class PublicationsPickerFieldDisplayer
    extends AbstractFieldDisplayer<PublicationsPickerField> {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{PublicationsPickerField.TYPE};
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pageContext) {
    if (!PublicationsPickerField.TYPE.equals(template.getTypeName())) {
      SilverLogger.getLogger(this).warn("The expected type of the publicationsPicker field is invalid: "
        + template.getTypeName());
    }
    produceMandatoryCheck(out, template, pageContext);
    Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
  }

  @Override
  public void display(PrintWriter out, PublicationsPickerField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    //noinspection DuplicatedCode
    String language = pageContext.getLanguage();
    String selectImg = Util.getIcon("explorer");
    String selectLabel = Util.getString("field.explorer.browse", language);
    String deleteImg = Util.getIcon("delete");
    String deleteLabel = Util.getString("GML.delete", language);

    String displayedValue = "";
    String rawRefs = "";
    StringBuilder html = new StringBuilder();

    String fieldName = template.getFieldName();

    if (field.getTypeName().equals(PublicationsPickerField.TYPE)) {
      rawRefs = field.getRawResourceReferences();
    }

    html.append("<input type=\"hidden\" id=\"").append(fieldName).append("\"");
    html.append(" name=\"").append(fieldName).append("\"");
    html.append(" value=\"").append(WebEncodeHelper.javaStringToHtmlString(rawRefs)).append("\"");
    html.append("/>");

    if (!template.isHidden()) {
      html.append("<textarea disabled=\"disabled\"");
      html.append(" id=\"").append(fieldName).append("_publications\"");
      html.append(" name=\"").append(fieldName).append("$$publications\"");
      if (field.getNbPublications() > 0) {
        html.append(" rows=\"").append(field.getNbPublications()+1).append("\"");
      }
      html.append(" cols=\"").append("120").append("\">");
      html.append(WebEncodeHelper.javaStringToHtmlString(field.getValueAsText(language)));
      html.append("</textarea>");
    }

    if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {

      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      String scope = parameters.get("scope");

      html.append("&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('")
          .append(URLUtil.getApplicationURL())
          .append("/explorer/jsp/explorer.jsp?elementHidden=")
          .append(fieldName)
          .append("&elementVisible=").append(fieldName).append("_publications&scope=")
          .append(scope);
      html.append("&publicationsPicker=true");
      html.append("','explorer',800,600,'scrollbars=yes');return false;\" >");
      html.append("<img src=\"")
          .append(selectImg)
          .append("\" width=\"15\" height=\"15\" border=\"0\" alt=\"")
          .append(selectLabel)
          .append("\" align=\"top\" title=\"")
          .append(selectLabel)
          .append("\"/></a>");
      html.append("&nbsp;<a href=\"#\" onclick=\"javascript:document.")
          .append(pageContext.getFormName())
          .append(".")
          .append(fieldName)
          .append(".value='';document.")
          .append(pageContext.getFormName())
          .append(".")
          .append(fieldName)
          .append("$$publications.value='';return false;\">");
      html.append("<img src=\"")
          .append(deleteImg)
          .append("\" width=\"15\" height=\"15\" border=\"0\" alt=\"")
          .append(deleteLabel)
          .append("\" align=\"top\" title=\"")
          .append(deleteLabel)
          .append("\"/></a>");

      if (template.isMandatory() && pageContext.useMandatory()) {
        html.append(Util.getMandatorySnippet());
      }
    }

    out.println(html);
  }

  @Override
  public List<String> update(String newIds, PublicationsPickerField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    if (PublicationsPickerField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(newIds)) {
        field.setNull();
      } else {
        field.setRawResourceReferences(newIds);
      }
    } else {
      throw new FormException("Incorrect field value type. Expected {0}", ExplorerField.TYPE);
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
  public List<String> update(List<FileItem> items, PublicationsPickerField field,
      FieldTemplate template, PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    return applyUpdate(field,value, template, pageContext);
  }

}
