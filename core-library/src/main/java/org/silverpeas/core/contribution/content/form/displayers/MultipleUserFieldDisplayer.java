/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.displayers;

import org.apache.commons.fileupload.FileItem;
import org.apache.ecs.xhtml.div;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.MultipleUserField;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.html.plugin.UserGroupSelectProducer;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.contribution.content.form.displayers.UserFieldDisplayer
    .produceMandatoryCheck;
import static org.silverpeas.core.html.plugin.UserGroupSelectProducer.SelectionType.USER;
import static org.silverpeas.core.html.plugin.UserGroupSelectProducer.withContainerId;

/**
 * A MultipleUserFieldDisplayer is an object which can display a MultipleUserField in HTML and can
 * retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class MultipleUserFieldDisplayer extends AbstractFieldDisplayer<MultipleUserField> {

  private static final int NB_HTML_ELEMENTS = 2;

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{MultipleUserField.TYPE};
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
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws java.io.IOException {
    produceMandatoryCheck(out, template, pagesContext);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void display(PrintWriter out, MultipleUserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    final boolean writable =
        !template.isHidden() && !template.isDisabled() && !template.isReadOnly();
    final String language = pageContext.getLanguage();
    final String selectUsersLab = Util.getString("usersPanel", language);
    final String deleteUsersLab = Util.getString("clearUser", language);
    final String fieldName = template.getFieldName();
    final String rootContainerId = "select-user-group-" + fieldName;
    final String userIds =
        field.getTypeName().equals(MultipleUserField.TYPE) ? field.getStringValue() : "";

    final UserGroupSelectProducer selectUsers = withContainerId(rootContainerId)
        .withUserInputName(fieldName)
        .selectionOf(USER)
        .multiple(true)
        .readOnly(!writable)
        .hidden(template.isHidden())
        .withUserIds(userIds)
        .withUserPanelButtonLabel(selectUsersLab)
        .withRemoveButtonLabel(deleteUsersLab);
    if (writable) {
      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      String roles = parameters.get("roles");
      boolean usersOfInstanceOnly =
          StringUtil.getBooleanValue(parameters.get("usersOfInstanceOnly")) ||
              StringUtil.isDefined(roles);
      if (usersOfInstanceOnly) {
        selectUsers.filterOnComponentId(pageContext.getComponentId());
      }
      selectUsers.filterOnRoles(roles);
      selectUsers.mandatory(template.isMandatory() && pageContext.useMandatory());
    }

    out.println(new div().setID(rootContainerId));
    out.println(selectUsers.produce());
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  @Override
  public List<String> update(String newIds, MultipleUserField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (MultipleUserField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(newIds)) {
        field.setNull();
      } else {
        field.setStringValue(newIds);
      }
    } else {
      throw new FormException("UserFieldDisplayer.update",
          "form.EX_NOT_CORRECT_VALUE", UserField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> update(List<FileItem> items, MultipleUserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES && !StringUtil.
        isDefined(value)) {
      return new ArrayList<>();
    }
    return update(value, field, template, pageContext);
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template,
      PagesContext pagesContext) {
    return NB_HTML_ELEMENTS;
  }
}
