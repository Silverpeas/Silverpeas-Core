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
import org.silverpeas.core.contribution.content.form.field.GroupField;
import org.silverpeas.core.html.plugin.UserGroupSelectProducer;
import org.silverpeas.core.util.file.FileUploadUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.silverpeas.core.html.plugin.UserGroupSelectProducer.SelectionType.GROUP;
import static org.silverpeas.core.html.plugin.UserGroupSelectProducer.withContainerId;
import static org.silverpeas.kernel.util.StringUtil.getBooleanValue;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A GroupFieldDisplayer is an object which allow to select a group and display it in HTML and can
 * retrieve via HTTP any updated value.
 * <p>
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class GroupFieldDisplayer extends AbstractFieldDisplayer<GroupField> {

  private static final int NB_HTML_ELEMENTS = 2;

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[] { GroupField.TYPE };
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    produceMandatoryCheck(out, template, pagesContext);
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public void display(PrintWriter out, GroupField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    final boolean writable =
        !template.isHidden() && !template.isDisabled() && !template.isReadOnly();
    final String language = pageContext.getLanguage();
    final String selectGroupLab = Util.getString("groupPanel", language);
    final String deleteGroupLab = Util.getString("clearGroup", language);
    final String fieldName = template.getFieldName();
    final String rootContainerId = "select-user-group-" + fieldName + UUID.randomUUID();
    final String groupId = field.getTypeName().equals(GroupField.TYPE) ? field.getGroupId() : "";

    final UserGroupSelectProducer selectGroup = withContainerId(rootContainerId)
        .withGroupInputName(fieldName)
        .selectionOf(GROUP)
        .multiple(false)
        .readOnly(!writable)
        .hidden(template.isHidden())
        .withGroupIds(groupId)
        .withUserPanelButtonLabel(selectGroupLab)
        .withRemoveButtonLabel(deleteGroupLab);
    if (writable) {
      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      String roles = parameters.get("roles");
      boolean groupsOfInstanceOnly =
          getBooleanValue(parameters.get("groupsOfInstanceOnly")) || isDefined(roles);
      if (groupsOfInstanceOnly) {
        selectGroup.filterOnComponentId(pageContext.getComponentId());
      }
      selectGroup.filterOnRoles(roles);
      selectGroup.mandatory(template.isMandatory() && pageContext.useMandatory());
    }

    out.println(new div().setID(rootContainerId));
    out.println(selectGroup.produce());
  }

  @Override
  public List<String> update(String newId, GroupField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (field.getTypeName().equals(GroupField.TYPE)) {
      if (!isDefined(newId)) {
        field.setNull();
      } else {
        field.setGroupId(newId);
      }
    } else {
      throw new FormException("Incorrect field value type. Expected {0}", GroupField.TYPE);
    }
    return new ArrayList<>();
  }

  /**
   * Method declaration
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return NB_HTML_ELEMENTS;
  }

  @Override
  public List<String> update(List<FileItem> items, GroupField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
        && !isDefined(value)) {
      return new ArrayList<>();
    }
    return update(value, field, template, pageContext);
  }
}
