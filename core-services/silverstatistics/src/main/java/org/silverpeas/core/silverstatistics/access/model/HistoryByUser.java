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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.silverstatistics.access.model;

import org.silverpeas.core.admin.user.model.User;

import java.io.Serializable;
import java.util.Date;

/**
 * Class declaration
 * @author silveryocha
 */
public class HistoryByUser implements Serializable {
  private static final long serialVersionUID = 2990221033670463787L;

  private String userId;
  private Date lastAccess;
  private int nbAccess;
  private User user;

  /**
   * Default constructor
   * @param userId the user identifier
   * @param lastAccess the last access date
   * @param nbAccess the number of access
   */
  public HistoryByUser(String userId, Date lastAccess, int nbAccess) {
    this.userId = userId;
    this.lastAccess = lastAccess;
    this.nbAccess = nbAccess;
  }

  /**
   * Default constructor
   * @param user the user
   * @param lastAccess the last access date
   * @param nbAccess the number of access
   */
  public HistoryByUser(User user, Date lastAccess, int nbAccess) {
    this(user.getId(), lastAccess, nbAccess);
    this.user = user;
  }

  public String getUserId() {
    return userId;
  }

  public User getUser() {
    if (user == null) {
      user = User.getById(userId);
    }
    return user;
  }

  public Date getLastAccess() {
    return lastAccess;
  }

  public int getNbAccess() {
    return nbAccess;
  }
}
