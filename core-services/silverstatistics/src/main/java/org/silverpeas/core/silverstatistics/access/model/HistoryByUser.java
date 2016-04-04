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

package org.silverpeas.core.silverstatistics.access.model;

import java.io.Serializable;
import java.util.Date;

import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * Class declaration
 * @author
 */
public class HistoryByUser implements Serializable {

  private static final long serialVersionUID = 1L;

  private UserDetail user;
  private Date lastAccess;
  private int nbAccess;

  /**
   * Default constructor
   * @param user the user detail
   * @param lastAccess the last access date
   * @param nbAccess the number of access
   */
  public HistoryByUser(UserDetail user, Date lastAccess, int nbAccess) {
    this.lastAccess = lastAccess;
    this.user = user;
    this.nbAccess = nbAccess;
  }

  public Date getLastAccess() {
    return lastAccess;
  }

  public UserDetail getUser() {
    return user;
  }

  public int getNbAccess() {
    return nbAccess;
  }

  public void setLastAccess(Date lastAccess) {
    this.lastAccess = lastAccess;
  }

  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }

  public void setUser(UserDetail user) {
    this.user = user;
  }

}
