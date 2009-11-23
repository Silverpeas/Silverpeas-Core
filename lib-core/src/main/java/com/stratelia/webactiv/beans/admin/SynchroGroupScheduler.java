/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SynchroGroupScheduler implements SchedulerEventHandler {

  public static final String ADMINSYNCHROGROUP_JOB_NAME = "AdminSynchroGroupJob";

  private List synchronizedGroupIds = null;
  private Admin admin = null;

  public void initialize(String cron, Admin admin, List synchronizedGroupIds) {
    try {
      this.admin = admin;
      this.synchronizedGroupIds = synchronizedGroupIds;

      Vector jobList = SimpleScheduler.getJobList(this);
      if (jobList != null && jobList.size() > 0)
        SimpleScheduler.removeJob(this, ADMINSYNCHROGROUP_JOB_NAME);
      SimpleScheduler.getJob(this, ADMINSYNCHROGROUP_JOB_NAME, cron, this,
          "doSynchroGroup");
    } catch (Exception e) {
      SilverTrace.error("admin", "SynchroGroupScheduler.initialize()",
          "importExport.EX_CANT_INIT_SCHEDULED_IMPORT", e);
    }
  }

  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("admin",
            "SynchroGroupScheduler.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was not successfull");
        break;

      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("admin",
            "SynchroGroupScheduler.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was successfull");
        break;

      default:
        SilverTrace.error("admin",
            "SynchroGroupScheduler.handleSchedulerEvent", "Illegal event type");
        break;
    }
  }

  public void doSynchroGroup(Date date) {
    SilverTrace.info("admin", "SynchroGroupScheduler.doSynchroGroup()",
        "root.MSG_GEN_ENTER_METHOD");

    SynchroGroupReport.startSynchro();

    String groupId = null;
    for (int i = 0; synchronizedGroupIds != null
        && i < synchronizedGroupIds.size(); i++) {
      groupId = (String) synchronizedGroupIds.get(i);
      try {
        admin.synchronizeGroupByRule(groupId, true);
      } catch (AdminException e) {
        SilverTrace.error("admin", "SynchroGroupScheduler.doSynchroGroup",
            "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      }
    }

    SynchroGroupReport.stopSynchro();

    SilverTrace.info("admin", "SynchroGroupScheduler.doScheduledImport()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void addGroup(String groupId) {
    if (synchronizedGroupIds == null)
      synchronizedGroupIds = new ArrayList();
    synchronizedGroupIds.add(groupId);
  }

  public void removeGroup(String groupId) {
    if (synchronizedGroupIds != null)
      synchronizedGroupIds.remove(groupId);
  }
}