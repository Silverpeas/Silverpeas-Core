/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.selection;

import org.silverpeas.core.SilverpeasResource;

import java.util.Objects;

/**
 * An entry in the {@link SelectionBasket}. It maps the resource that has been put into the basket
 * with the context of its selection. It is related to the resource that has been put into the
 * basket whatever its context, so the equality between two entries is done on the underlying
 * resources.
 * @author mmoquillon
 */
public class SelectionEntry<T extends SilverpeasResource> {

  private final SelectionContext context;
  private final T resource;

  SelectionEntry(final T resource, final SelectionContext context) {
    this.context = context;
    this.resource = resource;
  }

  /**
   * Gets the context of the resource selection.
   * @return a {@link SelectionContext} instance.
   */
  public SelectionContext getContext() {
    return context;
  }

  /**
   * Gets the selected resource.
   * @return a {@link SilverpeasResource} object.
   */
  public T getResource() {
    return resource;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SelectionEntry<?> entry = (SelectionEntry<?>) o;
    return resource.equals(entry.resource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resource);
  }
}
