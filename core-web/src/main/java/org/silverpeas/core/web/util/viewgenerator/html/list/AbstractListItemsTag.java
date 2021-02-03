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

import org.apache.taglibs.standard.tag.rt.core.ForEachTag;
import org.silverpeas.core.util.SilverpeasList;

import javax.servlet.jsp.JspTagException;
import java.util.List;

/**
 * Centralizing code about iterating over items.
 * <p>If an instance of {@link SilverpeasList} is given, optimizations are offered</p>
 * @author silveryocha
 */
public abstract class AbstractListItemsTag extends ForEachTag {
  private static final long serialVersionUID = -6470873825192693523L;

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void setItems(final Object items) throws JspTagException {
    if (items instanceof List) {
      final SilverpeasList optimized = optimize((List) items);
      getListPane().setNbItems((int) optimized.originalListSize());
      super.setItems(optimized);
    } else {
      super.setItems(items);
    }
  }

  protected abstract <T> SilverpeasList<T> optimize(final List<T> list);

  protected abstract <T extends AbstractListPaneTag> T getListPane();
}
