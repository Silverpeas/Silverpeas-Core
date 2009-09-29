/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.contact.model;

import java.io.Serializable;
import java.util.Date;

/**
 * This object contains the description of a contact
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class ContactDetail implements Serializable {

  private ContactPK pk;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String fax;
  private String userId;
  private Date creationDate;
  private String creatorId;

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

  public ContactPK getPK() {
    return pk;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getFax() {
    return fax;
  }

  public void setFax(String fax) {
    this.fax = fax;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public String getCreatorId() {
    return creatorId;
  }

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

  public String toString() {
    String result = "ContactDetail {" + "\n";
    result = result + "  getPK().getId() = " + getPK().getId() + "\n";
    result = result + "  getPK().getEd() = " + getPK().getSpace() + "\n";
    result = result + "  getPK().getCo() = " + getPK().getComponentName()
        + "\n";
    result = result + "  getFirstName() = " + getFirstName() + "\n";
    result = result + "  getLastName() = " + getLastName() + "\n";
    result = result + "  getEmail() = " + getEmail() + "\n";
    result = result + "  getPhone() = " + getPhone() + "\n";
    result = result + "  getFax() = " + getFax() + "\n";
    result = result + "  getUserId() = " + getUserId() + "\n";
    result = result + "  getCreationDate() = " + getCreationDate() + "\n";
    result = result + "  getCreatorId() = " + getCreatorId() + "\n";
    result = result + "}";
    return result;
  }
}