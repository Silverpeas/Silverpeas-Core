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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.stratelia.webactiv;

import org.junit.Test;
import org.silverpeas.core.admin.user.model.SilverpeasRole;

import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 17/12/13
 */
public class SilverpeasRoleTest {

  @Test
  public void fromOneRoleAsString() {
    assertThat(SilverpeasRole.from((String) null), nullValue());
    assertThat(SilverpeasRole.from(""), nullValue());
    assertThat(SilverpeasRole.from(" "), nullValue());
    assertThat(SilverpeasRole.from(" admin "), is(SilverpeasRole.admin));
    assertThat(SilverpeasRole.from("admin"), is(SilverpeasRole.admin));
    assertThat(SilverpeasRole.from("AdmiN"), is(SilverpeasRole.admin));
    assertThat(SilverpeasRole.from("Manager"), is(SilverpeasRole.Manager));
    assertThat(SilverpeasRole.from("manager"), is(SilverpeasRole.Manager));
    for (final SilverpeasRole role : SilverpeasRole.values()) {
      assertThat(SilverpeasRole.from(role.name()), is(role));
    }
  }

  @Test
  public void exists() {
    assertThat(SilverpeasRole.exists(null), is(false));
    assertThat(SilverpeasRole.exists(""), is(false));
    assertThat(SilverpeasRole.exists(" "), is(false));
    assertThat(SilverpeasRole.exists(" admin "), is(true));
    assertThat(SilverpeasRole.exists("admin"), is(true));
    assertThat(SilverpeasRole.exists("AdmiN"), is(true));
    assertThat(SilverpeasRole.exists("Manager"), is(true));
    assertThat(SilverpeasRole.exists("manager"), is(true));
    for (final SilverpeasRole role : SilverpeasRole.values()) {
      assertThat(SilverpeasRole.exists(role.name()), is(true));
    }
  }

  @Test
  public void fromSeveralRolesAsStringWithMalformedParameter() {
    assertThat(SilverpeasRole.listFrom("a dmin"), empty());
  }

  @Test
  public void fromSeveralRolesAsStringWithMalformedParameter2() {
    assertThat(SilverpeasRole.listFrom("admin, admin"), contains(SilverpeasRole.admin));
  }

  @Test
  public void fromSeveralRolesAsStringWithMalformedParameter3() {
    assertThat(SilverpeasRole.listFrom("admin,manager"),
        contains(SilverpeasRole.admin, SilverpeasRole.Manager));
  }

  @Test
  public void fromSeveralRolesAsString() {
    assertThat(SilverpeasRole.listFrom(null), empty());
    assertThat(SilverpeasRole.listFrom(" "), empty());
    assertThat(SilverpeasRole.listFrom("admin"), contains(SilverpeasRole.admin));
    assertThat(SilverpeasRole.listFrom("admin,admin,Admin,Manager,reader"),
        contains(SilverpeasRole.admin, SilverpeasRole.Manager, SilverpeasRole.reader));
    assertThat(SilverpeasRole.listFrom("admin,admin,Admin, Manager,reader"),
        contains(SilverpeasRole.admin, SilverpeasRole.Manager, SilverpeasRole.reader));
  }


  @Test
  public void fromSeveralRolesAsStringArrayWithMalformedParameter() {
    assertThat(SilverpeasRole.from(new String[]{"a dmin"}), empty());
  }

  @Test
  public void fromSeveralRolesAsStringArrayWithMalformedParameter2() {
    assertThat(SilverpeasRole.from(new String[]{"admin", " admin"}),
        contains(SilverpeasRole.admin));
  }

  @Test
  public void fromSeveralRolesAsStringArrayWithMalformedParameter3() {
    assertThat(SilverpeasRole.from(new String[]{"admin", "manager"}),
        contains(SilverpeasRole.admin, SilverpeasRole.Manager));
  }

  @Test
  public void fromSeveralRolesAsStringArray() {
    assertThat(SilverpeasRole.from((String[]) null), empty());
    assertThat(SilverpeasRole.from(new String[]{}), empty());
    assertThat(SilverpeasRole.from(new String[]{" "}), empty());
    assertThat(SilverpeasRole.from(new String[]{"admin"}), contains(SilverpeasRole.admin));
    assertThat(SilverpeasRole.from(new String[]{"admin", "admin"}), contains(SilverpeasRole.admin));
    assertThat(SilverpeasRole.from(new String[]{"admin", "admin", "Manager", "reader"}),
        contains(SilverpeasRole.admin, SilverpeasRole.Manager, SilverpeasRole.reader));
  }

  @Test
  public void asString() {
    assertThat(SilverpeasRole.asString(null), nullValue());
    assertThat(SilverpeasRole.asString(EnumSet.noneOf(SilverpeasRole.class)), is(""));
    assertThat(SilverpeasRole.asString(EnumSet.of(SilverpeasRole.admin)), is("admin"));
    assertThat(SilverpeasRole.asString(EnumSet.of(SilverpeasRole.admin, SilverpeasRole.admin)),
        is("admin"));
    assertThat(SilverpeasRole.asString(EnumSet.of(SilverpeasRole.admin, SilverpeasRole.Manager)),
        is("admin,Manager"));
  }

  @Test
  public void isGreaterThan() {
    StringBuilder sb = new StringBuilder();
    SilverpeasRole[] roles = SilverpeasRole.values();
    for (int i = 1; i < roles.length; i++) {
      sb.append(roles[i - 1].name()).append(", ");
      assertThat(roles[i - 1].name() + " > " + roles[i - 1].name(),
          roles[i - 1].isGreaterThan(roles[i - 1]), is(false));
      assertThat(roles[i - 1].name() + " > " + roles[i].name(),
          roles[i - 1].isGreaterThan(roles[i]), is(true));
    }
    sb.append(roles[roles.length - 1]);
    assertThat(sb.toString(),
        is("admin, Manager, publisher, writer, privilegedUser, user, reader, supervisor"));

    assertThat(SilverpeasRole.publisher.isGreaterThan(SilverpeasRole.writer), is(true));
    assertThat(SilverpeasRole.publisher.isGreaterThan(SilverpeasRole.publisher), is(false));
    assertThat(SilverpeasRole.publisher.isGreaterThan(SilverpeasRole.admin), is(false));
  }

  @Test
  public void isGreaterThanOrEquals() {
    assertThat(SilverpeasRole.publisher.isGreaterThanOrEquals(SilverpeasRole.writer), is(true));
    assertThat(SilverpeasRole.publisher.isGreaterThanOrEquals(SilverpeasRole.publisher), is(true));
    assertThat(SilverpeasRole.publisher.isGreaterThanOrEquals(SilverpeasRole.admin), is(false));
  }

  @Test
  public void getGreaterFrom() {
    assertThat(SilverpeasRole.getGreaterFrom(), nullValue());
    assertThat(SilverpeasRole.getGreaterFrom(SilverpeasRole.writer, SilverpeasRole.admin),
        is(SilverpeasRole.admin));
    assertThat(SilverpeasRole.getGreaterFrom(SilverpeasRole.writer, SilverpeasRole.writer),
        is(SilverpeasRole.writer));
  }
}
