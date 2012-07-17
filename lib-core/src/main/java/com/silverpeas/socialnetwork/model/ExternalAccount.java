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
 * "http://www.silverpeas.org/legal/licensing"
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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
// @IdClass(AccountId.class)
@Table(name = "sb_sn_externalaccount")
@NamedQueries( { @NamedQuery(name = "ExternalAccount.findBySilverpeasUserId", query = "select e FROM ExternalAccount e WHERE e.silverpeasUserId = :silverpeasUserId") })
public class ExternalAccount {

  @EmbeddedId
  private AccountId accountId = new AccountId();

  @Column(name = "silverpeasUserId")
  private String silverpeasUserId = null;

  public String getProfileId() {
    return accountId.getProfileId();
  }

  public void setProfileId(String profileId) {
    accountId.setProfileId(profileId);
  }

  public SocialNetworkID getNetworkId() {
    return accountId.getNetworkId();
  }

  public void setNetworkId(SocialNetworkID networkId) {
    accountId.setNetworkId(networkId);
  }

  public String getSilverpeasUserId() {
    return silverpeasUserId;
  }

  public void setSilverpeasUserId(String silverpeasUserId) {
    this.silverpeasUserId = silverpeasUserId;
  }

}
