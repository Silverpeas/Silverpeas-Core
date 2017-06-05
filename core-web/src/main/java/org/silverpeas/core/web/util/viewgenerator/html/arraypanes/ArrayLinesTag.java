/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.taglibs.standard.tag.rt.core.ForEachTag;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.servlet.jsp.JspTagException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.AbstractArrayPane
    .getStartLastIndexesFrom;

/**
 * Iterate over lines.
 * <p>If an instance of {@link SilverpeasList} is given, optimizations are offered</p>
 * @author Yohann Chastagnier
 */
public class ArrayLinesTag extends ForEachTag {

  @Override
  public void setItems(final Object items) throws JspTagException {
    final ArrayPane arrayPane = getArrayPane();
    if (items instanceof List && arrayPane instanceof ArrayPaneSilverpeasV5) {
      final ArrayPaneSilverpeasV5 spArrayPane = (ArrayPaneSilverpeasV5) arrayPane;
      optimize(spArrayPane, (List) items);
    } else {
      super.setItems(items);
    }
  }

  @SuppressWarnings("unchecked")
  private void optimize(final ArrayPaneSilverpeasV5 spArrayPane, final List list)
      throws JspTagException {
    final SilverpeasList silverpeasList = SilverpeasList.wrap(list);
    if (silverpeasList.isSlice()) {
      // The list is already paginated
      spArrayPane.setPaginationList(silverpeasList);
      super.setItems(silverpeasList);
    } else {
      // Sort if necessary
      sort(spArrayPane, silverpeasList);
      // Getting (and initializing) the pagination
      final Pagination pagination = spArrayPane.getPagination(list.size());
      // Computing the paginated list
      final SilverpeasList paginatedList = getPaginatedList(silverpeasList, pagination);
      // Set the new paginated list as items provider
      spArrayPane.setPaginationList(paginatedList);
      super.setItems(paginatedList);
    }
  }

  @SuppressWarnings("unchecked")
  private SilverpeasList getPaginatedList(final List list, final Pagination pagination) {
    final List lightList = new ArrayList();
    final Pair<Integer, Integer> indexes = getStartLastIndexesFrom(pagination);
    final int firstIndex = indexes.getLeft();
    final int lastIndex = indexes.getRight();
    for (int i = firstIndex; i < lastIndex; i++) {
      lightList.add(list.get(i));
    }
    return PaginationList.from(lightList, list.size());
  }

  @SuppressWarnings("unchecked")
  private void sort(final ArrayPaneSilverpeasV5 spArrayPane, final List list) {
    List<ArrayColumn> columns = spArrayPane.getColumns();
    if ((spArrayPane.getColumnToSort() != 0) && (spArrayPane.getColumnToSort() <= columns.size())) {
      final int columnIndex = Math.abs(spArrayPane.getColumnToSort()) - 1;
      final Function compareOn = columns.get(columnIndex).getCompareOn();
      if (compareOn != null) {
        boolean asc = spArrayPane.getColumnToSort() >= 0;
        list.sort(new OptimizedLineComparator(asc, compareOn));
      }
    }
  }

  public ArrayPane getArrayPane() {
    return (ArrayPane) pageContext.getAttribute(ArrayPaneTag.ARRAY_PANE_PAGE_ATT);
  }

  private static class OptimizedLineComparator extends AbstractComplexComparator<Object> {
    final List<Function<Object, Comparable>> compareOnList;
    final boolean asc;

    @SafeVarargs
    private OptimizedLineComparator(final boolean asc,
        final Function<Object, Comparable>... compareOnList) {
      this.compareOnList = Arrays.stream(compareOnList).collect(Collectors.toList());
      this.asc = asc;
    }

    @Override
    protected ValueBuffer getValuesToCompare(final Object value) {
      final ValueBuffer valueBuffer = new ValueBuffer();
      compareOnList.forEach(c -> valueBuffer.append(c.apply(value), asc));
      return valueBuffer;
    }
  }
}
