/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.list;

import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.viewgenerator.html.list.AccumulativeListPaneTag.State;

import java.util.List;

import static org.silverpeas.core.util.PaginationList.from;

/**
 * Iterate over items.
 * <p>If an instance of {@link SilverpeasList} is given, optimizations are offered</p>
 * @author silveryocha
 */
public class AccumulativeListItemsTag extends AbstractListItemsTag {
  private static final long serialVersionUID = 8537592673853508370L;

  @Override
  protected <T> SilverpeasList<T> optimize(final List<T> list) {
    final AccumulativeListPaneTag pane = getListPane();
    final SilverpeasList<T> silverpeasList = SilverpeasList.wrap(list);
    final int originalListSize = (int) silverpeasList.originalListSize();
    // Getting (and initializing if necessary) the pagination
    final State state = pane.getState();
    final int batchSize = state.getBatchSize();
    final int currentIndex;
    if (state.isFirstDisplay()) {
      currentIndex = 0;
    } else if (silverpeasList.isSlice()) {
      currentIndex = state.getCurrentStartIndex() + batchSize;
    } else {
      final int offset = Math.max(originalListSize - state.getCurrentListSize(), 0);
      currentIndex = state.getCurrentStartIndex() + offset + batchSize;
    }
    state.setCurrentListSize(originalListSize);
    state.setCurrentStartIndex(currentIndex);
    final int nextIndex = Math.min(currentIndex + batchSize, originalListSize);
    state.setNextStartIndex(nextIndex);
    pane.setMoreItems(nextIndex < originalListSize);
    if (silverpeasList.isSlice()) {
      // The list is already paginated
      return silverpeasList;
    } else {
      // Computing the paginated list
      int lastIndex = Math.min(currentIndex + batchSize, originalListSize);
      return from(list.subList(currentIndex, lastIndex), list.size());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected AccumulativeListPaneTag getListPane() {
    return (AccumulativeListPaneTag) findAncestorWithClass(this, AccumulativeListPaneTag.class);
  }
}
