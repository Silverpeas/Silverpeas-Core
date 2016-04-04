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

package org.silverpeas.core.contribution.publication.model;

/**
 * @author bourakbi
 */

public class PublicationWithStatus {

  private PublicationDetail pub;
  private boolean update;

  public PublicationWithStatus(PublicationDetail pub, boolean update) {
    this.pub = pub;
    this.update = update;
  }

  public PublicationDetail getPublication() {
    return pub;
  }

  public boolean isUpdate() {
    return update;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pub == null) ? 0 : pub.hashCode());
    result = prime * result + (update ? 1231 : 1237);
    return result;
  }

  /*
   * (non-Javadoc)
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
    if (!(obj instanceof PublicationWithStatus)) {
      return false;
    }
    PublicationWithStatus other = (PublicationWithStatus) obj;
    if (pub == null) {
      if (other.pub != null) {
        return false;
      }
    } else if (!pub.equals(other.pub)) {
      return false;
    }
    if (update != other.update) {
      return false;
    }
    return true;
  }

}
