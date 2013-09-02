/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.socialnetwork.model;

import java.io.Serializable;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class AccountId implements Serializable {
  private static final long serialVersionUID = -9044047461214852788L;

  @Enumerated(EnumType.STRING)
  private SocialNetworkID networkId = null;
  private String profileId = null;

  public String getProfileId() {
    return profileId;
  }

  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  public SocialNetworkID getNetworkId() {
    return networkId;
  }

  public void setNetworkId(SocialNetworkID networkId) {
    this.networkId = networkId;
  }

  public AccountId(SocialNetworkID networkId, String profileId) {
    super();
    this.networkId = networkId;
    this.profileId = profileId;
  }

  public AccountId() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((networkId == null) ? 0 : networkId.hashCode());
    result = prime * result + ((profileId == null) ? 0 : profileId.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AccountId other = (AccountId) obj;
    if (networkId != other.networkId)
      return false;
    if (profileId == null) {
      if (other.profileId != null)
        return false;
    } else if (!profileId.equals(other.profileId))
      return false;
    return true;
  }

}
