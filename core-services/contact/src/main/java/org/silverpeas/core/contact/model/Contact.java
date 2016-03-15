package org.silverpeas.core.contact.model;

import java.util.Date;

import com.stratelia.webactiv.beans.admin.UserFull;
import org.silverpeas.core.contact.model.ContactPK;

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
