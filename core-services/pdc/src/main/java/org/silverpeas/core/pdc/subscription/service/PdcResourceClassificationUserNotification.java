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
package org.silverpeas.core.pdc.subscription.service;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.contentcontainer.content.ManagedContribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.kernel.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.util.URLUtil.getSearchResultURL;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * The notification sent to the subscribers to position criteria on the PdC when a contribution is
 * classified on a position satisfying these criteria, out of any event on the contribution
 * itself (for a contribution that is created, modified or published, the subscribers on the PdC
 * are notified with the other subscribers by the application that manages the contribution).
 * <p>
 * It is a notification to subscribers as any other one: its message is rendered from a template,
 * that can be customized for a given application, and its sending can be delayed according to the
 * preferences of the subscribers.
 * </p>
 */
public class PdcResourceClassificationUserNotification
    extends AbstractTemplateUserNotificationBuilder<ManagedContribution>
    implements UserSubscriptionNotificationBehavior, FallbackToCoreTemplatePathBehavior {

  private static final String BUNDLE_PATH = "org.silverpeas.pdcSubscription.multilang.pdcsubscription";
  private static final String SUBJECT_KEY = "standartMessage";
  private static final String TEMPLATE_PATH = "pdcSubscription";
  private static final String TEMPLATE_FILE_NAME = "classified";

  private final PdcSubscriptionPositionCriteria criteria;
  private final Collection<String> recipientIds;
  private final User author;

  /**
   * Constructs the notification about the classification of the specified contribution.
   * @param criteria the position criteria on the PdC satisfied by the classification of the
   * contribution.
   * @param recipientIds the identifiers of the subscribers to these position criteria to notify.
   * @param contribution the classified contribution.
   * @param author the user that has classified the contribution. Null if the classification
   * isn't done on behalf of a user.
   */
  public PdcResourceClassificationUserNotification(final PdcSubscriptionPositionCriteria criteria,
      final Collection<String> recipientIds, final ManagedContribution contribution,
      final User author) {
    super(contribution);
    this.criteria = criteria;
    this.recipientIds = List.copyOf(recipientIds);
    this.author = author;
  }

  /**
   * Gets the position criteria on the PdC the notification is about.
   * @return a {@link PdcSubscriptionPositionCriteria} instance.
   */
  public PdcSubscriptionPositionCriteria getPdcSubscriptionPositionCriteria() {
    return criteria;
  }

  @Override
  protected @NonNull String getLocalizationBundlePath() {
    return BUNDLE_PATH;
  }

  /**
   * The subject of the notification is always the one defined by the PdC, whatever the
   * application that manages the classified contribution.
   */
  @Override
  protected String getTitle(final String language) {
    return getBundle(language).getString(SUBJECT_KEY);
  }

  @Override
  protected String getTemplatePath() {
    return TEMPLATE_PATH;
  }

  @Override
  protected String getTemplateFileName() {
    return TEMPLATE_FILE_NAME;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.CLASSIFIED;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  /**
   * The sender is the author of the classification. When the classification isn't done on behalf
   * of a user, the subscribers are notified on behalf of nobody.
   */
  @Override
  protected String getSender() {
    return author == null ? "" : author.getId();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return recipientIds;
  }

  /**
   * Only the subscribers to the position criteria the notification is about are notified, not
   * all those concerned by the whole classification of the contribution.
   */
  @Override
  protected Optional<ContributionIdentifier> getSubscribedContribution() {
    return Optional.empty();
  }

  @Override
  protected void performTemplateData(final String language, final ManagedContribution contribution,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("centerOfInterest", Encode.forHtml(criteria.getName()));
    template.setAttribute("contributionName", Encode.forHtml(contribution.getName(language)));
  }

  @Override
  protected void performNotificationResource(final String language,
      final ManagedContribution contribution,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(contribution.getName(language));
    notificationResourceData.setResourceDescription(contribution.getDescription(language));
  }

  @Override
  protected String getResourceURL(final ManagedContribution contribution) {
    return defaultStringIfNotDefined(getSearchResultURL(contribution), null);
  }
}
