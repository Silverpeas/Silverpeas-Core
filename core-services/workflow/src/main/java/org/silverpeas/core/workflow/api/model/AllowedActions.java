/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;allowedActions&gt; element of a Process Model.
 */
public interface AllowedActions {
  /**
   * Iterate through the AllowedAction objects
   * @return an iterator
   */
  Iterator<AllowedAction> iterateAllowedAction();

  /**
   * Create an AllowedAction
   * @return an object implementing AllowedAction
   */
  AllowedAction createAllowedAction();

  /**
   * Add an allowedAction to the collection
   * @param allowedAction to be added
   */
  void addAllowedAction(AllowedAction allowedAction);

  /**
   * Get available actions
   * @return allowed actions in an array
   */
  Action[] getAllowedActions();

  /**
   * Get allowed action by action name
   * @param strActionName the name of the action to find
   * @return allowed action or <code>null</code> if action not found
   */
  AllowedAction getAllowedAction(String strActionName);

}
