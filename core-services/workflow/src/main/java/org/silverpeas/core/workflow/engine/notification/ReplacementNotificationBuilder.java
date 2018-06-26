/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.workflow.engine.notification;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.RemoveSenderRecipientBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.workflow.api.user.Replacement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.DateUtil.getDateOutputFormat;

/**
 * A builder of notifications about a replacement between two users in a given workflow instance.
 * @author mmoquillon
 */
public class ReplacementNotificationBuilder
    extends AbstractTemplateUserNotificationBuilder<Replacement>
    implements RemoveSenderRecipientBehavior, FallbackToCoreTemplatePathBehavior {

  private static final String MULTILANG_BUNDLE =
      "org.silverpeas.workflow.multilang.usernotification";

  private final NotifAction action;

  /**
   * Default constructor
   * @param resource the resource which is the object of the notification.
   */
  public ReplacementNotificationBuilder(final Replacement resource, final NotifAction action) {
    super(resource);
    this.action = action;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "notification.subject";
  }

  @Override
  protected String getTemplateFileName() {
    return action.name().toLowerCase() + "Replacement";
  }

  @Override
  protected void performTemplateData(final String language, final Replacement resource,
      final SilverpeasTemplate template) {
    final String senderId = getSender();
    if (!resource.getIncumbent().getUserId().equals(senderId)) {
      template.setAttribute("incumbent", resource.getIncumbent().getFullName());
    }
    if (!resource.getSubstitute().getUserId().equals(senderId)) {
      template.setAttribute("substitute", resource.getSubstitute().getFullName());
    }

    final ComponentInst workflow =
        OrganizationController.get().getComponentInst(resource.getWorkflowInstanceId());
    template.setAttribute("workflow", workflow.getName(language));
    template.setAttribute("startDate",
        toLocalizedDateText(resource.getPeriod().getStartDate(), language));
    template.setAttribute("endDate",
        toLocalizedDateText(resource.getPeriod().getEndDate(), language));
  }

  @Override
  protected void performNotificationResource(final String language, final Replacement resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setComponentInstanceId(resource.getWorkflowInstanceId());
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(resource.getClass().getSimpleName());
  }

  @Override
  protected String getTemplatePath() {
    return "workflow";
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getWorkflowInstanceId();
  }

  @Override
  protected String getSender() {
    User sender = OperationContext.fromCurrentRequester().getUser();
    if (sender == null) {
      sender = User.getMainAdministrator();
    }
    return sender.getId();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final String incumbentId = getResource().getIncumbent().getUserId();
    final String substituteId = getResource().getSubstitute().getUserId();
    return Stream.of(incumbentId, substituteId).collect(Collectors.toSet());
  }

  @Override
  protected String getMultilangPropertyFile() {
    return MULTILANG_BUNDLE;
  }

  private String toLocalizedDateText(final Temporal temporal, final String language) {
    final LocalDate date = TemporalConverter.asLocalDate(temporal);
    return date.format(DateTimeFormatter.ofPattern(getDateOutputFormat(language).getPattern()));
  }
}
  