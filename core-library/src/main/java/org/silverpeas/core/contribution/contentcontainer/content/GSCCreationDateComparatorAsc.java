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

package org.silverpeas.core.contribution.contentcontainer.content;

import java.util.Comparator;

public class GSCCreationDateComparatorAsc implements Comparator<GlobalSilverContent> {
  final static public GSCCreationDateComparatorAsc comparator = new GSCCreationDateComparatorAsc();

  /**
   * A matching index entry is greater another if his score is higher. This result is reversed as we
   * want a descending sort.
   * @param gsc1
   * @param gsc2
   * @return
   */
  @Override
  public int compare(GlobalSilverContent gsc1, GlobalSilverContent gsc2) {
    int compareResult = gsc1.getCreationDate().compareTo(gsc2.getCreationDate());
    if (compareResult == 0) {
      // both objects have been created on the same date
      compareResult = gsc1.getId().compareTo(gsc2.getId());
    }

    return compareResult;
  }

}