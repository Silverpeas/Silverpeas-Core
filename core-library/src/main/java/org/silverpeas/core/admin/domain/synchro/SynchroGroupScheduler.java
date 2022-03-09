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
package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.synchronizedSet;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

public class SynchroGroupScheduler implements SchedulerEventListener {

  public static final String ADMIN_SYNCHRO_GROUP_JOB_NAME = "AdminSynchroGroupJob";
  private final Set<String> synchronizedGroupIds = synchronizedSet(new HashSet<>());

  /**
   * Initializing the JOB to schedule it according the CRON data with given synchronized groups.
   * @param cron CRON data.
   * @param synchronizedGroupIds identifiers of groups that MUST be updated according theirs
   * synchronization rules (groups that {@link Group#isSynchronized()} returns true).
   */
  public void initialize(String cron, List<String> synchronizedGroupIds) {
    try {
      this.synchronizedGroupIds.clear();
      this.synchronizedGroupIds.addAll(synchronizedGroupIds);
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(ADMIN_SYNCHRO_GROUP_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(ADMIN_SYNCHRO_GROUP_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  protected void doSynchroGroup() {
    SynchroGroupReport.startSynchro();
    new ArrayList<>(synchronizedGroupIds).forEach(i -> {
      try {
        Administration.get().synchronizeGroupByRule(i, true);
      } catch (AdminException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    });
    SynchroGroupReport.stopSynchro();
  }

  /**
   * Updates the context of the scheduler with the given group data.
   * <p>
   *   If the group is a synchronized one, then it will be add to the list of group to perform
   *   update on. If it is not synchronized, then it is removed from this list.
   * </p>
   * @param group data representing a group.
   * @throws IllegalArgumentException if group identifier does not exists into data.
   */
  public void updateContextWith(final Group group) {
    final String groupId = group.getId();
    if (isNotDefined(groupId)) {
      throw new IllegalArgumentException("Missing group identifier");
    }
    if (group.isSynchronized()) {
      synchronizedGroupIds.add(groupId);
    } else {
      synchronizedGroupIds.remove(groupId);
    }
  }

  /**
   * Removes the given group from the context of the scheduler.
   * @param group data representing a group.
   * @throws IllegalArgumentException if group identifier does not exists into data.
   */
  public void removeFromContext(final Group group) {
    final String groupId = group.getId();
    if (isNotDefined(groupId)) {
      throw new IllegalArgumentException("Missing group identifier");
    }
    synchronizedGroupIds.remove(groupId);
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doSynchroGroup();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    final String jobName = anEvent.getJobExecutionContext().getJobName();
    SilverLogger.getLogger(this).error("The domain synchronization job {0} failed!", jobName);
  }
}
