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
package org.silverpeas.core.comment.test;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.comment.service.notification.CommentEvent;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Singleton;

/**
 * The listener of actions on comments to use within the unit tests.
 */
@Bean
@Singleton
public class MyCommentActionListener extends CDIResourceEventListener<CommentEvent> {

  private int invocation = 0;
  private boolean commentAdded = false;
  private boolean commentRemoved = false;

  /**
   * Has this listener been invoked?
   * @return true if this callback has been invoked at least one time, false otherwise.
   */
  public synchronized boolean isInvoked() {
    return invocation > 0;
  }

  /**
   * Is a comment added?
   * @return true if the listener was invoked for comment adding.
   */
  public synchronized boolean isCommentAdded() {
    return commentAdded;
  }

  /**
   * Is a comment removed?
   * @return true if the listener was invoked for comment removing.
   */
  public synchronized boolean isCommentRemoved() {
    return commentRemoved;
  }

  /**
   * Gets the count of invocation of this listener.
   * @return the invocation count.
   */
  public synchronized int getInvocationCount() {
    return invocation;
  }

  /**
   * An event on the creation of a resource has be listened.
   * @param event the event on the creation of a resource.
   */
  @Override
  public synchronized void onCreation(final CommentEvent event) {
    invocation++;
    commentAdded = true;
  }

  /**
   * An event on the deletion of a resource has be listened. A deleted resource is nonexistent and
   * nonrecoverable.
   * @param event the event on the deletion of a resource.
   */
  @Override
  public synchronized void onDeletion(final CommentEvent event) {
    invocation++;
    commentRemoved = true;
  }

  /**
   * Resets the counter used in test assertions.
   */
  public synchronized void reset() {
    invocation = 0;
    commentRemoved = false;
    commentAdded = false;
  }
}
