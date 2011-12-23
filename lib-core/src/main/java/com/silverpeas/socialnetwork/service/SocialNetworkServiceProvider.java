package com.silverpeas.socialnetwork.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.socialnetwork.model.SocialNetworkID;

public class SocialNetworkServiceProvider {

	@Inject @Named("serviceFacebook")
	private SocialNetworkService facebook = null;

	@Inject @Named("serviceLinkedIn")
	private SocialNetworkService linkedIn = null;

	private static SocialNetworkServiceProvider instance = null;

	private SocialNetworkServiceProvider() {}

	static public SocialNetworkServiceProvider getInstance() {
		if (instance == null) {
			instance = new SocialNetworkServiceProvider();
		}
		return instance;
	}

	/**
	 * Get social network service implementation specific to given social network
	 *
	 * @param networkid		enum representing network id
	 *
	 * @return
	 */
	public SocialNetworkService getSocialNetworkService(SocialNetworkID networkId) {
		switch(networkId) {
		case FACEBOOK:
			return facebook;

		case LINKEDIN:
			return linkedIn;
		}

		return null;
	}

	/**
	 * Get social network service implementation specific to given social network
	 *
	 * @param networkIdAsString		network id as String
	 *
	 * @return
	 */
	public SocialNetworkService getSocialNetworkService(String networkIdAsString) {
		SocialNetworkID networkId = SocialNetworkID.valueOf(networkIdAsString);
		switch(networkId) {
		case FACEBOOK:
			return facebook;

		case LINKEDIN:
			return linkedIn;
		}

		return null;
	}

}
