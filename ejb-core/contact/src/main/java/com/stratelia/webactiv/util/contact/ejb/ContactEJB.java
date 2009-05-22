package com.stratelia.webactiv.util.contact.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.contact.info.InfoDAO;
import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.contact.model.ContactRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;

public class ContactEJB implements EntityBean {
  private EntityContext context;
  private ContactPK pk;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String fax;
  private String userId;
  private Date creationDate;
  private String creatorId;


  private boolean isModified = false;


  public ContactEJB() {
  }

  private Connection getConnection() {
      try {
           return DBUtil.makeConnection( JNDINames.CONTACT_DATASOURCE );
      } catch (Exception re) {
		   throw new ContactRuntimeException("ContactEJB.getConnection()",SilverpeasRuntimeException.ERROR,"root.EX_CONNECTION_OPEN_FAILED", re);
      }
  }

  private void closeConnection(Connection con)
  {
        try 
		{
             if (con != null)
               con.close();
        } 
		catch (SQLException re) 
		{
			throw new ContactRuntimeException("ContactEJB.closeConnection()",SilverpeasRuntimeException.ERROR,"root.EX_CONNECTION_CLOSE_FAILED", re);
		}
  }

  /**
	* Get the attributes of THIS contact
	* @return a ContactDetail
	* @see com.stratelia.webactiv.util.contact.model.ContactDetail
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public ContactDetail getDetail() {
	return new ContactDetail(pk, firstName, lastName, email, phone, fax, userId, creationDate, creatorId);
  }

  /**
	* Update the attributes of the contact
	* @param pubDetail the ContactDetail which contains updated data
	* @see com.stratelia.webactiv.util.contact.model.ContactDetail
	* @since 1.0
	*/
  public void setDetail(ContactDetail pubDetail) {
    if (pubDetail.getPK().equals(pk)) {
        if (pubDetail.getFirstName() != null)
              firstName = pubDetail.getFirstName();
        if (pubDetail.getLastName() != null)
              lastName = pubDetail.getLastName();
        if (pubDetail.getEmail() != null)
              email = pubDetail.getEmail();
        if (pubDetail.getPhone() != null)
              phone = pubDetail.getPhone();
        if (pubDetail.getFax() != null)
              fax = pubDetail.getFax();
        userId = pubDetail.getUserId();
        if (pubDetail.getCreationDate() != null)
              creationDate =  pubDetail.getCreationDate();
        if (pubDetail.getCreatorId() != null)
			creatorId = pubDetail.getCreatorId();
        isModified = true;
      } 
	  else 
	  {
		throw new ContactRuntimeException("ContactEJB.setDetail()",SilverpeasRuntimeException.ERROR,"contact.EX_SET_CONTACT_DETAIL_FAILED");
      }
  }

	/**
	* Add a new father to this contact
	* @param fatherPK the father NodePK
	* @see com.stratelia.webactiv.util.node.model.NodePK
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public void addFather(NodePK fatherPK) {
      Connection con = getConnection();
      try 
	  {
          ContactDAO.addFather(con, pk, fatherPK);
      } 
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.addFather()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_ADD_TO_FATHER_FAILED", re);
	  }
	  finally 
	  {
		closeConnection(con);
      }
  }

	/**
	* Remove a father to this contact
	* @param fatherPK the father NodePK to remove
	* @see com.stratelia.webactiv.util.node.model.NodePK
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public void removeFather(NodePK fatherPK) {
      Connection con = getConnection();
      try 
	  {
          ContactDAO.removeFather(con, pk, fatherPK);
      } 
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.removeFather()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_REMOVE_FROM_FATHER_FAILED", re);
	  }
	  finally 
	  {
		closeConnection(con);
      }
  }

	/**
	* Remove all fathers to this contact - this contact will be linked to no Node
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public void removeAllFather() {
      Connection con = getConnection();
      try 
	  {
          ContactDAO.removeAllFather(con, pk);
      } 
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.removeAllFather()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_REMOVE_FROM_ALLFATHERS_FAILED", re);
	  }
	  finally 
	  {
		closeConnection(con);
      }
  }

  	/**
	* Get all fathers of this contact
	* @return A collection of NodePK
	* @see com.stratelia.webactiv.util.node.model.NodePK
	* @see java.util.Collection
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public Collection getAllFatherPK() {
      Connection con = getConnection();
      try 
	  {
          Collection result = ContactDAO.getAllFatherPK(con, pk);
          return result;
      } 
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.getAllFatherPK()",SilverpeasRuntimeException.ERROR,"contact.EX_GET_CONTACT_FATHERS_FAILED", re);
	  }
	  finally 
	  {
		closeConnection(con);
      }
  }


 /**
	* Create or update info to this contact
	* @param modelId The modelId corresponding to the choosen model
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public void createInfo(String modelId) {
      Connection con = getConnection();
      try {
          InfoDAO.createInfo(con, modelId, this.pk);
      } 
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.createInfoDetail()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_INFOMODEL_CREATE_FAILED", re);
	  }
	  finally 
	  {
		closeConnection(con);
      }
  }

  /**
	* Get all info on contact
	* @return A completeContact
	* @see com.stratelia.webactiv.util.contact.model.CompleteContact
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public CompleteContact getCompleteContact(String modelId) {
      Connection con = getConnection();
      try {
          //get detail
          ContactDetail pubDetail = ContactDAO.loadRow(con, this.pk);
          
          return new CompleteContact(pubDetail, modelId);
      } 
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.getCompleteContact()",SilverpeasRuntimeException.ERROR,"contact.EX_GET_CONTACT_DETAIL_FAILED", re);
	  }
	  finally {
		closeConnection(con);
      }
  }

	/**
	* Create a new Contact object
	* @param pubDetail the ContactDetail which contains data
	* @return the ContactPK of the new Contact
	* @see com.stratelia.webactiv.util.contact.model.ContactDetail
	* @exception javax.ejb.CreateException
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public ContactPK ejbCreate(ContactDetail pubDetail) {
      Connection con = getConnection();
      try 
	  {
          int id = 0;
          id = DBUtil.getNextId(pubDetail.getPK().getTableName(), "contactId");
          pubDetail.getPK().setId(String.valueOf(id));
          ContactDAO.insertRow(con, pubDetail);
	  }
	  catch(Exception re)
	  {
		  throw new ContactRuntimeException("ContactEJB.ejbCreate()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_CREATE_FAILED", re);
	  }
	  finally 
	  {
			closeConnection(con);
	  }
      pk = pubDetail.getPK();
      firstName = pubDetail.getFirstName();
      lastName = pubDetail.getLastName();
      email = pubDetail.getEmail();
      phone = pubDetail.getPhone();
      fax = pubDetail.getFax();
      userId = pubDetail.getUserId();
      creationDate =  pubDetail.getCreationDate();
	  creatorId = pubDetail.getCreatorId();

      return pk;
  }

  public void ejbPostCreate(ContactDetail pubDetail) {
  }

	/**
	* Create an instance of a Contact object
	* @param pk the PK of the Contact to instanciate
	* @return the ContactPK of the instanciated Contact if it exists in database
	* @see com.stratelia.webactiv.util.contact.model.ContactDetail
	* @exception javax.ejb.FinderException
	* @exception java.sql.SQLException
	* @since 1.0
	*/
  public ContactPK ejbFindByPrimaryKey(ContactPK pk)
  {
    Connection con = getConnection();
    try 
	{
		ContactPK primary = ContactDAO.selectByPrimaryKey(con, pk);
		if (primary != null) {
			return primary;
		} 
		else 
		{
			throw new ContactRuntimeException("ContactEJB.ejbFindByPrimaryKey()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_NOT_FOUND");
		}
    } 
	catch(Exception re)
	{
		  throw new ContactRuntimeException("ContactEJB.ejbFindByPrimaryKey()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_NOT_FOUND", re);
	}
	finally {
		closeConnection(con);
    }
  }

  /**
	* Load contact attributes from database
	* @since 1.0
	*/
  public void ejbLoad() {
    if (pk == null) 
		return;
    ContactDetail pubDetail = null;
    Connection con = null;
    try 
	{
		if (pk.pubDetail != null) {
			pubDetail = pk.pubDetail;
	    } 
		else 
		{
			con = getConnection();
	        pubDetail = ContactDAO.loadRow(con, pk);
		}
	  firstName = pubDetail.getFirstName();
	  lastName = pubDetail.getLastName();
	  email = pubDetail.getEmail();
	  phone = pubDetail.getPhone();
	  fax = pubDetail.getFax();
	  userId = pubDetail.getUserId();
	  creationDate =  pubDetail.getCreationDate();
	  creatorId = pubDetail.getCreatorId();
      isModified = false;
    } 
	catch(Exception re)
	{
		  throw new ContactRuntimeException("ContactEJB.ejbLoad()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_NOT_FOUND", re);
	}
	finally 
	{
		closeConnection(con);
    }
  }

   /**
	* Store contact attributes into database
	* @since 1.0
	*/
  public void ejbStore() {
    if (! isModified) return;
    if (pk == null) return;
    ContactDetail detail = new ContactDetail(pk, firstName, lastName, email, phone, fax, userId, creationDate, creatorId);
    Connection con = getConnection();
    try {
        ContactDAO.storeRow(con, detail);
        isModified = false;
    } 
	catch(Exception re)
	{
		  throw new ContactRuntimeException("ContactEJB.ejbStore()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_RECORD_FAILED", re);
	}
	finally {
		closeConnection(con);
    }
  }

   /**
	* Delete this Contact and all info associated
	* @since 1.0
	*/
  public void ejbRemove() 
  {
    Connection con = getConnection();
    try {
        //delete all info associated from database
        InfoDAO.deleteInfoDetailByContactPK(con, this.pk);
        //delete contact from database
        ContactDAO.deleteRow(con, pk);
    } 
	catch(Exception re)
	{
		  throw new ContactRuntimeException("ContactEJB.ejbRemove()",SilverpeasRuntimeException.ERROR,"contact.EX_CONTACT_DELETE_FAILED", re);
	}
	finally {
		closeConnection(con);
    }
  }

  public void ejbActivate() 
  {
    pk = (ContactPK) context.getPrimaryKey();
  }

  public void ejbPassivate() {
    pk = null;
  }

  public void setEntityContext(EntityContext ec) {
    context = ec;
  }

  public void unsetEntityContext() {
    context = null;
   }
}