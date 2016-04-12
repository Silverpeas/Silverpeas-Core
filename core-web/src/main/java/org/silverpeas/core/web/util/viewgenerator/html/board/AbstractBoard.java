/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Board.java
 *
 * Created on 27 march 2001, 16:11
 */

package org.silverpeas.core.web.util.viewgenerator.html.board;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

/**
 * @author lloiseau
 * @version 1.0
 */
public abstract class AbstractBoard implements Board {

  // private String iconsPath = null;
  private String body = null;
  private String classes = null;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractBoard() {
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
   * add a string on the top of the frame.
   */
  public void addBody(String body) {
    this.body = body;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getBody() {
    return this.body;
  }

  public String getClasses() {
    return classes;
  }

  @Override
  public void setClasses(final String classes) {
    this.classes = classes;
  }
}
