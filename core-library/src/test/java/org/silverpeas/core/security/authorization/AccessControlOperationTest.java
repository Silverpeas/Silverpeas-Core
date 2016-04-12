/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.security.authorization;

import org.junit.Test;
import org.silverpeas.core.security.authorization.AccessControlOperation;

import java.util.EnumSet;
import java.util.Set;

import static org.silverpeas.core.security.authorization.AccessControlOperation.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public class AccessControlOperationTest {

  @Test
  public void testGeneralities() {
    assertThat(from(null), is(AccessControlOperation.unknown));
    assertThat(from("toto"), is(AccessControlOperation.unknown));

    assertThat(from("modification"), is(AccessControlOperation.modification));
    assertThat(from("modifiCation"), is(AccessControlOperation.unknown));
  }

  @Test
  public void testIsPersistActionFrom() {
    assertThat(isPersistActionFrom(null), is(false));
    assertThat(isPersistActionFrom(EnumSet.noneOf(AccessControlOperation.class)), is(false));
    assertThat(isPersistActionFrom(EnumSet.of(sharing)), is(false));
    assertThat(isPersistActionFrom(EnumSet.of(sharing, download)), is(false));
    Set<AccessControlOperation> allExceptingPersistOperations =
        EnumSet.allOf(AccessControlOperation.class);
    allExceptingPersistOperations.remove(creation);
    allExceptingPersistOperations.remove(modification);
    allExceptingPersistOperations.remove(deletion);
    assertThat(isPersistActionFrom(allExceptingPersistOperations), is(false));

    assertThat(isPersistActionFrom(EnumSet.of(sharing, download, creation)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(sharing, download, modification)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(sharing, download, deletion)), is(true));

    assertThat(isPersistActionFrom(EnumSet.of(creation)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(modification)), is(true));
    assertThat(isPersistActionFrom(EnumSet.of(deletion)), is(true));

    assertThat(isPersistActionFrom(EnumSet.of(creation, modification, deletion)), is(true));
  }

  @Test
  public void testIsSharingActionFrom() {
    assertThat(isSharingActionFrom(null), is(false));
    assertThat(isSharingActionFrom(EnumSet.noneOf(AccessControlOperation.class)), is(false));
    Set<AccessControlOperation> allExceptingSharing = EnumSet.allOf(AccessControlOperation.class);
    allExceptingSharing.remove(sharing);
    assertThat(isSharingActionFrom(allExceptingSharing), is(false));

    assertThat(isSharingActionFrom(EnumSet.of(creation, sharing)), is(true));
    assertThat(isSharingActionFrom(EnumSet.of(sharing)), is(true));
  }

  @Test
  public void testIsDownloadActionFrom() {
    assertThat(isDownloadActionFrom(null), is(false));
    assertThat(isDownloadActionFrom(EnumSet.noneOf(AccessControlOperation.class)), is(false));
    Set<AccessControlOperation> allExceptingDownload = EnumSet.allOf(AccessControlOperation.class);
    allExceptingDownload.remove(download);
    assertThat(isDownloadActionFrom(allExceptingDownload), is(false));

    assertThat(isDownloadActionFrom(EnumSet.of(creation, download)), is(true));
    assertThat(isDownloadActionFrom(EnumSet.of(download)), is(true));
  }
}