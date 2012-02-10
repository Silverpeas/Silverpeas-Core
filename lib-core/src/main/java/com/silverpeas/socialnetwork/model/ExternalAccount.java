package com.silverpeas.socialnetwork.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
//@IdClass(AccountId.class)
@Table(name = "sb_socialnetwork_externalaccount")
@NamedQueries({
  @NamedQuery(name = "ExternalAccount.findBySilverpeasUserId", query = "select e FROM ExternalAccount e WHERE e.silverpeasUserId = :silverpeasUserId")
  })
  public class ExternalAccount {

  @EmbeddedId
  private AccountId accountId = new AccountId();

	@Column(name="silverpeasUserId")
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
