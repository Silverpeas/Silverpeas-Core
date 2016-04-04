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

/* $Id$ */
package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.WAPrimaryKey;

import java.io.Serializable;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class PublicationPK extends WAPrimaryKey implements Serializable {
  private static final long serialVersionUID = -6957633959517435029L;

  /**
   * Constructor which set only the id
   * @param id
   * @since 1.0
   */
  public PublicationPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @param id
   * @param space
   * @param componentName
   * @since 1.0
   */
  public PublicationPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public PublicationPK(String id, String componentId) {
    super(id, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @param id
   * @param pk
   * @since 1.0
   */
  public PublicationPK(String id, WAPrimaryKey pk) {
    super(id, pk.getSpace(), pk.getInstanceId());
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   * @since 1.0
   */
  @Override
  public String getRootTableName() {
    return "Publication";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   */
  @Override
  public String getTableName() {
    return "SB_Publication_Publi";
  }

  /**
   * Check if an another object is equal to this object
   * @param obj the object to compare to this PublicationPK
   * @return true if other is equals to this object
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PublicationPK)) {
      return false;
    }
    PublicationPK other = (PublicationPK) obj;
    if (id == null) {
      return (other.id == null);
    }
    return (id.equals(other.id));
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
