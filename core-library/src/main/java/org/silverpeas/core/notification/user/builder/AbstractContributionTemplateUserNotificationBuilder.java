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

package org.silverpeas.core.notification.user.builder;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Centralization of common behavior around the contribution implementations.
 * @param <C>
 */
public abstract class AbstractContributionTemplateUserNotificationBuilder<C extends Contribution>
    extends AbstractTemplateUserNotificationBuilder<C> {

  private LocalizedContribution localizedContribution;

  public AbstractContributionTemplateUserNotificationBuilder(final C resource) {
    super(resource);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected final void performTemplateData(final String language, final C contribution,
      final SilverpeasTemplate template) {
    localizedContribution = LocalizedContribution.from(contribution, language);
    final String contributionType = localizedContribution.getContributionType();
    final String title = getTitle(language);
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("contribution", localizedContribution);
    template.setAttribute("contributionType_" + contributionType, contributionType);
    template.setAttribute("contributionName", Encode.forHtml(localizedContribution.getTitle()));
    template.setAttribute("senderName", getSenderName());
    performTemplateData((C) localizedContribution, template);
  }

  /**
   * Gets the sender name from {@link #getSender()} method.
   * <p>This method can be overridden if getting sender name from {@link #getSender()} method is not
   * satisfying.</p>
   * @return the sender name as string.
   */
  protected String getSenderName() {
    return isDefined(getSender()) ? User.getById(getSender()).getDisplayedName() : "";
  }

  /**
   * Performs additional template data.
   * <p>
   * Following attributes have already been set:</p>
   * <ul>
   * <li>{@code contribution} with instance of {@link LocalizedContribution}</li>
   * <li>{@code contributionName} from the contribution title</li>
   * <li>{@code senderName} from {@link #getSenderName()} method</li>
   * </ul>
   * @param localizedContribution the localized contribution.
   * @param template the current localized template.
   */
  protected void performTemplateData(final C localizedContribution,
      final SilverpeasTemplate template) {
  }

  @Override
  protected final void performNotificationResource(final String language, final C contribution,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(localizedContribution.getTitle());
    notificationResourceData.setResourceDescription(localizedContribution.getDescription());
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getIdentifier().getComponentInstanceId();
  }
}