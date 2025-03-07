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
package org.silverpeas.core.web.session;

import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.VolatileResourceCacheService;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.io.upload.UploadSession;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.notification.user.server.channel.popup.PopupMessageService;
import org.silverpeas.core.notification.user.server.channel.server.ServerMessageService;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionValidationContext;
import org.silverpeas.core.silverstatistics.volume.service.SilverStatisticsManager;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.Nonnull;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * Implementation of the {@code org.silverpeas.core.security.session.SessionManagement} interface.
 * It extends the session management performed by the underlying application service by adding
 * useful session-related features like statistics about the connection time of each user or like
 * the actual number of connected users.
 *
 * @author Nicolas Eysseric
 */
@Service
@Singleton
public class SessionManager implements SessionManagement, Initialization {

  // Object on which to synchronize (the instance indeed)
  private final Object mutex;

  // Local constants
  private static final String NOTIFY_DATE_FORMAT = " HH:mm (dd/MM/yyyy) ";
  private static final String SESSION_MANAGER_JOB_NAME = "SessionManagerScheduler";
  // 10mn
  private static final int DEFAULT_USER_SESSION_TIMEOUT = 600000;
  // 20mn
  private static final int DEFAULT_ADMIN_SESSION_TIMEOUT = 1200000;
  // 1mn
  private static final int DEFAULT_SCHEDULED_SESSION_MANAGEMENT_TIMESTAMP = 60000;
  // 1mn30
  private static final int DEFAULT_MAX_REFRESH_INTERVAL = 90000;
  // Max session duration in ms
  private long userSessionTimeout = DEFAULT_USER_SESSION_TIMEOUT;
  private long adminSessionTimeout = DEFAULT_ADMIN_SESSION_TIMEOUT;
  // Timestamp of execution of the scheduled job in ms
  private long scheduledSessionManagementTimeStamp = DEFAULT_SCHEDULED_SESSION_MANAGEMENT_TIMESTAMP;
  // Client refresh interval in ms (see Clipboard Session Controller)
  private long maxRefreshInterval = DEFAULT_MAX_REFRESH_INTERVAL;
  // Contains all current sessions
  private final ConcurrentMap<String, SessionInfo> userDataSessions = new ConcurrentHashMap<>(100);
  private final ConcurrentMap<String, SessionInfo> anonymousSessions = new ConcurrentHashMap<>(1000);

  // Contains the session when notified
  private final List<String> userNotificationSessions =
      Collections.synchronizedList(new ArrayList<>(100));
  @Inject
  private SilverStatisticsManager myStatisticsManager;
  @Inject
  private Scheduler scheduler;
  @Inject
  private DefaultServerEventNotifier defaultServerEventNotifier;
  @Inject
  private Event<UserSessionEvent> userSessionNotifier;

  /**
   * Prevent the class from being instantiated (private)
   */
  protected SessionManager() {
    this.mutex = this;
  }

  @Override
  public void init() {
    try {
      // init maxRefreshInterval : add 60 seconds delay because of network traffic
      SettingBundle rl =
          ResourceLocator.getSettingBundle("org.silverpeas.clipboard.settings.clipboardSettings");

      maxRefreshInterval = (60 + Long.parseLong(rl.getString("IntervalInSec"))) * 1000;

      // init userSessionTimeout and scheduledSessionManagementTimeStamp
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.peasCore.SessionManager");

      scheduledSessionManagementTimeStamp = convertMinuteInMilliseconds(
          settings.getLong("scheduledSessionManagementTimeStamp"));
      userSessionTimeout = convertMinuteInMilliseconds(settings.getLong("userSessionTimeout"));
      if (scheduledSessionManagementTimeStamp > userSessionTimeout) {
        scheduledSessionManagementTimeStamp = userSessionTimeout;
      }
      adminSessionTimeout = convertMinuteInMilliseconds(settings.getLong("adminSessionTimeout"));
      if (scheduledSessionManagementTimeStamp > adminSessionTimeout) {
        scheduledSessionManagementTimeStamp = adminSessionTimeout;
      }
      initSchedulerTimeStamp();

      // register the shutdown session management process when the server is in shutdown.
      Runtime.getRuntime().addShutdownHook(new Thread(SessionManager.this::shutdown));

    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  @Override
  public void release() throws Exception {
    scheduler.unscheduleJob(SESSION_MANAGER_JOB_NAME);
  }

  @Override
  public SessionInfo validateSession(String sessionKey) {
    return validateSession(SessionValidationContext.withSessionKey(sessionKey));
  }

  @Override
  public SessionInfo validateSession(final SessionValidationContext context) {
    String sessionKey = context.getSessionKey();
    SessionInfo sessionInfo = getSessionInfo(sessionKey);
    if (!sessionInfo.isAnonymous() && !context.mustSkipLastUserAccessTimeRegistering()) {
      if (sessionInfo.isDefined()) {
        sessionInfo.updateLastAccess();
      }
      userNotificationSessions.remove(sessionKey);
    }
    return sessionInfo;
  }

  /**
   * This method schedules the {@link SessionManager#manageSession()} job every n minutes, whose n
   * is provided by the settings of the session management. The
   * timestamp (every time the job will execute) property is given in minutes and logically must be
   * less than the session timeout.
   *
   * @throws SchedulerException if the scheduling fails
   * @see Scheduler for more infos
   */
  private void initSchedulerTimeStamp() throws SchedulerException {
    int minute = (int) convertMillisecondsToMinutes(scheduledSessionManagementTimeStamp);
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
    scheduler.scheduleJob(manageSession(), trigger);
  }

  @Override
  public void closeSession(String sessionId) {
    SessionInfo si = getSessionInfo(sessionId);
    if (si.isDefined()) {
      if (si.isAnonymous()) {
        anonymousSessions.remove(sessionId);
      } else {
        removeUserSession(si);
      }
    }
  }

  private void removeUserSession(SessionInfo si) {
    try {
      // Notify statistics
      Date now = new java.util.Date();
      long duration = now.getTime() - si.getOpeningTimestamp();
      synchronized (mutex) {
        myStatisticsManager.addStatConnection(si.getUserDetail().getId(), now, 1, duration);

        // Delete in wait server messages corresponding to the session to invalidate
        removeInQueueMessages(si.getUserDetail().getId(), si.getSessionId());

        // Clears all volatile resources
        VolatileResourceCacheService.clearFrom(si);

        // Clears all upload sessions
        UploadSession.clearFrom(si);

        // Remove the session from lists
        userDataSessions.remove(si.getSessionId());
        userNotificationSessions.remove(si.getSessionId());

        si.onClosed();
      }

      defaultServerEventNotifier.notify(UserSessionServerEvent.aClosingOneFor(si));
      userSessionNotifier.fire(new UserSessionEvent(si));
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  @Override
  @Nonnull
  public SessionInfo getSessionInfo(String sessionId) {
    SessionInfo session = userDataSessions.get(sessionId);
    if (session == null) {
      if (User.getCurrentRequester() != null &&
          User.getCurrentRequester().isAnonymous()) {
        session = anonymousSessions.getOrDefault(sessionId, SessionInfo.NoneSession);
      } else {
        session = SessionInfo.NoneSession;
      }
    }
    return session;
  }

  private void removeInQueueMessages(String userId, String sessionId) {
    if (StringUtil.isDefined(sessionId)) {
      ServerMessageService.get().deleteAll(userId, sessionId);
    }
    // Remove "end of session" messages
    PopupMessageService.get().deleteAll(userId);
  }

  /**
   * Gets all the connected users and the duration of their session.
   *
   * @return a collection of all opened user sessions in Silverpeas.
   */
  @Override
  public Collection<SessionInfo> getConnectedUsersList() {
    return userDataSessions.values();
  }

  /**
   * Gets for the specified user all the connected users and the duration of their session. The
   * actual domain restriction policy is taken into account for the user asking the users currently
   * connected in Silverpeas.
   *
   * @param user the current user asking for actual opened user sessions.
   * @return Collection of opened user sessions.
   * @author dlesimple
   */
  @Override
  public Collection<org.silverpeas.core.security.session.SessionInfo> getDistinctConnectedUsersList(
      User user) {
    Map<String, org.silverpeas.core.security.session.SessionInfo> distinctConnectedUsersList
        = new HashMap<>();
    Collection<SessionInfo> sessionsInfos = getConnectedUsersList();
    for (SessionInfo si : sessionsInfos) {
      User sessionUser = si.getUserDetail();
      String key = sessionUser.getLogin() + sessionUser.getDomainId();
      // keep users with distinct login and domainId
      if (!distinctConnectedUsersList.containsKey(key) && !sessionUser.isAccessGuest()) {
        addInConnectedUsersList(user, distinctConnectedUsersList, si, sessionUser, key);
      }
    }

    return distinctConnectedUsersList.values();
  }

  private void addInConnectedUsersList(final User user,
      final Map<String, SessionInfo> distinctConnectedUsersList, final SessionInfo si,
      final User sessionUser, final String key) {
    if (DomainProperties.areDomainsVisibleToAll()) {
      // all users are visible
      distinctConnectedUsersList.put(key, si);
    } else if (DomainProperties.areDomainsNonVisibleToOthers()) {
      // only users of user's domain are visible
      if (user.getDomainId().equalsIgnoreCase(sessionUser.getDomainId())) {
        distinctConnectedUsersList.put(key, si);
      }
    } else if (DomainProperties.areDomainsVisibleOnlyToDefaultOne()) {
      // users in the default Silverpeas domain can see all the users in Silverpeas, whatever their
      // domain.
      // users of other domains can see only the users of their domain
      if ("0".equals(user.getDomainId())) {
        distinctConnectedUsersList.put(key, si);
      } else {
        if (user.getDomainId().equalsIgnoreCase(sessionUser.getDomainId())) {
          distinctConnectedUsersList.put(key, si);
        }
      }
    }
  }

  /**
   * Gets the number of connected users in Silverpeas for the specified user. The domain restriction
   * policy is taken into account for the given user in the filtering of the opened user sessions.
   *
   * @param user the user asking the number of actually connected users.
   * @return nb of connected users
   * @author dlesimple
   */
  @Override
  public int getNbConnectedUsersList(User user) {
    return getDistinctConnectedUsersList(user).size();
  }

  /**
   * This method is called every scheduledSessionManagementTimeStamp minute by the scheduler, it
   * notifies the user when timeout has expired and then invalidates the session if the user has not
   * accessed the server. The maximum minutes duration of session before invalidation is
   * userSessionTimeout + scheduledSessionManagementTimeStamp.
   *
   * @param currentDate the date when the method is called by the scheduler
   * @see Scheduler for parameters, addSession, setLastAccess
   */
  private void doSessionManagement(Date currentDate) {
    try {
      long currentTime = currentDate.getTime();
      Collection<SessionInfo> allSI = userDataSessions.values();
      List<SessionInfo> expiredSessions = new ArrayList<>(allSI.size());

      for (SessionInfo si : allSI) {
        User userDetail = si.getUserDetail();
        long userSessionTimeoutMillis = userDetail.isAccessAdmin() ? adminSessionTimeout
            : userSessionTimeout;
        // Has the session expired (timeout)
        if (currentTime - si.getLastAccessTimestamp() >= userSessionTimeoutMillis) {
          if (si instanceof HTTPSessionInfo) {
            synchronized (mutex) {
              performUserSessionExpiration(si, currentTime, expiredSessions);
            }
          } else {
            // the session isn't a Servlet API one (session opened directly by a web service for example).
            expiredSessions.add(si);
          }
        }
      }
      for (SessionInfo expiredSession : expiredSessions) {
        removeUserSession(expiredSession);
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  /**
   * Performs the user session expiration.
   * @param si the session info
   * @param currentTime the current time
   * @param expiredSessions the expired sessions to fill
   */
  private void performUserSessionExpiration(final SessionInfo si, final long currentTime,
      final List<SessionInfo> expiredSessions) {
    // the session was opened by a servlet (it is a servlet HTTPSession)
    long duration = si.getLastIdleDuration();
    // Has the user been notified (only for living client)
    if ((duration < maxRefreshInterval)
        && !userNotificationSessions.contains(si.getSessionId())) {
      try {
        notifyEndOfSession(si.getUserDetail().getId(), currentTime
            + scheduledSessionManagementTimeStamp, si.getSessionId());
      } catch (NotificationException ex) {
        SilverLogger.getLogger(this)
            .error("Unable to notify on the session expiration for user {0}",
                new String[]{log(si)}, ex);
      } finally {
        // Add to the notifications
        userNotificationSessions.add(si.getSessionId());
      }
    } else {
      // Remove dead session or timeout with a notification
      expiredSessions.add(si);
    }
  }

  /**
   * This method notify a user's end session.
   *
   * @param userId :the user whom session is about to expire.
   * @param endOfSession the time of the end of session (in milliseconds).
   * @param sessionId the id of the session about to expire.
   */
  private void notifyEndOfSession(String userId, long endOfSession, String sessionId)
      throws NotificationException {
    UserDetail user = UserDetail.getById(userId);
    String userLanguage = DisplayI18NHelper.getDefaultLanguage();
    if (user != null) {
      userLanguage = user.getUserPreferences().getLanguage();
    }
    LocalizationBundle bundle =
        ResourceLocator.getLocalizationBundle("org.silverpeas.peasCore.multilang.peasCoreBundle",
            userLanguage);
    String endOfSessionDate = DateUtil.formatDate(new Date(endOfSession), NOTIFY_DATE_FORMAT);
    String msgTitle = bundle.getString("EndOfSessionNotificationMsgTitle");
    msgTitle += endOfSessionDate;

    // Notify user the end of session
    NotificationSender notifSender = new NotificationSender(null);

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL,
        msgTitle, bundle.getString("EndOfSessionNotificationMsgText"));
    notifMetaData.setSessionId(sessionId);
    notifMetaData.addUserRecipient(new UserRecipient(userId));
    notifMetaData.setSender(bundle.getString("administrator"));
    notifSender.notifyUser(BuiltInNotifAddress.BASIC_POPUP.getId(), notifMetaData);
  }

  /**
   * This method remove and invalidates all sessions. The unique instance of the SessionManager will
   * be destroyed.
   */
  public void shutdown() {
    try {
      scheduler.unscheduleJob(SESSION_MANAGER_JOB_NAME);
    } catch (SchedulerException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    Collection<SessionInfo> allSI = new ArrayList<>(userDataSessions.values());
    for (SessionInfo si : allSI) {
      removeUserSession(si);
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
   *
   * @return the job for managing the session.
   */
  private Job manageSession() {
    return new Job(SESSION_MANAGER_JOB_NAME) {
      @Override
      public void execute(JobExecutionContext context) {
        Date date = context.getFireTime();
        doSessionManagement(date);
      }
    };
  }

  @Override
  public SessionInfo openSession(User user, HttpServletRequest request) {
    SessionInfo si = createUserSessionInfo(user, request, false);
    try {
      registerSession(si);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return si;
  }

  @Override
  public SessionInfo openOneShotSession(final User user, final HttpServletRequest request) {
    return createUserSessionInfo(user, request, true);
  }

  @Override
  public SessionInfo openAnonymousSession(final HttpServletRequest request) {
    SessionInfo sessionInfo = createAnonymousSession(request);
    if (!sessionInfo.isDefined()) {
      throw new SilverpeasRuntimeException("No Anonymous Session was configured!");
    }
    (CacheAccessorProvider.getSessionCacheAccessor()).setCurrentSessionCache(
        sessionInfo.getCache());
    anonymousSessions.put(sessionInfo.getId(), sessionInfo);
    return sessionInfo;
  }

  /**
   * Registers internally the session described by the specified information about that session.
   * @param sessionInfo information about the session to open.
   */
  private void registerSession(SessionInfo sessionInfo) {
    if (!sessionInfo.isAnonymous()) {
      // anonymous session aren't related to any identified Silverpeas user. Only non-anonymous
      // user session requires to be monitored by the session manager.
      userDataSessions.put(sessionInfo.getSessionId(), sessionInfo);
      defaultServerEventNotifier.notify(UserSessionServerEvent.anOpeningOneFor(sessionInfo));
    }
  }

  @Nonnull
  private SessionInfo createUserSessionInfo(final User user, final HttpServletRequest request,
      final boolean isOneShot) {
    if (user.isAnonymous()) {
      throw new IllegalArgumentException("Connection with anonymous access is invoked here!");
    }

    // If X-Forwarded-For header exists, we use the IP address contained in it as the client IP address
    // This is the case when Silverpeas is behind a reverse-proxy
    final String xForwardedFor = request.getHeader("X-Forwarded-For");
    final String requestRemoteHost = request.getRemoteHost();
    String anIP;
    try {
      anIP = InetAddress.getByName(defaultStringIfNotDefined(xForwardedFor, requestRemoteHost))
          .getHostAddress();
    } catch (Exception ex) {
      SilverLogger.getLogger(this).debug(ex.getMessage(), ex);
      // In case of error, simply taking the value from the servlet request
      anIP = requestRemoteHost;
    }

    if (isOneShot) {
      return new OneShotSessionInfo(anIP, user);
    }
    HttpSession session = request.getSession();
    return new HTTPSessionInfo(session, anIP, user);
  }

  /**
   * Creates an anonymous session for an unknown user upon the HTTP session related to specified
   * incoming HTTP request. If no HTTP session has been created, a new one is then created.
   * If no anonymous user account is defined, then {@link SessionInfo#NoneSession} is returned.
   * @param request the incoming request of an unknown user.
   * @return a new anonymous user session.
   */
  @Nonnull
  private SessionInfo createAnonymousSession(final HttpServletRequest request) {
    return ofNullable(UserDetail.getAnonymousUser())
        .map(a -> {
          HttpSession session = request.getSession();
          return new HTTPSessionInfo(session, request.getRemoteHost(), a);
        })
        .map(SessionInfo.class::cast)
        .orElse(SessionInfo.NoneSession);
  }

  @Override
  public boolean isUserConnected(User user) {
    final String userId = user.getId();
    for (SessionInfo session : userDataSessions.values()) {
      if (userId.equals(session.getUserDetail().getId())) {
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
}
