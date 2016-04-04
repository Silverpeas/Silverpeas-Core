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

package org.silverpeas.core.web.treemenu.process;

import org.silverpeas.core.web.treemenu.model.MenuRuntimeException;
import org.silverpeas.core.web.treemenu.model.TreeFilter;
import org.silverpeas.core.web.treemenu.model.TreeFilterDefault;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

/**
 * Allows getting a TreeFilter implementation
 */
public class TreeFilterFactory {

  /**
   *
   */
  private TreeFilterFactory() {
  }

  /**
   * Gets a TreeFilter implementation relating to the filterName parameter's. If no implementation
   * is found the default implementation is returned. This default implementation allow displaying
   * all menu elements @see {@link TreeFilterDefault}
   * @param filterName key to retrieve the implementation instance
   * @return a TreeFiler implementation
   */
  public static TreeFilter getTreeFilter(String filterName) {
    TreeFilter filter = null;
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.treeMenu.TreeMenu");
    String className =
        settings.getString(filterName, "org.silverpeas.core.web.treemenu.model.TreeFilterDefault");
    if (StringUtil.isDefined(className)) {
      try {
        return (TreeFilter) Class.forName(className).newInstance();
      } catch (Exception e) {
        throw new MenuRuntimeException("TreeFilterFactory.getTreeFilter()",
            SilverpeasRuntimeException.ERROR,
            "treeMenu.EX_BUILDING_MENU_FAILED", e);
      }
    }
    return filter;
  }

}
