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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.fieldType.TextFieldImpl;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.user.UserInfo;
import com.silverpeas.workflow.api.user.UserSettings;

/**
 * A UserInfoTemplate builds fields giving information about user
 */
public class UserInfoTemplate extends ProcessInstanceFieldTemplate {
  private String role;
  private String lang;
  private Item item;

  public UserInfoTemplate(String fieldName, Item item, String role, String lang) {
    super(fieldName, item.getType(), item.getType(), item.getLabel(role, lang));
    this.role = role;
    this.lang = lang;
    this.item = item;
  }

  /**
   * Returns a field built from this template and filled from the given process instance.
   */
  public Field getField(ProcessInstance instance) throws FormException {
    Field field = null;

    try {
      String shortFieldName = getFieldName();
      int index = shortFieldName.lastIndexOf(".actor.");

      String actionName = shortFieldName.substring(7, index);
      HistoryStep step = instance.getMostRecentStep(actionName);
      if (step != null) {
        shortFieldName = shortFieldName.substring(index + 7);

        if (item.getMapTo() != null && item.getMapTo().length() != 0) {
          User user = Workflow.getUserManager().getUser(
              step.getUser().getUserId());

          field = new TextFieldImpl();
          if (user != null)
            field.setStringValue(user.getInfo(item.getMapTo()));
        }

        else {
          UserSettings settings = Workflow.getUserManager().getUserSettings(
              step.getUser().getUserId(), instance.getModelId());
          UserInfo info = settings.getUserInfo(shortFieldName);

          field = instance.getProcessModel().getUserInfos().toRecordTemplate(
              role, lang, false).getEmptyRecord().getField(shortFieldName);
          if (field != null && info != null)
            field.setStringValue(info.getValue());
        }
      }

      return field;
    } catch (WorkflowException e) {
      throw new FormException("UserInfoTemplate", "form.EXP_UNKNOWN_FIELD",
          getFieldName());
    }
  }
}
