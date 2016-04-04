/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General License as published by the Free Software Foundation, either version 3
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
 * Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contact.service;

import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

public interface ContactService {

  static ContactService get() {
    return ServiceProvider.getService(ContactService.class);
  }

  /**
   * get details on the contact specified by the primary key given in contactPK parameter
   * @param contactPK the contact primary key
   * @return the contact detail
   */
  ContactDetail getDetail(ContactPK contactPK);

  /**
   * Create a new contact A new line will be added in contact table. The "id" in
   * "detail" is not used (a new one will be computed). The "ed" is used to know the table name.
   * @param contact the contact detail
   * @return contact primary key
   */
  ContactPK createContact(Contact contact);

  /**
   * removeContact() remove the contact designed by contactPK parameter.
   * @param contactPK
   */
  void removeContact(ContactPK contactPK);

  /**
   * setDetail() update the contact content.
   * @param detail the contact detail to update
   */
  void setDetail(Contact detail);

  /**
   * addFather() add a new father (designed by "fatherPK") to a contact ("contactPK") The contact
   * will be visible from its new father node.
   * @param contactPK
   * @param fatherPK
   */
  void addFather(ContactPK contactPK, NodePK fatherPK);

  /**
   * removeFather() remove a father (designed by "fatherPK") from a contact ("pubPK") The contact
   * won't be visible from its old father node.
   * @param contactPK
   * @param fatherPK
   */
  void removeFather(ContactPK contactPK, NodePK fatherPK);

  /**
   * removeAllFather() remove all father from a contact ("pubPK") The contact won't be visible.
   * @param contactPK
   */
  void removeAllFather(ContactPK contactPK);

  /**
   * removeAllIssue() remove all links between contacts and node N N is a descendant of the node
   * designed by originPK
   * @param originPK
   * @param contactPK
   */
  void removeAllIssue(NodePK originPK, ContactPK contactPK);

  /**
   * getOrphanContacts() return the Detail of contact which are not linked to a father
   * @param contactPK the contact primary key
   * @return list of contact which are not linked to a father
   */
  Collection<ContactDetail> getOrphanContacts(ContactPK contactPK);

  void deleteOrphanContactsByCreatorId(ContactPK contactPK, String creatorId);

  Collection<ContactDetail> getUnavailableContactsByPublisherId(ContactPK contactPK,
      String publisherId, String nodeId);

  /**
   * getAllFatherPK() return a collection, containing all node primary key from where the contact
   * is visible
   * @param contactPK
   * @return
   */
  Collection<NodePK> getAllFatherPK(ContactPK contactPK);

  /**
   * getDetailsByFatherPK() return a ContactDetail collection of all contact visible from the node
   * identified by "fatherPK" parameter
   * @param fatherPK
   * @return
   */
  Collection<ContactDetail> getDetailsByFatherPK(NodePK fatherPK);

  Collection<ContactDetail> getDetailsByLastName(ContactPK pk, String query);

  Collection<ContactDetail> getDetailsByLastNameOrFirstName(ContactPK pk, String query);

  Collection<ContactDetail> getDetailsByLastNameAndFirstName(ContactPK pk, String lastName,
      String firstName);

  void createInfoModel(ContactPK contactPK, String modelId);

  CompleteContact getCompleteContact(ContactPK contactPK);

  CompleteContact getCompleteContact(ContactPK pubPK, String modelId);

  Collection<ContactDetail> getContacts(Collection<ContactPK> contactPKs);

  int getNbPubInFatherPKs(Collection<NodePK> fatherPKs);

  Collection<ContactFatherDetail> getDetailsByFatherPKs(Collection<NodePK> fatherPKs,
      ContactPK contactPK, NodePK nodePK);

  int getNbPubByFatherPath(NodePK fatherPK, String fatherPath);

  /**
   * Gets all non transitive contacts from an component represented by the given identifier that are
   * not in basket.
   * A transitive contact is a contact of user type linked directly by a group.
   * @param instanceId the identifier of the component instance into which contact are retrieved.
   * @return the list of complete contact data. Empty list if none.
   */
  List<CompleteContact> getVisibleContacts(String instanceId);

  void index(ContactPK contactPK);

  void deleteIndex(ContactPK contactPK);

}