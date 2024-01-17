/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.directory.model;

import java.util.Date;

/**
 * An item in a directory of persons. An item in a directory identifies a given person with his
 * communication data (email, phone, and so one).
 */
public interface DirectoryItem extends Comparable<DirectoryItem> {

  enum ITEM_TYPE {
    /**
     * The item in the directory refers a user in Silverpeas
     */
    USER,
    /**
     * The item in the directory refers a contact of a user.
     */
    CONTACT
  }

  /**
   * Gets the first name of the person.
   * @return the first name.
   */
  String getFirstName();

  /**
   * Gets the last name of the person.
   * @return th last name.
   */
  String getLastName();

  /**
   * Gets the URL of the person avatar.
   * @return the avatar image.
   */
  String getAvatar();

  /**
   * Gets the main email address.
   * @return the email address.
   */
  String getMail();

  /**
   * Gets the phone number.
   * @return a phone number.
   */
  String getPhone();

  /**
   * Gets the Fax number.
   * @return the Fax number
   */
  String getFax();

  /**
   * Gets the type of this item.
   * @return the type of this item.
   */
  ITEM_TYPE getType();

  /**
   * Gets the date at which this item has been created into the directory.
   * @return the item creation date.
   */
  Date getCreationDate();

  /**
   * Gets the unique identifier of the person referred by this item.
   * @return the unique identifier of the person.
   */
  String getOriginalId();

  /**
   * Gets the unique identifier of this item in the directory.
   * @return the unique identifier of the item.
   */
  String getUniqueId();
}
