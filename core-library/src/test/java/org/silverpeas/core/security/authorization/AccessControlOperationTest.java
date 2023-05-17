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
package org.silverpeas.core.security.authorization;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.security.authorization.AccessControlOperation.*;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
class AccessControlOperationTest {

  @Test
  void testGeneralities() {
    assertThat(from(null), is(AccessControlOperation.UNKNOWN));
    assertThat(from("toto"), is(AccessControlOperation.UNKNOWN));

    assertThat(from("modification"), is(AccessControlOperation.MODIFICATION));
    assertThat(from("modifiCation"), is(AccessControlOperation.MODIFICATION));
  }

  @Test
  void testIsPersistActionFrom() {
    assertThat(isPersistActionFrom(null), is(false));
    assertThat(isPersistActionFrom(EnumSet.noneOf(AccessControlOperation.class)), is(false));
    assertThat(isPersistActionFrom(EnumSet.of(SHARING)), is(false));
    assertThat(isPersistActionFrom(EnumSet.of(SHARING, DOWNLOAD)), is(false));
    Set<AccessControlOperation> allExceptingPersistOperations =
        EnumSet.allOf(AccessControlOperation.class);
    allExceptingPersistOperations.remove(CREATION);
    allExceptingPersistOperations.remove(MODIFICATION);
    allExceptingPersistOperations.remove(DELETION);
    assertThat(isPersistActionFrom(allExceptingPersistOperations), is(false));

    assertThat(isPersistActionFrom(EnumSet.of(SHARING, DOWNLOAD, CREATION)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(SHARING, DOWNLOAD, MODIFICATION)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(SHARING, DOWNLOAD, DELETION)), is(true));

    assertThat(isPersistActionFrom(EnumSet.of(CREATION)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(MODIFICATION)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(DELETION)), is(true));

    assertThat(isPersistActionFrom(EnumSet.of(CREATION, MODIFICATION, DELETION)), is(true));
  }

  @Test
  void testIsSharingActionFrom() {
    assertThat(isSharingActionFrom(null), is(false));
    assertThat(isSharingActionFrom(EnumSet.noneOf(AccessControlOperation.class)), is(false));
    Set<AccessControlOperation> allExceptingSharing = EnumSet.allOf(AccessControlOperation.class);
    allExceptingSharing.remove(SHARING);
    assertThat(isSharingActionFrom(allExceptingSharing), is(false));

    assertThat(isSharingActionFrom(EnumSet.of(CREATION, SHARING)), is(true));
    assertThat(isSharingActionFrom(EnumSet.of(SHARING)), is(true));
  }

  @Test
  void testIsDownloadActionFrom() {
    assertThat(isDownloadActionFrom(null), is(false));
    assertThat(isDownloadActionFrom(EnumSet.noneOf(AccessControlOperation.class)), is(false));
    Set<AccessControlOperation> allExceptingDownload = EnumSet.allOf(AccessControlOperation.class);
    allExceptingDownload.remove(DOWNLOAD);
    assertThat(isDownloadActionFrom(allExceptingDownload), is(false));

    assertThat(isDownloadActionFrom(EnumSet.of(CREATION, DOWNLOAD)), is(true));
    assertThat(isDownloadActionFrom(EnumSet.of(DOWNLOAD)), is(true));
  }
}