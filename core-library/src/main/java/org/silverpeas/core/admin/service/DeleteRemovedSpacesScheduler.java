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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.SchedulingInitializer;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.silverpeas.core.admin.AdminSettings.*;
import static org.silverpeas.core.util.DateUtil.toLocalDate;

/**
 * Batch in charge of the deletion of removed spaces.
 *
 * @author mmoquillon
 */
@Service
public class DeleteRemovedSpacesScheduler extends SchedulingInitializer {

  protected static final String JOB_NAME = "DeleteRemovedSpacesJob";
  private final Job job = new DeleteRemovedSpacesJob();

  @Inject
  private Administration admin;

  @NonNull
  @Override
  protected String getCron() {
    return getDeletionOfRemovedSpacesCron();
  }

  @NonNull
  @Override
  protected Job getJob() {
    return job;
  }

  @Override
  protected boolean isSchedulingEnabled() {
    return isAutomaticDeletionOfRemovedSpacesEnabled();
  }

  private class DeleteRemovedSpacesJob extends Job {

    DeleteRemovedSpacesJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      if (isAutomaticDeletionOfRemovedSpacesEnabled()) {
        try {
          var allRemovedSpaces = admin.getRemovedSpaces();
          LocalDate now = LocalDate.now();
          for (var removedSpace : allRemovedSpaces) {
            LocalDate stateSaveDayDateWithDelay = toLocalDate(removedSpace.getRemovalDate())
                .plusDays(getDeletionOfRemovedSpacesDayDelay());
            if (stateSaveDayDateWithDelay.isBefore(now) ||
                stateSaveDayDateWithDelay.isEqual(now)) {
              admin.deleteSpaceInstById(User.getSystemUser().getId(), removedSpace.getId(), true);
            }
          }
        } catch (AdminException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      }
    }
  }
}
