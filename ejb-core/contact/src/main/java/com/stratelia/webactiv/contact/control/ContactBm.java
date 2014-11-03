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
package com.stratelia.webactiv.contact.control;

import java.util.Collection;

import javax.ejb.Local;

import com.stratelia.webactiv.contact.model.CompleteContact;
import com.stratelia.webactiv.contact.model.ContactDetail;
import com.stratelia.webactiv.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.contact.model.ContactPK;
import com.stratelia.webactiv.node.model.NodePK;

@Local
public interface ContactBm {

  /**
   * get details on the contact specified by the primary key given in contactPK parameter
   * @param contactPK the contact primary key
   * @return the contact detail
   */
  public ContactDetail getDetail(ContactPK contactPK);

  /**
   * Create a new contact A new line will be added in contact table. The "id" in
   * "detail" is not used (a new one will be computed). The "ed" is used to know the table name.
   * @param detail the contact detail
   * @return contact primary key
   */
  public ContactPK createContact(ContactDetail detail);

  /**
   * removeContact() remove the contact designed by contactPK parameter.
   * @param contactPK
   */
  public void removeContact(ContactPK contactPK);

  /**
   * setDetail() update the contact content.
   * @param detail the contact detail to update
   */
  public void setDetail(ContactDetail detail);

  /**
   * addFather() add a new father (designed by "fatherPK") to a contact ("contactPK") The contact
   * will be visible from its new father node.
   * @param contactPK
   * @param fatherPK
   */
  public void addFather(ContactPK contactPK, NodePK fatherPK);

  /**
   * removeFather() remove a father (designed by "fatherPK") from a contact ("pubPK") The contact
   * won't be visible from its old father node.
   * @param contactPK
   * @param fatherPK
   */
  public void removeFather(ContactPK contactPK, NodePK fatherPK);

  /**
   * removeAllFather() remove all father from a contact ("pubPK") The contact won't be visible.
   * @param contactPK
   */
  public void removeAllFather(ContactPK contactPK);

  /**
   * removeAllIssue() remove all links between contacts and node N N is a descendant of the node
   * designed by originPK
   * @param originPK
   * @param contactPK
   */
  public void removeAllIssue(NodePK originPK, ContactPK contactPK);

  /**
   * getOrphanContacts() return the Detail of contact which are not linked to a father
   * @param contactPK the contact primary key
   * @return list of contact which are not linked to a father
   */
  public Collection<ContactDetail> getOrphanContacts(ContactPK contactPK);

  public void deleteOrphanContactsByCreatorId(ContactPK contactPK, String creatorId);

  public Collection<ContactDetail> getUnavailableContactsByPublisherId(ContactPK contactPK,
      String publisherId, String nodeId);

  /**
   * getAllFatherPK() return a collection, containing all node primary key from where the contact
   * is visible
   * @param contactPK
   * @return
   */
  public Collection<NodePK> getAllFatherPK(ContactPK contactPK);

  /**
   * getDetailsByFatherPK() return a ContactDetail collection of all contact visible from the node
   * identified by "fatherPK" parameter
   * @param fatherPK
   * @return
   */
  public Collection<ContactDetail> getDetailsByFatherPK(NodePK fatherPK);

  public Collection<ContactDetail> getDetailsByLastName(ContactPK pk, String query);

  public Collection<ContactDetail> getDetailsByLastNameOrFirstName(ContactPK pk, String query);

  public Collection<ContactDetail> getDetailsByLastNameAndFirstName(ContactPK pk, String lastName,
      String firstName);

  public void createInfoModel(ContactPK contactPK, String modelId);

  public CompleteContact getCompleteContact(ContactPK pubPK, String modelId);

  public Collection<ContactDetail> getContacts(Collection<ContactPK> contactPKs);

  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs);

  public Collection<ContactFatherDetail> getDetailsByFatherPKs(Collection<NodePK> fatherPKs,
      ContactPK contactPK, NodePK nodePK);

  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath);
}