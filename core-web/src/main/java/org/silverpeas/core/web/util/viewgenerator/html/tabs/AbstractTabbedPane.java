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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * TabbedPane.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package org.silverpeas.core.web.util.viewgenerator.html.tabs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squere
 * @version
 */
public abstract class AbstractTabbedPane implements TabbedPane {

  List<Tab> tabs = new ArrayList<>();

  /**
   * Constructor declaration
   * @see
   */
  public AbstractTabbedPane() {
  }


  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @see
   */
  public Tab addTab(String label, String action, boolean disabled) {
    Tab tab = new Tab(label, action, disabled);
    tabs.add(tab);
    return tab;
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @param enabled
   * @see
   */
  public Tab addTab(String label, String action, boolean disabled, boolean enabled) {
    Tab tab = new Tab(label, action, disabled, enabled);
    tabs.add(tab);
    return tab;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public List<Tab> getTabs() {
    return tabs;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();

}