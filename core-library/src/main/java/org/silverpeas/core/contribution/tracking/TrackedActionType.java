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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.tracking;

/**
 * All the types of actions that can be performed by a user on a contribution in Silverpeas and that
 * can be tracked.
 * @author mmoquillon
 */
public enum TrackedActionType {
  /**
   * A contribution has been created
   */
  CREATION,
  /**
   * A minor modification has been performed onto a contribution. A minor modification is a
   * modification that doesn't change the structure and the semantic content of the contribution; it
   * is, for example, an orthographic or grammatical fix, a rewording of a sentence, ...
   */
  MINOR_UPDATE,
  /**
   * A major modification has been performed onto a contribution. A major modification is a
   * modification that change either the structure or the semantic content or both of the
   * contribution.
   */
  MAJOR_UPDATE,
  /**
   * Modifications has been performed, but no functional mechanism has been used to specify the
   * type of the modification.
   */
  UPDATE,
  /**
   * A contribution has been deleted.
   */
  DELETION,
  /**
   * A contribution has been moved from a component instance to another one in Silverpeas.
   */
  OUTER_MOVE,
  /**
   * A contribution has been moved from a node to another one in the same application in Silverpeas.
   */
  INNER_MOVE
}
