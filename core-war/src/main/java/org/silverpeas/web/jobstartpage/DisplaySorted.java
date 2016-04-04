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

package org.silverpeas.web.jobstartpage;

public class DisplaySorted implements Comparable<DisplaySorted> {
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_COMPONENT = 1;
  public static final int TYPE_SPACE = 2;
  public static final int TYPE_SUBSPACE = 3;

  public String name = "";
  public int orderNum = 0;
  public String id = "";
  public String htmlLine = "";
  public int type = TYPE_UNKNOWN;
  public int deep = 0;
  public boolean isAdmin = true;
  public boolean isVisible = true;

  public int compareTo(DisplaySorted o) {
    return orderNum - o.orderNum;
  }

  public void copy(DisplaySorted src) {
    name = src.name;
    orderNum = src.orderNum;
    id = src.id;
    htmlLine = src.htmlLine;
    type = src.type;
    deep = src.deep;
    isAdmin = src.isAdmin;
    isVisible = src.isVisible;
  }

  public boolean isAdmin() {
    return isAdmin;
  }
}
