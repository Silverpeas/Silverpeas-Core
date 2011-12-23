package com.silverpeas.socialnetwork.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity @IdClass(AccountId.class)
@Table(name = "sb_socialnetwork_externalaccount")
public class ExternalAccount {

	@Id
	private String profileId = null;

	@Id @Enumerated(EnumType.STRING)
	private SocialNetworkID networkId = null;

	@Column(name="silverpeasUserId")
	private String silverpeasUserId = null;

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

	public String getSilverpeasUserId() {
		return silverpeasUserId;
	}

	public void setSilverpeasUserId(String silverpeasUserId) {
		this.silverpeasUserId = silverpeasUserId;
	}


}
