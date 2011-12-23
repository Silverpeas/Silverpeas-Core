package com.silverpeas.admin.service;

import com.stratelia.webactiv.beans.admin.AdminException;


public interface UserService {
	String registerUser(String firstName, String lastName, String email) throws AdminException;
}
