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
package org.silverpeas.core.persistence.jcr.util;

import java.util.Iterator;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

/**
 *
 * @author ehugonnet
 */
public class PropertyIterable implements Iterable<Property> {

  /**
   * The property iterator being adapted.
   */
  private final PropertyIterator iterator;

  /**
   * Creates an iterable adapter for the given property iterator.
   *
   * @param iterator the property iterator to be adapted
   */
  public PropertyIterable(PropertyIterator iterator) {
    this.iterator = iterator;
  }

  /**
   * Returns the property iterator.
   *
   * @return property iterator
   */
  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Property> iterator() {
    return iterator;
  }
}