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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.*;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;

import java.text.ParseException;
import java.time.LocalDate;

import static org.silverpeas.core.admin.AdminSettings.*;
import static org.silverpeas.core.admin.AdminSettings.getDeletionOfRemovedUsersCron;
import static org.silverpeas.core.admin.AdminSettings.isAutomaticDeletionOfRemovedUsersEnabled;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;
import static org.silverpeas.core.util.DateUtil.toLocalDate;

/**
 * Batch in charge of the deletion of removed users.
 * @author silveryocha
 */
@Service
public class DeleteRemovedUsersScheduler extends SchedulingInitializer {

  protected static final String JOB_NAME = "DeleteRemovedUsersJob";
  private final Job job = new DeleteRemovedUsersJob();

  @NonNull
  @Override
  protected String getCron() {
    return getDeletionOfRemovedUsersCron();
  }

  @NonNull
  @Override
  protected Job getJob() {
    return job;
  }

  @Override
  protected boolean isSchedulingEnabled() {
    return isAutomaticDeletionOfRemovedUsersEnabled();
  }

  private static class DeleteRemovedUsersJob extends Job {

    DeleteRemovedUsersJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      if (isAutomaticDeletionOfRemovedUsersEnabled()) {
        final Administration administration = Administration.get();
        for (final Domain domain : getOrganisationController().getAllDomains()) {
          try {
            for (final User user : administration.getRemovedUsers(domain.getId())) {
              final LocalDate stateSaveDayDateWithDelay = toLocalDate(user.getStateSaveDate())
                  .plusDays(getDeletionOfRemovedUsersDayDelay());
              final LocalDate now = LocalDate.now();
              if (stateSaveDayDateWithDelay.isBefore(now) ||
                  stateSaveDayDateWithDelay.isEqual(now)) {
                administration.deleteUser(user.getId());
              }
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        }
      }
    }
  }
}
