/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.apache.commons.lang3.time.DateUtils;
import org.silverpeas.core.security.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that provides tools to verify if the user can try to login one more time after a login
 * error.
 * User: Yohann Chastagnier
 * Date: 05/02/13
 */
public class UserCanTryAgainToLoginVerifier extends AbstractAuthenticationVerifier {

  private static final Map<String, UserCanTryAgainToLoginVerifier> cache =
      new ConcurrentHashMap<String, UserCanTryAgainToLoginVerifier>();

  private static boolean isActivated = false;
  private static boolean isCacheCleanerInitialized = false;
  private static int nbMaxAttempts = 0;

  static {
    nbMaxAttempts = settings.getInteger("nbConnectionAttemptsBeforeBlockingUser", 0);
    if (nbMaxAttempts > 0) {
      isActivated = true;
    }
  }

  private int nbAttempts = 0;
  private Date initializationOrLastVerifyDate = DateUtil.getNow();

  /**
   * Default constructor.
   * @param user
   */
  private UserCanTryAgainToLoginVerifier(final UserDetail user) {
    super(user);
    if (isActivated && !isCacheCleanerInitialized) {
      synchronized (cache) {
        if (!SchedulerProvider.getScheduler()
            .isJobScheduled(CacheCleanerJob.JOB_NAME)) {
          // Cache cleaner.
          try {
            SchedulerProvider.getScheduler()
                .scheduleJob(new CacheCleanerJob(), JobTrigger.triggerEvery(10, TimeUnit.MINUTE));
            isCacheCleanerInitialized = true;
          } catch (SchedulerException e) {
            SilverTrace.error("authentication", "UserCanTryAgainToLoginVerifier()",
                "root.MSG_ERR_CACHE_CLEANER_INITIALIZATION");
          }
        }
      }
    }
  }

  /**
   * Indicates if the verifier is activated.
   * @return
   */
  public boolean isActivated() {
    return isActivated;
  }

  /**
   * Performs request and an original url.
   * @param request
   * @param originalUrl
   * @return
   */
  public String performRequestUrl(final HttpServletRequest request, final String originalUrl) {

    if (isActivated) {

      // Adding warning message
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.setAttribute("WarningMessage", getMessage());
      }
    }

    // Original URL
    return originalUrl;
  }

  /**
   * Gets (warning) message the message according to connection attempts.
   * @return
   */
  public String getMessage() {

    if (isActivated && (getUser() == null || !getUser().isAnonymous())) {
      return getString("authentication.attempts.remaining",
          (getUser() != null && StringUtil.isDefined(getUser().getId())) ?
              getUser().getUserPreferences().getLanguage() : I18NHelper.defaultLanguage,
          String.valueOf(nbMaxAttempts - nbAttempts));
    }

    // No message.
    return "";
  }

  /**
   * Gets the error destination.
   * @return
   */
  public String getErrorDestination() {
    return "/Login.jsp?ErrorCode=" + UserCanLoginVerifier.ERROR_USER_ACCOUNT_BLOCKED;
  }

  /**
   * Verify user connection attempts and block user account if necessary.
   */
  public UserCanTryAgainToLoginVerifier verify()
      throws AuthenticationNoMoreUserConnectionAttemptException {
    initializationOrLastVerifyDate = DateUtil.getNow();
    if (!isAtLeastOneUserConnectionAttempt()) {
      if (getUser() != null && StringUtil.isDefined(getUser().getId())) {
        AdminController adminController = ServiceProvider.getService(AdminController.class);
        adminController.blockUser(getUser().getId());
        clearCache();
      }
      throw new AuthenticationNoMoreUserConnectionAttemptException(
          "UserCanTryAgainToLoginVerifier.verify()", SilverpeasException.ERROR,
          "authentication.EX_VERIFY_USER_CAN_TRY_AGAIN_TO_LOGIN",
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
    return this;
  }

  /**
   * Indicates if the user can try to login one more time after an login error.
   * If the system is not activated (see file settings), this method answers always yes.
   * If the system is activated, this method answers yes until the try number of login is less than
   * the maximum number of try set.
   * @return true if the user can try to login one more time, false otherwise.
   */
  private synchronized boolean isAtLeastOneUserConnectionAttempt() {
    return !isActivated ||
        (getUser() != null && (getUser().isAnonymous() || ++nbAttempts < nbMaxAttempts));
  }

  /**
   * Clearing the cache associated to the user.
   */
  public UserCanTryAgainToLoginVerifier clearCache() {
    clearCache(getUser());
    return this;
  }

  /**
   * Clearing the HTTP session.
   */
  public UserCanTryAgainToLoginVerifier clearSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute("WarningMessage");
    }
    return this;
  }

  /**
   * Gets user connection attempt handling.
   * @param user
   * @return
   */
  protected static synchronized UserCanTryAgainToLoginVerifier get(UserDetail user) {
    if (user == null) {
      return new UserCanTryAgainToLoginVerifier(user);
    }
    String userKey = key(user);
    UserCanTryAgainToLoginVerifier verifier = cache.get(userKey);
    if (verifier == null) {
      verifier = new UserCanTryAgainToLoginVerifier(user);
      if (isActivated) {
        cache.put(userKey, verifier);
      }
    } else {
      verifier.setUser(user);
    }
    return verifier;
  }

  /**
   * Clear cache of user connection attempt handling.
   * @param user
   */
  private static synchronized void clearCache(UserDetail user) {
    if (user != null) {
      cache.remove(key(user));
    }
  }

  /**
   * Centralized key build.
   * @param user
   * @return
   */
  private static String key(UserDetail user) {
    return "key(" + user.getLogin() + "#@#" + user.getDomainId() + ")";
  }

  /**
   * Cache cleaner.
   * Every 1 hour, cache is cleaned : all connection attempts that are older that one hour are
   * removed.
   * @author Yohann Chastagnier
   */
  private class CacheCleanerJob extends Job {

    public final static String JOB_NAME = "AuthenticationUserConnectionAttemptsVerifierCleanerJob";

    /**
     * Default constructor.
     */
    public CacheCleanerJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) throws Exception {
      Date now = DateUtil.getNow();
      Iterator<Map.Entry<String, UserCanTryAgainToLoginVerifier>> it = cache.entrySet().iterator();
      while (it.hasNext()) {
        UserCanTryAgainToLoginVerifier verifier = it.next().getValue();
        if (DateUtils.addHours(verifier.initializationOrLastVerifyDate, 1).compareTo(now) < 0) {
          it.remove();
        }
      }
    }
  }
}
