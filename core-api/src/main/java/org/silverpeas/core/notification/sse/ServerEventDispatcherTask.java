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
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.notification.sse.behavior.KeepAlwaysStoring;
import org.silverpeas.core.notification.sse.behavior.StoreLastOnly;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * This task is in charge of dispatching server events without blocking the thread of the emitter.
 * <br/>
 * {@link javax.servlet.Servlet} implementation must call
 * {@link #registerAsyncContext(SilverpeasAsyncContext)} to be taken into account.
 */
public class ServerEventDispatcherTask extends AbstractRequestTask {

  private static final Set<SilverpeasAsyncContext> contexts = new LinkedHashSet<>(2000);

  /**
   * Please consult {@link AbstractRequestTask} documentation.
   */
  private static final List<Request> requestList = new ArrayList<>();

  /**
   * A store of server events.
   */
  private static final ServerEventStore serverEventStore = new ServerEventStore();

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
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param context the instance to unregister.
   */
  static void unregisterAsyncContext(SilverpeasAsyncContext context) {
    unregisterAsyncContext(context, false);
  }

  /**
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param context the instance to unregister.
   */
  static void unregisterAsyncContext(SilverpeasAsyncContext context, boolean sendSessionClose) {
    synchronized (contexts) {
      if (contexts.remove(context)) {
        SilverLogger.getLogger(ServerEventDispatcherTask.class).debug(
            () -> format("Unregistering {0}, handling now {1} async context(s)", context.toString(),
                contexts.size()));
        if (sendSessionClose) {
          ServerEvent event = new UserSessionExpiredServerEvent();
          push(new AimedServerEventDispatchRequest(event, context));
        }
      }
    }
  }

  /**
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param sessionId an identifier od a session.
   * @param isAnonymous indicates if the session is the one of an anonymous user.
   */
  public static void unregisterBySessionId(String sessionId, final boolean isAnonymous) {
    List<SilverpeasAsyncContext> contextsToRemove;
    synchronized (contexts) {
      contextsToRemove = contexts.stream().filter(c -> sessionId.equals(c.getSessionId()))
          .collect(Collectors.toList());
    }
    contextsToRemove.forEach(c -> unregisterAsyncContext(c, !isAnonymous));
  }

  /**
   * Register an {@link SilverpeasAsyncContext} instance.<br/>
   * If the instance is already registered, nothing is again registered.
   * @param context the instance to register.
   */
  public static void registerAsyncContext(SilverpeasAsyncContext context) {
    serverEventStore.cleanExpired();
    synchronized (contexts) {
      contexts.add(context);
      SilverLogger.getLogger(ServerEventDispatcherTask.class).debug(
          () -> format("Registering {0}, handling now {1} async contexts", context.toString(),
              contexts.size()));
    }
  }

  /**
   * Sends server events which the id is higher than the given one.
   * @param request the request linked to the given response.
   * @param response the response on which the server events must be sent.
   * @param lastServerEventId the last identifier performed by the client.
   * @param receiverSessionInfoId the identifier of the receiver session.
   * @param receiver the receiver instance.
   * @return the last server event identifier sent or the given one if nothing has been sent.
   * @throws IOException
   */
  public static long sendLastServerEventsFromId(final HttpServletRequest request,
      HttpServletResponse response, long lastServerEventId, final String receiverSessionInfoId,
      final User receiver) throws IOException {
    List<ServerEvent> serverEventsToSendAgain = serverEventStore.getFromId(lastServerEventId);
    if (serverEventsToSendAgain.isEmpty()) {
      return lastServerEventId;
    }
    for (ServerEvent serverEventToSendAgain : serverEventsToSendAgain) {
      SilverLogger.getLogger(ServerEventDispatcherTask.class)
          .debug(() -> format("Sending not consumed {0}", serverEventToSendAgain.toString()));
      serverEventToSendAgain.send(request, response, receiverSessionInfoId, receiver);
    }
    return serverEventsToSendAgain.get(serverEventsToSendAgain.size() - 1).getId();
  }

  /**
   * Add a server event to dispatch.
   * @param serverEventToDispatch the server event to dispatch.
   */
  public static void dispatch(ServerEvent serverEventToDispatch) {
    ServerEventDispatchRequest request = new ServerEventDispatchRequest(serverEventToDispatch);
    push(request);
  }

  private static void push(Request request) {
    synchronized (requestList) {
      requestList.add(request);
      startIfNotAlreadyDone();
    }
  }

  @Override
  protected List<Request> getRequestList() {
    return requestList;
  }

  @Override
  protected void taskIsEnding() {
    running = false;
  }

  private static void push(final ServerEvent serverEventToDispatch,
      final SilverpeasAsyncContext asyncContext) {
    try {
      HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
      HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
      final Long lastServerEventId = asyncContext.getLastServerEventId();
      final Long serverEventId = serverEventToDispatch.getId();
      if (lastServerEventId != null && serverEventId != null &&
          lastServerEventId < (serverEventId - 1)) {
        sendLastServerEventsFromId(request, response, lastServerEventId,
            asyncContext.getSessionId(), asyncContext.getUser());
        asyncContext.setLastServerEventId(null);
      }
      serverEventToDispatch
          .send(request, response, asyncContext.getSessionId(), asyncContext.getUser());
    } catch (IOException e) {
      SilverLogger.getLogger(ServerEventDispatcherTask.class).error("Can not send SSE", e);
      unregisterAsyncContext(asyncContext);
    }
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
      SilverLogger.getLogger(this).debug(() -> format("Sending {0}", serverEventToDispatch.toString()));
      getSafeContexts().forEach(asyncContext -> push(this.serverEventToDispatch, asyncContext));
      serverEventStore.add(serverEventToDispatch);
    }

    /**
     * Gets the context safely, so a list on which the caller can work without concurrency problems.
     * @return the list of context.
     */
    List<SilverpeasAsyncContext> getSafeContexts() {
      final List<SilverpeasAsyncContext> safeContexts;
      synchronized (contexts) {
        safeContexts = contexts.stream().collect(Collectors.toList());
      }
      return safeContexts;
    }
  }

  private static class AimedServerEventDispatchRequest extends ServerEventDispatchRequest {
    private final SilverpeasAsyncContext silverpeasAsyncContext;

    /**
     * @param serverEventToDispatch the server event to dispatch.
     * @param silverpeasAsyncContext
     */
    AimedServerEventDispatchRequest(final ServerEvent serverEventToDispatch,
        final SilverpeasAsyncContext silverpeasAsyncContext) {
      super(serverEventToDispatch);
      this.silverpeasAsyncContext = silverpeasAsyncContext;
    }

    @Override
    List<SilverpeasAsyncContext> getSafeContexts() {
      return Collections.singletonList(silverpeasAsyncContext);
    }
  }

  /**
   * Handles a store of server events.
   */
  static class ServerEventStore {
    private final List<StoredServerEvent> store = new ArrayList<>();

    /**
     * Cleaning expired server events (lifetime of 40000ms maximum).
     */
    void cleanExpired() {
      long currentTime = System.currentTimeMillis();
      long maxLifeTime = Math.max(ServerEvent.CLIENT_RETRY * 4, 40000);
      synchronized (store) {
        Iterator<StoredServerEvent> it = store.iterator();
        while (it.hasNext()) {
          StoredServerEvent item = it.next();
          if (item.getServerEvent() instanceof KeepAlwaysStoring) {
            continue;
          }
          final long lifetime = currentTime - item.getStoreTime();
          if (lifetime >= maxLifeTime) {
            it.remove();
            SilverLogger.getLogger(this).debug(
                () -> format("Removing expired {0} lifetime of {1}ms",
                    item.getServerEvent().toString(), lifetime));
          } else {
            break;
          }
        }
        SilverLogger.getLogger(this)
            .debug(() -> format("Size of the server event store (after clean): {0}", store.size()));
      }
    }

    /**
     * Gets server events from the store which the id is higher than the given one.
     * @param lastServerEventId the last identifier performed by the client.
     * @return a list of {@link ServerEvent} instances.
     * @throws IOException
     */
    List<ServerEvent> getFromId(long lastServerEventId) throws IOException {
      List<ServerEvent> serverEventsToSendAgain = new ArrayList<>();
      synchronized (store) {
        if (!store.isEmpty()) {
          serverEventsToSendAgain.addAll(store.stream()
              .filter(item -> item.getServerEvent().getId().compareTo(lastServerEventId) > 0)
              .map(StoredServerEvent::getServerEvent).collect(Collectors.toList()));
        }
      }
      return serverEventsToSendAgain;
    }

    /**
     * Add the given {@link ServerEvent} into the store.
     * @param serverEvent the server event to store.
     */
    public void add(final ServerEvent serverEvent) {
      synchronized (store) {
        if (serverEvent.getId() != null && !(serverEvent instanceof IgnoreStoring)) {
          if (serverEvent instanceof StoreLastOnly) {
            removeAll(((StoreLastOnly) serverEvent));
          }
          store.add(new StoredServerEvent(serverEvent));
          SilverLogger.getLogger(this)
              .debug(() -> format("Add {0} into the store (size={1})", serverEvent, store.size()));
        }
      }
    }

    /**
     * Removes from the store all the stored server events which the type is compatible with the
     * given one.
     * @param storeLastOnly the server event class to identifying for removing.
     */
    private void removeAll(StoreLastOnly storeLastOnly) {
      synchronized (store) {
        Class<?> classToIdentify = storeLastOnly.getClass();
        Iterator<StoredServerEvent> it = store.iterator();
        while (it.hasNext()) {
          StoredServerEvent item = it.next();
          if (classToIdentify.isInstance(item.getServerEvent())) {
            it.remove();
            SilverLogger.getLogger(this).debug(
                () -> format("Remove {0} from the store (size={1})", item.getServerEvent(),
                    store.size()));
            break;
          }
        }
      }
    }

    /**
     * Clears the store.
     */
    public void clear() {
      synchronized (store) {
        store.clear();
      }
    }
  }

  /**
   * Represents a server event into the store.
   */
  private static class StoredServerEvent {
    private final long storeTime;
    private final ServerEvent serverEvent;

    private StoredServerEvent(final ServerEvent serverEvent) {
      this.storeTime = System.currentTimeMillis();
      this.serverEvent = serverEvent;
    }

    /**
     * Gets the time of the storing of the server event.
     * @return a time as long (EPOCH time).
     */
    long getStoreTime() {
      return storeTime;
    }

    /**
     * Gets the server event.
     * @return a server event.
     */
    ServerEvent getServerEvent() {
      return serverEvent;
    }
  }
}
