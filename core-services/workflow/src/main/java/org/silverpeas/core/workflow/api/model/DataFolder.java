/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;dataFolder&gt; element of a Process Model.
 */
public interface DataFolder {

  /**
   * Get the items
   * @return the items as a Vector
   */
  Item[] getItems();

  /**
   * Converts this object in a RecordTemplate object
   * @param role
   * @param lang
   * @param disabled
   * @return the resulting RecordTemplate
   * @throws WorkflowException
   */
  RecordTemplate toRecordTemplate(String role, String lang, boolean disabled) throws
      WorkflowException;

  /**
   * Get item contained in the DataFolder by role name
   * @param strRoleName to search with
   * @return an object implementing the Item interface
   */
  Item getItem(String strRoleName);

  /**
   * Iterate through the Item objects
   * @return an iterator
   */
  Iterator<Item> iterateItem();

  /**
   * Create an Item
   * @return an object implementing Item
   */
  Item createItem();

  /**
   * Add an item to the collection
   * @param item to be added
   */
  void addItem(Item item);

  /**
   * Remove an item from the collection
   * @param strItemName the name of the item to be removed.
   * @throws WorkflowException when the item could not be found
   */
  void removeItem(String strItemName) throws WorkflowException;
}
