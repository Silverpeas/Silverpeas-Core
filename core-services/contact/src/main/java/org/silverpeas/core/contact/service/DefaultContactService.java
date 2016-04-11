/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contact.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.contact.info.InfoDAO;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contact.model.ContactRuntimeException;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.i18n.I18NHelper;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultContactService implements ContactService, ComponentInstanceDeletion {

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  @Override
  public ContactDetail getDetail(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      ContactPK primary = ContactDAO.selectByPrimaryKey(con, contactPK);
      if (primary != null) {
        return primary.contactDetail;
      } else {
        throw new ContactRuntimeException("DefaultContactService.getDetail()",
            SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_NOT_FOUND");
      }
    } catch (SQLException | ParseException re) {
      throw new ContactRuntimeException("DefaultContactService.getDetail()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACT_DETAIL_FAILED",
          "id = " + contactPK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public ContactPK createContact(Contact contact) {
    Connection con = getConnection();
    try {
      int id = DBUtil.getNextId(contact.getPK().getTableName(), "contactId");
      contact.getPK().setId(String.valueOf(id));
      ContactDAO.insertRow(con, contact);

      if (contact instanceof CompleteContact) {
        CompleteContact fullContact = (CompleteContact) contact;
        if (fullContact.isFormDefined()) {
          fullContact.saveForm();
          createInfoModel(contact.getPK(), fullContact.getModelId());
        }
      }

      createIndex(contact);
      return contact.getPK();
    } catch (Exception e) {
      throw new ContactRuntimeException("DefaultContactService.createContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_CREATE_FAILED",
          "contactDetail = " + contact.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeContact(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      // remove forms data
      List<String> modelIds = InfoDAO.getInfo(con, contactPK);
      for(String modelId : modelIds) {
        CompleteContact completeContact = getCompleteContact(contactPK, modelId);
        completeContact.removeForm();
      }

      // remove forms association
      InfoDAO.deleteInfoDetailByContactPK(con, contactPK);

      // remove contact itself
      ContactDAO.deleteContact(con, contactPK);

      // remove contact index
      deleteIndex(contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.removeContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_DELETE_FAILED", "pk = " + contactPK,
          re);
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
        fullContact.saveForm();
      }

      createIndex(contact);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.setDetail()",
          SilverpeasRuntimeException.ERROR, "contact.EX_SET_CONTACT_DETAIL_FAILED",
          "contactDetail = " + contact, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * addFather() add a new father (designed by "fatherPK") to a contact ("pubPK") The contact will
   * be visible from its new father node.
   * @param contactPK
   * @param fatherPK
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addFather(ContactPK contactPK, NodePK fatherPK) {
    Connection con = getConnection();
    try {
      ContactDAO.addFather(con, contactPK, fatherPK);
    } catch (SQLException re) {
      throw new ContactRuntimeException("ContactEJB.addFather()", SilverpeasRuntimeException.ERROR,
          "contact.EX_CONTACT_ADD_TO_FATHER_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * removeFather() remove a father (designed by "fatherPK") from a contact ("pubPK") The contact
   * won't be visible from its old father node.
   * @param contactPK
   * @param fatherPK
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeFather(ContactPK contactPK, NodePK fatherPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeFather(con, contactPK, fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.removeFather()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_REMOVE_FROM_FATHER_FAILED",
          "fatherPK = " + fatherPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Remove all the fathers from a contact ("pubPK") The contact won't be visible.
   * @param contactPK the id of the contact.
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeAllFather(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeAllFather(con, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.removeAllFather()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_REMOVE_FROM_ALLFATHERS_FAILED",
          "contactPK = " + contactPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * removeAllIssue() remove all links between contacts and node N N is a descendant of the node
   * designed by originPK.
   * @param originPK
   * @param contactPK
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeAllIssue(NodePK originPK, ContactPK contactPK) {
    Connection con = getConnection();
    try {
      ContactDAO.removeAllIssue(con, originPK, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.removeAllIssue()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_REMOVE_ALLISSUES_FAILED",
          "fatherPK = " + originPK, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getOrphanContacts(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      return ContactDAO.getOrphanContacts(con, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.getOrphanContacts()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_WITHOUT_FATHERS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteOrphanContactsByCreatorId(ContactPK contactPK, String creatorId) {
    Connection con = getConnection();
    try {
      ContactDAO.deleteOrphanContactsByCreatorId(con, contactPK, creatorId);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.deleteOrphanContactsByCreatorId()",
          SilverpeasRuntimeException.ERROR, "contact.EX_DELETE_CONTACTS_WITHOUT_FATHERS_FAILED",
          re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getUnavailableContactsByPublisherId(ContactPK contactPK,
      String publisherId, String nodeId) {
    Connection con = getConnection();
    try {
      return ContactDAO.getUnavailableContactsByPublisherId(con, contactPK, publisherId, nodeId);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.getUnavailableContactsByPublisherId()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * getAllFatherPK
   */
  @Override
  public Collection<NodePK> getAllFatherPK(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      return ContactDAO.getAllFatherPK(con, contactPK);
    } catch (SQLException re) {
      throw new ContactRuntimeException("DefaultContactService.getAllFatherPK()",
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
      throw new ContactRuntimeException("DefaultContactService.getDetailsByFatherPK()",
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
      throw new ContactRuntimeException("DefaultContactService.getDetailsByLastName()",
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
      throw new ContactRuntimeException("DefaultContactService.getDetailsByLastNameOrFirstName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastNameAndFirstName(ContactPK pk, String lastName,
      String firstName) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByLastNameAndFirstName(con, pk, lastName, firstName);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.getDetailsByLastNameAndFirstName()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void createInfoModel(ContactPK contactPK, String modelId) {
    Connection con = getConnection();
    try {
      InfoDAO.createInfo(con, modelId, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.createInfoModel()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_INFOMODEL_CREATE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public CompleteContact getCompleteContact(ContactPK contactPK, String modelId) {
    try {
      // get detail
      ContactDetail contact = getDetail(contactPK);
      return new CompleteContact(contact, modelId);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getCompleteContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACT_DETAIL_FAILED", re);
    }
  }

  @Override
  public CompleteContact getCompleteContact(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      // get detail
      ContactDetail contact = getDetail(contactPK);
      List<String> modelIds = InfoDAO.getInfo(con, contactPK);
      String modelId = null;
      if (modelIds != null && !modelIds.isEmpty()) {
        modelId = modelIds.get(0);
      }
      return new CompleteContact(contact, modelId);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.getCompleteContact()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACT_DETAIL_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getContacts(Collection<ContactPK> contactPKs) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByContactPKs(con, contactPKs);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.getContacts()",
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
      throw new ContactRuntimeException("DefaultContactService.getNbPubInFatherPKs()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_NB_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactFatherDetail> getDetailsByFatherPKs(Collection<NodePK> fatherPKs,
      ContactPK contactPK, NodePK nodePK) {
    Connection con = getConnection();
    try {
      return ContactDAO.selectByFatherPKs(con, fatherPKs, contactPK, nodePK);
    } catch (Exception re) {
      throw new ContactRuntimeException("DefaultContactService.getContacts()",
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
      throw new ContactRuntimeException("DefaultContactService.getNbPubByFatherPath()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_NB_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<CompleteContact> getVisibleContacts(String instanceId) {
    Connection con = getConnection();
    try {
      return ContactDAO.getVisibleContacts(con, instanceId);
    } catch (Exception re) {
      throw new ContactRuntimeException("ContactBmEJB.getVisibleContacts()",
          SilverpeasRuntimeException.ERROR, "contact.EX_GET_CONTACTS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (SQLException re) {
      throw new ContactRuntimeException("DefaultContactService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", re);
    }
  }

  public void index(ContactPK contactPK) {
    CompleteContact contact = getCompleteContact(contactPK);
    createIndex(contact);
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
      throw new ContactRuntimeException("DefaultContactService.createIndex()",
          SilverpeasRuntimeException.ERROR, "contact.EX_CONTACT_CREATE_INDEX_FAILED", re);
    }
  }

  /**
   * Called on : - deleteContact()
   * @param contactPK
   */
  public void deleteIndex(ContactPK contactPK) {
    IndexEntryKey indexEntry = new IndexEntryKey(contactPK.getComponentName(), "Contact", contactPK.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public DefaultContactService() {
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      ContactDAO.deleteAllContacts(connection, componentInstanceId);
      InfoDAO.deleteAllInfoByInstanceId(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}