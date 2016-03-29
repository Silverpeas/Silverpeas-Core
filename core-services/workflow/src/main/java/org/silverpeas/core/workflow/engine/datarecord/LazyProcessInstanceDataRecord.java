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

package org.silverpeas.core.workflow.engine.datarecord;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.FormFatalException;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;

/**
 * A ProcessInstanceDataRecord groups in a single DataRecord all the data items of a
 * ProcessInstance. The instance : instance instance.title instance.<columnName> The model : model
 * model.label model.peas-label The folder : <folderItem> The forms : form.<formName>
 * form.<formName>.title form.<formName>.<fieldItem> The actions : action.<actionName>
 * action.<actionName>.label action.<actionName>.date action.<actionName>.actor The users :
 * participant.<participantName>
 */
public class LazyProcessInstanceDataRecord extends AbstractProcessInstanceDataRecord {

  private static final long serialVersionUID = 1L;

  /**
   * Builds the data record representation of a process instance.
   */
  public LazyProcessInstanceDataRecord(ProcessInstance instance, String role, String lang) {
    this.instance = instance;
    this.role = role;
    this.lang = lang;
  }

  /**
   * Returns the data record id.
   */
  public String getId() {
    return instance.getInstanceId();
  }

  /**
   * Returns the named field.
   * @throw FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException {
    if (fieldName.startsWith("folder.") || fieldName.startsWith("instance.")) {
      int pos = fieldName.indexOf(".");
      return getFolderField(fieldName.substring(pos + 1, fieldName.length()));
    }

    if (fieldName.startsWith("action.")) {
      int pos = fieldName.indexOf(".");
      return getActionField(fieldName.substring(pos + 1, fieldName.length()));
    }

    return null;
  }

  private Field getActionField(String fieldName) throws FormException {

    try {
      // Action label
      if (fieldName.indexOf(".") == -1) {
        Field field = new TextFieldImpl();
        Action action = instance.getProcessModel().getAction(fieldName);
        field.setStringValue(action.getLabel(this.role, this.lang));
        return field;
      } // Action label
      else if (fieldName.endsWith(".label")) {
        Field field = new TextFieldImpl();
        String actionName = fieldName.substring(0, fieldName.length() - 6);
        Action action = instance.getProcessModel().getAction(actionName);
        field.setStringValue(action.getLabel(this.role, this.lang));
        return field;
      } // Action last realization date
      else if (fieldName.endsWith(".date")) {
        String actionName = fieldName.substring(0, fieldName.length() - 5);
        HistoryStep step = instance.getMostRecentStep(actionName);
        if (step != null) {
          return new DateRoField(step.getActionDate());
        } else {
          return new DateRoField(null);
        }
      } // Action last actor
      else if (fieldName.endsWith(".actor")) {
        String actionName = fieldName.substring(0, fieldName.length() - 6);
        HistoryStep step = instance.getMostRecentStep(actionName);
        if (step != null) {
          return new TextRoField(step.getUser().getFullName());
        } else {
          return new TextRoField(null);
        }
      } // Relation with action last actor
      else if (fieldName.indexOf(".actor.") != -1) {
        String actionName = fieldName.substring(0, fieldName.indexOf(".actor."));
        HistoryStep step = instance.getMostRecentStep(actionName);
        if (step != null) {
          String shortFieldName = fieldName.substring(fieldName.indexOf(".actor.") + 7);
          Item item = instance.getProcessModel().getUserInfos().getItem(shortFieldName);
          if (item != null) {
            if (item.getMapTo() != null && item.getMapTo().length() != 0) {
              User user = Workflow.getUserManager().getUser(
                  step.getUser().getUserId());

              Field field = new TextFieldImpl();
              if (user != null) {
                field.setStringValue(user.getInfo(item.getMapTo()));
              }
              return field;
            } else {
              UserSettings settings = Workflow.getUserManager().getUserSettings(
                  step.getUser().getUserId(), instance.getModelId());
              UserInfo info = settings.getUserInfo(shortFieldName);

              Field field = instance.getProcessModel().getUserInfos().toRecordTemplate(
                  role, lang, false).getEmptyRecord().getField(shortFieldName);
              if (field != null && info != null) {
                field.setStringValue(info.getValue());
              }
              return field;
            }
          } else {
            return new TextRoField(null);
          }
        } else {
          return new TextRoField(null);
        }
      } else {
        throw new FormFatalException("LazyProcessInstanceDataRecord",
            "form.EXP_FIELD_NOT_FOUND", fieldName);
      }
    } catch (Exception e) {
      throw new FormFatalException("LazyProcessInstanceDataRecord",
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldName);
    }

  }

  private Field getFolderField(String fieldName) throws FormException {
    Field field;
    try {
      Item fieldItem = instance.getProcessModel().getDataFolder().getItem(fieldName);
      Class fieldImpl = TypeManager.getInstance().getFieldImplementation(fieldItem.getType());
      Class[] noParameterClass = new Class[0];
      Constructor constructor = fieldImpl.getConstructor(noParameterClass);
      Object[] noParameter = new Object[0];
      field = (Field) constructor.newInstance(noParameter);
      field.setStringValue(getFieldValue(fieldName));

      return field;
    } catch (Exception e) {
      throw new FormFatalException("LazyProcessInstanceDataRecord",
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldName);
    }

  }

  private String getFieldValue(String fieldName) throws WorkflowException, FormException {
    String rawValue = null;
    if (rawValues.get(fieldName) == null) {
      String folderRecordSetName = instance.getProcessModel().getFolderRecordSetName();
      rawValue = GenericRecordSetManager.getInstance().getRawValue(folderRecordSetName, instance.
          getInstanceId(), fieldName);
      rawValues.put(fieldName, rawValue);
    }

    return rawValue;
  }

  /**
   * Returns the field at the index position in the record.
   * @throw FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException {
    return null;
  }

  public String[] getFieldNames() {
    return new String[0];
  }

  /**
   * The process instance whose data are managed by this data record.
   */
  final ProcessInstance instance;
  final String role;
  final String lang;
  private Map<String, String> rawValues = new HashMap<>();
}
