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
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.kernel.test.UnitTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the computation of the changes of a user to write into the LDAP directory.
 * @author mmoquillon
 */
@UnitTest
class LDAPUserChangesTest {

  private static final String CITY = "city";
  private static final String PHONE = "phone";
  private static final String MATRICULE = "matricule";

  private DomainDriver domainDriver;
  private UserFull userInDirectory;

  @BeforeEach
  void setUpUserInDirectory() {
    domainDriver = mock(DomainDriver.class);
    when(domainDriver.getPropertiesNames()).thenReturn(new String[]{CITY, PHONE, MATRICULE});
    DomainProperty city = property(CITY, true, false);
    DomainProperty phone = property(PHONE, false, true);
    DomainProperty matricule = property(MATRICULE, false, false);
    when(domainDriver.getProperty(CITY)).thenReturn(city);
    when(domainDriver.getProperty(PHONE)).thenReturn(phone);
    when(domainDriver.getProperty(MATRICULE)).thenReturn(matricule);

    userInDirectory = new UserFull(domainDriver);
    userInDirectory.setFirstName("John");
    userInDirectory.setLastName("Doe");
    userInDirectory.setEmailAddress("john.doe@silverpeas.org");
    userInDirectory.setValue(CITY, "Grenoble");
    userInDirectory.setValue(MATRICULE, "7");
  }

  @Test
  void noChangesWhenTheUserIsUpdatedWithTheSameData() {
    UserFull updatedUser = copyOf(userInDirectory);

    LDAPUserChanges changes = new LDAPUserChanges(userInDirectory, updatedUser);

    assertThat(changes.isEmpty(), is(true));
    assertThat(changes.isFirstNameChanged(), is(false));
    assertThat(changes.isLastNameChanged(), is(false));
    assertThat(changes.isEmailAddressChanged(), is(false));
    assertThat(changes.getChangedProperties(), is(empty()));
  }

  @Test
  void noChangesWhenAnUndefinedValueIsUpdatedWithAnEmptyOne() {
    userInDirectory.setEmailAddress(null);
    UserFull updatedUser = copyOf(userInDirectory);
    updatedUser.setEmailAddress("");
    updatedUser.setValue(PHONE, "");

    LDAPUserChanges changes = new LDAPUserChanges(userInDirectory, updatedUser);

    assertThat(changes.isEmpty(), is(true));
  }

  @Test
  void changesOnlyOnTheModifiedBasicData() {
    UserFull updatedUser = copyOf(userInDirectory);
    updatedUser.setLastName("Smith");

    LDAPUserChanges changes = new LDAPUserChanges(userInDirectory, updatedUser);

    assertThat(changes.isEmpty(), is(false));
    assertThat(changes.isFirstNameChanged(), is(false));
    assertThat(changes.isLastNameChanged(), is(true));
    assertThat(changes.isEmailAddressChanged(), is(false));
    assertThat(changes.getChangedProperties(), is(empty()));
  }

  @Test
  void changesOnlyOnTheModifiedUpdatableProperties() {
    UserFull updatedUser = copyOf(userInDirectory);
    updatedUser.setValue(PHONE, "0102030405");

    LDAPUserChanges changes = new LDAPUserChanges(userInDirectory, updatedUser);

    assertThat(changes.isEmpty(), is(false));
    assertThat(changes.isFirstNameChanged(), is(false));
    assertThat(changes.isLastNameChanged(), is(false));
    assertThat(changes.isEmailAddressChanged(), is(false));
    assertThat(namesOf(changes.getChangedProperties()), contains(PHONE));
  }

  @Test
  void aResetPropertyIsAChange() {
    UserFull updatedUser = copyOf(userInDirectory);
    updatedUser.setValue(CITY, "");

    LDAPUserChanges changes = new LDAPUserChanges(userInDirectory, updatedUser);

    assertThat(namesOf(changes.getChangedProperties()), contains(CITY));
  }

  @Test
  void aModifiedPropertyThatIsNotUpdatableIsNotAChange() {
    UserFull updatedUser = copyOf(userInDirectory);
    updatedUser.setValue(MATRICULE, "123");

    LDAPUserChanges changes = new LDAPUserChanges(userInDirectory, updatedUser);

    assertThat(changes.isEmpty(), is(true));
  }

  private UserFull copyOf(final UserFull user) {
    UserFull copy = new UserFull(domainDriver, user);
    for (String name : user.getPropertiesNames()) {
      copy.setValue(name, user.getValue(name));
    }
    return copy;
  }

  private static DomainProperty property(final String name, final boolean updatableByUser,
      final boolean updatableByAdmin) {
    DomainProperty property = mock(DomainProperty.class);
    when(property.getName()).thenReturn(name);
    when(property.isUpdateAllowedToUser()).thenReturn(updatableByUser);
    when(property.isUpdateAllowedToAdmin()).thenReturn(updatableByAdmin);
    return property;
  }

  private static List<String> namesOf(final List<DomainProperty> properties) {
    return properties.stream().map(DomainProperty::getName).collect(Collectors.toList());
  }
}
