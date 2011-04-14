/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

package com.stratelia.webactiv.util.statistic.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Class declaration
 * @author
 */
public class StatisticResultDetail implements Serializable {

  private static final long serialVersionUID = 1L;

  private WAPrimaryKey pk;
  private String result;
  private Object detail = null;

  /**
   * Constructor declaration
   * @param pk
   * @param result
   * @see
   */
  public StatisticResultDetail(WAPrimaryKey pk, String result) {
    this.pk = pk;
    this.result = result;
  }

  /**
   * Constructor declaration
   * @param pk
   * @param result
   * @param detail
   * @see
   */
  public StatisticResultDetail(WAPrimaryKey pk, String result, Object detail) {
    this.pk = pk;
    this.result = result;
    this.detail = detail;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public WAPrimaryKey getPK() {
    return pk;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getResult() {
    return result;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Object getDetail() {
    return detail;
  }

  /**
   * Method declaration
   * @param detail
   * @see
   */
  public void setDetail(Object detail) {
    this.detail = detail;
  }

  /**
   * Method declaration
   * @param pk
   * @param list
   * @return
   * @see
   */
  static public StatisticResultDetail getFromCollection(WAPrimaryKey pk,
      Collection<StatisticResultDetail> list) {
    Iterator<StatisticResultDetail> i = list.iterator();

    while (i.hasNext()) {
      try {
        StatisticResultDetail detail = i.next();

        if (detail.getPK().equals(pk)) {
          return detail;
        }
      } catch (Exception e) {
      }
    }
    return null;
  }

}
