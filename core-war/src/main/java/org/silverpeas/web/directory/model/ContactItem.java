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

package org.silverpeas.web.directory.model;

import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.Contact;

import java.util.Date;

public class ContactItem extends AbstractDirectoryItem {

  private CompleteContact contact;

  public ContactItem(CompleteContact contact) {
    this.contact = contact;
  }

  @Override
  public String getFirstName() {
    return getContact().getFirstName();
  }

  @Override
  public String getLastName() {
    return getContact().getLastName();
  }

  @Override
  public String getAvatar() {
    return null;
  }

  @Override
  public DirectoryItem.ITEM_TYPE getType() {
    return DirectoryItem.ITEM_TYPE.Contact;
  }

  @Override
  public Date getCreationDate() {
    return getContact().getCreationDate();
  }

  @Override
  public String getOriginalId() {
    return getContact().getPK().getId();
  }

  @Override
  public String getMail() {
    return getContact().getEmail();
  }

  public Contact getContact() {
    return contact;
  }

  @Override
  public String getPhone() {
    return getContact().getPhone();
  }

  @Override
  public String getFax() {
    return getContact().getFax();
  }
}
