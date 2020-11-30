/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.wopi;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SilverpeasUserSession;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.security.SecuritySettings;
import org.silverpeas.core.wopi.discovery.WopiDiscovery;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.text.MessageFormat.format;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Optional.*;
import static javax.ws.rs.core.Response.Status.OK;
import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;
import static org.silverpeas.core.util.HttpUtil.httpClientTrustingAnySslContext;
import static org.silverpeas.core.util.HttpUtil.toUrl;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.wopi.WopiLogger.logger;
import static org.silverpeas.core.wopi.WopiSettings.*;

@Service
public class DefaultWopiFileEditionManager implements WopiFileEditionManager, Initialization {

  private final Map<String, String> baseUrlByMimeTypes = new ConcurrentHashMap<>();
  private final Map<String, String> baseUrlByExtension = new ConcurrentHashMap<>();
  private final WopiCache cache = new WopiCache();
  private LocalDateTime lastDateTimeOfDiscoveryGet;
  private String lastDiscoveryUrl;

  private boolean enabled = true;

  protected DefaultWopiFileEditionManager() {
  }

  @Override
  public void init() throws Exception {
    registerSecurityDomains();
  }

  @Override
  public void notifyEditionWith(final WopiFile file, final Set<String> userIds) {
    if (isEnabled()) {
      cache.registerEdition(file, userIds);
    }
  }

  @Override
  public List<WopiFile> getEditedFilesBy(final WopiUser user) {
    return cache.getEditedFilesBy(user);
  }

  @Override
  public List<WopiUser> getEditorsOfFile(final WopiFile file) {
    return cache.getEditorsOfFile(file);
  }

  @Override
  public void enable(final boolean enable) {
    this.enabled = enable;
    if (!enable) {
      clear();
    }
  }

  @Override
  public boolean isEnabled() {
    return WopiSettings.isEnabled() && enabled;
  }

  @Override
  public boolean isHandled(final WopiFile file) {
    try {
      return getClientBaseUrlFor(file).isPresent();
    } catch (WebApplicationException e) {
      return false;
    }
  }

  @Override
  public Optional<WopiEdition> prepareEditionWith(final SilverpeasUserSession spUserSession,
      final WopiFile anyFile) {
    final String spUserSessionId = spUserSession.getId();
    final Optional<String> clientBaseUrl = getClientBaseUrlFor(anyFile);
    if (clientBaseUrl.isPresent()) {
      final WopiUser user = cache.computeUserIfAbsent(spUserSessionId, () -> new DefaultWopiUser(spUserSession));
      final WopiFile file = cache.computeFileIfAbsent(anyFile);
      user.setLastEditionDateAtNow();
      file.setLastEditionDateAtNow();
      return of(new WopiEdition(file, user, clientBaseUrl.get()));
    } else {
      logger().debug(() -> format(
          "from {0} preparing WOPI edition for {1} and for user {2} but WOPI is not enabled!!!",
          spUserSessionId, anyFile, spUserSession.getUser().getId()));
    }
    return empty();
  }

  @Override
  public <R> R getEditionContextFrom(final String fileId, final String accessToken,
      final BiFunction<Optional<WopiUser>, Optional<WopiFile>, R> contextInitializer) {
    final Optional<WopiFile> file = cache.getFileFromId(fileId);
    final Optional<Pair<String, WopiUser>> user = cache.getFileFromAccessToken(accessToken);
    user.ifPresent(p -> {
      final SessionInfo sessionInfo = getSessionManagement().getSessionInfo(p.getFirst());
      if (sessionInfo != null) {
        sessionInfo.updateLastAccess();
      }
    });
    logger().debug(() -> format("getting edition context from {0} and {1}", user, file));
    return contextInitializer.apply(user.map(Pair::getSecond), file);
  }

  @Override
  public void revokeUser(final WopiUser user) {
    if (isEnabled()) {
      cache.removeUser(user);
    }
  }

  @Override
  public void revokeFile(final WopiFile file) {
    if (isEnabled()) {
      cache.removeFile(file);
    }
  }

  @Override
  public List<WopiUser> listCurrentUsers() {
    return cache.listAllUsers();
  }

  @Override
  public List<WopiFile> listCurrentFiles() {
    return cache.listAllFiles();
  }

  /**
   * WOPI discovery is the process by which a WOPI host identifies Office for the web
   * capabilities and how to initialize Office for the web applications within a site. WOPI hosts
   * use the discovery XML to determine how to interact with Office for the web.
   * <p>
   *   The discovery is processed every {@link WopiSettings#getWopiClientDiscoveryTimeToLive}
   *   hours to ensures the most up-to-date capabilities.
   * </p>
   */
  private synchronized void discover() {
    final String discoveryUrl = getWopiClientDiscoveryUrl();
    if (lastDiscoveryUrl == null ||
        !lastDiscoveryUrl.equals(discoveryUrl) ||
        baseUrlByMimeTypes.isEmpty() ||
        HOURS.between(lastDateTimeOfDiscoveryGet, LocalDateTime.now()) >= getWopiClientDiscoveryTimeToLive()) {
      clear();
      logger().debug(() -> format("discovering WOPI client with URL {0}", discoveryUrl));
      try {
        final HttpResponse<InputStream> response = httpClientTrustingAnySslContext().send(toUrl(discoveryUrl)
            .timeout(Duration.of(2, SECONDS))
            .build(), ofInputStream());
        if (response.statusCode() != OK.getStatusCode()) {
          throw new WebApplicationException(response.statusCode());
        }
        final WopiDiscovery wopiDiscovery;
        try (final InputStream body = response.body()) {
          wopiDiscovery = WopiDiscovery.load(body);
        }
        wopiDiscovery.consumeBaseUrlMimeType((n, a) -> {
          baseUrlByMimeTypes.put(n, a.getUrlsrc());
          if (isDefined(a.getExt()) && "edit".equals(a.getName())) {
            baseUrlByExtension.put(a.getExt(), a.getUrlsrc());
          }
        });
      } catch (Exception e) {
        logger().error(e);
        throw new WebApplicationException(e);
      }
      registerSecurityDomains();
      lastDiscoveryUrl = discoveryUrl;
      lastDateTimeOfDiscoveryGet = LocalDateTime.now();
    }
  }

  private void clear() {
    baseUrlByMimeTypes.clear();
    baseUrlByExtension.clear();
    cache.clear();
  }

  private void registerSecurityDomains() {
    getWopiClientBaseUrl()
        .map(u -> Stream.of(u, u.replaceFirst("^http", "ws")))
        .orElse(Stream.empty())
        .forEach(u -> {
          final SecuritySettings.Registration registration = SecuritySettings.registration();
          registration.registerDefaultSourceInCSP(u);
          registration.registerDomainInCORS(u);
          logger().debug(() -> format("registering into security WOPI client base URL {0}", u));
        });
  }

  private Optional<String> getClientBaseUrlFor(final WopiFile file) {
    if (isEnabled()) {
      discover();
      final String clientBaseUrl = baseUrlByMimeTypes.get(file.mimeType());
      if (isDefined(clientBaseUrl)) {
        return of(clientBaseUrl);
      }
      return ofNullable(baseUrlByExtension.get(file.ext()));
    } else if (!baseUrlByMimeTypes.isEmpty()){
      clear();
      logger().debug(() -> format("removing all discovered actions because of WOPI disabling"));
    }
    return empty();
  }

  public static class WopiCacheCleanerJob extends Job implements Initialization {

    private static final String WOPI_CLEANER_JOB_NAME = "WopiCleanerJob";

    @Inject
    private Scheduler scheduler;

    @Inject
    private DefaultWopiFileEditionManager manager;

    /**
     * Creates a new job.
     */
    private WopiCacheCleanerJob() {
      super(WOPI_CLEANER_JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      logger().debug(() -> "executing WOPI cleaner JOB");
      final OffsetDateTime offset = OffsetDateTime.now().minusHours(8);
      manager.cache.clearAllBefore(offset);
    }

    @Override
    public void init() throws Exception {
      logger().debug(() -> "initializing WOPI cleaner JOB");
      scheduler.unscheduleJob(WOPI_CLEANER_JOB_NAME);
      scheduler.scheduleJob(this, JobTrigger.triggerEvery(5, TimeUnit.MINUTE));
    }
  }
}
