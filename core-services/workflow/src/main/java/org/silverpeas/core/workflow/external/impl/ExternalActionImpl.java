/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.workflow.external.impl;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.field.MultipleUserField;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.api.model.Trigger;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.external.ExternalAction;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class ExternalActionImpl implements ExternalAction {

  private OrganizationController orga = null;
  private ProcessInstance process;
  private GenericEvent event;
  private Trigger trigger;

  public void setProcessInstance(ProcessInstance process) {
    this.process = process;
  }

  public void setEvent(GenericEvent event) {
    this.event = event;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  public Parameter getTriggerParameter(String paramName) {
    return trigger.getParameter(paramName);
  }

  public ProcessInstance getProcessInstance() {
    return process;
  }

  public GenericEvent getEvent() {
    return event;
  }

  public String getParameterValue(String parameterName) throws Exception {
    return decodeParameterValue(parameterName, false);
  }

  /*private String decodeParameterValue(String parameterName, boolean stringValue) throws Exception {
    if (getTriggerParameter(parameterName) != null) {
      String parameterValue = getTriggerParameter(parameterName).getValue();
      SilverLogger.getLogger(this).info("decode parameter " + parameterName + " = " + parameterValue);

      Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher m = p.matcher(parameterValue);
      String finalParameterValue = new String(parameterValue);

      while (m.find()) {
        String value = m.group(1);
        if (value.startsWith("folder")) {
          value = value.replace("folder.", "");
          Field f = getProcessInstance().getFolder().getField(value);
          if (stringValue) {
            if (f instanceof UserField) {
              value = getUserDetail(f.getStringValue()).getDisplayedName();
            } else if (f instanceof MultipleUserField) {
              MultipleUserField multipleUserField = (MultipleUserField) f;
              String[] userIds = multipleUserField.getUserIds();
              for (String userId : userIds) {
                if (userId != null) {
                  UserDetail user = getUserDetail(userId);
                  value += user.getDisplayedName() + " ";
                }
              }
            } else {
              value = f.getStringValue();
            }
          } else {
            value = f.getStringValue();
          }
        } else if (value.startsWith("instance")) {
          value = value.replace("instance.", "");
          org.silverpeas.core.admin.component.model.Parameter param = getOrganizationController().getComponentInst(getProcessInstance().getModelId()).getParameter(value);
          if (param != null) value = param.getValue();
        } else if (value.startsWith("participant.")) {
          value = value.replace("participant.", "");
          if (stringValue) {
            value = getProcessInstance().getParticipant(value).getUser().getFullName();
          } else {
            value = getProcessInstance().getParticipant(value).getUser().getUserId();
          }
        } else if (value.startsWith("action.")) {
          value = value.replace("action.", "");
          if (value.endsWith(".actor")) {
            value = value.replace(".actor", "");
            for (int i = 0; i < getProcessInstance().getHistorySteps().length; i++) {
              HistoryStep step = getProcessInstance().getHistorySteps()[i];
              if (step.getAction().equals(value)) {
                if (stringValue) {
                  value = step.getUser().getFullName();
                } else {
                  value = step.getUser().getUserId();
                }
                break;
              }
            }
          }
        }

        if (value == null) value = "";
        finalParameterValue = finalParameterValue.replace("${" + m.group(1) + "}", value);
      }

      return finalParameterValue;
    } else {
      return null;
    }
  }*/

  private String decodeParameterValue(String parameterName, boolean stringValue) throws Exception {
    Parameter param = getTriggerParameter(parameterName);
    if (param == null) return null;

    String parameterValue = param.getValue();
    SilverLogger.getLogger(this).info("decode parameter " + parameterName + " = " + parameterValue);

    Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
    Matcher matcher = pattern.matcher(parameterValue);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      String placeholder = matcher.group(1);
      String resolved = resolvePlaceholder(placeholder, stringValue);
      matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
    }
    matcher.appendTail(result);

    return result.toString();
  }

  private String resolvePlaceholder(String placeholder, boolean stringValue) throws Exception {
    if (placeholder.startsWith("folder.")) {
      return resolveFolderValue(placeholder.replace("folder.", ""), stringValue);
    } else if (placeholder.startsWith("instance.")) {
      return resolveInstanceValue(placeholder.replace("instance.", ""));
    } else if (placeholder.startsWith("participant.")) {
      return resolveParticipantValue(placeholder.replace("participant.", ""), stringValue);
    } else if (placeholder.startsWith("action.")) {
      return resolveActionValue(placeholder.replace("action.", ""), stringValue);
    }
    return "";
  }

  private String resolveInstanceValue(String paramName) {
    org.silverpeas.core.admin.component.model.Parameter param = getOrganizationController()
            .getComponentInst(getProcessInstance().getModelId())
            .getParameter(paramName);
    return param != null ? param.getValue() : "";
  }

  private String resolveParticipantValue(String participantName, boolean stringValue) throws WorkflowException {
    User user = getProcessInstance().getParticipant(participantName).getUser();
    return stringValue ? user.getFullName() : user.getUserId();
  }

  private String resolveActionValue(String actionName, boolean stringValue) throws WorkflowException {
    String baseAction = actionName.replace(".actor", "");
    for (HistoryStep step : getProcessInstance().getHistorySteps()) {
      if (step.getAction().equals(baseAction)) {
        return stringValue ? step.getUser().getFullName() : step.getUser().getUserId();
      }
    }
    return "";
  }

  private String resolveFolderValue(String fieldName, boolean stringValue) throws Exception {
    Field f = getProcessInstance().getFolder().getField(fieldName);
    if (f == null) return "";

    if (!stringValue) return f.getStringValue();

    if (f instanceof UserField) {
      return getUserDetail(f.getStringValue()).getDisplayedName();
    } else if (f instanceof MultipleUserField) {
      MultipleUserField multi = (MultipleUserField) f;
      return Arrays.stream(multi.getUserIds())
              .filter(Objects::nonNull)
              .map(id -> getUserDetail(id).getDisplayedName())
              .collect(Collectors.joining(" "));
    } else {
      return f.getStringValue();
    }
  }

  protected OrganizationController getOrganizationController() {
    if (this.orga == null) {
      this.orga = OrganizationControllerProvider.getOrganisationController();
    }
    return this.orga;
  }

  protected UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }
}