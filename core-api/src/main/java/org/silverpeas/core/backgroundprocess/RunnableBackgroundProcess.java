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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.backgroundprocess;

import org.silverpeas.core.backgroundprocess.BackgroundProcessTask.LOCK_DURATION;

import static org.silverpeas.core.backgroundprocess.BackgroundProcessTask.LOCK_DURATION.NO_TIME;

/**
 * Common implementation of {@link AbstractBackgroundProcessRequest} which is in charge of
 * executing a {@link Runnable} implementation into {@link BackgroundProcessTask} API.
 * @author silveryocha
 */
public class RunnableBackgroundProcess extends AbstractBackgroundProcessRequest {

  private final Runnable process;

  /**
   * Registers into {@link BackgroundProcessTask} context a new process to perform.
   * @param process an {@link AbstractBackgroundProcessRequest} implementation to perform.
   */
  public static void register(final Runnable process) {
    BackgroundProcessTask.push(new RunnableBackgroundProcess(process));
  }

  /**
   * Registers into {@link BackgroundProcessTask} context a new process to perform.
   * @param uniqueId identifier of the {@link BackgroundProcessTask} which ensures having only
   * one process into registry with a same identifier.
   * @param process an {@link AbstractBackgroundProcessRequest} implementation to perform.
   */
  public static void register(final String uniqueId, final Runnable process) {
    BackgroundProcessTask.push(new RunnableBackgroundProcess(uniqueId, process));
  }

  /**
   * Registers into {@link BackgroundProcessTask} context a new process to perform.
   * @param uniqueId identifier of the {@link BackgroundProcessTask} which ensures having only
   * one process into registry with a same identifier.
   * @param lockDuration a lock duration to avoid executing several same process into a short time.
   * @param process an {@link AbstractBackgroundProcessRequest} implementation to perform.
   */
  public static void register(final String uniqueId, final LOCK_DURATION lockDuration,
      final Runnable process) {
    BackgroundProcessTask.push(new RunnableBackgroundProcess(uniqueId, lockDuration, process));
  }

  private RunnableBackgroundProcess(final Runnable process) {
    super();
    this.process = process;
  }

  private RunnableBackgroundProcess(final String uniqueId, final Runnable process) {
    this(uniqueId, NO_TIME, process);
  }

  private RunnableBackgroundProcess(final String uniqueId,
      final LOCK_DURATION lockDuration, final Runnable process) {
    super(uniqueId, lockDuration);
    this.process = process;
  }

  @Override
  protected void process() {
    process.run();
  }
}
