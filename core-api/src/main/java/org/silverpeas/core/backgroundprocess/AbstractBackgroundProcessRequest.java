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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.backgroundprocess;

import org.silverpeas.core.backgroundprocess.BackgroundProcessTask.LOCK_DURATION;
import org.silverpeas.core.thread.task.AbstractRequestTask;

import java.util.UUID;

import static org.silverpeas.core.backgroundprocess.BackgroundProcessTask.LOCK_DURATION
    .NO_TIME;

/**
 * Defines a request performed by {@link BackgroundProcessTask}.
 * @author silveryocha
 */
public abstract class AbstractBackgroundProcessRequest
    implements AbstractRequestTask.Request<AbstractRequestTask.ProcessContext> {

  private final LOCK_DURATION lockDuration;
  private final String uniqueId;

  /**
   * Initializes a unique request.
   */
  protected AbstractBackgroundProcessRequest() {
    this(UUID.randomUUID().toString(), NO_TIME);
  }

  /**
   * Initializes a request by given a unique identifier.
   * @param uniqueId a unique identifier.
   */
  protected AbstractBackgroundProcessRequest(final String uniqueId,
      final LOCK_DURATION lockDuration) {
    this.uniqueId = uniqueId;
    this.lockDuration = lockDuration;
  }

  @Override
  public void process(final AbstractRequestTask.ProcessContext context) throws InterruptedException {
    process();
  }

  /**
   * The unique identifier of the request.
   * @return a string unique identifier.
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * Gets the time after then an another process can be performed.
   * @return the {@link LOCK_DURATION} instance.
   */
  public LOCK_DURATION getLockDuration() {
    return lockDuration;
  }

  /**
   * The treatment which will be performed.
   */
  protected abstract void process();
}
