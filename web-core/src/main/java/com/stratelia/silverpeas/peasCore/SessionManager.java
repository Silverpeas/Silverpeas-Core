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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.silverpeas.peasCore;

import com.silverpeas.util.FileUtil;
import com.silverpeas.scheduler.JobExecutionContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;

import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsManager;

import com.stratelia.silverpeas.silvertrace.SilverLog;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.servlets.LogoutServlet;

import com.stratelia.webactiv.util.ResourceLocator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Class declaration This object is a singleton used by LoginServlet : when the user log in,
 * ComponentRequestRouter : when the user access a component. It provides functions to manage the
 * sessions, to write a log journal and getFactory informations about the logged users.
 * @author Marc Guillemin
 */
public class SessionManager
    implements SchedulerEventListener, SessionManagement {
  // Global constants

  public static final SimpleDateFormat NOTIFY_DATE_FORMAT = new SimpleDateFormat(
      " HH:mm (dd/MM/yyyy) ");
  // Local constants
  private static final String SESSION_MANAGER_JOB_NAME = "SessionManagerScheduler";
  // Singleton implementation
  private static SessionManager myInstance = null;
  // Max session duration in ms
  private long userSessionTimeout = 600000; // 10mn
  private long adminSessionTimeout = 1200000; // 20mn
  // Timestamp of execution of the scheduled job in ms
  private long scheduledSessionManagementTimeStamp = 60000; // 1mn
  // Client refresh intervall in ms (see Clipboard Session Controller)
  private long maxRefreshInterval = 90000; // 1mn30
  // Contains all current sessions
  private Map<String, SessionInfo> userDataSessions = null;
  // Contains the session when notified
  private List<String> userNotificationSessions = null;
  private ResourceLocator m_Multilang = null;
  private SilverStatisticsManager myStatisticsManager = null;

  /**
   * Prevent the class from being instantiate (private)
   */
  private SessionManager() {
  }

  /**
   * Init attributes
   */
  private synchronized void initSessionManager() {
    ResourceBundle resources = null;

    try {
      // init Hashtables
      userDataSessions = new HashMap<String, SessionInfo>(100);
      userNotificationSessions = new ArrayList<String>(100);

      // init maxRefreshInterval : add 60 seconds delay because of network
      // traffic
      ResourceLocator rl = new ResourceLocator(
          "com.stratelia.webactiv.clipboard.settings.clipboardSettings", "");
      maxRefreshInterval = (60 + Long.parseLong(rl.getString("IntervalInSec"))) * 1000;

      // init userSessionTimeout and scheduledSessionManagementTimeStamp
      resources = FileUtil.loadBundle("com.stratelia.silverpeas.peasCore.SessionManager",
          new Locale("", ""));
      String language = resources.getString("language");
      if ((language == null) || (language.length() <= 0)) {
        language = "fr";
      }
      m_Multilang = new ResourceLocator(
          "com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle",
          language);

      scheduledSessionManagementTimeStamp = Long.parseLong(resources.getString(
          "scheduledSessionManagementTimeStamp"))
          * (long) 60 * (long) 1000; // Translate from mn tu ms
      userSessionTimeout = Long.parseLong(resources.getString("userSessionTimeout"))
          * (long) 60 * (long) 1000; // Translate from mn tu ms
      if (scheduledSessionManagementTimeStamp > userSessionTimeout) {
        scheduledSessionManagementTimeStamp = userSessionTimeout;
      }
      adminSessionTimeout = Long.parseLong(resources.getString("adminSessionTimeout"))
          * (long) 60 * (long) 1000; // Translate from mn tu ms
      if (scheduledSessionManagementTimeStamp > adminSessionTimeout) {
        scheduledSessionManagementTimeStamp = adminSessionTimeout;
      }
      initSchedulerTimeStamp();

      myStatisticsManager = SilverStatisticsManager.getInstance();

      // Writing journal
      SilverLog.logConnexion("SessionManager starting", "TimeStamp="
          + Long.toString(scheduledSessionManagementTimeStamp / 60000),
          "UserSessionTimeout=" + Long.toString(userSessionTimeout / 60000)
          + " adminSessionTimeout="
          + Long.toString(adminSessionTimeout / 60000));

    } catch (Exception ex) {
      SilverTrace.fatal("peasCore", "SessionManager.getInstance",
          "root.EX_CLASS_NOT_INITIALIZED", ex);
    }
  }

  /**
   * SessionManager is a singleton
   * @return the instance of SessionManager
   */
  public static SessionManager getInstance() {
    synchronized (SessionManager.class) {
      if (myInstance == null) {
        myInstance = new SessionManager();
        // Init ONLY when myIntance is not null
        myInstance.initSessionManager();
      }
    }
    return myInstance;
  }

  /**
   * Set the server last acessed time by the user. Used to verify if the session duration has
   * expired (because of timeout).
   * @param session
   * @see ComponentRequestRouter and ClipboardRequestRouter
   */
  public synchronized void setLastAccess(HttpSession session) {
    SessionInfo si = userDataSessions.get(session.getId());
    if (si != null) {
      si.updateLastAccess();
    } else {
      SilverTrace.debug("peasCore", "SessionManager.setLastAccess",
          "L'objet de session n'a pas ete retrouve dans la variable userDataSessions !!! - sessionId = "
          + session.getId());
    }
    userNotificationSessions.remove(session.getId());
  }

  /**
   * This method creates a job that executes the "doSessionManagement" method. That job fires a
   * SchedulerEvent of the type 'EXECUTION_NOT_SUCCESSFULL', or 'EXECUTION_SUCCESSFULL'. The
   * timestamp (every time the job will execute) settings is given by minutes and logically must be
   * less than the session timeout.
   * @throws SchedulerException
   * @see SimpleScheduler for more infos
   */
  public void initSchedulerTimeStamp() throws SchedulerException {
    int minute = (int) (scheduledSessionManagementTimeStamp / (long) 60000);

    SilverTrace.debug("peasCore", "SessionManager.initSchedulerTimeStamp",
        "scheduledSessionManagementTimeStamp in minutes=" + minute);
    if (minute < 0 || minute > 59) {
      throw new SchedulerException(
          "SchedulerMethodJob.setParameter: minute value is out of range");
    }

    SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();

    // Remove previous scheduled job
    scheduler.unscheduleJob(SESSION_MANAGER_JOB_NAME);

    // Create new scheduled job
    JobTrigger trigger;
    try {
      trigger = computeJobTrigger(minute);
    } catch (ParseException ex) {
      throw new SchedulerException(ex.getMessage(), ex);
    }
    scheduler.scheduleJob(manageSession(), trigger, this);
//    SimpleScheduler.scheduleJob(myInstance, SESSION_MANAGER_JOB_NAME, startMinutes,
//        null, null, null, null, myInstance, "doSessionManagement");
  }

  /**
   * This method stores the users's sessions, initialises time counters and log session's data. The
   * stored session may become invalid (if the user close the browser, this class is not notified).
   * @param session the session to store
   * @param request
   * @param controller
   * @see closeSession
   */
  public synchronized void addSession(HttpSession session,
      HttpServletRequest request,
      MainSessionController controller) {
    String anIP = request.getRemoteHost();
    try {
      SilverTrace.debug("peasCore", "SessionManager.addSession", "sessionId="
          + session.getId() + " - userId : " + controller.getUserId());
      // Eventually remove the precedent session
      removeInQueueMessages(controller.getCurrentUserDetail().getId(), session.getId());
      removeSession(session);
      SessionInfo si = new SessionInfo(session, anIP, controller.getCurrentUserDetail());
      userDataSessions.put(si.getSessionId(), si);
      // Writing journal
      SilverLog.logConnexion("login", si.getUserHostIP(), si.getLog());
    } catch (Exception ex) {
      // because no journal writing
      SilverTrace.error("peasCore", "SessionManager.addSession",
          "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * Remove a session and log session's data.
   * @param session
   * @see LogoutServlet
   */
  public void removeSession(HttpSession session) {
    closeSession(session.getId());
  }

  @Override
  public synchronized void closeSession(String sessionId) {
    SessionInfo si = userDataSessions.get(sessionId);
    if (si != null) {
      removeSession(si);
      si = null;
    } else {
      SilverTrace.debug(
          "peasCore",
          "SessionManager.removeSession",
          "L'objet de session n'a pas ete retrouve dans la variable userDataSessions !!! (sessionId = "
          + sessionId + ")");
    }
  }

  protected synchronized void removeSession(SessionInfo si) {
    try {
      SilverTrace.debug("peasCore", "SessionManager.removeSession",
          "START on session=" + si.getSessionId() + " - " + si.getLog());
      // Writing journal
      SilverLog.logConnexion("logout", si.getUserHostIP(), si.getLog());
      // Notify statistics
      Date now = new java.util.Date();
      long duration = now.getTime() - si.getStartDate();
      myStatisticsManager.addStatConnection(si.getUserDetail().getId(), now, 1, duration);

      // Delete in wait server messages corresponding to the session to
      // invalidate
      removeInQueueMessages(si.getUserDetail().getId(), si.getSessionId());

      // Remove the session from lists
      userDataSessions.remove(si.getSessionId());
      userNotificationSessions.remove(si.getSessionId());
      SilverTrace.debug("peasCore", "SessionManager.removeSession",
          "DONE on session=" + si.getSessionId() + " - " + si.getLog());

      si.cleanSession();
    } catch (Exception ex) {
      // because no journal writing
      SilverTrace.error("peasCore", "SessionManager.removeSession",
          "root.EX_NO_MESSAGE", ex);
    }
  }

  @Override
  public synchronized SessionInfo getSessionInfo(String sessionId) {
    return userDataSessions.get(sessionId);
  }

  /**
   * -------------------------------------------------------------------------- pop del
   */
  private void removeInQueueMessages(String userId,
      String sessionId) {
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();
      String whereClause;

      if (StringUtil.isDefined(sessionId)) {
        dao = SilverpeasBeanDAOFactory.getDAO(
            "com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
        whereClause = " USERID=" + userId + " AND SESSIONID='" + sessionId
            + "'";
        dao.removeWhere(pk, whereClause);
      }

      // Remove "end of session" messages
      dao = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
      whereClause = "userid=" + userId + " AND senderid='-1'";
      dao.removeWhere(pk, whereClause);
      // TODO : Remove bodies as longText
    } catch (PersistenceException e) {
      SilverTrace.error("peasCore", "SessionManager.removeInQueueMessages()",
          "root.EX_NO_MESSAGE", "USERID=" + userId + " AND SESSIONID = "
          + sessionId, e);
    }
  }

  public synchronized void setIsAlived(HttpSession session) {
    SessionInfo si = userDataSessions.get(session.getId());
    if (si != null) {
      si.updateIsAlive();
    } else {
      SilverTrace.debug("peasCore", "SessionManager.setIsAlived",
          "L'objet de session n'a pas ete retrouve dans la variable userDataSessions !!! - sessionId = "
          + session.getId());
    }
  }

  /**
   * Gets all the connected users and the duration of their session.
   * @return
   */
  public synchronized Collection<SessionInfo> getConnectedUsersList() {
    return userDataSessions.values();
  }

  /**
   * Gets all the connected users and the duration of their session.
   * @author dlesimple
   * @return Collection of SessionInfo
   */
  @Override
  public Collection<SessionInfo> getDistinctConnectedUsersList() {
    Map<String, SessionInfo> distinctConnectedUsersList = new HashMap<String, SessionInfo>();
    Collection<SessionInfo> sessionsInfos = getConnectedUsersList();
    for (SessionInfo si : sessionsInfos) {
      String userLogin = si.getUserDetail().getLogin();
      // keep users with distinct login
      if (!distinctConnectedUsersList.containsKey(userLogin)
          && !si.getUserDetail().isAccessGuest()) {
        distinctConnectedUsersList.put(userLogin, si);
      }
    }
    return distinctConnectedUsersList.values();
  }

  /**
   * Gets number of connected users
   * @author dlesimple
   * @return nb of connected users
   */
  @Override
  public int getNbConnectedUsersList() {
    return getDistinctConnectedUsersList().size();
  }

  /**
   * This method is called every scheduledSessionManagementTimeStamp minute by the scheduler, it
   * notify the user when timeout has expired and then invalidates the session if the user has not
   * accessed the server. The maximum minutes duration of session before invalidation is
   * userSessionTimeout + scheduledSessionManagementTimeStamp.
   * @param currentDate the date when the method is called by the scheduler
   * @see SimpleScheduler for parameters, addSession, setLastAccess
   */
  public synchronized void doSessionManagement(Date currentDate) {
    try {
      long currentTime = currentDate.getTime();

      Collection<SessionInfo> allSI = userDataSessions.values();
      for (SessionInfo si : allSI) {
        long userSessionTimeoutMillis = (si.getUserDetail().isAccessAdmin()) ? adminSessionTimeout
            : userSessionTimeout;
        // Has the session expired (timeout)
        if (currentTime - si.getLastAccessDate() >= userSessionTimeoutMillis) {
          long duration = currentTime - si.getIsAliveDate();
          // Has the user been notified (only for living client)
          if ((duration < maxRefreshInterval)
              && !userNotificationSessions.contains(si.getSessionId())) {
            try {
              notifyEndOfSession(si.getUserDetail().getId(), currentTime
                  + scheduledSessionManagementTimeStamp, si.getSessionId());
            } catch (NotificationManagerException ex) {
              SilverTrace.error("peasCore",
                  "SessionManager.doSessionManagement",
                  "notificationManager.EX_CANT_SEND_USER_NOTIFICATION",
                  "sessionId=" + si.getSessionId() + " - user=" + si.getLog(), ex);
            } finally {
              // Add to the notifications
              userNotificationSessions.add(si.getSessionId());
            }
          } else {
            // Remove dead session or timeout with a notification
            removeSession(si);
          }
        } // if (hasSessionExpired )
      }
    } catch (Exception ex) {
      SilverTrace.error("peasCore", "SessionManager.doSessionManagement",
          "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * This method notify a user's end session
   * @param session the session to getFactory the user to notify
   * @endOfSession the time of the end of session (in milliseconds)
   * @see
   */
  private void notifyEndOfSession(String userId,
      long endOfSession,
      String sessionId) throws NotificationManagerException {
    SilverTrace.debug("peasCore", "SessionManager.notifyEndOfSession",
        "userId=" + userId + " sessionId=" + sessionId);
    String endOfSessionDate = NOTIFY_DATE_FORMAT.format(new Date(endOfSession));
    String msgTitle = m_Multilang.getString("EndOfSessionNotificationMsgTitle");
    msgTitle += endOfSessionDate;

    // Notify user the end of session
    NotificationSender notifSender = new NotificationSender(null);

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, msgTitle, m_Multilang.getString(
        "EndOfSessionNotificationMsgText"));
    notifMetaData.setSender(null);
    notifMetaData.setSessionId(sessionId);
    notifMetaData.addUserRecipient(userId);
    notifMetaData.setSource(m_Multilang.getString("administrator"));

    notifSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP,
        notifMetaData);
  }

  /**
   * This method remove and invalidates all sessions. The unique instance of the SessionManager will
   * be destroyed.
   */
  public synchronized void shutdown() {
    SilverTrace.debug("peasCore", "SessionManager.shutdown()", "");
    // Remove previous scheduled job
    SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();
    try {
      scheduler.unscheduleJob(SESSION_MANAGER_JOB_NAME);
    } catch (SchedulerException ex) {
      SilverTrace.error("peasCore", "SessionManager.shutdown", ex.getMessage(), ex);
    }
    Collection<SessionInfo> allSI = userDataSessions.values();
    for (SessionInfo si : allSI) {
      removeSession(si);
    }

    // Writing journal
    SilverLog.logConnexion("SessionManager shutdown", null, null);

    // Destroy the unique instance
    myInstance = null;
  }

  public synchronized void closeHttpSession(HttpSession session) {
    SilverTrace.debug("peasCore", "SessionManager.closeHttpSession",
        "sesionId=" + session.getId());
    SessionInfo si = userDataSessions.get(session.getId());
    if (si != null) {
      removeSession(si);
      si.terminateSession();
    } else {
      SilverTrace.debug("peasCore", "SessionManager.closeHttpSession",
          "L'objet de session n'a pas ete retrouve dans la variable userDataSessions !!!");
    }
  }

  private JobTrigger computeJobTrigger(int minute) throws ParseException {
    StringBuilder cronBuilder = new StringBuilder();
    if (60 % minute == 0) {
      cronBuilder.append("0");
    }
    for (int i = minute; i < 60; i += minute) {
      cronBuilder.append(",").append(i);
    }
    cronBuilder.append(" * * * ?");
    JobTrigger trigger;
    if (cronBuilder.toString().startsWith(",")) {
      trigger = JobTrigger.triggerAt(cronBuilder.substring(1));
    } else {
      trigger = JobTrigger.triggerAt(cronBuilder.toString());
    }
    return trigger;
  }

  /**
   * Gets the job that performs the session management.
   * @return the job for managing the session.
   */
  private Job manageSession() {
    return new Job(SESSION_MANAGER_JOB_NAME)  {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        Date date = context.getFireTime();
        doSessionManagement(date);
      }
    };
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("peasCore", "SessionManager.handleSchedulerEvent",
            "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is starting");
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("peasCore", "SessionManager.handleSchedulerEvent",
            "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("peasCore", "SessionManager.handleSchedulerEvent",
            "The job '" + anEvent.getJobExecutionContext().getJobName()
            + "' was not successfull");
  }

  /**
   * This operation is not implemented. Call the addSession method instead.
   * @param sessionInfo information about the session to open.
   * @return the session key.
   */
  @Override
  public String openSession(SessionInfo sessionInfo) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
