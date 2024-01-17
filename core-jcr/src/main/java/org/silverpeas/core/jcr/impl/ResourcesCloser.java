/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.jcr.impl;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A Closer of internal resources handled by a JCR implementation. Some resources can be allocated
 * at repository creation and those require to be freed during the shutdown of the application. The
 * closer aims to take in charge of their release during the application is stopping.
 * @author mmoquillon
 */
@Technical
@Service
@Singleton
public class ResourcesCloser {

  private final Deque<Closeable> stack = new ArrayDeque<>(4);

  /**
   * Gets an instance of {@link ResourcesCloser}.
   * @return a {@link ResourcesCloser} instance.
   */
  public static ResourcesCloser get() {
    return ServiceProvider.getSingleton(ResourcesCloser.class);
  }

  /**
   * Register the specified closeable resource to be closed at application shutdown. If the given
   * closeable is null, nothing is done.
   * @param closeable a {@link Closeable} resource of the JCR.
   */
  public void register(@Nullable final Closeable closeable) {
    if (closeable != null) {
      stack.addFirst(closeable);
    }
  }

  @PreDestroy
  private void closeAllResources() {
    // close in LIFO order
    while (!stack.isEmpty()) {
      Closeable closeable = stack.removeFirst();
      try {
        closeable.close();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
