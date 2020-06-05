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
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.workflow.api.user.Replacement;

import static org.silverpeas.core.util.URLUtil.Permalink.COMPONENT;

/**
 * A builder of notifications about a replacement between two users in a given workflow instance.
 * @author mmoquillon
 */
public abstract class AbstractReplacementNotificationBuilder
    extends AbstractTemplateUserNotificationBuilder<Replacement>
    implements RemoveSenderRecipientBehavior, FallbackToCoreTemplatePathBehavior {

  private static final String MULTILANG_BUNDLE =
      "org.silverpeas.workflow.multilang.usernotification";

  private final NotifAction action;
  private final ComponentInst workflow;

  /**
   * Default constructor
   * @param resource the resource which is the object of the notification.
   */
  AbstractReplacementNotificationBuilder(final Replacement resource, final NotifAction action) {
    super(resource);
    this.action = action;
    this.workflow = OrganizationController.get().getComponentInst(resource.getWorkflowInstanceId());
  }

  @Override
  protected String getBundleSubjectKey() {
    return "notification.subject." + action.name().toLowerCase();
  }

  @Override
  protected void performTemplateData(final String language, final Replacement resource,
      final SilverpeasTemplate template) {

    getNotificationMetaData().addLanguage(language,
        getBundle(language).getStringWithParams(getBundleSubjectKey(), workflow.getName(language)),
        null);

    final String linkUrl = URLUtil.getCurrentServerURL() +
        URLUtil.getPermalink(COMPONENT, resource.getWorkflowInstanceId());
    final String linkLabel = getBundle(language)
        .getStringWithParams("replacement.notifLinkLabel", workflow.getName(language));
    getNotificationMetaData().setLink(new Link(linkUrl, linkLabel), language);

    template.setAttribute("incumbent", resource.getIncumbent().getFullName());
    template.setAttribute("substitute", resource.getSubstitute().getFullName());
    final NotificationTemporal startDate = new NotificationTemporal(
        resource.getPeriod().getStartDate(), null, language);
    final NotificationTemporal endDate = new NotificationTemporal(
        TemporalConverter.asLocalDate(resource.getPeriod().getEndDate()).minusDays(1), null,
        language);
    template.setAttribute("start", startDate);
    if (!startDate.getDayDate().equals(endDate.getDayDate())) {
      template.setAttribute("end", endDate);
    }
  }

  @Override
  protected void performNotificationResource(final String language, final Replacement resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setComponentInstanceId(resource.getWorkflowInstanceId());
    notificationResourceData.setResourceId(resource.getId());
  }

  @Override
  protected String getTemplatePath() {
    return "workflow";
  }

  @Override
  protected String getTemplateFileName() {
    return action.name().toLowerCase() + "Replacement";
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
    User sender = User.getCurrentRequester();
    if (sender == null) {
      sender = User.getMainAdministrator();
    }
    return sender.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return MULTILANG_BUNDLE;
  }
}
  