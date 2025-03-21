/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.socialnetwork.model;

import org.silverpeas.core.persistence.datasource.model.CompositeEntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "sb_sn_externalaccount")
@NamedQuery(name = "ExternalAccount.findBySilverpeasUserId",
    query = "select e FROM ExternalAccount e WHERE e.silverpeasUserId = :silverpeasUserId")
public class ExternalAccount
    extends BasicJpaEntity<ExternalAccount, ExternalAccountIdentifier>
    implements Serializable {

  @Column(name = "silverpeasUserId")
  private String silverpeasUserId = null;

  public void setExternalId(SocialNetworkID socialNetworkID, String profileId) {
    setId(socialNetworkID.name() + CompositeEntityIdentifier.COMPOSITE_SEPARATOR + profileId);
  }

  public SocialNetworkID getNetworkId() {
    return SocialNetworkID.from(getStringIds()[0]);
  }

  private String[] getStringIds() {
    return getId().split(CompositeEntityIdentifier.COMPOSITE_SEPARATOR);
  }

  public String getProfileId() {
    return getStringIds()[1];
  }

  public String getSilverpeasUserId() {
    return silverpeasUserId;
  }

  public void setSilverpeasUserId(String silverpeasUserId) {
    this.silverpeasUserId = silverpeasUserId;
  }
}
