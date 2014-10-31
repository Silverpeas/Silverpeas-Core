/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.contact.model;

import java.io.Serializable;

/**
 * This object contains the description of a complete contact (contact parameter, model detail,
 * info)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompleteContact implements Serializable {

  private ContactDetail contactDetail;
  private String modelId;

  /**
   * Create a new CompleteContact
   * @param contactDetail the contact detail
   * @param modelId the modeil identifier
   * @see com.stratelia.webactiv.contact.model.ContactDetail
   * @since 1.0
   */
  public CompleteContact(ContactDetail contactDetail, String modelId) {
    this.contactDetail = contactDetail;
    this.modelId = modelId;
  }

  /**
   * Get the contact parameters
   * @return a ContactDetail - the contact parameters
   * @see com.stratelia.webactiv.contact.model.ContactDetail
   * @since 1.0
   */
  public ContactDetail getContactDetail() {
    return contactDetail;
  }

  /**
   * @return the model identifier
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * @param modelId the model identifier to set
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

}