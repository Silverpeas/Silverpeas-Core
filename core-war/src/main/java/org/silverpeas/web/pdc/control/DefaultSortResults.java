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
package org.silverpeas.web.pdc.control;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;

import javax.inject.Named;

/**
 * @author David Derigent
 */
@Named
public class DefaultSortResults implements SortResults {

  private DefaultSortResults() {
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.search.searchEngine.model.SortResults#execute(java.util.List)
   */
  @Override
  public List<GlobalSilverResult> execute(List<GlobalSilverResult> results, String sortOrder,
      String sortValue, final String language) {

    // Title comparator
    final Comparator<GlobalSilverResult> cTitreAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        String string1 = o1.getName(language);
        String string2 = o2.getName(language);
        if (string1 != null && string2 != null) {
          return string1.compareToIgnoreCase(string2);
        }
        return -1;
      }
    };

    Comparator<GlobalSilverResult> cPertAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        Float float1 = new Float(o1.getRawScore());
        Float float2 = new Float(o2.getRawScore());

        if (float1 != null && float2 != null) {
          int result = float1.compareTo(float2);
          // Add comparison on title if we have the same pertinence
          return (result != 0) ? result : cTitreAsc.compare(o1, o2);
        }
        return -1;
      }
    };

    Comparator<GlobalSilverResult> cAuteurAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        String string1 = o1.getCreatorName();
        String string2 = o2.getCreatorName();

        if (string1 != null && string2 != null) {
          int result = string1.compareToIgnoreCase(string2);
          // Add comparison on title if we have the same author
          return (result != 0) ? result : cTitreAsc.compare(o1, o2);
        }
        return -1;
      }
    };

    Comparator<GlobalSilverResult> cDateAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        String string1 = o1.getCreationDate();
        String string2 = o2.getCreationDate();

        if (string1 != null && string2 != null) {
          int result = string1.compareTo(string2);
          // Add comparison on title if we have the same creation date
          return (result != 0) ? result : cTitreAsc.compare(o1, o2);
        }
        return -1;
      }
    };

    Comparator<GlobalSilverResult> cUpdateDateAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        String string1 = o1.getDate();
        String string2 = o2.getDate();

        if (string1 != null && string2 != null) {
          int result = string1.compareTo(string2);
          // Add comparison on title if we have the same update date
          return (result != 0) ? result : cTitreAsc.compare(o1, o2);
        }
        return -1;
      }
    };

    Comparator<GlobalSilverResult> cEmplAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        String string1 = o1.getLocation();
        String string2 = o2.getLocation();
        if (string1 != null && string2 != null) {
          int result = string1.compareToIgnoreCase(string2);
          // Add comparison on title if we have the same emplacement
          return (result != 0) ? result : cTitreAsc.compare(o1, o2);
        }
        return -1;
      }
    };

    Comparator<GlobalSilverResult> cPopularityAsc = new Comparator<GlobalSilverResult>() {

      @Override
      public int compare(GlobalSilverResult o1, GlobalSilverResult o2) {
        Integer pop1 = Integer.valueOf(o1.getHits());
        Integer pop2 = Integer.valueOf(o2.getHits());

        if (pop1 != null && pop2 != null) {
          int result = pop1.compareTo(pop2);
          // Add comparison on title if we have the same popularity
          return (result != 0) ? result : cTitreAsc.compare(o1, o2);
        }
        return -1;
      }
    };

    int sortValueInt = Integer.parseInt(sortValue);

    if (sortValueInt == 1 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Pertinence ASC
      Collections.sort(results, cPertAsc);
    } else if (sortValueInt == 1 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Pertinence DESC
      Collections.sort(results, cPertAsc);
      Collections.reverse(results);
    } else if (sortValueInt == 2 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Titre ASC
      Collections.sort(results, cTitreAsc);
    } else if (sortValueInt == 2 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Titre DESC
      Collections.sort(results, cTitreAsc);
      Collections.reverse(results);
    } else if (sortValueInt == 3 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Auteur ASC
      Collections.sort(results, cAuteurAsc);
    } else if (sortValueInt == 3 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Auteur DESC
      Collections.sort(results, cAuteurAsc);
      Collections.reverse(results);
    } else if (sortValueInt == 4 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Date ASC
      Collections.sort(results, cDateAsc);
    } else if (sortValueInt == 4 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Date DESC
      Collections.sort(results, cDateAsc);
      Collections.reverse(results);
    } else if (sortValueInt == 5 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Date ASC
      Collections.sort(results, cUpdateDateAsc);
    } else if (sortValueInt == 5 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Date DESC
      Collections.sort(results, cUpdateDateAsc);
      Collections.reverse(results);
    } else if (sortValueInt == 6 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Emplacement ASC
      Collections.sort(results, cEmplAsc);
    } else if (sortValueInt == 6 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Emplacement DESC
      Collections.sort(results, cEmplAsc);
      Collections.reverse(results);
    } else if (sortValueInt == 7 && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {
      // Popularity ASC
      Collections.sort(results, cPopularityAsc);
    } else if (sortValueInt == 7 && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {
      // Popularity DESC
      Collections.sort(results, cPopularityAsc);
      Collections.reverse(results);
    }

    return results;
  }

  @Override
  public void setPdcSearchSessionController(PdcSearchSessionController controller) {
    // Not needed by the default sort
  }
}