/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.util.ArrayMap;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.UserName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author silveryocha
 */
class GoogleUserBuilder {
  private final User user = new User();

  static GoogleUserBuilder aUser(final String id, final String ou) {
    GoogleUserBuilder builder = new GoogleUserBuilder();
    builder.user.setId(id);
    final UserName name = new UserName().setFamilyName("FN_" + id).setGivenName("GN_" + id);
    builder.user.setName(name);
    builder.user.setEmails(new ArrayList<>());
    builder.user.setOrgUnitPath(ou);
    builder.user.setArchived(false);
    builder.user.setSuspended(false);
    builder.withPrimaryEmail(name.getGivenName() + "." + name.getFamilyName() + "@silverpeas.org");
    return builder;
  }

  @SuppressWarnings("unchecked")
  private GoogleUserBuilder withPrimaryEmail(final String address) {
    final Map<String, Object> data = new HashMap<>();
    data.put("address", address);
    data.put("primary", true);
    ((List) user.getEmails()).add(data);
    user.setPrimaryEmail(address);
    return this;
  }

  @SuppressWarnings("unchecked")
  GoogleUserBuilder withEmail(final String address, final String type) {
    final Map<String, String> data = new HashMap<>();
    data.put("address", address);
    data.put("type", type);
    ((List) user.getEmails()).add(data);
    return this;
  }

  @SuppressWarnings("unchecked")
  GoogleUserBuilder withCustomEmail(final String address, final String type) {
    final Map<String, String> data = new HashMap<>();
    data.put("address", address);
    data.put("customType", type);
    ((List) user.getEmails()).add(data);
    return this;
  }

  GoogleUserBuilder suspended() {
    user.setSuspended(true);
    return this;
  }

  @SuppressWarnings("unchecked")
  GoogleUserBuilder withCustomSchemas(final String schemas, final String key, final String value) {
    Map<String, Map<String, Object>> customSchemas = user.getCustomSchemas();
    if (customSchemas == null) {
      customSchemas = new ArrayMap<>();
      user.setCustomSchemas(customSchemas);
    }
    customSchemas.putIfAbsent(schemas, new ArrayMap<>());
    customSchemas.get(schemas).put(key, value);
    return this;
  }

  User build() {
    return user;
  }
}
