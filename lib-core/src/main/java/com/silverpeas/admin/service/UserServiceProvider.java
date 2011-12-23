package com.silverpeas.admin.service;

import javax.inject.Inject;
import javax.inject.Named;

public class UserServiceProvider {

	@Inject @Named("silverpeasUserService")
	private UserService userService = null;

	private static UserServiceProvider instance = null;

	private UserServiceProvider() {}

	static public UserServiceProvider getInstance() {
		if (instance == null) {
			instance = new UserServiceProvider();
		}
		return instance;
	}

	public UserService getService() {
		return userService;
	}

}
