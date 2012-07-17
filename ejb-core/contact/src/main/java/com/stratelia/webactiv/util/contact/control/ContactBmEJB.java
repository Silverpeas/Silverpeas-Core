/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.contact.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Collection;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.contact.ejb.Contact;
import com.stratelia.webactiv.util.contact.ejb.ContactDAO;
import com.stratelia.webactiv.util.contact.ejb.ContactHome;
import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.contact.model.ContactRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.node.model.NodePK;

public class ContactBmEJB implements SessionBean {

  private static final long serialVersionUID = 7603553259862289647L;

  private String dbName = JNDINames.CONTACT_DATASOURCE;
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  public ContactDetail getDetail(ContactPK pubPK) {
    ContactDetail result = null;
    Contact pub = findContact(pubPK);
    try {
      result = pub.getDetail();
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getDetail()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_CONTACT_DETAIL_FAILED", "id = " + pubPK.getId().toString(), re);
    }
    return result;
  }

  public ContactPK createContact(ContactDetail detail) {
    Contact pub = null;
    ContactPK pk = null;
    ContactHome pubHome = getContactHome();
    try {
      pub = pubHome.create(detail);
      pk = pub.getDetail().getPK();
      createIndex(pk);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.createContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_CREATE_FAILED",
          "contactDetail = " + detail.toString(), re);
    }
    return pk;
  }

  public void removeContact(ContactPK pubPK) {
    ContactHome pubHome = getContactHome();
    Connection con = getConnection();
    try {
      pubHome.remove(pubPK);
      deleteIndex(pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_DELETE_FAILED",
          "pk = " + pubPK.toString(), re);
    } finally {
      freeConnection(con);
    }
  }

  public void setDetail(ContactDetail detail) {
    Contact pub = findContact(detail.getPK());
    try {
      pub.setDetail(detail);
      createIndex(detail.getPK());
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.setDetail()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_SET_CONTACT_DETAIL_FAILED", "contactDetail = "
          + detail.toString(), re);
    }
  }

  /**
   * addFather() add a new father (designed by "fatherPK") to a contact ("pubPK") The contact will
   * be visible from its new father node.
   */
  public void addFather(ContactPK pubPK, NodePK fatherPK) {
    Contact pub = findContact(pubPK);
    try {
      pub.addFather(fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.addFather()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_ADD_TO_FATHER_FAILED", "fatherPK = "
          + fatherPK.toString(), re);
    }

  }

  /**
   * removeFather() remove a father (designed by "fatherPK") from a contact ("pubPK") The contact
   * won't be visible from its old father node.
   */
  public void removeFather(ContactPK pubPK, NodePK fatherPK) {
    Contact pub = findContact(pubPK);
    try {
      pub.removeFather(fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeFather()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_REMOVE_FROM_FATHER_FAILED", "fatherPK = "
          + fatherPK.toString(), re);
    }
  }

  /**
   * removeAllFather() remove all father from a contact ("pubPK") The contact won't be visible.
   */
  public void removeAllFather(ContactPK pubPK) {
    Contact pub = findContact(pubPK);
    try {
      pub.removeAllFather();
      // this contact must be unfindable to standard user
      // however the creator must find it
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeAllFather()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_REMOVE_FROM_ALLFATHERS_FAILED", "contactPK = "
          + pubPK.toString(), re);
    }
  }

  /**
   * removeAllIssue() remove all links between contacts and node N N is a descendant of the node
   * designed by originPK
   */
  public void removeAllIssue(NodePK originPK, ContactPK pubPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeAllIssue(con, originPK, pubPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.removeAllIssue()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_REMOVE_ALLISSUES_FAILED", "fatherPK = "
          + originPK.toString(), re);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getOrphanContacts(ContactPK pubPK) {
    Connection con = getConnection();
    try {
      Collection pubDetails = ContactDAO.getOrphanContacts(con, pubPK);
      return pubDetails;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getOrphanContacts()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_CONTACTS_WITHOUT_FATHERS_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

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
      freeConnection(con);
    }
  }

  public Collection getUnavailableContactsByPublisherId(ContactPK pubPK,
      String publisherId, String nodeId) {
    Connection con = getConnection();
    try {
      Collection pubDetails = ContactDAO.getUnavailableContactsByPublisherId(
          con, pubPK, publisherId, nodeId);
      return pubDetails;
    } catch (Exception re) {
      throw new ContactRuntimeException(
          "ContactBmEJB.getUnavailableContactsByPublisherId()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }

  }

  /**
   * getAllFatherPK
   */
  public Collection getAllFatherPK(ContactPK pubPK) {
    Collection result = null;
    Contact pub = findContact(pubPK);
    try {
      result = pub.getAllFatherPK();
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getAllFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_CONTACT_FATHERS_FAILED", re);
    }
    return result;
  }

  /**
   * getDetailsByFatherPK() return a ContactDetail collection of all contact visible from the node
   * identified by "fatherPK" parameter
   */
  public Collection getDetailsByFatherPK(NodePK fatherPK) {
    Connection con = getConnection();
    Collection detailList = null;
    try {
      detailList = ContactDAO.selectByFatherPK(con, fatherPK);
      return detailList;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByLastName(ContactPK pk, String query) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastName(con, pk, query);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getDetailsByLastName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByLastNameOrFirstName(ContactPK pk, String query) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastNameOrFirstName(con, pk, query);
    } catch (Exception re) {
      throw new ContactRuntimeException(
          "ContactBmEJB.getDetailsByLastNameOrFirstName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByLastNameAndFirstName(ContactPK pk,
      String lastName, String firstName) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastNameAndFirstName(con, pk, lastName,
          firstName);
    } catch (Exception re) {
      throw new ContactRuntimeException(
          "ContactBmEJB.getDetailsByLastNameAndFirstName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }
  }

  public void createInfoModel(ContactPK pubPK, String modelId) {
    Contact pub = findContact(pubPK);
    try {
      pub.createInfo(modelId);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.createInfoModel()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_INFOMODEL_CREATE_FAILED", re);
    }
  }

  public CompleteContact getCompleteContact(ContactPK pubPK, String modelId) {
    Contact pub = findContact(pubPK);
    try {
      return pub.getCompleteContact(modelId);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getCompleteContact()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_CONTACT_DETAIL_FAILED", re);
    }
  }

  public Collection getContacts(Collection contactPKs) throws RemoteException {
    Connection con = getConnection();
    try {
      Collection contacts = ContactDAO.selectByContactPKs(con, contactPKs);
      return contacts;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getContacts()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }
  }

  public int getNbPubInFatherPKs(Collection fatherPKs) throws RemoteException {
    Connection con = getConnection();
    try {
      int result = ContactDAO.getNbPubInFatherPKs(con, fatherPKs);
      return result;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getNbPubInFatherPKs()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_NB_CONTACTS_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByFatherPKs(Collection fatherPKs,
      ContactPK pubPK, NodePK nodePK) throws RemoteException {
    Connection con = getConnection();
    Collection detailList = null;
    try {
      detailList = ContactDAO.selectByFatherPKs(con, fatherPKs, pubPK, nodePK);
      return detailList;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getContacts()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED",
          re);
    } finally {
      freeConnection(con);
    }
  }

  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath)
      throws RemoteException {
    Connection con = getConnection();
    try {
      int result = ContactDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
      return result;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getNbPubByFatherPath()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_GET_NB_CONTACTS_FAILED", re);
    } finally {
      freeConnection(con);
    }
  }

  // internal methods

  private ContactHome getContactHome() {
    try {
      ContactHome pubHome = (ContactHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.CONTACT_EJBHOME, ContactHome.class);
      return pubHome;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getContactHome()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACTHOME_GET_FAILED", re);
    }

  }

  private Contact findContact(ContactPK pubPK) {
    ContactHome pubHome = getContactHome();
    try {
      return (pubHome.findByPrimaryKey(pubPK));
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.findContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_NOT_FOUND", re);
    }
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);
      return con;
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          re);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null)
      try {
        con.close();
      } catch (Exception re) {
        throw new ContactRuntimeException("ContactBmEJB.closeConnection()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CONNECTION_CLOSE_FAILED", re);
      }
  }

  /**
   * Called on : - createContact() - updateContact() - createInfoModel() - updateInfoDetail() -
   * deleteAttachments()
   */
  private void createIndex(ContactPK pubPK) {
    try {
      ContactDetail pubDetail = getDetail(pubPK);
      FullIndexEntry indexEntry = null;

      if (pubDetail != null) {
        // Index the Contact Header
        indexEntry = new FullIndexEntry(pubPK.getComponentName(), "Contact",
            pubDetail.getPK().getId());
        indexEntry.setTitle(pubDetail.getFirstName() + " "
            + pubDetail.getLastName());
        // indexEntry.setPreView(pubDetail.getDescription());
        indexEntry.setLang("fr");
        indexEntry.setCreationDate(formatter
            .format(pubDetail.getCreationDate()));
        indexEntry.setCreationUser(pubDetail.getCreatorId());
        // Index the Contact Content
        IndexEngineProxy.addIndexEntry(indexEntry);
      }
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.createIndex()",
          SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_CREATE_INDEX_FAILED", re);
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

  public void ejbCreate() {
  }

  public void ejbRemove() {
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  public void setSessionContext(SessionContext sc) {
  }
}
