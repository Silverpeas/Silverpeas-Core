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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.notification.sse.behavior.AfterSentToAllContexts;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.notification.sse.behavior.KeepAlwaysLastStored;
import org.silverpeas.core.notification.sse.behavior.StoreLastOnly;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.*;
import static org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig.defaultConfig;

/**
 * This task is in charge of dispatching server events without blocking the thread of the emitter.
 * <br>
 * {@link javax.servlet.Servlet} implementation must call
 * {@link #registerAsyncContext(SilverpeasAsyncContext)} to be taken into account.
 */
@Technical
@Bean
public class ServerEventDispatcherTask
    extends AbstractRequestTask<AbstractRequestTask.ProcessContext> {

  private static final int MIN_LIFE_TIME = 40000;
  private static final Set<SilverpeasAsyncContext> synchronizedContexts =
      Collections.synchronizedSet(new LinkedHashSet<>(2000));

  /**
   * Hidden constructor.
   */
  ServerEventDispatcherTask() {
  }

  /**
   * A store of server events.
   */
  private static final ServerEventStore serverEventStore = new ServerEventStore();

  /**
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param context the instance to unregister.
   */
  static void unregisterAsyncContext(SilverpeasAsyncContext context) {
    if (synchronizedContexts.remove(context)) {
      SseLogger.get()
          .debug(() -> format(
              "Unregistering {0}, handling now {1} {1,choice, 1#async context| 1<async contexts}",
              context, synchronizedContexts.size()));
      context.complete();
    }
  }

  /**
   * Gets a snapshot of handled asynchronous contexts.
   * @return a list of asynchronous context.
   */
  static List<SilverpeasAsyncContext> getAsyncContextSnapshot() {
    return new ArrayList<>(synchronizedContexts);
  }

  /**
   * Unregister an {@link SilverpeasAsyncContext} instance.
   * @param sessionId an identifier od a session.
   */
  public static void unregisterBySessionId(String sessionId) {
    List<SilverpeasAsyncContext> contextsToRemove = new ArrayList<>();
    synchronizedContexts.forEach(c -> {
      if (sessionId.equals(c.getSessionId())) {
        contextsToRemove.add(c);
      }
    });
    contextsToRemove.forEach(ServerEventDispatcherTask::unregisterAsyncContext);
  }

  /**
   * Register an {@link SilverpeasAsyncContext} instance.<br> If the instance is already registered,
   * nothing is again registered.
   * @param context the instance to register.
   */
  public static void registerAsyncContext(SilverpeasAsyncContext context) {
    serverEventStore.cleanExpired();
    synchronizedContexts.add(context);
    SseLogger.get()
        .debug(() -> format(
            "Registering {0}, handling now {1} {1,choice, 1#async context| 1<async contexts}",
            context, synchronizedContexts.size()));
  }

  /**
   * Sends server events which the id is higher than the given one.
   *
   * @param request the request linked to the given response.
   * @param response the response on which the server events must be sent.
   * @param lastServerEventId the last identifier performed by the client.
   * @param receiverSessionInfoId the identifier of the receiver session.
   * @param receiver the receiver instance.
   * @return the last server event identifier sent or the given one if nothing has been sent.
   * @throws IOException if the sending fails.
   */
  public static long sendLastServerEventsFromId(final HttpServletRequest request,
      HttpServletResponse response, long lastServerEventId, final String receiverSessionInfoId,
      final User receiver) throws IOException {
    List<ServerEvent> serverEventsToSendAgain = serverEventStore.getFromId(lastServerEventId);
    if (serverEventsToSendAgain.isEmpty()) {
      return lastServerEventId;
    }
    for (ServerEvent serverEventToSendAgain : serverEventsToSendAgain) {
      boolean sent =
          serverEventToSendAgain.send(request, response, receiverSessionInfoId, receiver);
      if (sent) {
        SseLogger.get().debug(() -> format("Send of not consumed {0}", serverEventToSendAgain));
      }
    }
    return serverEventsToSendAgain.get(serverEventsToSendAgain.size() - 1).getId();
  }

  /**
   * Add a server event to dispatch.
   * @param serverEventToDispatch the server event to dispatch.
   */
  public static void dispatch(ServerEvent serverEventToDispatch) {
    if (isSseEnabledFor(serverEventToDispatch)) {
      ServerEventDispatchRequest request = new ServerEventDispatchRequest(serverEventToDispatch);
      push(request);
    }
  }

  private static void push(ServerEventDispatchRequest request) {
    RequestTaskManager.get().push(ServerEventDispatcherTask.class, request);
  }

  /**
   * This is the implementation of a request in charge of dispatching a server event.
   */
  private static class ServerEventDispatchRequest
      implements Request<AbstractRequestTask.ProcessContext> {
    private final ServerEvent serverEventToDispatch;

    /**
     * @param serverEventToDispatch the server event to dispatch.
     */
    ServerEventDispatchRequest(ServerEvent serverEventToDispatch) {
      this.serverEventToDispatch = serverEventToDispatch;
    }

    @Override
    public String getReplacementId() {
      return serverEventToDispatch instanceof StoreLastOnly ?
          serverEventToDispatch.getName().asString() :
          null;
    }

    @Override
    public void process(AbstractRequestTask.ProcessContext context) {
      final List<SilverpeasAsyncContext> safeContexts = getSafeContexts();
      if (!safeContexts.isEmpty()) {
        sendTo(safeContexts);
      }
      serverEventStore.add(serverEventToDispatch);
    }

    private void push(final ServerEvent serverEventToDispatch,
        final SilverpeasAsyncContext asyncContext, final boolean completeAfterSend) {
      try {
        synchronized (asyncContext.getMutex()) {
          if (!asyncContext.isSendPossible()) {
            // This asynchronous context is no more usable
            SseLogger.get().debug(() -> format("No more usable {0}", asyncContext));
            // Removing from handled contexts (just in case)
            unregisterAsyncContext(asyncContext);
            return;
          }
          HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
          HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
          final Long lastServerEventId = asyncContext.getLastServerEventId();
          final Long serverEventId = serverEventToDispatch.getId();
          if (lastServerEventId != null && serverEventId != null) {
            if (lastServerEventId < (serverEventId - 1)) {
              sendLastServerEventsFromId(request, response, lastServerEventId,
                  asyncContext.getSessionId(), asyncContext.getUser());
            }
            asyncContext.setLastServerEventId(null);
          }
          boolean sendPerformed =
              serverEventToDispatch.send(request, response, asyncContext.getSessionId(),
                  asyncContext.getUser());
          if (sendPerformed) {
            SseLogger.get().debug(() -> format("Send done on {0}", asyncContext));
          }
          if (completeAfterSend && sendPerformed) {
            asyncContext.complete();
            SseLogger.get()
                .debug(() -> format("Complete requested after send on {0}", asyncContext));
          }
        }
      } catch (IOException e) {
        SseLogger.get().error("Can not send SSE", e);
        unregisterAsyncContext(asyncContext);
      }
    }

    private void sendTo(final List<SilverpeasAsyncContext> safeContexts) {
      SseLogger.get().debug(() -> format("Sending {0}", serverEventToDispatch));
      final List<Callable<Void>> threadedSends =
          safeContexts.stream().map(c -> (Callable<Void>) () -> {
            push(this.serverEventToDispatch, c, completeAfterSend());
            return null;
          }).collect(Collectors.toList());
      List<Future<Void>> sendResult;
      try {
        sendResult = ManagedThreadPool.getPool()
            .invoke(threadedSends,
                defaultConfig().withMaxThreadPoolSizeOf(getSseSendMaxThreadPool()));
      } catch (InterruptedException e) {
        SseLogger.get().error(e);
        sendResult = emptyList();
        Thread.currentThread().interrupt();
      }
      sendResult.forEach(s -> {
        try {
          s.get();
        } catch (ExecutionException e) {
          SseLogger.get().error(e);
        } catch (InterruptedException e) {
          SseLogger.get().error(e);
          Thread.currentThread().interrupt();
        }
      });
      if (serverEventToDispatch instanceof AfterSentToAllContexts) {
        ((AfterSentToAllContexts) serverEventToDispatch).afterAllContexts();
      }
    }

    /**
     * Gets the context safely, so a list on which the caller can work without concurrency
     * problems.
     * @return the list of context.
     */
    List<SilverpeasAsyncContext> getSafeContexts() {
      return new ArrayList<>(synchronizedContexts);
    }

    /**
     * Indicates if the context must be complete after send.
     * @return true if the context must be complete.
     */
    boolean completeAfterSend() {
      return false;
    }
  }

  /**
   * Handles a store of server events.
   */
  static class ServerEventStore {
    private final List<StoredServerEvent> synchronizedStore =
        Collections.synchronizedList(new ArrayList<>());

    /**
     * Cleaning expired server events (lifetime of 40000ms maximum).
     */
    void cleanExpired() {
      final long currentTime = System.currentTimeMillis();
      final long maxLifeTime = Math.max(getSseStoreEventLifeTime(), MIN_LIFE_TIME);
      synchronized (synchronizedStore) {
        final Iterator<StoredServerEvent> it = synchronizedStore.iterator();
        boolean done = false;
        while (it.hasNext() && !done) {
          final StoredServerEvent item = it.next();
          final boolean canProcess = !(item.getServerEvent() instanceof KeepAlwaysLastStored);
          if (canProcess) {
            final long lifetime = currentTime - item.getStoreTime();
            if (lifetime >= maxLifeTime) {
              it.remove();
              SseLogger.get()
                  .debug(
                      () -> format("Removing expired {0} lifetime of {1}ms", item.getServerEvent(),
                          lifetime));
            } else {
              done = true;
            }
          }
        }
        SseLogger.get()
            .debug(() -> format("Size of the server event store (after clean): {0}",
                synchronizedStore.size()));
      }
    }

    /**
     * Gets server events from the store which the id is higher than the given one.
     * @param lastServerEventId the last identifier performed by the client.
     * @return a list of {@link ServerEvent} instances.
     */
    List<ServerEvent> getFromId(long lastServerEventId) {
      List<ServerEvent> serverEventsToSendAgain = new ArrayList<>();
      synchronizedStore.forEach(i -> {
        if (i.getServerEvent().getId().compareTo(lastServerEventId) > 0) {
          ServerEvent serverEvent = i.getServerEvent();
          serverEventsToSendAgain.add(serverEvent);
        }
      });
      return serverEventsToSendAgain;
    }

    /**
     * Add the given {@link ServerEvent} into the store.
     * @param serverEvent the server event to store.
     */
    void add(final ServerEvent serverEvent) {
      if (serverEvent.getId() != null && !(serverEvent instanceof IgnoreStoring)) {
        if (serverEvent instanceof StoreLastOnly) {
          removeAll((StoreLastOnly) serverEvent);
        }
        synchronizedStore.add(new StoredServerEvent(serverEvent));
        SseLogger.get()
            .debug(() -> format("Add {0} into the store (size={1})", serverEvent,
                synchronizedStore.size()));
      }
    }

    /**
     * Removes from the store all the stored server events which the type is compatible with the
     * given one.
     * @param storeLastOnly the server event class to identifying for removing.
     */
    private void removeAll(StoreLastOnly storeLastOnly) {
      final Class<?> classToIdentify = storeLastOnly.getClass();
      synchronized (synchronizedStore) {
        final Iterator<StoredServerEvent> it = synchronizedStore.iterator();
        boolean done = false;
        while (it.hasNext() && !done) {
          final StoredServerEvent item = it.next();
          if (classToIdentify.isInstance(item.getServerEvent())) {
            final StoreLastOnly itemEvent = (StoreLastOnly) item.getServerEvent();
            if (storeLastOnly.getStoreDiscriminator().equals(itemEvent.getStoreDiscriminator())) {
              it.remove();
              SseLogger.get()
                  .debug(() -> format("Remove {0} from the store (size={1})", item.getServerEvent(),
                      synchronizedStore.size()));
              done = true;
            }
          }
        }
      }
    }

    /**
     * Clears the store.
     */
    public void clear() {
      synchronizedStore.clear();
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
