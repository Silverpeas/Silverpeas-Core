/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

package org.silverpeas.core.reminder;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionLocalizationBundle;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.builder
    .AbstractContributionTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.reminder.usernotification.ReminderUserNotificationSender;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.filter.FilterByType;

import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static org.silverpeas.core.reminder.ReminderSettings.getMessagesIn;
import static org.silverpeas.core.util.DateUtil.formatDateAndTime;

/**
 * @author silveryocha
 */
@Singleton
public class DefaultReminderUserNotificationSender implements ReminderUserNotificationSender {

  @Override
  public void send(final Reminder reminder) {
    new ContributionReminderUserNotification(reminder).build().send();
  }

  /**
   * @author silveryocha
   */
  private static class ContributionReminderUserNotification
      extends AbstractContributionTemplateUserNotificationBuilder<Contribution> {

    private final Reminder reminder;
    private final User receiver;
    private final Supplier<IllegalArgumentException> notHandledReminderType;
    private final OffsetDateTime reminderContributionDate;

    @SuppressWarnings("ConstantConditions")
    private ContributionReminderUserNotification(final Reminder reminder) {
      super(reminder.getContribution());
      this.reminder = reminder;
      this.receiver = User.getById(reminder.getUserId());
      notHandledReminderType = () -> new IllegalArgumentException(
          "Reminder type " + this.reminder.getClass() + " is not handled");
      reminderContributionDate =  new FilterByType(getResource())
          .matchFirst(DurationReminder.class::equals, r -> {
            DurationReminder durationReminder = (DurationReminder) r;
            return durationReminder.getTriggeringDate()
                .plus(durationReminder.getDuration(), durationReminder.getTimeUnit().toChronoUnit())
                .minusMinutes(5);
          })
          .matchFirst(DateTimeReminder.class::equals, r -> {
            DateTimeReminder dateTimeReminder = (DateTimeReminder) r;
            return dateTimeReminder.getTriggeringDate();
          })
          .result()
          .orElseThrow(notHandledReminderType);
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
      final String contributionTitle = new FilterByType(getResource())
          .matchFirst(DurationReminder.class::equals, r -> {
            DurationReminder durationReminder = (DurationReminder) r;
            return ContributionLocalizationBundle
                .getByInstanceAndLanguage(getResource(), language)
                .getUiMessageTitleByTypeAndProperty(durationReminder.getContributionProperty());
          })
          .matchFirst(DateTimeReminder.class::equals, r -> ContributionLocalizationBundle
              .getByInstanceAndLanguage(getResource(), language)
              .getUiMessageTitleByType())
          .result()
          .orElseThrow(notHandledReminderType);
      final String formattedReminderContributionDate = formatDateAndTime(reminderContributionDate, language);
      template.setAttribute("contributionTitle", contributionTitle);
      template.setAttribute("reminderContributionDate", formattedReminderContributionDate);
      getNotificationMetaData().addLanguage(language, getMessagesIn(language)
          .getStringWithParams("reminder.on", contributionTitle, formattedReminderContributionDate), "");
    }

    @Override
    protected String getTemplateFileName() {
      return new FilterByType(getResource())
          .matchFirst(DurationReminder.class::equals, r -> "reminder-duration")
          .matchFirst(DateTimeReminder.class::equals, r -> "reminder-datetime")
          .result()
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
  }
}
