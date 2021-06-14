/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.admin.user.DefaultUserProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.ServiceProvider;

import java.time.OffsetDateTime;
import java.util.Date;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * A test context to be used in the unit tests covering on the API of tracking for contribution
 * modifications.
 * @author silveruser
 */
public class ContributionTrackingTestContext {

  static final OffsetDateTime YESTERDAY = OffsetDateTime.now().minusDays(1);

  private final UserDetail requester;

  ContributionTrackingTestContext() {
    this.requester = new UserDetail();
    requester.setId("0");
    requester.setFirstName("Toto");
    requester.setLastName("Tartempion");
    requester.setLogin("toto");
  }

  void initMocks() {
    UserProvider userProvider = UserProvider.get();
    User systemUser = userProvider.getSystemUser();
    when(userProvider.getUser(anyString())).thenAnswer(i -> {
      String id = i.getArgument(0);
      if (id.equals("0")) {
        return getRequester();
      } else if (id.equals(systemUser.getId())) {
        return systemUser;
      }
      UserDetail user = new UserDetail();
      user.setId(id);
      user.setLastName("Tartempion" + id);
      user.setFirstName("Toto");
      user.setLogin("toto" + id);
      return user;
    });
  }

  PublicationDetail getPublication(final String instanceId) {
    PublicationDetail publi = new PublicationDetail();
    publi.setPk(new PublicationPK("23", instanceId));
    publi.setName("My publi");
    publi.setDescription("A description");
    publi.setCreationDate(Date.from(YESTERDAY.toInstant()));
    publi.setCreatorId("1");
    publi.setUpdateDate(publi.getCreationDate());
    publi.setUpdaterId(publi.getCreatorId());
    return publi;
  }

  User getRequester() {
    return requester;
  }

  void setUpRequester() {
    UserProvider userProvider = ServiceProvider.getService(UserProvider.class);
    when(userProvider.getCurrentRequester()).thenReturn(getRequester());
  }
}
