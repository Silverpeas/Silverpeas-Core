package com.silverpeas.socialnetwork.model;

import java.io.Serializable;

public class AccountId implements Serializable{
	private static final long serialVersionUID = -9044047461214852788L;

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

  /* (non-Javadoc)
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

  /* (non-Javadoc)
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
