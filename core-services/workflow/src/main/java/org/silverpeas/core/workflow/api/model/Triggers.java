/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;
import java.util.List;

/**
 * Interface describing a representation of the &lt;triggers&gt; element of a Process Model.
 */
public interface Triggers {

  /**
   * Get the referenced Trigger objects as a list
   * @return
   */
  public List<Trigger> getTriggerList();

  /**
   * Iterate through the Trigger objects
   * @return an iterator
   */
  public Iterator<Trigger> iterateTrigger();

  /**
   * Add a trigger to the collection
   * @param trigger to be added
   */
  public void addTrigger(Trigger trigger);

  /**
   * Create a trigger
   * @return an object implementing Trigger
   */
  public Trigger createTrigger();

  /**
   * Remove all trigger objects from the collection
   */
  public void removeAllTriggers();
}