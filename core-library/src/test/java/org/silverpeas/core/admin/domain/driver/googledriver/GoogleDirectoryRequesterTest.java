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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.json.GenericJson;
import com.google.api.services.admin.directory.model.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.AdminException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@Disabled
class GoogleDirectoryRequesterTest {

  @Test
  void listUsers() throws AdminException {
    final List<User> users = getRequester().users();
    assertThat(users, not(empty()));
    trace(users.stream().map(GenericJson::toString));
  }

  @SuppressWarnings("unchecked")
  @Test
  void getAUser() throws AdminException {
    final User user = getRequester().user("108687024306775155320");
    assertThat(user, notNullValue());
    assertThat(user.getId(), is("108687024306775155320"));
    final List<Map<String, String>> emails = (List) user.getEmails();
    assertThat(emails.stream().anyMatch(m -> m.get("address").equals("psc@silverpeas.org")),
        is(true));
  }

  @Nonnull
  private GoogleDirectoryRequester getRequester() {
    return new GoogleDirectoryRequester("directory@silverpeas.com", "auth.json", "");
  }

  private void trace(final Stream<String> stream) {
    System.out.println(stream.collect(Collectors.joining(",", "[", "];")));
  }
}