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
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

  public String[] getManagedTypes() {
    return new String[]{MultipleUserField.TYPE};
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    produceMandatoryCheck(out, template, pagesContext);
  }

  @Override
  public void display(PrintWriter out, MultipleUserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    final boolean writable =
        !template.isHidden() && !template.isDisabled() && !template.isReadOnly();
    final String language = pageContext.getLanguage();
    final String selectUsersLab = Util.getString("usersPanel", language);
    final String deleteUsersLab = Util.getString("clearUser", language);
    final String fieldName = template.getFieldName();
    final String rootContainerId = "select-user-group-" + fieldName + UUID.randomUUID();
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
      throw new FormException("Incorrect field value type. Expected {0}", UserField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> update(List<FileItem> items, MultipleUserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    return applyUpdate(field, value, template, pageContext);
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template,
      PagesContext pagesContext) {
    return NB_HTML_ELEMENTS;
  }
}
