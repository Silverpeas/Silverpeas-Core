/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.SilverpeasRuntimeException;
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
import org.silverpeas.core.workflow.engine.user.UserSettingsService;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
  private static final String LAZY_PROCESS_INSTANCE_DATA_RECORD = "LazyProcessInstanceDataRecord";
  private static final String ACTOR = ".actor.";

  /**
   * Builds the data record representation of a process instance.
   */
  public LazyProcessInstanceDataRecord(ProcessInstance instance, String role, String lang)
      throws WorkflowException {
    super(instance, role, lang);
    this.role = role;
    this.lang = lang;
  }

  @Override
  protected ProcessInstanceTemplate getTemplate(final String role, final String lang) {
    return null;
  }

  /**
   * Returns the data record id.
   */
  @Override
  public String getId() {
    return instance.getInstanceId();
  }

  /**
   * Returns the named field.
   * @throw FormException when the fieldName is unknown.
   */
  @Override
  public Field getField(String fieldName) throws FormException {
    if (fieldName.startsWith("folder.") || fieldName.startsWith("instance.")) {
      int pos = fieldName.indexOf('.');
      return getFolderField(fieldName.substring(pos + 1, fieldName.length()));
    }

    if (fieldName.startsWith("action.")) {
      int pos = fieldName.indexOf('.');
      return getActionField(fieldName.substring(pos + 1, fieldName.length()));
    }

    return null;
  }

  private Field getActionField(String fieldName) throws FormException {

    try {
      // Action label
      if (fieldName.indexOf('.') == -1) {
        Field field = new TextFieldImpl();
        Action action = instance.getProcessModel().getAction(fieldName);
        field.setStringValue(action.getLabel(this.role, this.lang));
        return field;
      } // Action label
      else if (fieldName.endsWith(".label")) {
        return getLabelRoField(fieldName);
      } // Action last realization date
      else if (fieldName.endsWith(".date")) {
        return getDateRoField(fieldName);
      } // Action last actor
      else if (fieldName.endsWith(".actor")) {
        return getActorNameRoField(fieldName);
      } // Relation with action last actor
      else if (fieldName.indexOf(ACTOR) != -1) {
        return getRelationRoField(fieldName);
      } else {
        throw new FormFatalException(LAZY_PROCESS_INSTANCE_DATA_RECORD,
            "form.EXP_FIELD_NOT_FOUND", fieldName);
      }
    } catch (Exception e) {
      throw new FormFatalException(LAZY_PROCESS_INSTANCE_DATA_RECORD,
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldName);
    }

  }

  private Field getLabelRoField(final String fieldName) throws WorkflowException, FormException {
    Field field = new TextFieldImpl();
    String actionName = fieldName.substring(0, fieldName.length() - 6);
    Action action = instance.getProcessModel().getAction(actionName);
    field.setStringValue(action.getLabel(this.role, this.lang));
    return field;
  }

  private Field getRelationRoField(final String fieldName) throws WorkflowException, FormException {
    int fieldIndex = fieldName.indexOf(ACTOR);
    String actionName = fieldName.substring(0, fieldIndex);
    String shortFieldName = fieldName.substring(fieldIndex + 7);
    HistoryStep step = instance.getMostRecentStep(actionName);
    Item item = instance.getProcessModel().getUserInfos().getItem(shortFieldName);
    if (step != null && item != null) {
      if (item.getMapTo() != null && item.getMapTo().length() != 0) {
        User user = Workflow.getUserManager().getUser(step.getUser().getUserId());

        Field field = new TextFieldImpl();
        if (user != null) {
          field.setStringValue(user.getInfo(item.getMapTo()));
        }
        return field;
      } else {
        UserSettings settings =
            UserSettingsService.get().get(step.getUser().getUserId(), instance.getModelId());
        UserInfo info = settings.getUserInfo(shortFieldName);

        Field field = instance.getProcessModel()
            .getUserInfos()
            .toRecordTemplate(role, lang, false)
            .getEmptyRecord()
            .getField(shortFieldName);
        if (field != null && info != null) {
          field.setStringValue(info.getValue());
        }
        return field;
      }
    }
    return new TextRoField(null);
  }

  private Field getActorNameRoField(final String fieldName) throws WorkflowException {
    String actionName = fieldName.substring(0, fieldName.length() - 6);
    HistoryStep step = instance.getMostRecentStep(actionName);
    if (step != null) {
      return new TextRoField(step.getUser().getFullName());
    } else {
      return new TextRoField(null);
    }
  }

  private Field getDateRoField(final String fieldName) {
    String actionName = fieldName.substring(0, fieldName.length() - 5);
    HistoryStep step = instance.getMostRecentStep(actionName);
    if (step != null) {
      return new DateRoField(step.getActionDate());
    } else {
      return new DateRoField(null);
    }
  }

  private Field getFolderField(String fieldName) throws FormException {
    Field field;
    try {
      Item fieldItem = instance.getProcessModel().getDataFolder().getItem(fieldName);
      Class<? extends Field> fieldImpl = TypeManager.getInstance().getFieldImplementation(fieldItem.getType());
      Class<?>[] noParameterClass = new Class[0];
      Constructor<? extends Field> constructor = fieldImpl.getConstructor(noParameterClass);
      Object[] noParameter = new Object[0];
      field = constructor.newInstance(noParameter);
      field.setStringValue(getFieldValue(fieldName));

      return field;
    } catch (Exception e) {
      throw new FormFatalException(LAZY_PROCESS_INSTANCE_DATA_RECORD,
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldName);
    }

  }

  private String getFieldValue(String fieldName) {
    return rawValues.computeIfAbsent(fieldName, k -> {
      try {
        String folderRecordSetName = instance.getProcessModel().getFolderRecordSetName();
        return GenericRecordSetManager.getInstance().getRawValue(folderRecordSetName, instance.
            getInstanceId(), fieldName);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
  }

  /**
   * Returns the field at the index position in the record.
   * @throw FormException when the fieldIndex is unknown.
   */
  @Override
  public Field getField(int fieldIndex) {
    return null;
  }

  @Override
  public String[] getFieldNames() {
    return new String[0];
  }

  /**
   * The process instance whose data are managed by this data record.
   */
  final String role;
  final String lang;
  private Map<String, String> rawValues = new HashMap<>();
}
