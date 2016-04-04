/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.subscription.util;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.junit.Test;

import static org.silverpeas.core.subscription.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionUtilTest {

  @Test
  public void isUserSameVisibilityAsTheCurrentRequester() {
    UserDetail requester = mock(UserDetail.class);
    when(requester.getDomainId()).thenReturn("0");
    UserDetail user = mock(UserDetail.class);
    when(user.getDomainId()).thenReturn("0");
    assertThat(isSameVisibilityAsTheCurrentRequester(user, requester), is(true));

    // Not same domainId but no domain isolation
    when(user.getDomainId()).thenReturn("1");
    assertThat(isSameVisibilityAsTheCurrentRequester(user, requester), is(true));

    // Not same domainId but no domain isolation
    when(requester.isDomainRestricted()).thenReturn(true);
    assertThat(isSameVisibilityAsTheCurrentRequester(user, requester), is(false));
  }

  @Test
  public void isGroupSameVisibilityAsTheCurrentRequester() {
    UserDetail requester = mock(UserDetail.class);
    when(requester.getDomainId()).thenReturn("0");
    Group group = mock(Group.class);
    when(group.getDomainId()).thenReturn("0");
    assertThat(isSameVisibilityAsTheCurrentRequester(group, requester), is(true));

    // Not same domainId but no domain isolation
    when(group.getDomainId()).thenReturn("1");
    assertThat(isSameVisibilityAsTheCurrentRequester(group, requester), is(true));

    // Not same domainId but no domain isolation
    when(requester.isDomainRestricted()).thenReturn(true);
    assertThat(isSameVisibilityAsTheCurrentRequester(group, requester), is(false));
  }
}
