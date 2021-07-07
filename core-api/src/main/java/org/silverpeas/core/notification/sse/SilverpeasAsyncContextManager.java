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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;

/**
 * @author silveryocha
 */
@Service
public class SilverpeasAsyncContextManager {

  private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
  private final Set<SilverpeasAsyncContext> contexts = new HashSet<>(2000);

  SilverpeasAsyncContextManager() {
  }

  public static SilverpeasAsyncContextManager get() {
    return ServiceProvider.getSingleton(SilverpeasAsyncContextManager.class);
  }

  /**
   * Register safely the given {@link SilverpeasAsyncContext} instance.
   * @param context the {@link SilverpeasAsyncContext} instance to register.
   */
  public void register(final SilverpeasAsyncContext context) {
    if (add(context)) {
      SseLogger.get()
          .debug(() -> format(
              "Registering {0}, handling now {1} {1,choice, 1#async context| 1<async contexts}",
              context, getContextSize()));
    }
  }

  /**
   * Unregister safely the given {@link SilverpeasAsyncContext} instance.
   * @param context the {@link SilverpeasAsyncContext} instance to unregister.
   */
  public void unregister(final SilverpeasAsyncContext context) {
    if (remove(context)) {
      SseLogger.get()
          .debug(() -> format(
              "Unregistering {0}, handling now {1} {1,choice, 1#async context| 1<async contexts}",
              context, getContextSize()));
      context.markAsComplete(true);
    }
  }

  /**
   * Gets safely a snapshot of the current registered asynchronous contexts.
   * @return a list of {@link SilverpeasAsyncContext} instances.
   */
  public List<SilverpeasAsyncContext> getAsyncContextSnapshot() {
    return safeRead(() -> new ArrayList<>(contexts));
  }

  private boolean add(final SilverpeasAsyncContext context) {
    return safeWrite(context, contexts::add);
  }

  private boolean remove(final SilverpeasAsyncContext context) {
    return safeWrite(context, contexts::remove);
  }

  private int getContextSize() {
    return safeRead(contexts::size);
  }

  private <T> T safeRead(final Supplier<T> supplier) {
    lock.readLock().lock();
    try {
      return supplier.get();
    } finally {
      lock.readLock().unlock();
    }
  }

  private <T> T safeWrite(SilverpeasAsyncContext context,
      final Function<SilverpeasAsyncContext, T> function) {
    lock.writeLock().lock();
    try {
      return function.apply(context);
    } finally {
      lock.writeLock().unlock();
    }
  }
}
