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
package org.silverpeas.core.admin.user;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 17/12/13
 */
@EnableSilverTestEnv
class SilverpeasRoleTest {

  @Test
  void fromOneRoleAsString() {
    assertThat(SilverpeasRole.fromString((String) null), nullValue());
    assertThat(SilverpeasRole.fromString(""), nullValue());
    assertThat(SilverpeasRole.fromString(" "), nullValue());
    assertThat(SilverpeasRole.fromString(" admin "), is(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.fromString("admin"), is(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.fromString("AdmiN"), is(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.fromString("Manager"), is(SilverpeasRole.MANAGER));
    assertThat(SilverpeasRole.fromString("manager"), is(SilverpeasRole.MANAGER));
    for (final SilverpeasRole role : SilverpeasRole.values()) {
      assertThat(SilverpeasRole.fromString(role.getName()), is(role));
    }
  }

  @Test
  void exists() {
    assertThat(SilverpeasRole.exists(null), is(false));
    assertThat(SilverpeasRole.exists(""), is(false));
    assertThat(SilverpeasRole.exists(" "), is(false));
    assertThat(SilverpeasRole.exists(" admin "), is(true));
    assertThat(SilverpeasRole.exists("admin"), is(true));
    assertThat(SilverpeasRole.exists("AdmiN"), is(true));
    assertThat(SilverpeasRole.exists("Manager"), is(true));
    assertThat(SilverpeasRole.exists("manager"), is(true));
    for (final SilverpeasRole role : SilverpeasRole.values()) {
      assertThat(SilverpeasRole.exists(role.getName()), is(true));
    }
  }

  @Test
  void fromSeveralRolesAsStringWithMalformedParameter() {
    assertThat(SilverpeasRole.listFrom("a dmin"), empty());
  }

  @Test
  void fromSeveralRolesAsStringWithMalformedParameter2() {
    assertThat(SilverpeasRole.listFrom("admin, admin"), contains(SilverpeasRole.ADMIN));
  }

  @Test
  void fromSeveralRolesAsStringWithMalformedParameter3() {
    assertThat(SilverpeasRole.listFrom("admin,manager"),
        contains(SilverpeasRole.ADMIN, SilverpeasRole.MANAGER));
  }

  @Test
  void fromSeveralRolesAsString() {
    assertThat(SilverpeasRole.listFrom(null), empty());
    assertThat(SilverpeasRole.listFrom(" "), empty());
    assertThat(SilverpeasRole.listFrom("admin"), contains(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.listFrom("admin,admin,Admin,Manager,reader"),
        contains(SilverpeasRole.ADMIN, SilverpeasRole.MANAGER, SilverpeasRole.READER));
    assertThat(SilverpeasRole.listFrom("admin,admin,Admin, Manager,reader"),
        contains(SilverpeasRole.ADMIN, SilverpeasRole.MANAGER, SilverpeasRole.READER));
  }


  @Test
  void fromSeveralRolesAsStringArrayWithMalformedParameter() {
    assertThat(SilverpeasRole.fromStrings(new String[]{"a dmin"}), empty());
  }

  @Test
  void fromSeveralRolesAsStringArrayWithMalformedParameter2() {
    assertThat(SilverpeasRole.fromStrings(new String[]{"admin", " admin"}),
        contains(SilverpeasRole.ADMIN));
  }

  @Test
  void fromSeveralRolesAsStringArrayWithMalformedParameter3() {
    assertThat(SilverpeasRole.fromStrings(new String[]{"admin", "manager"}),
        contains(SilverpeasRole.ADMIN, SilverpeasRole.MANAGER));
  }

  @Test
  void fromSeveralRolesAsStringArray() {
    assertThat(SilverpeasRole.fromStrings((String[]) null), empty());
    assertThat(SilverpeasRole.fromStrings(new String[]{}), empty());
    assertThat(SilverpeasRole.fromStrings(new String[]{" "}), empty());
    assertThat(SilverpeasRole.fromStrings(new String[]{"admin"}), contains(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.fromStrings(new String[]{"admin", "admin"}), contains(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.fromStrings(new String[]{"admin", "admin", "Manager", "reader"}),
        contains(SilverpeasRole.ADMIN, SilverpeasRole.MANAGER, SilverpeasRole.READER));
  }

  @Test
  void asString() {
    assertThat(SilverpeasRole.asString(null), nullValue());
    assertThat(SilverpeasRole.asString(EnumSet.noneOf(SilverpeasRole.class)), is(""));
    assertThat(SilverpeasRole.asString(EnumSet.of(SilverpeasRole.ADMIN)), is("admin"));
    assertThat(SilverpeasRole.asString(EnumSet.of(SilverpeasRole.ADMIN, SilverpeasRole.ADMIN)),
        is("admin"));
    assertThat(SilverpeasRole.asString(EnumSet.of(SilverpeasRole.ADMIN, SilverpeasRole.MANAGER)),
        is("admin,Manager"));
  }

  @Test
  void isGreaterThan() {
    StringBuilder sb = new StringBuilder();
    SilverpeasRole[] roles = SilverpeasRole.values();
    for (int i = 1; i < roles.length; i++) {
      sb.append(roles[i - 1].getName()).append(", ");
      assertThat(roles[i - 1].getName() + " > " + roles[i - 1].getName(),
          roles[i - 1].isGreaterThan(roles[i - 1]), is(false));
      assertThat(roles[i - 1].getName() + " > " + roles[i].getName(),
          roles[i - 1].isGreaterThan(roles[i]), is(true));
    }
    sb.append(roles[roles.length - 1]);
    assertThat(sb.toString(),
        is("admin, supervisor, Manager, publisher, writer, privilegedUser, user, reader"));

    assertThat(SilverpeasRole.PUBLISHER.isGreaterThan(SilverpeasRole.WRITER), is(true));
    assertThat(SilverpeasRole.PUBLISHER.isGreaterThan(SilverpeasRole.PUBLISHER), is(false));
    assertThat(SilverpeasRole.PUBLISHER.isGreaterThan(SilverpeasRole.ADMIN), is(false));
  }

  @Test
  void isGreaterThanOrEquals() {
    assertThat(SilverpeasRole.PUBLISHER.isGreaterThanOrEquals(SilverpeasRole.WRITER), is(true));
    assertThat(SilverpeasRole.PUBLISHER.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER), is(true));
    assertThat(SilverpeasRole.PUBLISHER.isGreaterThanOrEquals(SilverpeasRole.ADMIN), is(false));
  }

  @Test
  void getHighestFrom() {
    assertThat(SilverpeasRole.getHighestFrom(), nullValue());
    assertThat(SilverpeasRole.getHighestFrom(SilverpeasRole.WRITER, SilverpeasRole.ADMIN),
        is(SilverpeasRole.ADMIN));
    assertThat(SilverpeasRole.getHighestFrom(SilverpeasRole.WRITER, SilverpeasRole.WRITER),
        is(SilverpeasRole.WRITER));
  }
}
