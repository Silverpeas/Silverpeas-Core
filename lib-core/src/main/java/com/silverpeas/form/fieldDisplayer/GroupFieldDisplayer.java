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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.GroupField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A GroupFieldDisplayer is an object which allow to select a group and display it in HTML and can
 * retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class GroupFieldDisplayer extends AbstractFieldDisplayer {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[0];

    s[0] = GroupField.TYPE;
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
  public void displayScripts(PrintWriter out, FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException {
    String language = PagesContext.getLanguage();

    if (!template.getTypeName().equals(GroupField.TYPE)) {
      SilverTrace.info("form", "GroupFieldDisplayer.displayScripts",
          "form.INFO_NOT_CORRECT_TYPE", GroupField.TYPE);

    }
    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("      errorMsg+=\"  - '"
          + EncodeHelper.javaStringToJsString(template.getLabel(language))
          + "' " + Util.getString("GML.MustBeFilled", language)
          + "\\n \";");
      out.println("      errorNb++;");
      out.println("   }");
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
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    SilverTrace.info("form", "GroupFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD",
        "fieldName = " + template.getFieldName() + ", value = " + field.getValue() +
        ", fieldType = " + field.getTypeName());

    String language = PagesContext.getLanguage();
    String mandatoryImg = Util.getIcon("mandatoryField");
    String selectGroupImg = Util.getIcon("groupPanel");
    String selectGroupLab = Util.getString("groupPanel", language);
    String deleteImg = Util.getIcon("delete");
    String deleteLab = Util.getString("clearGroup", language);

    String groupName = "";
    String groupId = "";
    String html = "";

    String fieldName = template.getFieldName();

    if (!field.getTypeName().equals(GroupField.TYPE)) {
      SilverTrace.info("form", "GroupFieldDisplayer.display",
          "form.INFO_NOT_CORRECT_TYPE", GroupField.TYPE);
    } else {
      groupId = ((GroupField) field).getGroupId();
    }
    if (!field.isNull()) {
      groupName = field.getValue();
    }
    html +=
        "<INPUT type=\"hidden\""
            + " id=\"" + fieldName + GroupField.PARAM_NAME_SUFFIX + "\" name=\"" + fieldName +
            GroupField.PARAM_NAME_SUFFIX + "\" value=\"" +
            EncodeHelper.javaStringToHtmlString(groupId) + "\" >";

    if (!template.isHidden()) {
      html +=
          "<INPUT type=\"text\" disabled size=\"50\" "
          + " id=\"" + fieldName + "$$name\" name=\"" + fieldName + "$$name\" value=\"" +
          EncodeHelper.javaStringToHtmlString(groupName) + "\" >";
    }

    if (!template.isHidden() && !template.isDisabled()
        && !template.isReadOnly()) {
      html +=
          "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('" +
          URLManager.getApplicationURL() + "/RselectionPeasWrapper/jsp/open"
          + "?formName=" + PagesContext.getFormName()
          + "&elementId=" + fieldName + GroupField.PARAM_NAME_SUFFIX
          + "&elementName=" + fieldName + "$$name"
          + "&selectable=" + SelectionUsersGroups.GROUP
          + "&selectedGroup=" + ((groupId == null) ? "" : groupId)
          + "','selectGroup',800,600,'');\" >";
      html += "<img src=\""
          + selectGroupImg
          + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
          + selectGroupLab + "\" align=\"absmiddle\" title=\""
          + selectGroupLab + "\"/></a>";
      html +=
          "&nbsp;<a href=\"#\" onclick=\"javascript:"
          + "document." + PagesContext.getFormName() + "." + fieldName +
          GroupField.PARAM_NAME_SUFFIX + ".value='';"
          + "document." + PagesContext.getFormName() + "." + fieldName + "$$name" +
          ".value='';"
          + "\">";
      html += "<img src=\""
          + deleteImg
          + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
          + deleteLab + "\" align=\"absmiddle\" title=\""
          + deleteLab + "\"/></a>";

      if (template.isMandatory() && PagesContext.useMandatory()) {
        html += "&nbsp;<img src=\"" + mandatoryImg
            + "\" width=\"5\" height=\"5\" border=\"0\" alt=\"\"/>";
      }
    }

    out.println(html);
  }

  public List<String> update(String newId, Field field,
      FieldTemplate template,
      PagesContext pagesContext) throws FormException {

    if (field.getTypeName().equals(GroupField.TYPE)) {
      if (newId == null || newId.trim().equals("")) {
        field.setNull();
      } else {
        ((GroupField) field).setGroupId(newId);
      }
    } else {
      throw new FormException("GroupFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          GroupField.TYPE);
    }
    return new ArrayList<String>();
  }

  /**
   * Method declaration
   * @return
   */
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   * @return
   */
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public List<String> update(List<FileItem> items, Field field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName() + GroupField.PARAM_NAME_SUFFIX;
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES &&
        !StringUtil.isDefined(value)) {
      return new ArrayList<String>();
    }
    return update(value, field, template, pageContext);
  }

}
