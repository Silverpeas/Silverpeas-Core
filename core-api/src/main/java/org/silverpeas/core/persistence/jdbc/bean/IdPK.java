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

package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.WAPrimaryKey;

public class IdPK extends WAPrimaryKey {

  private static final long serialVersionUID = -5451371913200985128L;

  public IdPK() {
    super("");
  }

  public IdPK(String id) {
    super(id);
  }

  public IdPK(int id) {
    super(Integer.toString(id));
  }

  /**
   * IdPK(String id, WAPrimaryKey value)
   */
  public IdPK(String id, WAPrimaryKey value) {
    super(id, value);
  }

  /**
   * equals
   */
  public boolean equals(Object other) {
    if (!(other instanceof IdPK)) {
      return false;
    }
    return (getId().equals(((IdPK) other).getId()));
  }

  /**
   * setIdAsLong( long value )
   */
  public void setIdAsLong(long value) {
    setId(Long.toString(value));
  }

  /**
   * getIdAsLong()
   */
  public long getIdAsLong() {
    return new Integer(getId()).longValue();
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 56 * hash + (this.id != null ? this.id.hashCode() : 0);
    return hash;
  }
}
