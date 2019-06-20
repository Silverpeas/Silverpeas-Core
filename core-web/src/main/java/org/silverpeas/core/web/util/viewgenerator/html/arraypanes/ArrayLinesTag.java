/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.apache.taglibs.standard.tag.rt.core.ForEachTag;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.servlet.jsp.JspTagException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Iterate over lines.
 * <p>If an instance of {@link SilverpeasList} is given, optimizations are offered</p>
 * @author Yohann Chastagnier
 */
public class ArrayLinesTag extends ForEachTag {
  private static final long serialVersionUID = 1621133978805756811L;

  private static final String AJAX_EXPORT_PARAMETER_NAME = "ArrayPaneAjaxExport";

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
    final HttpRequest httpRequest = HttpRequest.decorate(pageContext.getRequest());
    final boolean isAjaxExportAction = httpRequest.getParameterAsBoolean(AJAX_EXPORT_PARAMETER_NAME);
    if (silverpeasList.isSlice()) {
      if (isAjaxExportAction) {
        SilverLogger.getLogger(this).warn(
            "On URL ''{0}'' export is requested on optimized array by high performance loading " +
                "(load linked to repository queries), the export action is extracting only the " +
                "current displayed page... Please implement a special export to get an export of " +
                "all data",
            httpRequest.getRequestURL());
      }
      // The list is already paginated
      spArrayPane.setPaginationList(silverpeasList);
      super.setItems(silverpeasList);
    } else {
      // Sort if necessary
      sort(spArrayPane, silverpeasList);
      if (isAjaxExportAction) {
        // Set the original list as items provider
        spArrayPane.setPaginationList(silverpeasList);
        // All the data are necessary for export
        super.setItems(silverpeasList);
      } else {
        // Getting (and initializing) the pagination
        final Pagination pagination = spArrayPane.getPagination(list.size());
        // Computing the paginated list
        final SilverpeasList paginatedList = pagination.getPaginatedListFrom(silverpeasList);
        // Set the new paginated list as items provider
        spArrayPane.setPaginationList(paginatedList);
        super.setItems(paginatedList);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void sort(final ArrayPaneSilverpeasV5 spArrayPane, final List list) {
    List<ArrayColumn> columns = spArrayPane.getColumns();
    if ((spArrayPane.getColumnToSort() != 0) && (spArrayPane.getColumnToSort() <= columns.size())) {
      final int columnIndex = Math.abs(spArrayPane.getColumnToSort()) - 1;
      final BiFunction compareOn = columns.get(columnIndex).getCompareOn();
      if (compareOn != null) {
        boolean asc = spArrayPane.getColumnToSort() >= 0;
        list.sort(new OptimizedLineComparator(columnIndex, asc, compareOn));
      }
    }
  }

  public ArrayPane getArrayPane() {
    return (ArrayPane) pageContext.getAttribute(ArrayPaneTag.ARRAY_PANE_PAGE_ATT);
  }

  private static class OptimizedLineComparator extends AbstractComplexComparator<Object> {
    private static final long serialVersionUID = 8089102273880269806L;

    final int columnIndex;
    final List<BiFunction<Object, Integer, Comparable>> compareOnList;
    final boolean asc;

    @SafeVarargs
    private OptimizedLineComparator(final int columnIndex, final boolean asc,
        final BiFunction<Object, Integer, Comparable>... compareOnList) {
      super();
      this.columnIndex = columnIndex;
      this.compareOnList = Arrays.stream(compareOnList).collect(Collectors.toList());
      this.asc = asc;
    }

    @Override
    protected ValueBuffer getValuesToCompare(final Object value) {
      final ValueBuffer valueBuffer = new ValueBuffer();
      compareOnList.forEach(c -> valueBuffer.append(c.apply(value, columnIndex), asc));
      return valueBuffer;
    }
  }
}
