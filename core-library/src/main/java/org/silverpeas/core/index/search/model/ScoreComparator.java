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
package org.silverpeas.core.index.search.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator used to sort the results set.
 */
public class ScoreComparator implements Comparator<MatchingIndexEntry>, Serializable {

  private static final long serialVersionUID = -7673177477909749945L;
  public final static ScoreComparator comparator = new ScoreComparator();

  /**
   * A matching index entry is greater another if his score is higher. This result is reversed as we
   * want a descending sort.
   *
   * @param r1 matching index entry
   * @param r2 other matching index entry to compare
   */
  @Override
  public int compare(MatchingIndexEntry r1, MatchingIndexEntry r2) {
    if (r1.getScore() < r2.getScore()) {
      return 1;
    } else if (Math.abs(r1.getScore() - r2.getScore()) < 0.0001) {
      return 0;
    }
    return -1;
  }
}