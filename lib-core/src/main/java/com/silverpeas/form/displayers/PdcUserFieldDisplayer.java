/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.form.displayers;

import java.io.PrintWriter;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.PdcUserField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.ArrayList;
import java.util.List;

/**
 * A PdcUserFieldDisplayer is an object which can display a UserFiel in HTML and can retrieve via
 * HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class PdcUserFieldDisplayer extends AbstractFieldDisplayer<PdcUserField> {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[] { PdcUserField.TYPE };
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

    String fieldName = template.getFieldName();

    if (!template.getTypeName().equals(PdcUserField.TYPE)) {
      SilverTrace.info("form", "PdcUserFieldDisplayer.displayScripts",
          "form.INFO_NOT_CORRECT_TYPE", PdcUserField.TYPE);

    }
    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(document.forms['"
          + PagesContext.getFormName() + "'].elements['"
          + fieldName + "$$id'].value))) {");
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
  public void display(PrintWriter out, PdcUserField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {

    String language = PagesContext.getLanguage();
    String mandatoryImg = Util.getIcon("mandatoryField");
    String selectUserImg = Util.getIcon("userPanel");
    String selectUserLab = Util.getString("userPanel", language);

    String userNames = ""; // prénom nom,prénom nom,prénom nom, ...
    String userCardIds = ""; // userCardId,userCardId,userCardId, ...
    String html = "";

    String fieldName = template.getFieldName();
    SilverTrace.info("form", "PdcUserFieldDisplayer.display", "root.MSG_GEN_PARAM_VALUE",
        "fieldName=" + fieldName);

    if (!field.getTypeName().equals(PdcUserField.TYPE)) {
      SilverTrace
          .info("form", "PdcUserFieldDisplayer.display",
          "form.INFO_NOT_CORRECT_TYPE", PdcUserField.TYPE + ", type courant=" +
          field.getTypeName());

    } else {
      userCardIds = field.getUserCardIds();
    }

    if (!field.isNull()) {
      userNames = field.getValue();
    }

    html +=
        "<INPUT type=hidden"
        + " name=\"" + fieldName + "$$id\" value=\"" +
        EncodeHelper.javaStringToHtmlString(userCardIds) + "\" >";

    if (!template.isHidden()) {
      html +=
          "<INPUT type=\"text\" disabled size=\"50\" "
          + " id=\"" + fieldName + "$$name\" name=\"" + fieldName + "$$name\" value=\"" +
          EncodeHelper.javaStringToHtmlString(userNames) + "\" >";
    }

    if (!template.isHidden() && !template.isDisabled()
        && !template.isReadOnly()) {
      html +=
          "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('" +
          URLManager.getApplicationURL() + "/RpdcSearchUserWrapper/jsp/Open"
          + "?formName=" + PagesContext.getFormName()
          + "&elementId=" + fieldName + "$$id"
          + "&elementName=" + fieldName + "$$name"
          + "&selectedUsers=" + ((userCardIds == null) ? "" : userCardIds)
          + "','selectUsers',800,600,'');\" ><img src=\""
          + selectUserImg
          + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
          + selectUserLab + "\" align=\"absmiddle\" title=\""
          + selectUserLab + "\"></a>";

      if (template.isMandatory() && PagesContext.useMandatory()) {
        html += "&nbsp;<img src=\"" + mandatoryImg
            + "\" width=\"5\" height=\"5\" border=\"0\"/>";
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
  public List<String> update(String newId, PdcUserField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (PdcUserField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(newId)) {
        field.setNull();
      } else {
        field.setUserCardIds(newId);
      }
    } else {
      throw new FormException("PdcUserFieldDisplayer.update",
          "form.EX_NOT_CORRECT_VALUE",
          PdcUserField.TYPE);
    }
    return new ArrayList<String>();
  }

  /**
   * Method declaration
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

}