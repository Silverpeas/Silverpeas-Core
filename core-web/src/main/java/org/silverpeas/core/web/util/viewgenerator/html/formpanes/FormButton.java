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
 * FormButton.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author frageade
 * @version
 */
public abstract class FormButton extends FormLine {

  /**
   * Constructor declaration
   * @param nam
   * @param val
   * @see
   */
  public FormButton(String nam, String val) {
    super(nam, val);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String printDemo();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String toXML();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String toLineXML();

  /**
   * Method declaration
   * @param nam
   * @param url
   * @param pc
   * @return
   * @see
   */
  public abstract FormPane getDescriptor(String nam, String url, PageContext pc);

  /**
   * Method declaration
   * @param req
   * @see
   */
  public abstract void getConfigurationByRequest(HttpServletRequest req);

}
