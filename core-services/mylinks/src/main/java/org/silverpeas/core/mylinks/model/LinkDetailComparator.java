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
package org.silverpeas.core.mylinks.model;

import org.silverpeas.core.util.comparator.AbstractComplexComparator;

import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class LinkDetailComparator extends AbstractComplexComparator<LinkDetail> {
  private static final long serialVersionUID = 2701981793234156272L;

  /**
   * Easy way to apply this comparator on a list of links.
   * @param links the links to sort.
   * @return the given list.
   */
  public static List<LinkDetail> sort(List<LinkDetail> links) {
    links.sort(new LinkDetailComparator());
    return links;
  }

  @Override
  protected ValueBuffer getValuesToCompare(final LinkDetail link) {
    final ValueBuffer valueBuffer = new ValueBuffer();
    // Category
    final CategoryDetail category = link.getCategory();
    if (category != null) {
      // Position first
      valueBuffer.append(category.hasPosition() ? category.getPosition() : Integer.MIN_VALUE);
      // Then the creation order
      valueBuffer.append(category.getId(), false);
    } else {
      valueBuffer.append(Integer.MIN_VALUE);
      valueBuffer.append(Integer.MAX_VALUE, false);
    }
    // Position first
    valueBuffer.append(link.hasPosition() ? link.getPosition() : Integer.MIN_VALUE);
    // Then the creation order
    valueBuffer.append(link.getLinkId(), false);
    return valueBuffer;
  }
}
