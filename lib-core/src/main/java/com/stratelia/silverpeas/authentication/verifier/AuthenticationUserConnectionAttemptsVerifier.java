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
package com.stratelia.silverpeas.authentication.verifier;

import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.scheduler.trigger.TimeUnit;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.authentication.verifier.exception
    .AuthenticationNoMoreUserConnectionAttemptException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.apache.commons.lang.time.DateUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that provides tools to verify user connexion attempts.
 * User: Yohann Chastagnier
 * Date: 05/02/13
 */
public class AuthenticationUserConnectionAttemptsVerifier extends AbstractAuthenticationVerifier {

  private static final Map<String, AuthenticationUserConnectionAttemptsVerifier> cache =
      new ConcurrentHashMap<String, AuthenticationUserConnectionAttemptsVerifier>();

  private static boolean isActivated = false;
  private static boolean isCacheCleanerInitialized = false;
  private static int nbMaxAttempts = 0;

  static {
    nbMaxAttempts = settings.getInteger("nbConnexionAttemptsBeforeBlockingUser", 0);
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
  private AuthenticationUserConnectionAttemptsVerifier(final UserDetail user) {
    super(user);
    if (isActivated && !isCacheCleanerInitialized) {
      synchronized (cache) {
        if (!SchedulerFactory.getFactory().getScheduler()
            .isJobScheduled(CacheCleanerJob.JOB_NAME)) {
          // Cache cleaner.
          try {
            SchedulerFactory.getFactory().getScheduler()
                .scheduleJob(new CacheCleanerJob(), JobTrigger.triggerEvery(10, TimeUnit.MINUTE));
            isCacheCleanerInitialized = true;
          } catch (SchedulerException e) {
            SilverTrace.error("authentication", "AuthenticationUserConnectionAttemptsVerifier()",
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
   * Gets (warning) message the message according to connexion attempts.
   * @return
   */
  public String getMessage() {

    if (isActivated) {
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
    return "/Login.jsp?ErrorCode=" + AuthenticationUserStateVerifier.ERROR_USER_ACCOUNT_BLOCKED;
  }

  /**
   * Verify user connection attempts and block user account if necessary.
   */
  public AuthenticationUserConnectionAttemptsVerifier verify()
      throws AuthenticationNoMoreUserConnectionAttemptException {
    initializationOrLastVerifyDate = DateUtil.getNow();
    if (!check()) {
      if (getUser() != null && StringUtil.isDefined(getUser().getId())) {
        new AdminController(getUser().getId()).blockUser(getUser().getId());
        clearCache();
      }
      throw new AuthenticationNoMoreUserConnectionAttemptException(
          "AuthenticationUserConnectionAttemptsVerifier.verify()", SilverpeasException.ERROR,
          "authentication.EX_VERIFY_USER_CONNECTION_ATTEMPT",
          getUser() != null ? "Login=" + getUser().getLogin() : "");
    }
    return this;
  }

  /**
   * Check user connection attempts.
   */
  public synchronized boolean check() {
    return !isActivated || (getUser() != null && ++nbAttempts < nbMaxAttempts);
  }

  /**
   * Clearing the cache associated to the user.
   */
  public AuthenticationUserConnectionAttemptsVerifier clearCache() {
    clearCache(getUser());
    return this;
  }

  /**
   * Clearing the HTTP session.
   */
  public AuthenticationUserConnectionAttemptsVerifier clearSession(HttpServletRequest request) {
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
  protected static synchronized AuthenticationUserConnectionAttemptsVerifier get(UserDetail user) {
    if (user == null) {
      return new AuthenticationUserConnectionAttemptsVerifier(user);
    }
    String userKey = key(user);
    AuthenticationUserConnectionAttemptsVerifier verifier = cache.get(userKey);
    if (verifier == null) {
      verifier = new AuthenticationUserConnectionAttemptsVerifier(user);
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
      Iterator<Map.Entry<String, AuthenticationUserConnectionAttemptsVerifier>> it =
          cache.entrySet().iterator();
      while (it.hasNext()) {
        AuthenticationUserConnectionAttemptsVerifier verifier = it.next().getValue();
        if (DateUtils.addHours(verifier.initializationOrLastVerifyDate, 1).compareTo(now) < 0) {
          it.remove();
        }
      }
    }
  }
}
