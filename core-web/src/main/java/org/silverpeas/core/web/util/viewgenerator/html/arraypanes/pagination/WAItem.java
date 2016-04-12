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

/*
 * WADataPage.java
 *
 * Created on 26 mars 2001, 09:58
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination;

/**
 * @author jpouyadou
 * @version
 */
public interface WAItem {
  public int getFieldCount();

  public String getFirstField();

  public String getLastField();

  public String getNextField();

  public String getPreviousField();

  public void toggleFieldState();

  public String getFieldByName(String name);

  /**
   * If the data to be displayed is anchorable, this returns its anchor, otherwise it returns null
   */
  public String getAnchorByName(String name);

  public void setDataPaginator(WADataPaginator parent);

  /**
   * This method returns the style (in the sense of element of a style sheet) used to represent this
   * item. It has to be held by the WAItem, because it is the only one to know the meaning of the
   * information it carries, and thus the only one to know what, for example, needs to be emphasized
   * via styles. A null value means 'use default'
   */
  public String getStyle();
}
