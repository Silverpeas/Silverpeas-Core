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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.workflow.engine.notification;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;

import java.util.Collection;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class UserNotificationBuilder extends AbstractTemplateUserNotificationBuilder<Task>
    implements FallbackToCoreTemplatePathBehavior {

  private static final String MULTILANG_BUNDLE =
      "org.silverpeas.workflow.multilang.usernotification";

  private List<String> userIds;
  private final User sender;
  private final String text;
  private final boolean linkDisabled;
  private final String incumbentId;

  public UserNotificationBuilder(final List<String> userIds, final Task task, final User sender,
      String text, boolean linkDisabled, String incumbentId) {
    super(task);
    this.userIds = userIds;
    this.sender = sender;
    this.text = text;
    this.linkDisabled = linkDisabled;
    this.incumbentId = incumbentId;
  }

  @Override
  protected void performTemplateData(final String language, final Task task,
      final SilverpeasTemplate template) {

    ProcessInstance processInstance = task.getProcessInstance();

    getNotificationMetaData().addLanguage(language,
        processInstance.getTitle(task.getUserRoleName(), language), null);

    if (!linkDisabled) {
      String link = "/RprocessManager/" + processInstance.getModelId() +
          "/searchResult?Type=ProcessInstance&Id=" + processInstance.getInstanceId() +
          "&role=" + task.getUserRoleName();
      if (StringUtil.isDefined(incumbentId)) {
        link += "&IncumbentId="+incumbentId;
      }
      getNotificationMetaData()
          .setLink(new Link(link, getBundle(language).getString("notification.notifLinkLabel")),
              language);
    }

    try {
      DataRecord data = task.getProcessInstance().getAllDataRecord(task.getUserRoleName(), language);
      String content = DataRecordUtil.applySubstitution(text, data, language);

      template.setAttribute("content", content);
    } catch (WorkflowException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  @Override
  protected void performNotificationResource(final String language, final Task task,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(true);
    notificationResourceData
        .setResourceName(task.getProcessInstance().getTitle(task.getUserRoleName(), language));
  }



  @Override
  protected String getTemplatePath() {
    return "workflow";
  }

  @Override
  protected String getTemplateFileName() {
    return "notification";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getProcessInstance().getModelId();
  }

  @Override
  protected String getSender() {
    if (sender != null) {
      return sender.getUserId();
    }
    return null;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return userIds;
  }

  @Override
  protected String getLocalizationBundlePath() {
    return MULTILANG_BUNDLE;
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "notification.notifLinkLabel";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }
}