package com.silverpeas.socialnetwork.model;

/**
 * Few information about silverpeas account.
 *
 * @author Ludovic Bertin
 *
 */
public class Account {
	private String silverpeasId = null;
	private String silverpeasLogin = null;

	public String getSilverpeasId() {
		return silverpeasId;
	}

	public void setSilverpeasId(String silverpeasId) {
		this.silverpeasId = silverpeasId;
	}

	public String getSilverpeasLogin() {
		return silverpeasLogin;
	}

	public void setSilverpeasLogin(String silverpeasLogin) {
		this.silverpeasLogin = silverpeasLogin;
	}
}
