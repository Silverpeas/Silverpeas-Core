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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.notification.sse.behavior.AfterSentToAllContexts;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.notification.sse.behavior.KeepAlwaysLastStored;
import org.silverpeas.core.notification.sse.behavior.StoreLastOnly;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.ManagedThreadPoolException;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.*;
import static org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig.defaultConfig;

/**
 * This task is in charge of dispatching server events without blocking the thread of the emitter.
 * <br>
 * Network implementation must call {@link #registerContext(SilverpeasServerEventContext)} to be
 * taken into account.
 */
@Technical
@Bean
public class ServerEventDispatcherTask
    extends AbstractRequestTask<AbstractRequestTask.ProcessContext> {

  private static final int MIN_LIFE_TIME = 40000;

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
   * Unregister an {@link SilverpeasServerEventContext} instance.
   * @param context the instance to unregister.
   */
  static void unregisterContext(SilverpeasServerEventContext context) {
    SilverpeasServerEventContextManager.get().unregister(context);
  }

  /**
   * Gets a snapshot of handled contexts.
   * @return a list of context.
   */
  static List<SilverpeasServerEventContext> getContextSnapshot() {
    return SilverpeasServerEventContextManager.get().getContextSnapshot();
  }

  /**
   * Unregister an {@link SilverpeasServerEventContext} instance.
   * @param sessionId an identifier od a session.
   */
  public static void unregisterBySessionId(String sessionId) {
    getContextSnapshot().stream()
        .filter(c -> sessionId.equals(c.getSessionId()))
        .forEach(ServerEventDispatcherTask::unregisterContext);
  }

  /**
   * Register an {@link SilverpeasServerEventContext} instance.<br> If the instance is already registered,
   * nothing is again registered.
   * @param context the instance to register.
   */
  public static void registerContext(SilverpeasServerEventContext context) {
    serverEventStore.cleanExpired();
    SilverpeasServerEventContextManager.get().register(context);
  }

  /**
   * Gets server events which the id is higher than the given one.
   *
   * @param lastServerEventId the last identifier performed by the client.
   * @return the last server event identifier sent or the given one if nothing has been sent
   * associated to a list of events to send.
   */
  public static Pair<Long, List<ServerEvent>> getLastServerEventsFromId(long lastServerEventId) {
    List<ServerEvent> serverEventsToSendAgain = serverEventStore.getFromId(lastServerEventId);
    final long newLastServerEventId = serverEventsToSendAgain.isEmpty() ?
        lastServerEventId :
        serverEventsToSendAgain.get(serverEventsToSendAgain.size() - 1).getId();
    return Pair.of(newLastServerEventId, serverEventsToSendAgain);
  }

  /**
   * Add a server event to dispatch.
   * @param serverEventToDispatch the server event to dispatch.
   */
  public static void dispatch(ServerEvent serverEventToDispatch) {
    if (isSseEnabledFor(serverEventToDispatch) &&
        ServerEventWaitForManager.get().mustSendImmediately(serverEventToDispatch)) {
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
          ((StoreLastOnly) serverEventToDispatch).getStoreDiscriminator() :
          null;
    }

    @Override
    public void process(AbstractRequestTask.ProcessContext context) {
      final List<SilverpeasServerEventContext> safeContexts = getSafeContexts();
      if (!safeContexts.isEmpty()) {
        sendTo(safeContexts);
      }
      serverEventStore.add(serverEventToDispatch);
    }

    private void push(final ServerEvent serverEventToDispatch,
        final SilverpeasServerEventContext context, final boolean completeAfterSend) {
      final SilverLogger logger = SseLogger.get();
      ((AbstractServerEventContext<?>) context).safeWrite(() -> {
          try {
            sendNotConsumed(serverEventToDispatch, context, logger);
            boolean sendPerformed =
                serverEventToDispatch.send(context, context.getSessionId(),
                    context.getUser());
            if (sendPerformed) {
              logger.debug(() -> format("Send done on {0}", context));
            }
            if (completeAfterSend && sendPerformed) {
              context.close();
              logger.debug(() -> format("Complete requested after send on {0}", context));
            }
          } catch (Exception e) {
            logger.error("Can not send SSE", e);
            unregisterContext(context);
          }
        });
    }

    private void sendNotConsumed(final ServerEvent serverEventToDispatch,
        final SilverpeasServerEventContext context, final SilverLogger logger) throws IOException {
      final Long lastServerEventId = context.getLastServerEventId();
      if (lastServerEventId != null && serverEventToDispatch.isValidId()) {
        final long serverEventId = serverEventToDispatch.getId();
        if (lastServerEventId < (serverEventId - 1)) {
          final String sessionId = context.getSessionId();
          final User user = context.getUser();
          for (ServerEvent notConsumedEvent : getLastServerEventsFromId(lastServerEventId).getSecond()) {
            boolean sent = notConsumedEvent.send(context, sessionId, user);
            if (sent) {
              logger.debug(() -> format("Send of not consumed {0}", notConsumedEvent));
            }
          }
        }
        context.setLastServerEventId(null);
      }
    }

    private void sendTo(final List<SilverpeasServerEventContext> safeContexts) {
      SseLogger.get().debug(() -> format("Sending {0}", serverEventToDispatch));
      final Map<Boolean, List<SilverpeasServerEventContext>> sorted = safeContexts.stream()
          .collect(groupingBy(SilverpeasServerEventContext::isSendPossible, mapping(c -> c, toList())));
      sorted.getOrDefault(Boolean.FALSE, emptyList())
          .forEach(ServerEventDispatcherTask::unregisterContext);
      Stream<SilverpeasServerEventContext> stream = sorted.getOrDefault(Boolean.TRUE, emptyList())
          .stream()
          .filter(c -> serverEventToDispatch.isConcerned(c.getSessionId(), c.getUser()));
      final int sseSendMaxThreadPool = getSseSendMaxThreadPool();
      if (sseSendMaxThreadPool <= 1) {
        stream.forEach(c -> push(this.serverEventToDispatch, c, completeAfterSend()));
      } else {
        final List<Runnable> threadedSends = stream
            .map(c -> (Runnable) () -> push(this.serverEventToDispatch, c, completeAfterSend()))
            .collect(Collectors.toList());
        try {
          ManagedThreadPool.getPool()
              .invokeAndAwaitTermination(threadedSends,
                  defaultConfig().withMaxThreadPoolSizeOf(sseSendMaxThreadPool));
        } catch (ManagedThreadPoolException e) {
          SseLogger.get().error(e);
          Thread.currentThread().interrupt();
        }
      }
      if (serverEventToDispatch instanceof AfterSentToAllContexts) {
        SseLogger.get().debug(() -> format("Performing treatment after sent of {0}", serverEventToDispatch));
        ((AfterSentToAllContexts) serverEventToDispatch).afterAllContexts();
      }
    }

    /**
     * Gets the context safely, so a list on which the caller can work without concurrency
     * problems.
     * @return the list of context.
     */
    List<SilverpeasServerEventContext> getSafeContexts() {
      return getContextSnapshot();
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

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final List<StoredServerEvent> synchronizedStore = new ArrayList<>();

    /**
     * Cleaning expired server events (lifetime of 40000ms maximum).
     */
    void cleanExpired() {
      final long currentTime = System.currentTimeMillis();
      final long maxLifeTime = Math.max(getSseStoreEventLifeTime(), MIN_LIFE_TIME);
      safeWrite(() -> {
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
      });
    }

    /**
     * Gets server events from the store which the id is higher than the given one.
     * @param lastServerEventId the last identifier performed by the client.
     * @return a list of {@link ServerEvent} instances.
     */
    List<ServerEvent> getFromId(long lastServerEventId) {
      return safeRead(() -> synchronizedStore.stream()
          .filter(i -> i.getServerEvent().getId() > lastServerEventId)
          .map(StoredServerEvent::getServerEvent)
          .collect(Collectors.toList()));
    }

    /**
     * Add the given {@link ServerEvent} into the store.
     * @param serverEvent the server event to store.
     */
    void add(final ServerEvent serverEvent) {
      if (serverEvent.isValidId() && !(serverEvent instanceof IgnoreStoring)) {
        safeWrite(() -> {
          if (serverEvent instanceof StoreLastOnly) {
            removeAll((StoreLastOnly) serverEvent);
          }
          synchronizedStore.add(new StoredServerEvent(serverEvent));
          SseLogger.get()
              .debug(() -> format("Add {0} into the store (size={1})", serverEvent,
                  synchronizedStore.size()));
        });
      }
    }

    /**
     * Removes from the store all the stored server events which the type is compatible with the
     * given one.
     * @param storeLastOnly the server event class to identifying for removing.
     */
    private void removeAll(StoreLastOnly storeLastOnly) {
      final Class<?> classToIdentify = storeLastOnly.getClass();
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

    /**
     * Clears the store.
     */
    public void clear() {
      synchronizedStore.clear();
    }

    <T> T safeRead(final Callable<T> process) {
      try {
        lock.readLock().lock();
        try {
          return process.call();
        } finally {
          lock.readLock().unlock();
        }
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    void safeWrite(final Runnable process) {
      try {
        lock.writeLock().lock();
        try {
          process.run();
        } finally {
          lock.writeLock().unlock();
        }
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
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
