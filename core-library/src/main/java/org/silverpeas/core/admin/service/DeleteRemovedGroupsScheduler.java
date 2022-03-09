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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.logging.SilverLogger;

import java.time.LocalDate;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.silverpeas.core.admin.AdminSettings.*;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;
import static org.silverpeas.core.util.DateUtil.toLocalDate;

/**
 * Batch in charge of the deletion of removed groups.
 * @author silveryocha
 */
@Service
public class DeleteRemovedGroupsScheduler implements Initialization {

  protected static final String JOB_NAME = "DeleteRemovedGroupsJob";

  @Override
  public void init() throws Exception {
    if (isAutomaticDeletionOfRemovedGroupsEnabled()) {
      final Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(JOB_NAME);
      scheduler.scheduleJob(new DeleteRemovedGroupsJob(),
          JobTrigger.triggerAt(getDeletionOfRemovedGroupsCron()));
    }
  }

  private static class DeleteRemovedGroupsJob extends Job {

    DeleteRemovedGroupsJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      if (isAutomaticDeletionOfRemovedGroupsEnabled()) {
        final Administration administration = Administration.get();
        concat(of(Domain.MIXED_DOMAIN_ID),
            of(getOrganisationController().getAllDomains()).map(Domain::getId)).forEach(i -> {
          try {
            for (final Group group : administration.getRemovedGroups(i)) {
              final LocalDate stateSaveDayDateWithDelay = toLocalDate(group.getStateSaveDate())
                  .plusDays(getDeletionOfRemovedGroupsDayDelay());
              final LocalDate now = LocalDate.now();
              if (stateSaveDayDateWithDelay.isBefore(now) ||
                  stateSaveDayDateWithDelay.isEqual(now)) {
                administration.deleteGroupById(group.getId());
              }
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        });
      }
    }
  }
}
