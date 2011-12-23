package com.silverpeas.socialnetwork.service;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SocialNetworkAuthorizationException extends SilverpeasException {

	@Override
	public String getModule() {
		return "socialNetwork";
	}

	public SocialNetworkAuthorizationException(String callingClass,
			int errorLevel, String message, Exception nested) {
		super(callingClass, errorLevel, message, nested);
	}

	public SocialNetworkAuthorizationException(String callingClass,
			int errorLevel, String message, String extraParams, Exception nested) {
		super(callingClass, errorLevel, message, extraParams, nested);
	}

	public SocialNetworkAuthorizationException(String callingClass,
			int errorLevel, String message, String extraParams) {
		super(callingClass, errorLevel, message, extraParams);
	}

	public SocialNetworkAuthorizationException(String callingClass,
			int errorLevel, String message) {
		super(callingClass, errorLevel, message);
	}
}
