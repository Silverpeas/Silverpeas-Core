/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.web.socialnetwork.myprofil.control;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.silverpeas.core.admin.domain.exception.ReadOnlyDomainException;
import org.silverpeas.core.admin.service.AdminException;

/**
 * The outcome of the update of the profile of a user, with the message to display to them.
 * @author mmoquillon
 */
public enum ProfileUpdate {
  /**
   * All the modified data of the user are saved.
   */
  DONE("myProfile.MessageOK"),
  /**
   * The modified data of the user cannot be written into the repository of their domain (an
   * LDAP directory for example) as it is accessed in read-only.
   */
  REFUSED_BY_DOMAIN("myProfile.Error_readOnlyDomain"),
  /**
   * The modified data of the user cannot be written into the repository of their domain as it is
   * accessed in read-only, but their modified data of the extra form of the domain are saved.
   */
  REFUSED_BY_DOMAIN_EXTRA_DATA_SAVED("myProfile.Error_readOnlyDomain_extraDataSaved");

  private final String messageKey;

  ProfileUpdate(final String messageKey) {
    this.messageKey = messageKey;
  }

  /**
   * Gets the outcome of an update of a profile that has failed with the specified error.
   * @param failure the failure of the update of the user.
   * @param extraDataSaved true if modified data of the extra form were saved before the failure.
   * @return the outcome of the update when it is refused by the user domain.
   * @throws AdminException the given failure if it isn't a refusal of the user domain.
   */
  static ProfileUpdate failedWith(final AdminException failure, final boolean extraDataSaved)
      throws AdminException {
    if (ExceptionUtils.indexOfType(failure, ReadOnlyDomainException.class) < 0) {
      throw failure;
    }
    return extraDataSaved ? REFUSED_BY_DOMAIN_EXTRA_DATA_SAVED : REFUSED_BY_DOMAIN;
  }

  /**
   * Is the update of the profile fully done?
   * @return true if all the modified data are saved, false otherwise.
   */
  public boolean isDone() {
    return this == DONE;
  }

  /**
   * Gets the key in the localization bundle of the message to display to the user.
   * @return the key of the message.
   */
  public String getMessageKey() {
    return messageKey;
  }
}
