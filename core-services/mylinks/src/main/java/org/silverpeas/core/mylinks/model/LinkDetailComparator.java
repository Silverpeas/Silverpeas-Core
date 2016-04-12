/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.mylinks.model;

import org.silverpeas.core.util.comparator.AbstractComplexComparator;

import java.util.Collections;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class LinkDetailComparator extends AbstractComplexComparator<LinkDetail> {

  /**
   * Easy way to apply this comparator on a list of links.
   * @param links the links to sort.
   * @return the given list.
   */
  public static List<LinkDetail> sort(List<LinkDetail> links) {
    Collections.sort(links, new LinkDetailComparator());
    return links;
  }

  /**
   * Hidden constructor in order to force the call of static method(s).
   */
  private LinkDetailComparator() {
  }

  @Override
  protected ValueBuffer getValuesToCompare(final LinkDetail link) {
    ValueBuffer valueBuffer = new ValueBuffer();

    // Position first
    valueBuffer.append(link.hasPosition() ? link.getPosition() : Integer.MIN_VALUE);

    // Then the creation order
    valueBuffer.append(link.getLinkId(), false);

    return valueBuffer;
  }
}
