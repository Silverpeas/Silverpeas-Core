/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contact.service;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.contact.info.InfoDAO;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contact.model.ContactRuntimeException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultContactService implements ContactService, ComponentInstanceDeletion {

  @Inject
  private InfoDAO infoDAO;
  @Inject
  private ContactDAO contactDAO;

  @Override
  public ContactDetail getDetail(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      ContactPK primary = contactDAO.selectByPrimaryKey(con, contactPK);
      if (primary != null) {
        return primary.contactDetail;
      } else {
        throw new ContactRuntimeException("Contact not found with id = " + contactPK.getId());
      }
    } catch (SQLException | ParseException re) {
      throw new ContactRuntimeException(re);
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
      contactDAO.insertRow(con, contact);

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
      throw new ContactRuntimeException(e);
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
      List<String> modelIds = infoDAO.getInfo(con, contactPK);
      for(String modelId : modelIds) {
        CompleteContact completeContact = getCompleteContact(contactPK, modelId);
        completeContact.removeForm();
      }

      // remove forms association
      infoDAO.deleteInfoDetailByContactPK(con, contactPK);

      // remove contact itself
      contactDAO.deleteContact(con, contactPK);

      // remove contact index
      deleteIndex(contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void setDetail(Contact contact) {
    Connection con = getConnection();
    try {
      contactDAO.storeRow(con, contact);

      if (contact instanceof CompleteContact) {
        CompleteContact fullContact = (CompleteContact) contact;
        fullContact.saveForm();
      }

      createIndex(contact);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
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
      contactDAO.addFather(con, contactPK, fatherPK);
    } catch (SQLException re) {
      throw new ContactRuntimeException(re);
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
      contactDAO.removeFather(con, contactPK, fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
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
      contactDAO.removeAllFather(con, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
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
      contactDAO.removeAllIssue(con, originPK, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getOrphanContacts(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      return contactDAO.getOrphanContacts(con, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteOrphanContactsByCreatorId(ContactPK contactPK, String creatorId) {
    Connection con = getConnection();
    try {
      contactDAO.deleteOrphanContactsByCreatorId(con, contactPK, creatorId);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getUnavailableContactsByPublisherId(ContactPK contactPK,
      String publisherId, String nodeId) {
    Connection con = getConnection();
    try {
      return contactDAO.getUnavailableContactsByPublisherId(con, contactPK, publisherId, nodeId);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
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
      return contactDAO.getAllFatherPK(con, contactPK);
    } catch (SQLException re) {
      throw new ContactRuntimeException(re);
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
      return contactDAO.selectByFatherPK(con, fatherPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastName(ContactPK pk, String query) {
    Connection con = getConnection();
    try {
      return contactDAO.selectByLastName(con, pk, query);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastNameOrFirstName(ContactPK pk, String query) {
    Connection con = getConnection();
    try {
      return contactDAO.selectByLastNameOrFirstName(con, pk, query);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getDetailsByLastNameAndFirstName(ContactPK pk, String lastName,
      String firstName) {
    Connection con = getConnection();
    try {
      return contactDAO.selectByLastNameAndFirstName(con, pk, lastName, firstName);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void createInfoModel(ContactPK contactPK, String modelId) {
    Connection con = getConnection();
    try {
      infoDAO.createInfo(con, modelId, contactPK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
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
      throw new ContactRuntimeException(re);
    }
  }

  @Override
  public CompleteContact getCompleteContact(ContactPK contactPK) {
    Connection con = getConnection();
    try {
      // get detail
      ContactDetail contact = getDetail(contactPK);
      List<String> modelIds = infoDAO.getInfo(con, contactPK);
      String modelId = null;
      if (modelIds != null && !modelIds.isEmpty()) {
        modelId = modelIds.get(0);
      }
      return new CompleteContact(contact, modelId);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactDetail> getContacts(Collection<ContactPK> contactPKs) {
    Connection con = getConnection();
    try {
      return contactDAO.selectByContactPKs(con, contactPKs);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs) {
    Connection con = getConnection();
    try {
      return contactDAO.getNbPubInFatherPKs(con, fatherPKs);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ContactFatherDetail> getDetailsByFatherPKs(Collection<NodePK> fatherPKs,
      ContactPK contactPK, NodePK nodePK) {
    Connection con = getConnection();
    try {
      return contactDAO.selectByFatherPKs(con, fatherPKs, contactPK, nodePK);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath) {
    Connection con = getConnection();
    try {
      return contactDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<CompleteContact> getVisibleContacts(String instanceId) {
    Connection con = getConnection();
    try {
      return contactDAO.getVisibleContacts(con, instanceId);
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (SQLException re) {
      throw new ContactRuntimeException(re);
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
        indexEntry.setCreationDate(contact.getCreationDate());
        indexEntry.setCreationUser(contact.getCreatorId());
        indexEntry.addTextContent(contact.getPhone());
        indexEntry.addTextContent(contact.getEmail());

        if (contact instanceof CompleteContact) {
          CompleteContact completeContact = (CompleteContact) contact;
          completeContact.indexForm(indexEntry);
        }

        IndexEngineProxy.addIndexEntry(indexEntry);
      }
    } catch (Exception re) {
      throw new ContactRuntimeException(re);
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

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      contactDAO.deleteAllContacts(connection, componentInstanceId);
      infoDAO.deleteAllInfoByInstanceId(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}