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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * BrowseBar.java
 *
 * Created on 07 decembre 2000, 11:26
 */

package org.silverpeas.core.web.util.viewgenerator.html.operationpanes;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

/**
 * The Browse interface gives us the skeleton for all funtionnalities we need to display typical WA
 * browse bar
 * @author neysseri
 * @version 1.0
 */
public interface OperationPane extends SimpleGraphicElement {

  /**
   * Default type is {@link OperationPaneType#component}.
   * It is possible to change the type through this method.
   * @param type
   */
  void setType(OperationPaneType type);

  /**
   * Gets the type : {@link OperationPaneType}.
   */
  OperationPaneType getType();

  /**
   * Method declaration
   * @return
   *
   */
  public int nbOperations();

  /**
   * Method declaration
   * @param iconPath
   * @param label
   * @param action
   *
   */
  public void addOperation(String iconPath, String label, String action);

  /**
   * Method declaration
   * @param iconPath
   * @param label
   * @param action
   * @param id allows to easily identify operation
   *
   */
  public abstract void addOperation(String iconPath, String label, String action, String id);

  public void addOperationOfCreation(String iconPath, String label, String action);

  public void addOperationOfCreation(String iconPath, String label, String action, String id);

  /**
   * Method declaration
   *
   */
  public void addLine();

  public void setMultilang(LocalizationBundle multilang);

  /**
   * Removes all operations
   */
  public void clear();
}
