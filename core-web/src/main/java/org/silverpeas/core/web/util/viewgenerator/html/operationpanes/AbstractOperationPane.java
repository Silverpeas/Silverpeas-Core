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
 * ArrayPaneWA.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package org.silverpeas.core.web.util.viewgenerator.html.operationpanes;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public abstract class AbstractOperationPane implements OperationPane {

  private OperationPaneType type = OperationPaneType.component;
  private Vector<String> stack = null;
  private List<String> creationItems = null;
  private LocalizationBundle multilang;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractOperationPane() {
    stack = new Vector<String>();
    creationItems = new ArrayList<String>();
  }

  @Override
  public void setType(final OperationPaneType type) {
    this.type = type;
  }

  @Override
  public OperationPaneType getType() {
    return type;
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
  public Vector<String> getStack() {
    return this.stack;
  }

  public List<String> getCreationItems() {
    return creationItems;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public int nbOperations() {
    int nbOperations = 0;

    if (getStack() != null) {
      nbOperations = getStack().size();
    }
    return nbOperations;
  }

  @Override
  public void setMultilang(LocalizationBundle multilang) {
    this.multilang = multilang;
  }

  public LocalizationBundle getMultilang() {
    return multilang;
  }

  public boolean highlightCreationItems() {
    return GraphicElementFactory.getSettings().getBoolean("menu.actions.creation.highlight", true);
  }

  @Override
  public void addOperation(String iconPath, String label, String action) {
    addOperation(iconPath, label, action, null);
  }

  @Override
  public void addOperationOfCreation(String icon, String label, String action) {
    addOperationOfCreation(icon, label, action, null);
  }
}
