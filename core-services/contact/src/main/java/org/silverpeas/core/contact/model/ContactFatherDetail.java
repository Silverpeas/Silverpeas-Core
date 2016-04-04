/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contact.model;

import java.io.Serializable;

/**
 * This object contains the description of a contact and a node (contact parameter, model detail,
 * info)
 * @author SC
 * @version 1.0
 */
public class ContactFatherDetail implements Serializable {

  private ContactDetail contactDetail;
  private String nodeId;
  private String nodeName;

  /**
   * Create a new ContactFatherDetail
   * @param contactDetail the contact detail
   * @param nodeId the node identifier
   * @param nodeName the node name
   */
  public ContactFatherDetail(ContactDetail contactDetail, String nodeId, String nodeName) {
    this.contactDetail = contactDetail;
    this.nodeId = nodeId;
    this.nodeName = nodeName;
  }

  /**
   * Get the contact parameters
   * @return a ContactDetail - the contact parameters
   * @see ContactDetail
   * @since 1.0
   */
  public ContactDetail getContactDetail() {
    return contactDetail;
  }

  public String getNodeId() {
    return nodeId;
  }

  public String getNodeName() {
    return nodeName;
  }
}