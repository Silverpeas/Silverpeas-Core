/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The categories in which an event, scheduled in a calendar, is classified. The categories are
 * expected to be managed by the event itself.
 */
public class CalendarEventCategories {

  private Set<String> categories = new HashSet<String>();

  /**
   * Adds a category to an event scheduled in a calendar. The category is specified by its unique
   * identifier (its title for example). If the category is already added, then nothing is done.
   * @param categoryId the identifier of the category to add.
   */
  public void add(final String categoryId) {
    categories.add(categoryId);
  }

  /**
   * Adds several categories to an event scheduled in a calendar. The categories are specified by
   * their unique identifier (their title for example). If some of the categories to add are already
   * present, then they are not added.
   * @param categoryIds the identifiers of the categories to add.
   */
  public void addAll(final List<String> categoryIds) {
    categories.addAll(categoryIds);
  }

  /**
   * Adds one or several categories to an event scheduled in a calendar. The categories are
   * specified by their unique identifier (their title for example). If some of the categories to
   * add are already present, then they are not added.
   * @param categoryIds the identifiers of the categories to add.
   */
  public void addAll(final String... categoryIds) {
    addAll(Arrays.asList(categoryIds));
  }

  /**
   * Removes a category from the categories of an event scheduled in a calendar. The category is
   * specified by its unique identifier (its title for example). If the category isn't present, then
   * nothing is done.
   * @param categoryId the identifier of the category to remove.
   */
  public void remove(final String categoryId) {
    categories.remove(categoryId);
  }

  /**
   * Removes several categories from the categories of an event scheduled in a calendar. The
   * categories are specified by their unique identifier (their title for example). If some of the
   * categories to remove aren't present, then nothing is done with them.
   * @param categoryIds the identifiers of the categories to remove.
   */
  public void removeAll(final List<String> categoryIds) {
    categories.removeAll(categoryIds);
  }

  /**
   * Removes one or several categories from the categories of an event scheduled in a calendar. The
   * categories are specified by their unique identifier (their title for example). If some of the
   * categories to remove aren't present, then nothing is done with them.
   * @param categoryIds the identifiers of the categories to remove.
   */
  public void remove(final String... categoryIds) {
    removeAll(Arrays.asList(categoryIds));
  }

  /**
   * Converts this categories container to a list of category identifiers.
   * @return a list of category identifiers.
   */
  public List<String> asList() {
    return new ArrayList<String>(categories);
  }

  /**
   * Converts this categories container to an array of category identifiers.
   * @return an array of category identifiers.
   */
  public String[] asArray() {
    List<String> categoryList = asList();
    return categoryList.toArray(new String[categoryList.size()]);
  }

  /**
   * Is the specified category is in the categories of an event scheduled in a calendar.
   * @param category a category identifier.
   * @return true if the specified category is among the categories of an event, false otherwise.
   */
  public boolean contains(final String category) {
    return categories.contains(category);
  }

  /**
   * Is there is no any categories set for an event?
   * @return true if no categories are set, false otherwise.
   */
  public boolean isEmpty() {
    return categories.isEmpty();
  }

  protected CalendarEventCategories() {

  }
}
