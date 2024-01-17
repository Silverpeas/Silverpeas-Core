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
package org.silverpeas.core.mylinks.model;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@UnitTest
class CategoryDetailComparatorTest {

  @Test
  void sortCategoriesWithoutPositions() {
    List<CategoryDetail> categories = initCategoryPositions(null, null, null, null, null);

    assertThat(extractCategoryIdPositions(categories),
        contains(of(10, 0), of(11, 0), of(12, 0), of(13, 0), of(14, 0)));

    CategoryDetailComparator.sort(categories);

    assertThat(extractCategoryIdPositions(categories),
        contains(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)));
  }

  @Test
  void sortCategoriesWithPositions() {
    List<CategoryDetail> categories = initCategoryPositions(5, 0, 2, 1, 3);

    assertThat(extractCategoryIdPositions(categories),
        contains(of(10, 5), of(11, 0), of(12, 2), of(13, 1), of(14, 3)));

    CategoryDetailComparator.sort(categories);

    assertThat(extractCategoryIdPositions(categories),
        contains(of(11, 0), of(13, 1), of(12, 2), of(14, 3), of(10, 5)));
  }

  @Test
  void sortCategoriesWithAndWithoutPositions() {
    List<CategoryDetail> categories = initCategoryPositions(null, 0, null, 1, 3);

    assertThat(extractCategoryIdPositions(categories),
        contains(of(10, 0), of(11, 0), of(12, 0), of(13, 1), of(14, 3)));

    CategoryDetailComparator.sort(categories);

    assertThat(extractCategoryIdPositions(categories),
        contains(of(12, 0), of(10, 0), of(11, 0), of(13, 1), of(14, 3)));
  }

  /*
  METHOD TOOLS
   */

  private List<CategoryDetail> initCategoryPositions(Integer... positions) {
    List<CategoryDetail> categories = new ArrayList<>();
    for (Integer position : positions) {
      CategoryDetail category = new CategoryDetail();
      category.setId(categories.size() + 10);
      if (position != null) {
        category.setPosition(position);
        category.setHasPosition(true);
      }
      categories.add(category);
    }
    return categories;
  }

  private List<Pair<Integer, Integer>> extractCategoryIdPositions(List<CategoryDetail> categories) {
    List<Pair<Integer, Integer>> categoryIdPositions = new ArrayList<>();
    for (CategoryDetail category : categories) {
      categoryIdPositions.add(of(category.getId(), category.getPosition()));
    }
    return categoryIdPositions;
  }
}