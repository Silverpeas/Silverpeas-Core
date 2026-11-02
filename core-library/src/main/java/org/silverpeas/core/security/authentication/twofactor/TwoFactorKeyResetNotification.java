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
package org.silverpeas.core.security.authentication.twofactor;

import java.util.Collection;
import java.util.List;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

/**
 * Localized system notification asking affected users to enroll their authenticator again.
 */
public class TwoFactorKeyResetNotification extends AbstractTemplateUserNotificationBuilder<List<String>>
    implements FallbackToCoreTemplatePathBehavior {
  /**
   * Creates a notification for accounts that previously had an enabled second factor.
   * @param userIds the affected account identifiers.
   */
  public TwoFactorKeyResetNotification(final List<String> userIds) {
    super(List.copyOf(userIds));
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.social.multilang.socialNetworkBundle";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "myProfile.twoFactor.keyReset.subject";
  }

  @Override
  protected String getTemplatePath() {
    return "notification/user";
  }

  @Override
  protected String getTemplateFileName() {
    return "simple";
  }

  @Override
  protected void performTemplateData(final String language, final List<String> users,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getBundle(language).getString(getBundleSubjectKey()), "");
    template.setAttribute("message", getBundle(language).getString("myProfile.twoFactor.keyReset.message"));
  }

  @Override
  protected void performNotificationResource(final String language, final List<String> users,
      final NotificationResourceData resourceData) {
    resourceData.setResourceName(getBundle(language).getString(getBundleSubjectKey()));
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }

  @Override
  protected String getComponentInstanceId() {
    return null;
  }

  @Override
  protected String getSender() {
    return "";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return getResource();
  }
}
