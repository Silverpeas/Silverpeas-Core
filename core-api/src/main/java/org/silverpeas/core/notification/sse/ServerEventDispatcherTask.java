/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * This task is in charge of dispatching server events without blocking the thread of the emitter.
 * <br/>
 * {@link javax.servlet.Servlet} implementation must call
 * {@link #registerAsyncContext(SilverpeasAsyncContext)} to be taken into account.
 */
public class ServerEventDispatcherTask extends AbstractRequestTask {

  private static final ConcurrentMap<SilverpeasAsyncContext, SilverpeasAsyncContext> asyncContexts =
      new ConcurrentHashMap<>();

  /**
   * Please consult {@link AbstractRequestTask} documentation.
   */
  private static final List<Request> requestList = new ArrayList<>();
  /**
   * Indicator that represents the simple state of the task running.
   */
  private static final List<ServerEvent> lastServerEvents = new ArrayList<>();
  /**
   * Indicator that represents the simple state of the task running.
   */
  private static boolean running = false;

  /**
   * Hidden constructor in order to oblige the caller to use static methods.
   */
  private ServerEventDispatcherTask() {
    super();
  }

  /**
   * Builds and starts the task.
   */
  private static void startIfNotAlreadyDone() {
    if (!running) {
      running = true;
      ManagedThreadPool.invoke(new ServerEventDispatcherTask());
    }
  }

  /**
   * Cleaning expired server events which the first sent data is too far in the past.
   */
  private static void cleanExpiredServerEvents() {
    long expiredTime = System.currentTimeMillis() - (Math.max(ServerEvent.CLIENT_RETRY * 4, 40000));
    synchronized (lastServerEvents) {
      Iterator<ServerEvent> it = lastServerEvents.iterator();
      while (it.hasNext()) {
        ServerEvent serverEvent = it.next();
        final long lifetime = serverEvent.getFirstSentDate().getTime() - expiredTime;
        if (lifetime <= 0) {
          it.remove();
          SilverLogger.getLogger(ServerEventDispatchRequest.class).debug(
              () -> format("Removing expired {0} lifetime of {1}ms", serverEvent.toString(),
                  (lifetime * -1)));
        } else {
          break;
        }
      }
    }
  }

  /**
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param asyncContext the instance to unregister.
   */
  static void unregisterAsyncContext(SilverpeasAsyncContext asyncContext) {
    SilverpeasAsyncContext context = asyncContexts.remove(asyncContext);
    if (context != null) {
      SilverLogger.getLogger(ServerEventDispatcherTask.class).debug(
          () -> format("Unregistering {0}, handling now {1} async context(s)", context.toString(),
              asyncContexts.size()));
    }
  }

  /**
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param sessionId an identifier od a session.
   */
  public static void unregisterBySessionId(String sessionId) {
    final List<SilverpeasAsyncContext> asyncContextsToRemove = new ArrayList<>();
    asyncContexts.entrySet().stream().forEach(entry -> {
      if (sessionId.equals(entry.getKey().getSessionId())) {
        asyncContextsToRemove.add(entry.getKey());
      }
    });
    asyncContextsToRemove.forEach(ServerEventDispatcherTask::unregisterAsyncContext);
  }

  /**
   * Register an {@link SilverpeasAsyncContext} instance.<br/>
   * If the instance is already registered, nothing is again registered.
   * @param asyncContext the instance to register.
   */
  public static void registerAsyncContext(SilverpeasAsyncContext asyncContext) {
    asyncContexts.compute(asyncContext, (ac, oldSilverpeasContext) -> {
      SilverLogger.getLogger(ServerEventDispatcherTask.class).debug(
          () -> format("Registering {0}, handling now {1} async contexts", ac.toString(),
              (asyncContexts.size() + 1)));
      return asyncContext;
    });
  }

  /**
   * Sends server events which the id is higher than the given one.
   * @param response the response on which the server events must be sent.
   * @param lastServerEventId the last identifier performed by the client.
   * @param receiver the receiver.
   * @return the last server event identifier sent or the given one if nothing has been sent.
   * @throws IOException
   */
  public static long sendLastServerEventsFromId(HttpServletResponse response,
      long lastServerEventId, final User receiver) throws IOException {
    List<ServerEvent> serverEventsToSendAgain = new ArrayList<>();
    synchronized (lastServerEvents) {
      if (!lastServerEvents.isEmpty()) {
        serverEventsToSendAgain.addAll(lastServerEvents.stream()
            .filter(lastServerEvent -> lastServerEvent.getId().compareTo(lastServerEventId) > 0)
            .collect(Collectors.toList()));
      }
    }
    if (serverEventsToSendAgain.isEmpty()) {
      return lastServerEventId;
    }
    for (ServerEvent serverEventToSendAgain : serverEventsToSendAgain) {
      SilverLogger.getLogger(ServerEventDispatcherTask.class)
          .debug(() -> format("Sending not consumed {0}", serverEventToSendAgain.toString()));
      serverEventToSendAgain.send(response, receiver);
    }
    return serverEventsToSendAgain.get(serverEventsToSendAgain.size() - 1).getId();
  }

  /**
   * Add a server event to dispatch.
   * @param serverEventToDispatch the server event to dispatch.
   */
  public static void dispatch(ServerEvent serverEventToDispatch) {
    ServerEventDispatchRequest serverEventDispatchRequest =
        new ServerEventDispatchRequest(serverEventToDispatch);
    synchronized (requestList) {
      requestList.add(serverEventDispatchRequest);
      startIfNotAlreadyDone();
    }
    cleanExpiredServerEvents();
  }

  @Override
  protected List<Request> getRequestList() {
    return requestList;
  }

  @Override
  protected void taskIsEnding() {
    running = false;
  }

  /**
   * This is the implementation of a request in charge of dispatching a server event.
   */
  private static class ServerEventDispatchRequest implements Request {
    private final ServerEvent serverEventToDispatch;

    /**
     * @param serverEventToDispatch the server event to dispatch.
     */
    ServerEventDispatchRequest(ServerEvent serverEventToDispatch) {
      this.serverEventToDispatch = serverEventToDispatch;
    }

    @Override
    public void process(Object context) throws InterruptedException {
      if (asyncContexts.isEmpty()) {
        return;
      }
      SilverLogger.getLogger(this)
          .debug(() -> format("Sending {0}", serverEventToDispatch.toString()));
      final Long serverEventId = serverEventToDispatch.getId();
      asyncContexts.keySet().parallelStream().forEach(asyncContext -> {
        try {
          HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
          final Long lastServerEventId = asyncContext.getLastServerEventId();
          if (lastServerEventId != null && serverEventId != null &&
              lastServerEventId < (serverEventId - 1)) {
            SilverLogger.getLogger(this).debug(
                () -> format("Some events have not been received for {0}, from id {1} to {2}",
                    asyncContext.toString(), lastServerEventId, (serverEventId - 1)));
            sendLastServerEventsFromId(response, lastServerEventId, asyncContext.getUser());
            asyncContext.setLastServerEventId(null);
          }
          serverEventToDispatch.send(response, asyncContext.getUser());
        } catch (IOException e) {
          SilverLogger.getLogger(ServerEventDispatchRequest.this).error("Can not send SSE", e);
          unregisterAsyncContext(asyncContext);
        }
      });
      if (serverEventId != null) {
        synchronized (lastServerEvents) {
          lastServerEvents.add(serverEventToDispatch);
        }
      }
    }
  }
}
