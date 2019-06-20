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
package org.silverpeas.core.web.util.viewgenerator.html.list;

import org.apache.taglibs.standard.tag.rt.core.ForEachTag;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.servlet.jsp.JspTagException;
import java.util.List;

/**
 * Iterate over items.
 * <p>If an instance of {@link SilverpeasList} is given, optimizations are offered</p>
 * @author Yohann Chastagnier
 */
public class ListItemsTag extends ForEachTag {

  @Override
  public void setItems(final Object items) throws JspTagException {
    if (items instanceof List) {
      optimize((List) items);
    } else {
      super.setItems(items);
    }
  }

  @SuppressWarnings("unchecked")
  private void optimize(final List list) throws JspTagException {
    final ListPaneTag pane = getListPane();
    final SilverpeasList silverpeasList = SilverpeasList.wrap(list);
    if (silverpeasList.isSlice()) {
      // The list is already paginated
      pane.setNbItems((int) silverpeasList.originalListSize());
      super.setItems(silverpeasList);
    } else {
      // Getting (and initializing) the pagination
      final Pagination pagination = pane.getPagination(list.size());
      // Computing the paginated list
      final SilverpeasList paginatedList = pagination.getPaginatedListFrom(silverpeasList);
      // Set the new paginated list as items provider
      pane.setNbItems((int) silverpeasList.originalListSize());
      super.setItems(paginatedList);
    }
  }

  private ListPaneTag getListPane() {
    return (ListPaneTag) findAncestorWithClass(this, ListPaneTag.class);
  }
}
