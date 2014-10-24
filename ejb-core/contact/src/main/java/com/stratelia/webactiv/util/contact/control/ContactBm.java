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

import java.util.Collection;

import javax.ejb.Local;

import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.Contact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.node.model.NodePK;

@Local
public interface ContactBm {

  /**
   * getDetail() get details on the contact specified by the primary key given in pubPK parameter
   *
   * @param pubPK
   * @return
   */
  public ContactDetail getDetail(ContactPK pubPK);

  /**
   * createContact() Create a new contact A new line will be added in contact table. The "id" in
   * "detail" is not used (a new one will be computed). The "ed" is used to know the table name.
   *
   * @param detail
   * @return
   */
  public ContactPK createContact(Contact contact);

  /**
   * removeContact() remove the contact designed by pubPK parameter.
   *
   * @param pubPK
   */
  public void removeContact(ContactPK pubPK);

  /**
   * setDetail() update the contact content.
   *
   * @param detail
   */
  public void setDetail(Contact detail);

  /**
   * addFather() add a new father (designed by "fatherPK") to a contact ("pubPK") The contact will
   * be visible from its new father node.
   *
   * @param pubPK
   * @param fatherPK
   */
  public void addFather(ContactPK pubPK, NodePK fatherPK);

  /**
   * removeFather() remove a father (designed by "fatherPK") from a contact ("pubPK") The contact
   * won't be visible from its old father node.
   *
   * @param pubPK
   * @param fatherPK
   */
  public void removeFather(ContactPK pubPK, NodePK fatherPK);

  /**
   * removeAllFather() remove all father from a contact ("pubPK") The contact won't be visible.
   *
   * @param pubPK
   */
  public void removeAllFather(ContactPK pubPK);

  /**
   * removeAllIssue() remove all links between contacts and node N N is a descendant of the node
   * designed by originPK
   *
   * @param originPK
   * @param pubPK
   */
  public void removeAllIssue(NodePK originPK, ContactPK pubPK);

  /**
   * getOrphanContacts() return the Detail of contact which are not linked to a father
   *
   * @param pubPK
   * @return
   */
  public Collection<ContactDetail> getOrphanContacts(ContactPK pubPK);

  public void deleteOrphanContactsByCreatorId(ContactPK pubPK, String creatorId);

  public Collection<ContactDetail> getUnavailableContactsByPublisherId(ContactPK pubPK,
      String publisherId,
      String nodeId);

  /**
   * getAllFatherPK() return a collection, containing all node primary key from where the contact is
   * visible
   * @param pubPK
   * @return 
   */
  public Collection<NodePK> getAllFatherPK(ContactPK pubPK);

  /**
   * getDetailsByFatherPK() return a ContactDetail collection of all contact visible from the node
   * identified by "fatherPK" parameter
   *
   * @param fatherPK
   * @return
   */
  public Collection<ContactDetail> getDetailsByFatherPK(NodePK fatherPK);

  public Collection<ContactDetail> getDetailsByLastName(ContactPK pk, String query);

  public Collection<ContactDetail> getDetailsByLastNameOrFirstName(ContactPK pk, String query);

  public Collection<ContactDetail> getDetailsByLastNameAndFirstName(ContactPK pk, String lastName,
      String firstName);

  public void createInfoModel(ContactPK pubPK, String modelId);

  public CompleteContact getCompleteContact(ContactPK pubPK, String modelId);

  public Collection<ContactDetail> getContacts(Collection<ContactPK> contactPKs);

  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs);

  public Collection<ContactFatherDetail> getDetailsByFatherPKs(Collection<NodePK> fatherPKs,
      ContactPK pubPK, NodePK nodePK);

  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath);
}