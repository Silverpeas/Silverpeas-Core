package com.silverpeas.socialnetwork.service;

import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;

public class AccessToken {
	private AccessGrant accessGrant = null;
	private OAuthToken oAuthToken = null;

	public AccessToken(AccessGrant accessGrant) {
		super();
		this.accessGrant = accessGrant;
	}
	public AccessToken(OAuthToken oAuthToken) {
		super();
		this.oAuthToken = oAuthToken;
	}

	public AccessGrant getAccessGrant() {
		return accessGrant;
	}

	public void setAccessGrant(AccessGrant accessGrant) {
		this.accessGrant = accessGrant;
	}

	public OAuthToken getoAuthToken() {
		return oAuthToken;
	}

	public void setoAuthToken(OAuthToken oAuthToken) {
		this.oAuthToken = oAuthToken;
	}

}
