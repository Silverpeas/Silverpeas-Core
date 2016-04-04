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
 * Button.java
 *
 * Created on 10 octobre 2000, 16:16
 */

package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

/**
 * @author neysseri
 * @version
 */
public interface Button extends SimpleGraphicElement {

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @see
   */
  void init(String label, String action, boolean disabled);

  /**
   * This method permits to apply some process before calling the action.
   * The actionPreProcessing string must be a javascript routine in which the parameter {action}
   * will be replaced by the specified action on {@link #init(String, String, boolean)} method.
   * @param actionPreProcessing the javascript routine.
   */
  void setActionPreProcessing(String actionPreProcessing);

  /**
   * Method declaration
   * @return
   * @see
   */
  String print();

  /**
   * Method declaration
   * @param s
   * @see
   */
  @Deprecated
  void setRootImagePath(String s);
}
