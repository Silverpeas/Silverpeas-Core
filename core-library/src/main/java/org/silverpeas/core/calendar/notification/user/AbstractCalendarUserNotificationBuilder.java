/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.calendar.notification.user;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.builder.AbstractContributionTemplateUserNotificationBuilder;

/**
 * Centralizes the building of a {@link UserNotification} in relation with a calendar contribution.
 * <p>
 * Be aware about {@link FallbackToCoreTemplatePathBehavior}.
 * </p>
 * @param <C> a contribution implementation type
 */
public abstract class AbstractCalendarUserNotificationBuilder<C extends Contribution>
    extends AbstractContributionTemplateUserNotificationBuilder<C>
    implements FallbackToCoreTemplatePathBehavior {

  private final User user;

  protected AbstractCalendarUserNotificationBuilder(final C contribution, final User user) {
    super(contribution);
    this.user = user;
  }

  @Override
  protected void perform(final C resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getTemplatePath() {
    return "calendar";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getIdentifier().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return user.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.calendar.multilang.usernotification";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "event.link.label";
  }
}