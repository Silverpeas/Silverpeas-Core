/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.contact.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.contact.info.InfoDAO;
import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.Contact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.contact.model.ContactRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;

@Stateless(name = "ContactBm", description = "EJB to manage a user's contacts.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ContactBmEJB implements ContactBm {

  private static final long serialVersionUID = 7603553259862289647L;
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  @Override
  public ContactDetail getDetail(ContactPK pubPK) {
    Connection con = getConnection();
    try {
      ContactPK primary = ContactDAO.selectByPrimaryKey(con, pubPK);
      if (primary != null) {
        return primary.pubDetail;
      } else {
        throw new ContactRuntimeException("ContactBmEJB.getDetail()",
            SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_NOT_FOUND");
      }
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getDetail()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACT_DETAIL_FAILED", "id = "
          + pubPK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public ContactPK createContact(Contact contact) {
    Connection con = getConnection();
    try {
      int id = DBUtil.getNextId(contact.getPK().getTableName(), "contactId");
      contact.getPK().setId(String.valueOf(id));
      ContactDAO.insertRow(con, contact);
      
      if (contact instanceof CompleteContact) {
        CompleteContact fullContact = (CompleteContact) contact;
        fullContact.saveForm("fr");
        createInfoModel(contact.getPK(), fullContact.getModelId());
      }
      
      createIndex(contact);
      return contact.getPK();
    } catch (Exception e) {
      throw new ContactRuntimeException("ContactBmEJB.createContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_CREATE_FAILED",
          "contactDetail = " + contact.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removeContact(ContactPK pubPK) {
    Connection con = getConnection();
    try {
      // remove forms data
      List<String> modelIds = InfoDAO.getInfo(con, pubPK);
      for(String modelId : modelIds) {
        CompleteContact completeContact = getCompleteContact(pubPK, modelId);
        completeContact.removeForm();
      }
      
      // remove forms association
      InfoDAO.deleteInfoDetailByContactPK(con, pubPK);
      
      // remove contact itself
      ContactDAO.deleteRow(con, pubPK);
      
      // remove contact index
      deleteIndex(pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_DELETE_FAILED",
          "pk = " + pubPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void setDetail(Contact contact) {
    Connection con = getConnection();
    try {
      ContactDAO.storeRow(con, contact);
      
      if (contact instanceof CompleteContact) {
        CompleteContact fullContact = (CompleteContact) contact;
        fullContact.saveForm("fr");
      }
      
      createIndex(contact);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.setDetail()", SilverpeasRuntimeException.ERROR,
          "contact.EX_SET_CONTACT_DETAIL_FAILED", "contactDetail = " + contact, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * addFather() add a new father (designed by "fatherPK") to a contact ("pubPK") The contact will
   * be visible from its new father node.
   *
   * @param pubPK
   * @param fatherPK
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void addFather(ContactPK pubPK, NodePK fatherPK) {
    Connection con = getConnection();
    try {
      ContactDAO.addFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new ContactRuntimeException("ContactEJB.addFather()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_ADD_TO_FATHER_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * removeFather() remove a father (designed by "fatherPK") from a contact ("pubPK") The contact
   * won't be visible from its old father node.
   *
   * @param pubPK
   * @param fatherPK
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removeFather(ContactPK pubPK, NodePK fatherPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeFather(con, pubPK, fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeFather()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_REMOVE_FROM_FATHER_FAILED",
          "fatherPK = " + fatherPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Remove all the fathers from a contact ("pubPK") The contact won't be visible.
   *
   * @param pubPK the id of the contact.
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removeAllFather(ContactPK pubPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeAllFather(con, pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeAllFather()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_REMOVE_FROM_ALLFATHERS_FAILED",
          "contactPK = " + pubPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   *
   * removeAllIssue() remove all links between contacts and node N N is a descendant of the node
   * designed by originPK.
   *
   * @param originPK
   * @param pubPK
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removeAllIssue(NodePK originPK, ContactPK pubPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeAllIssue(con, originPK, pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeAllIssue()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_REMOVE_ALLISSUES_FAILED",
          "fatherPK = " + originPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getOrphanContacts(ContactPK pubPK) {
    Connection con = getConnection();
    try {
      return ContactDAO.getOrphanContacts(con, pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getOrphanContacts()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_CONTACTS_WITHOUT_FATHERS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteOrphanContactsByCreatorId(ContactPK pubPK, String creatorId) {
    Connection con = getConnection();
    try {
      ContactDAO.deleteOrphanContactsByCreatorId(con, pubPK, creatorId);
    } catch (Exception re) {
      throw new ContactRuntimeException(
          "ContactBmEJB.deleteOrphanContactsByCreatorId()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_DELETE_CONTACTS_WITHOUT_FATHERS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getUnavailableContactsByPublisherId(ContactPK pubPK,
      String publisherId,
      String nodeId) {
    Connection con = getConnection();
    try {
      return ContactDAO.getUnavailableContactsByPublisherId(con, pubPK, publisherId, nodeId);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getUnavailableContactsByPublisherId()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * getAllFatherPK
   */
  @Override
  public Collection<NodePK> getAllFatherPK(ContactPK pubPK) {
    Connection con = getConnection();
    try {
      return ContactDAO.getAllFatherPK(con, pubPK);
    } catch (SQLException re) {
      throw new ContactRuntimeException("ContactBmEJB.getAllFatherPK()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACT_FATHERS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * getDetailsByFatherPK() return a ContactDetail collection of all contact visible from the node
   * identified by "fatherPK" parameter
   */
  @Override
  public Collection<ContactDetail> getDetailsByFatherPK(NodePK fatherPK) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByFatherPK(con, fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastName(ContactPK pk, String query) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastName(con, pk, query);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getDetailsByLastName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastNameOrFirstName(ContactPK pk, String query) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastNameOrFirstName(con, pk, query);
    } catch (Exception re) {
      throw new ContactRuntimeException(
          "ContactBmEJB.getDetailsByLastNameOrFirstName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastNameAndFirstName(ContactPK pk,
      String lastName, String firstName) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastNameAndFirstName(con, pk, lastName, firstName);
    } catch (Exception re) {
      throw new ContactRuntimeException(
          "ContactBmEJB.getDetailsByLastNameAndFirstName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void createInfoModel(ContactPK pubPK, String modelId) {
    Connection con = getConnection();
    try {
      InfoDAO.createInfo(con, modelId, pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.createInfoModel()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_INFOMODEL_CREATE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public CompleteContact getCompleteContact(ContactPK pubPK, String modelId) {
    try {
      // get detail
      ContactDetail contact = getDetail(pubPK);
      return new CompleteContact(contact, modelId);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getCompleteContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACT_DETAIL_FAILED", re);
    }
  }

  @Override
  public Collection<ContactDetail> getContacts(Collection<ContactPK> contactPKs) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByContactPKs(con, contactPKs);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getContacts()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs) {
    Connection con = getConnection();
    try {
      int result = ContactDAO.getNbPubInFatherPKs(con, fatherPKs);
      return result;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getNbPubInFatherPKs()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_NB_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactFatherDetail> getDetailsByFatherPKs(Collection<NodePK> fatherPKs,
      ContactPK pubPK, NodePK nodePK) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByFatherPKs(con, fatherPKs, pubPK, nodePK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getContacts()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath) {
    Connection con = getConnection();
    try {
      return ContactDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getNbPubByFatherPath()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_NB_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.CONTACT_DATASOURCE);
    } catch (UtilException re) {
      throw new ContactRuntimeException("ContactBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", re);
    }
  }

  /**
   * Called on : - createContact() - updateContact() - createInfoModel() - updateInfoDetail() -
   * deleteAttachments()
   */
  private void createIndex(Contact contact) {
    try {
      if (contact != null) {
        FullIndexEntry indexEntry = new FullIndexEntry(contact.getPK().getComponentName(),
            "Contact", contact.getPK().getId());
        indexEntry.setTitle(contact.getFirstName() + " " + contact.getLastName());
        indexEntry.setLang(I18NHelper.defaultLanguage);
        indexEntry.setCreationDate(formatter.format(contact.getCreationDate()));
        indexEntry.setCreationUser(contact.getCreatorId());
        
        if (contact instanceof CompleteContact) {
          CompleteContact completeContact = (CompleteContact) contact;
          completeContact.indexForm(indexEntry);
        }
        
        IndexEngineProxy.addIndexEntry(indexEntry);
      }
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.createIndex()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_CREATE_INDEX_FAILED", re);
    }
  }

  /**
   * Called on : - deleteContact()
   */
  private void deleteIndex(ContactPK pubPK) {
    IndexEntryPK indexEntry = new IndexEntryPK(pubPK.getComponentName(),
        "Contact", pubPK.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public ContactBmEJB() {
  }
  
}