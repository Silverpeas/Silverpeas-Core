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
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import javax.annotation.Nonnull;

public class ArrayEmptyCell implements Comparable<Object>, SimpleGraphicElement {


  @SuppressWarnings("ComparatorMethodParameterNotUsed")
  @Override
  public int compareTo(@Nonnull final Object other) {
    if (!(other instanceof ArrayEmptyCell)) {
      return -1;
    }
    return 0;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ArrayEmptyCell;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(ArrayEmptyCell.class.toString()).toHashCode();
  }

  public String print() {
    return "<td>&nbsp;</td>";
  }

}
