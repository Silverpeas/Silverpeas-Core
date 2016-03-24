package org.silverpeas.core.contact.model;

import java.util.Date;

import org.silverpeas.core.admin.user.model.UserFull;

public interface Contact {

  ContactPK getPK();

  String getFirstName();

  String getLastName();

  String getEmail();

  String getPhone();

  String getFax();

  Date getCreationDate();

  String getCreatorId();

  String getUserId();

  UserFull getUserFull();

}
