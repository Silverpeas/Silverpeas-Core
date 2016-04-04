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
 * Created on 13 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.silverpeas.web.silverstatistics.control;

/**
 * @author BERTINL
 */
public class StatItem {
  private final long[] countValues;
  private String cmpId;
  private String name;

  /**
   * Default constructor
   * @param cmpId the component identifier
   * @param name
   * @param count
   */
  public StatItem(String cmpId, String name, long[] count) {
    this.cmpId = cmpId;
    this.name = name;
    if (count != null) {
      this.countValues = count.clone();
    } else {
      this.countValues = new long[0];
    }
  }

  /**
   * @return Returns the CountValues.
   */
  public long[] getCountValues() {
    return this.countValues.clone();
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return Returns the cmpId.
   */
  public String getCmpId() {
    return cmpId;
  }
}
