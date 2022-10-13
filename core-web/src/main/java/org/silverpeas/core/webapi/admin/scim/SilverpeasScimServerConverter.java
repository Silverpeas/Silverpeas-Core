/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.webapi.admin.scim;

import edu.psu.swe.scim.spec.resources.Email;
import edu.psu.swe.scim.spec.resources.Name;
import edu.psu.swe.scim.spec.resources.ScimUser;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;

import java.util.ArrayList;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * Centralization of conversion between {@link User} of Silverpeas server ans {@link ScimUser} of
 * SCIM clients.
 * @author silveryocha
 */
class SilverpeasScimServerConverter {

  private SilverpeasScimServerConverter() {
  }

  static UserFull convert(ScimUser scimUser) {
    if (scimUser == null) {
      return null;
    }
    final UserFull user = new UserFull();
    user.setId(decodeUserId(scimUser.getId()));
    applyTo(scimUser, user);
    return user;
  }

  static void applyTo(ScimUser scimUser, UserFull user) {
    user.setSpecificId(scimUser.getExternalId());
    final Name name = scimUser.getName();
    if (name != null) {
      user.setLastName(name.getFamilyName());
      user.setFirstName(name.getGivenName());
    } else {
      user.setLastName(null);
      user.setFirstName(null);
    }
    user.setLogin(scimUser.getUserName());
    // if no email, then the user name account is taken into account
    final String email = scimUser.getPrimaryEmailAddress()
        .map(Email::getValue)
        .orElse(scimUser.getUserName());
    user.seteMail(email);
    user.setPassword(scimUser.getPassword());
    if (!user.isRemovedState()) {
      if (scimUser.getActive()) {
        if (user.isDeactivatedState() || user.isDeletedState()) {
          user.setState(UserState.VALID);
        }
      } else {
        user.setState(UserState.DEACTIVATED);
      }
    }
  }

  static ScimUser convert(User user) {
    if (user == null) {
      return null;
    }
    final ScimUser scimUser = new ScimUser();
    scimUser.setId(encodeUserId(user));
    applyTo(user, scimUser);
    return scimUser;
  }

  private static void applyTo(User genericUser, ScimUser scimUser) {
    if (genericUser instanceof UserDetail) {
      final UserDetail user = (UserDetail) genericUser;
      scimUser.setExternalId(user.getSpecificId());
      scimUser.setUserName(user.getLogin());
      if (isDefined(user.geteMail()) && !user.getLogin().equals(user.geteMail())) {
        // putting an email if it exist an other one different from the user name
        scimUser.setEmails(new ArrayList<>(1));
        final Email email = new Email();
        email.setValue(user.geteMail());
        email.setPrimary(true);
        scimUser.getEmails().add(email);
      }
      final Name name = new Name();
      name.setGivenName(user.getFirstName());
      if (!user.getLogin().equals(user.getLastName())) {
        name.setFamilyName(user.getLastName());
        name.setFormatted(user.getDisplayedName());
      } else {
        name.setFormatted(user.getFirstName());
      }
      scimUser.setName(name);
      scimUser.setActive(user.isValidState());
    }
  }

  private static String encodeUserId(User user) {
    return "sp@domain" + user.getDomainId() + "$" + user.getId();
  }

  static String decodeUserId(String encodedId) {
    if (isNotDefined(encodedId)) {
      return null;
    }
    final String[] encodedSpId = encodedId.split("sp@domain");
    return encodedSpId.length == 1 ? encodedSpId[0] : encodedSpId[1].split("[$]")[1];
  }
}
