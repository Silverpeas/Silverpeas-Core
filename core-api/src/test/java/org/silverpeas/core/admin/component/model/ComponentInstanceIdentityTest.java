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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the identity of both the personal and the shared component instances.
 */
class ComponentInstanceIdentityTest {

  private final ComponentInstanceIdentityFactory factory = new ComponentInstanceIdentityFactory();

  @Nested
  class PersonalComponentInstanceIdentity {

    @Test
    void identityOfAPersonalInstanceOwnedByTheAdministratorIsValid() {
      User admin = aUserWithId("0");
      SilverpeasPersonalComponentInstance.Identity identity = factory.create("userCalendar", admin);
      assertThat(identity.toString(), is("userCalendar0_PCI"));
      assertThat(identity.getUserId(), is("0"));
    }

    @Test
    void identityOfAPersonalInstanceOwnedByAnyUserIsValid() {
      User user = aUserWithId("42");
      SilverpeasPersonalComponentInstance.Identity identity = factory.create("userCalendar", user);
      assertThat(identity.toString(), is("userCalendar42_PCI"));
      assertThat(identity.getUserId(), is("42"));
    }

    @Test
    void identifierOfAPersonalInstanceOwnedByTheAdministratorIsDecoded() {
      String instanceId = "userCalendar0_PCI";
      assertThat(SilverpeasPersonalComponentInstance.Identity.isValid(instanceId), is(true));
      SilverpeasComponentInstance.Identity identity = factory.create(instanceId);
      assertThat(identity.getComponentName(), is("userCalendar"));
      assertThat(identity.getInstanceLocalId(), is(0));
    }

    @Test
    void undefinedLocalIdentifierIsAccepted() {
      var identity = new SilverpeasPersonalComponentInstance.Identity("userCalendar", -1);
      assertThat(identity.getInstanceLocalId(), is(-1));
    }

    @Test
    void identityOfAnUndefinedInstanceHasNoComponentName() {
      var identity = new SilverpeasPersonalComponentInstance.Identity("", -1);
      assertThat(identity.getInstanceLocalId(), is(-1));
    }

    @Test
    void negativeLocalIdentifierIsRejected() {
      assertThrows(IllegalArgumentException.class,
          () -> new SilverpeasPersonalComponentInstance.Identity("userCalendar", -2));
    }

    @Test
    void undefinedComponentNameIsRejected() {
      assertThrows(IllegalArgumentException.class,
          () -> new SilverpeasPersonalComponentInstance.Identity("", 0));
    }
  }

  @Nested
  class SharedComponentInstanceIdentity {

    @Test
    void identityWithAPositiveLocalIdentifierIsValid() {
      SilverpeasSharedComponentInstance.Identity identity = factory.create("kmelia", "1");
      assertThat(identity.toString(), is("kmelia1"));
    }

    @Test
    void identifierWithAPositiveLocalIdentifierIsDecoded() {
      String instanceId = "kmelia12";
      assertThat(SilverpeasSharedComponentInstance.Identity.isValid(instanceId), is(true));
      SilverpeasComponentInstance.Identity identity = factory.create(instanceId);
      assertThat(identity.getComponentName(), is("kmelia"));
      assertThat(identity.getInstanceLocalId(), is(12));
    }

    @Test
    void undefinedLocalIdentifierIsAccepted() {
      var identity = new SilverpeasSharedComponentInstance.Identity("kmelia", -1);
      assertThat(identity.getInstanceLocalId(), is(-1));
    }

    @Test
    void identityOfAnUndefinedInstanceHasNoComponentName() {
      SilverpeasSharedComponentInstance.Identity identity = factory.create("", "-1");
      assertThat(identity.toString(), is("-1"));
    }

    @Test
    void zeroAsLocalIdentifierIsRejected() {
      assertThrows(IllegalArgumentException.class, () -> factory.create("kmelia", "0"));
    }

    @Test
    void identifierWithZeroAsLocalIdentifierIsRejected() {
      String instanceId = "kmelia0";
      assertThat(SilverpeasSharedComponentInstance.Identity.isValid(instanceId), is(false));
      assertThat(factory.isValid(instanceId), is(false));
      assertThrows(IllegalArgumentException.class,
          () -> SilverpeasSharedComponentInstance.getIdentity(instanceId));
    }

    @Test
    void undefinedComponentNameIsRejected() {
      assertThrows(IllegalArgumentException.class,
          () -> new SilverpeasSharedComponentInstance.Identity(null, 1));
    }
  }

  private static User aUserWithId(String id) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(id);
    return user;
  }
}
