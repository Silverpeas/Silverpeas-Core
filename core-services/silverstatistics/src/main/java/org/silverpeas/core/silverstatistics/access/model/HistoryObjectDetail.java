/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.silverstatistics.access.model;

import java.io.Serializable;
import java.util.Date;

import org.silverpeas.core.ResourceReference;

/**
 * @author
 */
public class HistoryObjectDetail implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date date;
  private String userId;
  private ResourceReference resourceReference;

  /**
   * Constructor declaration
   * @param date
   * @param userId
   * @param resourceReference
   *
   */
  public HistoryObjectDetail(Date date, String userId, ResourceReference resourceReference) {
    this.date = date;
    this.userId = userId;
    this.resourceReference = resourceReference;
  }

  /**
   * @return
   */
  public Date getDate() {
    return date;
  }

  /**
   * @return
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @return
   */
  public ResourceReference getResourceReference() {
    return resourceReference;
  }

}
