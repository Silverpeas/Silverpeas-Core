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
package org.silverpeas.web.pdc.control;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Named;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author David Derigent
 */
@Technical
@Bean
@Named
public class DefaultSortResults implements SortResults {

  private DefaultSortResults() {
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.search.searchEngine.model.SortResults#execute(java.util.List)
   */
  @Override
  public List<GlobalSilverResult> execute(List<GlobalSilverResult> results, SortOrder sortOrder,
      String sortValue, final String language) {

    // Title comparator
    Comparator<GlobalSilverResult> cTitreAsc = getAscComparatorByTitle(language);
    Comparator<GlobalSilverResult> cPertAsc = getAscComparatorByScore(cTitreAsc);
    Comparator<GlobalSilverResult> cAuteurAsc = getAscComparatorByAuthor(cTitreAsc);
    Comparator<GlobalSilverResult> cDateAsc = getAscComparatorByCreationDate(cTitreAsc);
    Comparator<GlobalSilverResult> cUpdateDateAsc = getAscComparatorByUpdateDate(cTitreAsc);
    Comparator<GlobalSilverResult> cEmplAsc = getAscComparatorByLocation(cTitreAsc);
    Comparator<GlobalSilverResult> cPopularityAsc = getAscComparatorByPopularity(cTitreAsc);

    int sortValueInt = Integer.parseInt(sortValue);
    switch (sortValueInt) {
      case 1:
        sortResults(results, cPertAsc, sortOrder);
        break;
      case 2:
        sortResults(results, cTitreAsc, sortOrder);
        break;
      case 3:
        sortResults(results, cAuteurAsc, sortOrder);
        break;
      case 4:
        sortResults(results, cDateAsc, sortOrder);
        break;
      case 5:
        sortResults(results, cUpdateDateAsc, sortOrder);
        break;
      case 6:
        sortResults(results, cEmplAsc, sortOrder);
        break;
      case 7:
        sortResults(results, cPopularityAsc, sortOrder);
        break;
      default:
        break;
    }

    return results;
  }

  private void sortResults(List<GlobalSilverResult> results,
      Comparator<GlobalSilverResult> comparator, SortOrder order) {
    results.sort(comparator);
    if (order == SortOrder.DESC) {
      Collections.reverse(results);
    }
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByPopularity(Comparator<GlobalSilverResult> cTitreAsc) {
    return (o1, o2) -> {
      Integer pop1 = o1.getHits();
      Integer pop2 = o2.getHits();

      int result = pop1.compareTo(pop2);
      // Add comparison on title if we have the same popularity
      return (result != 0) ? result : cTitreAsc.compare(o1, o2);
    };
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByLocation(Comparator<GlobalSilverResult> cTitreAsc) {
    return (o1, o2) -> {
      String string1 = StringUtil.defaultStringIfNotDefined(o1.getLocation());
      String string2 = StringUtil.defaultStringIfNotDefined(o2.getLocation());
      int result = string1.compareToIgnoreCase(string2);
      // Add comparison on title if we have the same emplacement
      return (result != 0) ? result : cTitreAsc.compare(o1, o2);
    };
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByUpdateDate(Comparator<GlobalSilverResult> cTitreAsc) {
    return (o1, o2) -> {
      LocalDate date1 = o1.getLastUpdateDate();
      LocalDate date2 = o2.getLastUpdateDate();

      if (date1 != null && date2 != null) {
        int result = date1.compareTo(date2);
        // Add comparison on title if we have the same update date
        return (result != 0) ? result : cTitreAsc.compare(o1, o2);
      }
      return -1;
    };
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByCreationDate(Comparator<GlobalSilverResult> cTitreAsc) {
    return (o1, o2) -> {
      LocalDate date1 = o1.getCreationDate();
      LocalDate date2 = o2.getCreationDate();

      if (date1 != null && date2 != null) {
        int result = date1.compareTo(date2);
        // Add comparison on title if we have the same creation date
        return (result != 0) ? result : cTitreAsc.compare(o1, o2);
      }
      return -1;
    };
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByAuthor(Comparator<GlobalSilverResult> cTitreAsc) {
    return (o1, o2) -> {
      String string1 = o1.getCreatorName();
      String string2 = o2.getCreatorName();

      if (string1 != null && string2 != null) {
        int result = string1.compareToIgnoreCase(string2);
        // Add comparison on title if we have the same author
        return (result != 0) ? result : cTitreAsc.compare(o1, o2);
      }
      return -1;
    };
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByScore(Comparator<GlobalSilverResult> cTitreAsc) {
    return (o1, o2) -> {
      Float float1 = o1.getScore();
      Float float2 = o2.getScore();

      int result = float1.compareTo(float2);
      // Add comparison on title if we have the same pertinence
      return (result != 0) ? result : cTitreAsc.compare(o1, o2);
    };
  }

  @NonNull
  private static Comparator<GlobalSilverResult> getAscComparatorByTitle(String language) {
    return (o1, o2) -> {
      String string1 = StringUtil.defaultStringIfNotDefined(o1.getName(language));
      String string2 = StringUtil.defaultStringIfNotDefined(o2.getName(language));
      final int result = string1.compareToIgnoreCase(string2);
      return result != 0 ? result : Boolean.compare(o2.isAlias(), o1.isAlias());
    };
  }

  @Override
  public void setPdcSearchSessionController(PdcSearchSessionController controller) {
    // Not needed by the default sort
  }
}