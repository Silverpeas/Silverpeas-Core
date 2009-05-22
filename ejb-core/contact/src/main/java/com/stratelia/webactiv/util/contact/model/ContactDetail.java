package com.stratelia.webactiv.util.contact.model;

import java.io.Serializable;
import java.util.Date;


/**
 * This object contains the description of a contact
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
		String email, String phone, String fax, String userId,
        Date creationDate, String creatorId)  {
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
		String email, String phone, String fax, String userId,
        Date creationDate, String creatorId)  {
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
  
  public String getFirstName(){
    return firstName;
  }
  
   public void setFirstName(String firstName){
    this.firstName = firstName;
  }
  
  public String getLastName(){
    return lastName;
  }
  
   public void setLastName(String lastName){
    this.lastName = lastName;
  }

  public String getEmail(){
    return email;
  }
  public void setEmail(String email){
    this.email = email;
  }

  public String getPhone(){
    return phone;
  }
  public void setPhone(String phone){
    this.phone = phone;
  }

  public String getFax(){
    return fax;
  }
  public void setFax(String fax){
    this.fax = fax;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  
  public String getCreatorId(){
    return creatorId;
  }

  public String getUserId(){
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
    result = result + "  getPK().getCo() = " + getPK().getComponentName() + "\n";
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