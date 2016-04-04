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
package org.silverpeas.core.socialnetwork.model;

import org.silverpeas.core.persistence.datasource.model.CompositeEntityIdentifier;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class ExternalAccountIdentifier implements CompositeEntityIdentifier {
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

  public ExternalAccountIdentifier(SocialNetworkID networkId, String profileId) {
    super();
    this.networkId = networkId;
    this.profileId = profileId;
  }

  public ExternalAccountIdentifier() {
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ExternalAccountIdentifier other = (ExternalAccountIdentifier) obj;
    if (networkId != other.networkId) {
      return false;
    }
    if (profileId == null) {
      if (other.profileId != null) {
        return false;
      }
    } else if (!profileId.equals(other.profileId)) {
      return false;
    }
    return true;
  }

  @Override
  public String asString() {
    return networkId.name() + COMPOSITE_SEPARATOR + profileId;
  }

  @Override
  public ExternalAccountIdentifier fromString(final String... values) {
    this.networkId = SocialNetworkID.from(values[0]);
    this.profileId = values[1];
    return this;
  }
}
