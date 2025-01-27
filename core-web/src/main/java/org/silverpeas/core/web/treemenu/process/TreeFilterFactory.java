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
package org.silverpeas.core.web.treemenu.process;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.treemenu.model.MenuConstants;
import org.silverpeas.core.web.treemenu.model.MenuRuntimeException;
import org.silverpeas.core.web.treemenu.model.TreeFilter;
import org.silverpeas.core.web.treemenu.model.TreeFilterDefault;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.util.function.Supplier;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Allows getting a TreeFilter implementation
 */
public class TreeFilterFactory {

  private TreeFilterFactory() {
  }

  /**
   * Gets a TreeFilter implementation relating to the filterName parameter's. If no implementation
   * is found the default implementation is returned. This default implementation allow displaying
   * all menu elements @see {@link TreeFilterDefault}
   *
   * @param filterName key to retrieve the implementation instance
   * @return a TreeFiler implementation or null if no filter is found
   * @throws MenuRuntimeException if no filter is found.
   */
  public static TreeFilter getTreeFilter(String filterName) {
    String defaultFilter = MenuConstants.DEFAULT_MENU_TYPE + TreeFilter.NAME_POSTFIX;
    String filterType = isDefined(filterName) ? filterName + TreeFilter.NAME_POSTFIX :
        defaultFilter;
    return providesOrElse(filterType,
        providesOrElse(defaultFilter, FAIL)).get();
  }

  private static Supplier<TreeFilter> providesOrElse(String filter,
      Supplier<TreeFilter> defaultSupplier) {
    return () -> {
      try {
        return ServiceProvider.getService(filter);
      } catch (SilverpeasRuntimeException e) {
        return defaultSupplier.get();
      }
    };
  }

  private static final Supplier<TreeFilter> FAIL = () -> {
    throw new MenuRuntimeException("No default tree filter found!");
  };
}
