/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contact.model;

import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.StringUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * This object contains the description of a contact
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class ContactDetail implements Contact, Serializable {
  private static final long serialVersionUID = 2773600943308714640L;

  private ContactPK pk;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String fax;
  private String userId;
  private Date creationDate;
  private String creatorId;

  private boolean userExtraDataRequired = false;
  private boolean userExtraDataLoaded = false;
  private UserFull userFull;

  public ContactDetail(ContactPK pk, String firstName, String lastName,
      String email, String phone, String fax, String userId, Date creationDate,
      String creatorId) {
    this.pk = pk;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.fax = fax;
    this.userId = userId;
    this.creationDate = creationDate;
    this.creatorId = creatorId;
  }

  public ContactDetail(String id, String firstName, String lastName,
      String email, String phone, String fax, String userId, Date creationDate,
      String creatorId) {
    this.pk = new ContactPK(id);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.fax = fax;
    this.userId = userId;
    this.creationDate = creationDate;
    this.creatorId = creatorId;
  }

  public ContactDetail(ContactPK pk) {
    this.pk = pk;
  }

  @Override
  public ContactPK getPK() {
    return pk;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getPhone() {
    if (StringUtil.isDefined(phone)) {
      return phone;
    } else if (getUserFull() != null) {
      return getUserFull().getValue("phone");
    } else {
      return null;
    }
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public String getFax() {
    if (StringUtil.isDefined(fax)) {
      return fax;
    } else if (getUserFull() != null) {
      return getUserFull().getValue("fax");
    } else {
      return null;
    }
  }

  public void setFax(String fax) {
    this.fax = fax;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Sets the indicator that permits to handle the loading of user extra data when necessary. (lazy
   * loading).
   * @param userExtraDataRequired true if required, false otherwise.
   */
  public void setUserExtraDataRequired(final boolean userExtraDataRequired) {
    this.userExtraDataRequired = userExtraDataRequired;
  }

  @Override
  public UserFull getUserFull() {
    if (userExtraDataRequired && !userExtraDataLoaded && getUserId() != null && userFull == null) {
      userFull = OrganizationController.get().getUserFull(getUserId());
      userExtraDataLoaded = true;
    }
    return userFull;
  }

  @Override
  public String toString() {
    String result = "ContactDetail {" + "\n";
    result = result + "  getPK() = " + getPK().toString() + "\n";
    result = result + "  getFirstName() = " + getFirstName() + "\n";
    result = result + "  getLastName() = " + getLastName() + "\n";
    result = result + "  getEmail() = " + getEmail() + "\n";
    result = result + "  getPhone() = " + getPhone() + "\n";
    result = result + "  getFax() = " + getFax() + "\n";
    result = result + "  getUserId() = " + getUserId() + "\n";
    result = result + "  getCreationDate() = " + getCreationDate() + "\n";
    result = result + "  getCreatorId() = " + getCreatorId() + "\n";
    result = result + "  getUserFull() = " + getUserFull() + "\n";
    result = result + "}";
    return result;
  }
}