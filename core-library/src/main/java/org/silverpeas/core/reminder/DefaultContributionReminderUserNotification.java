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

package org.silverpeas.core.reminder;

import net.htmlparser.jericho.Source;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionLocalizationBundle;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.builder.AbstractContributionTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.filter.FilterByType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static org.silverpeas.core.reminder.ReminderSettings.getMessagesIn;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
public class DefaultContributionReminderUserNotification
    extends AbstractContributionTemplateUserNotificationBuilder<Contribution>
    implements FallbackToCoreTemplatePathBehavior {

  private final Reminder reminder;
  private final User receiver;
  private final ZoneId userZoneId;
  private final Supplier<IllegalArgumentException> notHandledReminderType;
  private final Temporal reminderContributionStart;
  private final Temporal reminderContributionEnd;
  private final String zoneIdIfDifferentToContribution;

  protected DefaultContributionReminderUserNotification(final Reminder reminder) {
    super(reminder.getContribution());
    this.reminder = reminder;
    this.receiver = User.getById(reminder.getUserId());
    this.userZoneId = receiver.getUserPreferences().getZoneId();
    notHandledReminderType = () -> new IllegalArgumentException(
        "Reminder type " + this.reminder.getClass() + " is not handled");
    reminderContributionStart = computeReminderContributionStart();
    reminderContributionEnd = computeReminderContributionEnd();
    zoneIdIfDifferentToContribution = computeZoneIdIfDifferentToContribution();
  }

  protected Temporal computeReminderContributionStart() {
    return normalizeTemporal(getScheduledDateTimeWithZeroDuration());
  }

  private Temporal getReminderContributionStart() {
    return reminderContributionStart;
  }

  protected Temporal computeReminderContributionEnd() {
    return null;
  }

  private Temporal getReminderContributionEnd() {
    return reminderContributionEnd;
  }

  private String computeZoneIdIfDifferentToContribution() {
    if (!getZoneIdForNormalization().equals(userZoneId)) {
      return getZoneIdForNormalization().getId();
    }
    return null;
  }

  /**
   * Indicates if the contribution has a duration on several days.
   * @return true if it is, false otherwise.
   */
  private boolean isReminderContributionOnSeveralDays() {
    return getReminderContributionEnd() != null &&
        (getReminderContributionStart().get(ChronoField.YEAR) !=
            getReminderContributionEnd().get(ChronoField.YEAR) ||
            getReminderContributionStart().get(ChronoField.MONTH_OF_YEAR) !=
                getReminderContributionEnd().get(ChronoField.MONTH_OF_YEAR) ||
            getReminderContributionStart().get(ChronoField.DAY_OF_MONTH) !=
                getReminderContributionEnd().get(ChronoField.DAY_OF_MONTH));
  }

  @SuppressWarnings("ConstantConditions")
  protected Temporal getScheduledDateTimeWithZeroDuration() {
    return new FilterByType(reminder)
        .matchFirst(DurationReminder.class::equals, r -> {
          DurationReminder durationReminder = (DurationReminder) r;
          return durationReminder.getScheduledDateTime()
              .plus(durationReminder.getDuration(), durationReminder.getTimeUnit().toChronoUnit());
        })
        .matchFirst(DateTimeReminder.class::equals, r ->  {
          DateTimeReminder dateTimeReminder = (DateTimeReminder) r;
          return dateTimeReminder.getScheduledDateTime();
        })
        .result()
        .orElseThrow(notHandledReminderType);
  }

  /**
   * Gets the {@link ZoneId} instance which must have to be used in date normalization.
   * @return a {@link ZoneId} instance.
   */
  protected ZoneId getZoneIdForNormalization() {
    return ZoneId.systemDefault();
  }

  /**
   * Normalizes the temporal for user notification according to the result of
   * {@link #getZoneIdForNormalization()} method.
   * @param temporal a temporal.
   * @return a {@link LocalDate} or {@link ZonedDateTime}.
   */
  protected Temporal normalizeTemporal(final Temporal temporal) {
    return new FilterByType(temporal)
        .matchFirst(LocalDate.class::equals, Temporal.class::cast)
        .matchFirst(OffsetDateTime.class::equals,
            d -> ((OffsetDateTime) d).atZoneSameInstant(getZoneIdForNormalization()))
        .matchFirst(ZonedDateTime.class::equals,
            d -> ((ZonedDateTime) d).withZoneSameInstant(getZoneIdForNormalization())).result()
        .orElseThrow(() -> new SilverpeasRuntimeException("not handled type"));
  }

  protected User getReceiver() {
    return receiver;
  }

  /**
   * The title is built by {@link #getTitle()}
   * @return null value.
   */
  @Override
  protected String getBundleSubjectKey() {
    return null;
  }

  @Override
  protected void performTemplateData(final Contribution localizedContribution,
      final SilverpeasTemplate template) {
    super.performTemplateData(localizedContribution, template);
    final String language = ((LocalizedContribution) localizedContribution).getLanguage();
    final NotificationTemporal start = new NotificationTemporal(reminderContributionStart,
        userZoneId, language);
    final NotificationTemporal end = new NotificationTemporal(reminderContributionEnd,
        userZoneId, language);
    final String reminderTitle = new FilterByType(reminder)
        .matchFirst(DurationReminder.class::equals, r -> {
          DurationReminder durationReminder = (DurationReminder) r;
          return getUnitBundle(language)
              .getStringWithParams(durationReminder.getTimeUnit() + ".precise",
                  durationReminder.getDuration());
        }).result().orElse(StringUtil.EMPTY);
    final String contributionTitle = new FilterByType(reminder)
        .matchFirst(DurationReminder.class::equals, r -> {
          DurationReminder durationReminder = (DurationReminder) r;
          return ContributionLocalizationBundle.getByInstanceAndLanguage(getResource(), language)
              .getUiMessageTitleByTypeAndProperty(durationReminder.getContributionProperty());
        })
        .matchFirst(DateTimeReminder.class::equals,
            r -> ContributionLocalizationBundle.getByInstanceAndLanguage(getResource(), language)
                .getUiMessageTitleByType())
        .result()
        .orElseThrow(notHandledReminderType);
    final String contributionTitleText = new Source(contributionTitle).getTextExtractor()
        .toString();
    template.setAttribute("reminderTitle", reminderTitle);
    template.setAttribute("contributionTitle", contributionTitle);
    template.setAttribute("reminderContributionStart", start);
    template.setAttribute("reminderContributionEnd", end);
    template.setAttribute("reminderContributionSeveralDays", isReminderContributionOnSeveralDays());
    template.setAttribute("reminderContributionZoneId", zoneIdIfDifferentToContribution);
    getNotificationMetaData().addLanguage(language, getMessagesIn(language)
        .getStringWithParams("reminder.on", contributionTitleText,
            formatTemporalDataOfNotificationTitle(start, end)), null);
  }

  private String formatTemporalDataOfNotificationTitle(final NotificationTemporal start,
      final NotificationTemporal end) {
    final StringBuilder sb = new StringBuilder();
    sb.append(start.getDate());
    if (end.isDateExisting()) {
      if (isReminderContributionOnSeveralDays()) {
        sb.append(" - ");
        sb.append(end.getDate());
      } else if (end.isTimeExisting()) {
        sb.append(" - ");
        sb.append(end.getDayTime());
      }
    }
    if (isDefined(zoneIdIfDifferentToContribution)) {
      sb.append(" (").append(zoneIdIfDifferentToContribution).append(")");
    }
    return sb.toString();
  }

  @Override
  protected String getTemplateFileName() {
    return new FilterByType(reminder)
        .matchFirst(DurationReminder.class::equals, r -> "reminder-duration")
        .matchFirst(DateTimeReminder.class::equals, r -> "reminder-datetime").result()
        .orElseThrow(notHandledReminderType);
  }

  @Override
  protected String getTemplatePath() {
    return "reminder";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }

  @Override
  protected String getSender() {
    // Empty is here returned, because the notification is from the platform and not from an
    // other user.
    return "";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singleton(receiver.getId());
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  private LocalizationBundle getUnitBundle(final String language) {
    return ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.util", language);
  }
}
