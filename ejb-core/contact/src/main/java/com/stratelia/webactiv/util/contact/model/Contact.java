package com.stratelia.webactiv.util.contact.model;

import java.util.Date;

import com.stratelia.webactiv.beans.admin.UserFull;

public interface Contact {

  public ContactPK getPK();

  public String getFirstName();

  public String getLastName();

  public String getEmail();

  public String getPhone();

  public String getFax();

  public Date getCreationDate();

  public String getCreatorId();

  public String getUserId();

  public UserFull getUserFull();
  
}
