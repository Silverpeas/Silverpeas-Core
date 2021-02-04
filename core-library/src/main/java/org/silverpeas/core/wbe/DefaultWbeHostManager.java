/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.wbe;

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
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;
import static org.silverpeas.core.wbe.WbeLogger.logger;

@Service
public class DefaultWbeHostManager implements WbeHostManager {

  private final WbeCache cache = new WbeCache();
  private final List<WbeClientManager> clients = new ArrayList<>();

  private boolean enabled = true;

  protected DefaultWbeHostManager() {
  }

  @PostConstruct
  protected void init() {
    clients.addAll(ServiceProvider.getAllServices(WbeClientManager.class));
  }

  @Override
  public Optional<String> getClientAdministrationUrl() {
    return getClient().flatMap(WbeClientManager::getAdministrationUrl);
  }

  @Override
  public void notifyEditionWith(final WbeFile file, final Set<String> userIds) {
    if (isEnabled()) {
      logger().debug(() -> format("File {0} is currently edited by {1}", file, userIds));
      cache.registerEdition(file, userIds);
    }
  }

  @Override
  public List<WbeFile> getEditedFilesBy(final WbeUser user) {
    return cache.getEditedFilesBy(user);
  }

  @Override
  public List<WbeUser> getEditorsOfFile(final WbeFile file) {
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
    return enabled && getClient().isPresent();
  }

  @Override
  public boolean isHandled(final WbeFile file) {
    try {
      return getClient().filter(c -> c.isHandled(file)).isPresent();
    } catch (WebApplicationException e) {
      return false;
    }
  }

  @Override
  public <T extends WbeEdition> Optional<T> prepareEditionWith(final SilverpeasUserSession spUserSession,
      final WbeFile anyFile) {
    final String spUserSessionId = spUserSession.getId();
    if (isHandled(anyFile)) {
      final WbeUser user = cache.computeUserIfAbsent(spUserSessionId, () -> new DefaultWbeUser(spUserSession));
      final WbeFile file = cache.computeFileIfAbsent(anyFile);
      user.setLastEditionDateAtNow();
      file.setLastEditionDateAtNow();
      return getClient().flatMap(c -> c.prepareEditionWith(user, file));
    } else {
      logger().debug(() -> format(
          "from {0} preparing WBE edition for {1} and for user {2} but WBE is not enabled!!!",
          spUserSessionId, anyFile, spUserSession.getUser().getId()));
    }
    return empty();
  }

  @Override
  public <R> R getEditionContextFrom(final String fileId, final String accessToken,
      final BiFunction<Optional<WbeUser>, Optional<WbeFile>, R> contextInitializer) {
    final Optional<WbeFile> file = cache.getFileFromId(fileId);
    final Optional<Pair<String, WbeUser>> user = cache.getFileFromAccessToken(accessToken);
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
  public void revokeUser(final WbeUser user) {
    if (isEnabled()) {
      cache.removeUser(user);
    }
  }

  @Override
  public void revokeFile(final WbeFile file) {
    if (isEnabled()) {
      cache.removeFile(file);
    }
  }

  @Override
  public List<WbeUser> listCurrentUsers() {
    return cache.listAllUsers();
  }

  @Override
  public List<WbeFile> listCurrentFiles() {
    return cache.listAllFiles();
  }

  @Override
  public void clear() {
    cache.clear();
    clients.forEach(WbeClientManager::clear);
  }

  private Optional<WbeClientManager> getClient() {
    return clients.stream().filter(WbeClientManager::isEnabled).findFirst();
  }

  public static class WbeCacheCleanerJob extends Job implements Initialization {

    private static final String WBE_CLEANER_JOB_NAME = "WbeCleanerJob";

    @Inject
    private Scheduler scheduler;

    @Inject
    private DefaultWbeHostManager manager;

    /**
     * Creates a new job.
     */
    private WbeCacheCleanerJob() {
      super(WBE_CLEANER_JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      logger().debug(() -> "executing Web Browser Edition cleaner JOB");
      final OffsetDateTime offset = OffsetDateTime.now().minusHours(8);
      manager.cache.clearAllBefore(offset);
    }

    @Override
    public void init() throws Exception {
      logger().debug(() -> "initializing Web Browser Edition cleaner JOB");
      scheduler.unscheduleJob(WBE_CLEANER_JOB_NAME);
      scheduler.scheduleJob(this, JobTrigger.triggerEvery(5, TimeUnit.MINUTE));
    }
  }
}
