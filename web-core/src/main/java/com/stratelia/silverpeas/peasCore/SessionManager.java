/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore;

import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionValidationContext;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean;
import com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean;
import com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsManager;
import com.stratelia.silverpeas.silvertrace.SilverLog;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.servlets.LogoutServlet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Class declaration This object is a singleton used by SilverpeasSessionOpenener : when the user
 * log in, ComponentRequestRouter : when the user access a component. It provides functions to
 * manage the sessions, to write a log journal and getFactory informations about the logged users.
 *
 * @author Nicolas Eysseric
 */
@Named("sessionManagement")
public class SessionManager implements SchedulerEventListener, SessionManagement {

  private static final String NOTIFY_DATE_FORMAT = " HH:mm (dd/MM/yyyy) ";
  // Local constants
  private static final String SESSION_MANAGER_JOB_NAME = "SessionManagerScheduler";
  // Singleton implementation
  //private static SessionManager myInstance = null;
  // Max session duration in ms
  private long userSessionTimeout = 600000; // 10mn
  private long adminSessionTimeout = 1200000; // 20mn
  // Timestamp of execution of the scheduled job in ms
  private long scheduledSessionManagementTimeStamp = 60000; // 1mn
  // Client refresh intervall in ms (see Clipboard Session Controller)
  private long maxRefreshInterval = 90000; // 1mn30
  // Contains all current sessions
  private final Map<String, SessionInfo> userDataSessions = new HashMap<String, SessionInfo>(100);
  // Contains the session when notified
  private final List<String> userNotificationSessions = new ArrayList<String>(100);
  private ResourceLocator messages = null;
  @Inject
  private SilverStatisticsManager myStatisticsManager = null;
  @Inject
  private Scheduler scheduler;

  /**
   * Prevent the class from being instantiate (private)
   */
  private SessionManager() {
  }

  /**
   * Init attributes
   */
  @PostConstruct
  public void initSessionManager() {
    SilverTrace.
        info("peasCore", "SessionManagement.initialization", "peasCore.MSG_SERVICE_STARTING",
            "Initialization of the session management service");
    try {
      // init maxRefreshInterval : add 60 seconds delay because of network traffic
      ResourceLocator rl = new ResourceLocator("com.stratelia.webactiv.clipboard.settings"
          + ".clipboardSettings", "");

      maxRefreshInterval = (60 + Long.parseLong(rl.getString("IntervalInSec"))) * 1000;

      // init userSessionTimeout and scheduledSessionManagementTimeStamp
      ResourceBundle resources = FileUtil.loadBundle("com.stratelia.silverpeas.peasCore"
          + ".SessionManager", new Locale("", ""));
      String language = resources.getString("language");
      if (!StringUtil.isDefined(language)) {
        language = I18NHelper.defaultLanguage;
      }
      messages = new ResourceLocator("com.stratelia.silverpeas.peasCore.multilang"
          + ".peasCoreBundle", language);
      scheduledSessionManagementTimeStamp = convertMinuteInMilliseconds(
          Long.parseLong(resources.getString("scheduledSessionManagementTimeStamp")));
      userSessionTimeout = convertMinuteInMilliseconds(
          Long.parseLong(resources.getString("userSessionTimeout")));
      if (scheduledSessionManagementTimeStamp > userSessionTimeout) {
        scheduledSessionManagementTimeStamp = userSessionTimeout;
      }
      adminSessionTimeout = convertMinuteInMilliseconds(
          Long.parseLong(resources.getString("adminSessionTimeout")));
      if (scheduledSessionManagementTimeStamp > adminSessionTimeout) {
        scheduledSessionManagementTimeStamp = adminSessionTimeout;
      }
      initSchedulerTimeStamp();

      SilverLog.logConnexion("SessionManager starting", "TimeStamp="
          + convertMillisecondsToMinutes(scheduledSessionManagementTimeStamp),
          "UserSessionTimeout=" + convertMillisecondsToMinutes(userSessionTimeout)
          + " adminSessionTimeout=" + convertMillisecondsToMinutes(adminSessionTimeout));

      // register the shutdown session management process when the server is in shutdown.
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          SessionManager.this.shutdown();
        }
      });

    } catch (Exception ex) {
      SilverTrace.fatal("peasCore", "SessionManagement.initialization",
          "root.EX_CLASS_NOT_INITIALIZED",
          ex);
    }
  }

  @Override
  public SessionInfo validateSession(String sessionKey) {
    return validateSession(SessionValidationContext.withSessionKey(sessionKey));
  }

  @Override
  public synchronized SessionInfo validateSession(final SessionValidationContext context) {
    String sessionKey = context.getSessionKey();
    SessionInfo sessionInfo = getSessionInfo(sessionKey);
    if (!context.mustSkipLastUserAccessTimeRegistering()) {
      if (sessionInfo.isDefined()) {
        sessionInfo.updateLastAccess();
      }
      userNotificationSessions.remove(sessionKey);
    }
    return sessionInfo;
  }

  /**
   * This method creates a job that executes the "doSessionManagement" method. That job fires a
   * SchedulerEvent of the type 'EXECUTION_NOT_SUCCESSFULL', or 'EXECUTION_SUCCESSFULL'. The
   * timestamp (every time the job will execute) settings is given by minutes and logically must be
   * less than the session timeout.
   *
   * @throws SchedulerException
   * @see Scheduler for more infos
   */
  private void initSchedulerTimeStamp() throws SchedulerException {
    int minute = (int) convertMillisecondsToMinutes(scheduledSessionManagementTimeStamp);
    SilverTrace.debug("peasCore", "SessionManager.initSchedulerTimeStamp",
        "scheduledSessionManagementTimeStamp in minutes=" + minute);
    if (minute < 0 || minute > 59) {
      throw new SchedulerException("SchedulerMethodJob.setParameter: minute value is out of range");
    }

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
  }

  /**
   * Remove a session and log session's data.
   *
   * @param sessionId identifier
   * @see LogoutServlet
   */
  public void removeSession(String sessionId) {
    closeSession(sessionId);
  }

  @Override
  public synchronized void closeSession(String sessionId) {
    SilverTrace.debug("peasCore", "SessionManager.closeSession", "sessionId=" + sessionId);
    SessionInfo si = userDataSessions.get(sessionId);
    if (si != null) {
      removeSession(si);
    } else {
      SilverTrace.debug("peasCore", "SessionManager.removeSession",
          "L'objet de session n'a pas ete "
          + "retrouve dans la variable userDataSessions !!! (sessionId = " + sessionId + ")");

    }
  }

  private synchronized void removeSession(SessionInfo si) {
    try {
      SilverTrace.info("peasCore", "SessionManager.removeSession", "Close the session "
          + si.getSessionId());
      SilverLog.logConnexion("logout", si.getIPAddress(), log(si));
      // Notify statistics
      Date now = new java.util.Date();
      long duration = now.getTime() - si.getOpeningTimestamp();
      myStatisticsManager.addStatConnection(si.getUserDetail().getId(), now, 1, duration);

      // Delete in wait server messages corresponding to the session to invalidate
      removeInQueueMessages(si.getUserDetail().getId(), si.getSessionId());

      // Remove the session from lists
      userDataSessions.remove(si.getSessionId());
      userNotificationSessions.remove(si.getSessionId());
      si.onClosed();
    } catch (Exception ex) {
      SilverTrace.error("peasCore", "SessionManager.removeSession", "root.EX_NO_MESSAGE", ex);
    }
  }

  @Override
  public synchronized SessionInfo getSessionInfo(String sessionId) {
    SessionInfo session = userDataSessions.get(sessionId);
    if (session == null) {
      session = SessionInfo.NoneSession;
    }
    return session;
  }

  /**
   *
   * @param userId
   * @param sessionId
   */
  private void removeInQueueMessages(String userId, String sessionId) {
    try {
      IdPK pk = new IdPK();
      if (StringUtil.isDefined(sessionId)) {
        SilverpeasBeanDAO<ServerMessageBean> dao = SilverpeasBeanDAOFactory.getDAO(
            "com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
        String whereClause = " USERID=" + userId + " AND SESSIONID='" + sessionId + "'";
        dao.removeWhere(pk, whereClause);
      }

      // Remove "end of session" messages
      SilverpeasBeanDAO<POPUPMessageBean> dao = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
      String whereClause = "userid=" + userId + " AND senderid='-1'";
      dao.removeWhere(pk, whereClause);
    } catch (PersistenceException e) {
      SilverTrace.error("peasCore", "SessionManager.removeInQueueMessages()",
          "root.EX_NO_MESSAGE", "USERID=" + userId + " AND SESSIONID = " + sessionId, e);
    }
  }

  /**
   * Gets all the connected users and the duration of their session.
   *
   * @return
   */
  @Override
  public synchronized Collection<SessionInfo> getConnectedUsersList() {
    return userDataSessions.values();
  }

  /**
   * Gets all the connected users and the duration of their session.
   *
   * @return Collection of HTTPSessionInfo
   * @author dlesimple
   */
  @Override
  public Collection<com.silverpeas.session.SessionInfo> getDistinctConnectedUsersList(
      UserDetail user) {
    Map<String, com.silverpeas.session.SessionInfo> distinctConnectedUsersList
        = new HashMap<String, com.silverpeas.session.SessionInfo>();
    Collection<SessionInfo> sessionsInfos = getConnectedUsersList();
    for (SessionInfo si : sessionsInfos) {
      UserDetail sessionUser = si.getUserDetail();
      String key = sessionUser.getLogin() + sessionUser.getDomainId();
      // keep users with distinct login and domainId
      if (!distinctConnectedUsersList.containsKey(key) && !sessionUser.isAccessGuest()) {
        switch (GeneralPropertiesManager.getDomainVisibility()) {
          case GeneralPropertiesManager.DVIS_ALL:
            // all users are visible
            distinctConnectedUsersList.put(key, si);
            break;
          case GeneralPropertiesManager.DVIS_EACH:
            // only users of user's domain are visible
            if (user.getDomainId().equalsIgnoreCase(sessionUser.getDomainId())) {
              distinctConnectedUsersList.put(key, si);
            }
            break;
          case GeneralPropertiesManager.DVIS_ONE:
            // default domain users can see all users
            // users of other domains can see only users of their domain
            if ("0".equals(user.getDomainId())) {
              distinctConnectedUsersList.put(key, si);
            } else {
              if (user.getDomainId().equalsIgnoreCase(sessionUser.getDomainId())) {
                distinctConnectedUsersList.put(key, si);
              }
            }
        }
      }
    }

    return distinctConnectedUsersList.values();
  }

  /**
   * Gets number of connected users
   *
   * @param user
   * @return nb of connected users
   * @author dlesimple
   */
  @Override
  public int getNbConnectedUsersList(UserDetail user) {
    return getDistinctConnectedUsersList(user).size();
  }

  /**
   * This method is called every scheduledSessionManagementTimeStamp minute by the scheduler, it
   * notify the user when timeout has expired and then invalidates the session if the user has not
   * accessed the server. The maximum minutes duration of session before invalidation is
   * userSessionTimeout + scheduledSessionManagementTimeStamp.
   *
   * @param currentDate the date when the method is called by the scheduler
   * @see Scheduler for parameters, addSession, setLastAccess
   */
  public synchronized void doSessionManagement(Date currentDate) {
    try {
      long currentTime = currentDate.getTime();
      List<SessionInfo> expiredSessions = new ArrayList<SessionInfo>(userDataSessions.size());

      Collection<SessionInfo> allSI = userDataSessions.values();
      for (SessionInfo si : allSI) {
        UserDetail userDetail = si.getUserDetail();
        long userSessionTimeoutMillis = (userDetail.isAccessAdmin()) ? adminSessionTimeout
            : userSessionTimeout;
        // Has the session expired (timeout)
        if (currentTime - si.getLastAccessTimestamp() >= userSessionTimeoutMillis) {
          if (si instanceof HTTPSessionInfo) {
            // the session was opened by a servlet (it is a servlet HTTPSession)
            long duration = si.getLastIdleDuration();
            // Has the user been notified (only for living client)
            if ((duration < maxRefreshInterval)
                && !userNotificationSessions.contains(si.getSessionId())) {
              try {
                notifyEndOfSession(userDetail.getId(), currentTime
                    + scheduledSessionManagementTimeStamp, si.getSessionId());
              } catch (NotificationManagerException ex) {
                SilverTrace.error("peasCore", "SessionManager.doSessionManagement",
                    "notificationManager.EX_CANT_SEND_USER_NOTIFICATION",
                    "sessionId=" + si.getSessionId() + " - user=" + log(si), ex);
              } finally {
                // Add to the notifications
                userNotificationSessions.add(si.getSessionId());
              }
            } else {
              // Remove dead session or timeout with a notification
              expiredSessions.add(si);
            }
          } else {
            // the session isn't a Servlet API one (session opened directly by a web service for example).
            expiredSessions.add(si);
          }
        } // if (hasSessionExpired )
      }
      for (SessionInfo expiredSession : expiredSessions) {
        removeSession(expiredSession);
      }
    } catch (Exception ex) {
      SilverTrace.error("peasCore", "SessionManager.doSessionManagement", "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * This method notify a user's end session.
   *
   * @param userId :the user who's session is about to expire.
   * @param endOfSession the time of the end of session (in milliseconds).
   * @param sessionId the id of the session about to expire.
   */
  private void notifyEndOfSession(String userId, long endOfSession, String sessionId)
      throws NotificationManagerException {
    SilverTrace.debug("peasCore", "SessionManager.notifyEndOfSession", "userId=" + userId
        + " sessionId=" + sessionId);
    String endOfSessionDate = DateUtil.formatDate(new Date(endOfSession), NOTIFY_DATE_FORMAT);
    String msgTitle = messages.getString("EndOfSessionNotificationMsgTitle");
    msgTitle += endOfSessionDate;

    // Notify user the end of session
    NotificationSender notifSender = new NotificationSender(null);

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        msgTitle, messages.getString("EndOfSessionNotificationMsgText"));
    notifMetaData.setSender(null);
    notifMetaData.setSessionId(sessionId);
    notifMetaData.addUserRecipient(new UserRecipient(userId));
    notifMetaData.setSource(messages.getString("administrator"));
    notifSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP, notifMetaData);
  }

  /**
   * This method remove and invalidates all sessions. The unique instance of the SessionManager will
   * be destroyed.
   */
  public void shutdown() {
    SilverTrace.info("peasCore", "SessionManagement.shutdown", "peasCore.MSG_SERVICE_STOPPING",
        "Shutdown of the session management service: all the remaining sessions are cleared up");
    // Remove previous scheduled job
    try {
      scheduler.unscheduleJob(SESSION_MANAGER_JOB_NAME);
    } catch (SchedulerException ex) {
      SilverTrace.error("peasCore", "SessionManager.shutdown", ex.getMessage(), ex);
    }
    Collection<SessionInfo> allSI = new ArrayList<SessionInfo>(userDataSessions.values());
    for (SessionInfo si : allSI) {
      removeSession(si);
    }
    // Writing journal
    SilverLog.logConnexion("SessionManager shutdown", null, null);
    // Destroy the unique instance
    //myInstance = null;
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
   *
   * @return the job for managing the session.
   */
  private Job manageSession() {
    return new Job(SESSION_MANAGER_JOB_NAME) {
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
    SilverTrace.debug("peasCore", "SessionManager.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("peasCore", "SessionManager.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }

  /**
   * This method is dedicated to the authentication for only accessing the WEB services published in
   * Silverpeas. To openSession a user using a WEB browser to access Silverpeas, please prefers the
   * below openSession method.
   *
   * @param user the user for which the session has to be opened
   * @return a SessionInfo instance representing the opened session.
   */
  @Override
  public SessionInfo openSession(UserDetail user) {
    SessionInfo session = new SessionInfo(UUID.randomUUID().toString(), user);
    openSession(session);
    return session;
  }

  /**
   * This method is dedicated to the authentication of users behind a WEB browser.
   *
   * @param user the user for which the session has to be opened
   * @param request the HTTP servlet request in which the authentication is performed.
   * @return a SessionInfo instance representing the opened session.
   */
  @Override
  public SessionInfo openSession(UserDetail user, HttpServletRequest request) {
    HTTPSessionInfo si = null;
    String anIP = request.getRemoteHost();
    try {
      HttpSession session = request.getSession();
      si = new HTTPSessionInfo(session, anIP, user);
      openSession(si);
      userDataSessions.put(si.getSessionId(), si);
      SilverLog.logConnexion("login", si.getIPAddress(), log(si));
    } catch (Exception ex) {
      SilverTrace.error("peasCore", "SessionManagement.openSession", "root.EX_NO_MESSAGE", ex);
    }
    return si;
  }

  /**
   * Opens internally the session described by the specified information about that session.
   *
   * @param sessionInfo information about the session to open.
   */
  private void openSession(SessionInfo sessionInfo) {
    // TODO: remove the commented lines below. A session must be opened and therefore shouldn't be
    // existed before, so we shouldn't remove it in the case it is already opened!
    //removeInQueueMessages(sessionInfo.getUserDetail().getId(), sessionInfo.getSessionId());
    //removeSession(sessionInfo.getSessionId());
    userDataSessions.put(sessionInfo.getSessionId(), sessionInfo);
    SilverLog.logConnexion("login", sessionInfo.getIPAddress(), log(sessionInfo));
  }

  @Override
  public synchronized boolean isUserConnected(UserDetail user) {
    for (SessionInfo session : userDataSessions.values()) {
      if (user.getId().equals(session.getUserDetail().getId())) {
        return true;
      }
    }
    return false;
  }

  private long convertMinuteInMilliseconds(long minutes) {
    return minutes * 60000L;
  }

  private long convertMillisecondsToMinutes(long milliseconds) {
    return milliseconds / 60000L;
  }

  private String log(final SessionInfo sessionInfo) {
    return sessionInfo.getUserDetail().getLogin() + " (" + sessionInfo.getUserDetail().getDomainId()
        + ")";
  }

  @Override
  public long getNextSessionTimeOut(String sessionKey) {
    SessionInfo session = userDataSessions.get(sessionKey);
    long actualUserSessionTimeout = (session.getUserDetail().isAccessAdmin()) ? adminSessionTimeout
        : userSessionTimeout;
    return session.getLastAccessTimestamp() + actualUserSessionTimeout;
  }
}
