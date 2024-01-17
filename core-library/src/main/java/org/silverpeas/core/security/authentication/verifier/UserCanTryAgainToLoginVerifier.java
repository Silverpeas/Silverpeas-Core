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
package org.silverpeas.core.security.authentication.verifier;

import org.apache.commons.lang3.time.DateUtils;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that provides tools to verify if the user can try to log in one more time after a login
 * error.
 * @author Yohann Chastagnier
 * Date: 05/02/13
 */
public class UserCanTryAgainToLoginVerifier extends AbstractAuthenticationVerifier {

  private static final Map<String, UserCanTryAgainToLoginVerifier> cache =
      new ConcurrentHashMap<>();

  private static boolean isActivated = false;
  private static boolean isCacheCleanerInitialized = false;
  @SuppressWarnings("FieldMayBeFinal")
  private static int nbMaxAttempts;

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
   * @param user the user behind a login attempt.
   */
  private UserCanTryAgainToLoginVerifier(final User user) {
    super(user);
    if (isActivated && !isCacheCleanerInitialized) {
      synchronized (cache) {
        if (!SchedulerProvider.getVolatileScheduler()
            .isJobScheduled(CacheCleanerJob.JOB_NAME)) {
          // Cache cleaner.
          try {
            SchedulerProvider.getVolatileScheduler()
                .scheduleJob(new CacheCleanerJob(), JobTrigger.triggerEvery(10, TimeUnit.MINUTE));
            isCacheCleanerInitialized = true;
          } catch (SchedulerException e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        }
      }
    }
  }

  /**
   * Indicates if the verifier is activated.
   * @return true if this verifier has to be used. False otherwise.
   */
  public boolean isActivated() {
    return isActivated;
  }

  /**
   * Performs request and an original url.
   * @param request the incoming HTTP request for login.
   * @param originalUrl the URL at which the user should be directed if all is ok.
   * @return the actual destination URL once this verifier completed its work.
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
   * Gets the (warning) message according to connection attempts.
   * @return the message to render to the user for its login attempt.
   */
  public String getMessage() {

    if (isActivated && (getUser() == null || !getUser().isAnonymous())) {
      return getString("authentication.attempts.remaining",
          (getUser() != null && StringUtil.isDefined(getUser().getId())) ?
              getUser().getUserPreferences().getLanguage() : I18NHelper.DEFAULT_LANGUAGE,
          String.valueOf(nbMaxAttempts - nbAttempts));
    }

    // No message.
    return "";
  }

  /**
   * Gets the error destination.
   * @return relative URL of the web page for login errors.
   */
  public String getErrorDestination() {
    return "/Login?ErrorCode=" + AuthenticationResponse.Status.USER_ACCOUNT_BLOCKED;
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
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
    return this;
  }

  /**
   * Indicates if the user can try to log in one more time after a login error.
   * If the system is not activated (see file settings), this method answers always yes.
   * If the system is activated, this method answers yes until the try number of login is less than
   * the maximum number of try set.
   * @return true if the user can try to log in one more time, false otherwise.
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
  public void clearSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute("WarningMessage");
    }
  }

  /**
   * Gets a verifier of user connection for the specified user.
   * @param user the user behind the login attempt.
   * @return an instance of this verifier for the given user.
   */
  protected static synchronized UserCanTryAgainToLoginVerifier get(User user) {
    if (user == null) {
      return new UserCanTryAgainToLoginVerifier(null);
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
   * Clear the cache of user connection attempts.
   * @param user the user behind the login attempt.
   */
  private static synchronized void clearCache(User user) {
    if (user != null) {
      cache.remove(key(user));
    }
  }

  /**
   * Centralized build of the connexion attempts cache key.
   * @param user the user behind a login attempt.
   * @return the key to use in cache for the given user.
   */
  private static String key(User user) {
    return "key(" + user.getLogin() + "#@#" + user.getDomainId() + ")";
  }

  /**
   * Cache cleaner.
   * Every 1 hour, cache is cleaned : all connection attempts that are older that one hour are
   * removed.
   * @author Yohann Chastagnier
   */
  private static class CacheCleanerJob extends Job {

    public static final String JOB_NAME = "AuthenticationUserConnectionAttemptsVerifierCleanerJob";

    /**
     * Default constructor.
     */
    public CacheCleanerJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
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
