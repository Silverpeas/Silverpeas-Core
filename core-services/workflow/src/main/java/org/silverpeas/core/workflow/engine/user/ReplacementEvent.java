/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.notification.system.AbstractResourceEvent;
import org.silverpeas.core.workflow.api.user.Replacement;

/**
 * Event about the replacements of users in a given workflow. Such events are fired when an
 * operation was performed on a replacement such as the creation, the deletion, and so on.
 * @author mmoquillon
 */
public class ReplacementEvent extends AbstractResourceEvent<Replacement> {

  /**
   * Constructs a new event about a given replacement.
   * @param type the type of the event reflecting the cause of it (creation, deletion, ...)
   * @param states the replacement concerned by the event as it was before the cause of that event
   * and once the cause was done. If there is no state before the cause, then just passe
   * the state after (case of the creation). If there is no state after the cause, then just passe
   * the state before (case of the deletion).
   */
  public ReplacementEvent(final Type type, final Replacement... states) {
    super(type, states);
  }
}
  