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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * TabbedPane.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package org.silverpeas.core.web.util.viewgenerator.html.tabs;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

/**
 * TabbedPane is an interface to be implemented by a graphic element to print tabs in an html
 * format.
 * @author squere
 * @version 1.0
 */
public interface TabbedPane extends SimpleGraphicElement {

  /**
   * Add a new tab to our TabPane.
   * @param label The label of the tab.
   * @param action The action associated with this pane. Exemple : "javascript:onClick=viewByDay()"
   * @param selected Specify if the tab is selected.
   */
  public Tab addTab(String label, String action, boolean selected);

  /**
   * Add a new tab to our TabPane.
   * @param label The label of the tab.
   * @param action The action associated with this pane. Exemple : "javascript:onClick=viewByDay()"
   * @param selected Specify if the tab is selected.
   * @param enabled Specify if the tab is enabled. If enabled is false, the action won't be
   * possible.
   */
  public Tab addTab(String label, String action, boolean selected, boolean enabled);

  /**
   * Print the TabbedPane in an html format
   * @return The TabbedPane representation
   */
  public String print();

}