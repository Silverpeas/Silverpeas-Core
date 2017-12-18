/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * This task is in charge of processing background processes.<br/>
 * All services which needs to perform background processes needs to push a
 * {@link AbstractBackgroundProcessRequest} to {@link BackgroundProcessTask}.
 * <p>
 * Each request has an identifier set by the caller.<br/>
 * If a request is performing, then the new request is pushed into queue.<br/>
 * If a request is in queue, then the caller choose if the existing must be replaced by a new or
 * ignored.<br/>
 * In other cases, the request is pushed into queue.
 * </p>
 * @author silveryocha
 */
public class BackgroundProcessTask extends AbstractRequestTask {

  static final Map<String, RequestContext> synchronizedContexts =
      Collections.synchronizedMap(new LinkedHashMap<>(2000));

  /**
   * Hidden constructor.
   */
  BackgroundProcessTask() {
  }

  @SuppressWarnings("unchecked")
  private static void updateContextWith(final AbstractBackgroundProcessRequest request) {
    final boolean hasToPush;
    synchronized (synchronizedContexts) {
      RequestContext context = synchronizedContexts.get(request.getUniqueId());
      if (context == null) {
        final RequestContext newRequestContext = new RequestContext(request);
        synchronizedContexts.put(request.getUniqueId(), newRequestContext);
        hasToPush = true;
        getLogger()
            .debug(() -> format("pushing a not yet referenced process {0}", newRequestContext));
      } else {
        if (!context.hasBeenProcessed()) {
          // Request to process is updated
          context.setRealRequest(request);
          hasToPush = false;
          getLogger().debug(() -> format("replacing a referenced process {0}", context));
        } else if (context.isRemovable()) {
          final RequestContext newRequestContext = new RequestContext(request);
          synchronizedContexts.put(request.getUniqueId(), newRequestContext);
          hasToPush = true;
          getLogger().debug(() -> format("replacing referenced process, but no more valid, {0}",
              newRequestContext));
        } else {
          getLogger().debug(() -> format(
              "ignoring a process because it is yet valid and it has been processed {0}", context));
          hasToPush = false;
        }
      }
    }
    if (hasToPush) {
      RequestTaskManager.push(BackgroundProcessTask.class, request);
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
    final AbstractBackgroundProcessRequest realRequest;
    synchronized (synchronizedContexts) {
      final RequestContext context = synchronizedContexts.get(currentRequest.getUniqueId());
      getLogger().debug(() -> format("get real request {0}", context));
      realRequest = context.getRealRequest();
      context.requestWillBeExecuted();
    }
    super.processRequest(realRequest);
  }

  public enum LOCK_DURATION {
    NO_TIME_TO_LIVE(0), TEN_SECONDS(10000), AN_HOUR(60 * 60000), AN_HALF_DAY(12 * 60 * 60000),
    A_DAY(24 * 60 * 60000);

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
    private AbstractBackgroundProcessRequest realRequest;
    private long lastProcessing = 0;

    RequestContext(AbstractBackgroundProcessRequest request) {
      this.realRequest = request;
    }

    void requestWillBeExecuted() {
      synchronized (synchronizedContexts) {
        lastProcessing = System.currentTimeMillis();
        if (isRemovable()) {
          getLogger().debug(() -> format("removing {0} from context", this));
          synchronizedContexts.remove(realRequest.getUniqueId());
        }
      }
    }

    AbstractBackgroundProcessRequest getRealRequest() {
      return realRequest;
    }

    void setRealRequest(final AbstractBackgroundProcessRequest realRequest) {
      this.realRequest = realRequest;
    }

    boolean hasBeenProcessed() {
      return lastProcessing > 0;
    }

    boolean isRemovable() {
      return hasBeenProcessed() && realRequest.getLockDuration().isNoMoreValid(lastProcessing);
    }

    @Override
    public String toString() {
      synchronized (synchronizedContexts) {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
            .append("uniqueId", realRequest.getUniqueId())
            .append("lock duration", realRequest.getLockDuration())
            .append("last processing time", lastProcessing).build();
      }
    }
  }
}
