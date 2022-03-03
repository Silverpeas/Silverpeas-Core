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

package org.silverpeas.core.backgroundprocess;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * This task is in charge of processing background processes.<br>
 * All services which needs to perform background processes needs to push a
 * {@link AbstractBackgroundProcessRequest} to {@link BackgroundProcessTask}.
 * <p>
 *  Each request has an identifier and a lock duration set by the caller (through the
 *  {@link AbstractBackgroundProcessRequest} implementation). The lock duration is the minimal
 *  time the requests are ignored after a first one with a same identifier has been processed.
 * </p>
 * <p>Here the rules:</p>
 * <ul>
 *    <li>If a new request is pushed whereas it does not exist already one into the queue with a
 *    same identifier, then the new request is pushed into queue.</li>
 *    <li>If a new request is pushed whereas it exists already one into the queue with a same
 *    identifier:
 *      <ul>
 *        <li>the lock duration has passed, then the new request is pushed into queue</li>
 *        <li>the lock duration nas not passed or the existing request has not yet been performed,
 *        then the new request is ignored</li>
 *    </ul>
 *    </li>
 *  </ul>
 * <p>
 *   If a request has not yet been processed, it could happen that the lock duration will be
 *   greater than the one specified in case a new request with a same identifier is pushed.
 * </p>
 * @author silveryocha
 */
@Technical
@Bean
public class BackgroundProcessTask extends AbstractRequestTask<AbstractRequestTask.ProcessContext> {

  static final Map<String, RequestContext> synchronizedContexts =
      Collections.synchronizedMap(new LinkedHashMap<>(2000));

  /**
   * Hidden constructor.
   */
  BackgroundProcessTask() {
  }

  @SuppressWarnings("unchecked")
  private static void updateContextWith(final AbstractBackgroundProcessRequest request) {
    synchronized (synchronizedContexts) {
      RequestContext context = synchronizedContexts.get(request.getUniqueId());
      if (context == null || context.isRemovable()) {
        final RequestContext newRequestContext = new RequestContext(request);
        synchronizedContexts.put(request.getUniqueId(), newRequestContext);
        getLogger().debug(() -> format("pushing a new process {0}", newRequestContext));
        RequestTaskManager.push(BackgroundProcessTask.class, request);
      } else {
        getLogger().debug(
            () -> format("ignoring a process because it is yet registered and still living {0}",
            context));
      }
    }
  }

  private static SilverLogger getLogger() {
    return BackgroundProcessLogger.get();
  }

  /**
   * Pushes a request to process.
   * @param request a request.
   */
  public static void push(AbstractBackgroundProcessRequest request) {
    updateContextWith(request);
  }

  static void purgeContexts() {
    synchronizedContexts.entrySet().removeIf(entry -> entry.getValue().isRemovable());
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void processRequest(final Request request) throws SilverpeasException {
    AbstractBackgroundProcessRequest currentRequest = (AbstractBackgroundProcessRequest) request;
    synchronized (synchronizedContexts) {
      final RequestContext context = synchronizedContexts.get(currentRequest.getUniqueId());
      getLogger().debug(() -> format("process will be executed {0}", context));
      context.requestWillBeExecuted();
    }
    super.processRequest(request);
  }

  public enum LOCK_DURATION {
    NO_TIME(0), TEN_SECONDS(10000), AN_HOUR(60L * 60000), AN_HALF_DAY(12L * 60 * 60000),
    A_DAY(24L * 60 * 60000);

    private final long duration;

    LOCK_DURATION(final long duration) {
      this.duration = duration;
    }

    public long getDuration() {
      return duration;
    }

    public boolean isNoMoreValid(long reference) {
      final long now = System.currentTimeMillis();
      return (now - reference) >= duration;
    }
  }

  private static class RequestContext {
    private AbstractBackgroundProcessRequest request;
    private long lastProcessing = 0;

    RequestContext(AbstractBackgroundProcessRequest request) {
      this.request = request;
    }

    void requestWillBeExecuted() {
      synchronized (synchronizedContexts) {
        lastProcessing = System.currentTimeMillis();
        if (isRemovable()) {
          getLogger().debug(() -> format("removing {0} from context", this));
          synchronizedContexts.remove(request.getUniqueId());
        }
      }
    }

    boolean isRemovable() {
      return lastProcessing > 0 && request.getLockDuration().isNoMoreValid(lastProcessing);
    }

    @Override
    public String toString() {
      synchronized (synchronizedContexts) {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
            .append("uniqueId", request.getUniqueId())
            .append("lock duration", request.getLockDuration())
            .append("last processing time", lastProcessing).build();
      }
    }
  }
}
