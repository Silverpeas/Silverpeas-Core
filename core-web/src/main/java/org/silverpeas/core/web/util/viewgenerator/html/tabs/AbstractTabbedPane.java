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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * TabbedPane.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package org.silverpeas.core.web.util.viewgenerator.html.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

/**
 * @author squere
 * @version
 */
public abstract class AbstractTabbedPane implements TabbedPane {

  public static final int RIGHT = 0;
  public static final int LEFT = 1;

  private Vector<Collection<Tab>> tabLines = null; // A collection tabs vector
  private int nbLines = 1;
  // private Collection tabs = null;
  private int indentation = RIGHT;

  // private String iconsPath = null;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractTabbedPane() {
  }

  /**
   * Method declaration
   * @param nbLines
   * @see
   */
  public void init(int nbLines) {
    Vector<Collection<Tab>> tabLines = new Vector<Collection<Tab>>(2, 1);

    for (int i = 1; i <= nbLines; i++) {
      tabLines.add(new ArrayList<Tab>());
    }
    this.nbLines = nbLines;
    this.tabLines = tabLines;
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @see
   */
  public void addTab(String label, String action, boolean disabled) {
    Vector<Collection<Tab>> tabLines = getTabLines();
    Collection<Tab> tabs = tabLines.get(0);

    tabs.add(new Tab(label, action, disabled));
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @param enabled
   * @see
   */
  public void addTab(String label, String action, boolean disabled,
      boolean enabled) {
    Vector<Collection<Tab>> tabLines = getTabLines();
    Collection<Tab> tabs = tabLines.get(0);

    tabs.add(new Tab(label, action, disabled, enabled));
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @param nbLines
   * @see
   */
  public void addTab(String label, String action, boolean disabled, int nbLines) {
    Vector<Collection<Tab>> tabLines = getTabLines();
    Collection<Tab> tabs = null;

    if (nbLines <= 0) {
      tabs = tabLines.get(0);
    } else {
      tabs = tabLines.get(nbLines - 1);
    }
    tabs.add(new Tab(label, action, disabled));
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @param enabled
   * @param nbLines
   * @see
   */
  public void addTab(String label, String action, boolean disabled,
      boolean enabled, int nbLines) {
    Vector<Collection<Tab>> tabLines = getTabLines();
    Collection<Tab> tabs = null;

    if (nbLines <= 0) {
      tabs = tabLines.get(0);
    } else {
      tabs = tabLines.get(nbLines - 1);
    }
    tabs.add(new Tab(label, action, disabled, enabled));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Vector<Collection<Tab>> getTabLines() {
    return this.tabLines;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getNbLines() {
    return this.nbLines;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();

  /* onglet cale a gauche */

  /**
   * Method declaration
   * @see
   */
  public void setIndentationLeft() {
    indentation = LEFT;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getIndentation() {
    return this.indentation;
  }

  /* cas ou les pages JSP ne soient pas toutes au meme niveau */
  /* DEPRECATED */

  /**
   * Method declaration
   * @param level
   * @see
   */
  public void setLevelRootImage(int level) {
    // this.levelPath = level;
  }

}
