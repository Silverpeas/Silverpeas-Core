/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.web.util;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.admin.migration.UIUserCache;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Abstraction which permits to get a standardization about managing service data into UI context.
 * @param <D> the type of the handled data.
 * @author silveryocha
 */
public abstract class SelectableUIEntity<D> {

  private final D data;
  private final Set<String> selectedIds;
  private Boolean selected;

  /**
   * Initializes the item with the given data.<br>
   * By default, item is marked as not selected.
   * @param data the data to handle.
   */
  public SelectableUIEntity(final D data) {
    this(data, null);
  }

  /**
   * Initializes the item with the given data and a set of item identifiers which will provide
   * the selected indicator lazily.<br>
   * If no selected id set is given, then the item is marked as not selected by default.
   * @param data the data to handle.
   * @param selectedIds the set of selected identifiers (identifiers provided by {@link #getId()}
   * method).
   */
  public SelectableUIEntity(final D data, final Set<String> selectedIds) {
    this.data = data;
    this.selectedIds = selectedIds;
    this.selected = selectedIds != null ? null : false;
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item handling the data.
   * @param dataList the list of data.
   * @param converter the function which converts the data into a {@link SelectableUIEntity} one.
   * @param <W> the type of the item data handler.
   * @param <I> the type of the data.
   * @return the {@link SilverpeasList} of handled data item.
   */
  public static <W extends SelectableUIEntity, I> SilverpeasList<W> convert(final List<I> dataList,
      final Function<I, W> converter) {
    SilverpeasList<I> list = SilverpeasList.wrap(dataList);
    return dataList.stream().map(converter).collect(SilverpeasList.collector(list));
  }

  /**
   * Gets the unique identifier of te item into the UI list.
   * @return a unique identifier as string.
   */
  public abstract String getId();

  /**
   * Gets the data handled by the item.
   * @return the data.
   */
  public D getData() {
    return data;
  }

  /**
   * Indicates if the UI item is selected.
   * @return true if selected, false otherwise.
   */
  @SuppressWarnings("unchecked")
  public boolean isSelected() {
    if (selected == null) {
      selected = selectedIds.contains(getId());
    }
    return selected;
  }

  /**
   * Sets the selected indicator.
   * @param selected true if selected, false otherwise.
   */
  public void setSelected(final boolean selected) {
    this.selected = selected;
  }

  /**
   * Gets from a dedicated UI cache the details about a user by its id.
   * @param id identifier of a user.
   * @return a {@link User} instance if any, null otherwise.
   */
  protected User getUserByIdFromCache(final String id) {
    return UIUserCache.getById(id);
  }
}
