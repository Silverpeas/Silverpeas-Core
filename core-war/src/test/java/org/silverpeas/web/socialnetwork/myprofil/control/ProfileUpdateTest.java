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
package org.silverpeas.web.socialnetwork.myprofil.control;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.domain.exception.ReadOnlyDomainException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.kernel.test.UnitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests on the outcome of the update of the profile of a user.
 * @author mmoquillon
 */
@UnitTest
class ProfileUpdateTest {

  @Test
  void aRefusalOfTheDomainIsReportedAsSuch() throws AdminException {
    AdminException failure = wrapped(new ReadOnlyDomainException("read-only", null));

    ProfileUpdate update = ProfileUpdate.failedWith(failure, false);

    assertThat(update, is(ProfileUpdate.REFUSED_BY_DOMAIN));
    assertThat(update.isDone(), is(false));
  }

  @Test
  void aRefusalOfTheDomainWithTheExtraDataSavedIsReportedAsSuch() throws AdminException {
    AdminException failure = wrapped(new ReadOnlyDomainException("read-only", null));

    ProfileUpdate update = ProfileUpdate.failedWith(failure, true);

    assertThat(update, is(ProfileUpdate.REFUSED_BY_DOMAIN_EXTRA_DATA_SAVED));
    assertThat(update.isDone(), is(false));
  }

  @Test
  void anyOtherFailureIsRethrown() {
    AdminException failure = wrapped(new AdminException("LDAP access error"));

    AdminException thrown =
        assertThrows(AdminException.class, () -> ProfileUpdate.failedWith(failure, true));

    assertThat(thrown, is(sameInstance(failure)));
  }

  @Test
  void eachOutcomeHasItsOwnMessage() {
    assertThat(ProfileUpdate.DONE.isDone(), is(true));
    assertThat(ProfileUpdate.DONE.getMessageKey(), is("myProfile.MessageOK"));
    assertThat(ProfileUpdate.REFUSED_BY_DOMAIN.getMessageKey(),
        is("myProfile.Error_readOnlyDomain"));
    assertThat(ProfileUpdate.REFUSED_BY_DOMAIN_EXTRA_DATA_SAVED.getMessageKey(),
        is("myProfile.Error_readOnlyDomain_extraDataSaved"));
  }

  /**
   * The failure of the domain driver is wrapped by the layers of the administration service.
   */
  private static AdminException wrapped(final AdminException driverFailure) {
    return new AdminException("Failure on update of user 42",
        new AdminException("Failure on update of user 42", driverFailure));
  }
}
