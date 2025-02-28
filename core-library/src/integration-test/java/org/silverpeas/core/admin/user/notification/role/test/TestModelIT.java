/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test to ensure the model used for tests works correctly.
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class TestModelIT {

  @Inject
  private ResourceValidators resourceValidators;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule
          .createTablesFrom(
              "/org/silverpeas/core/admin/user/notification/role/create_database.sql")
          .loadInitialDataSetFrom(
              "/org/silverpeas/core/admin/user/notification/role/insert_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(TestModelIT.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .addPublicationTemplateFeatures()
        .addAsResource("org/silverpeas/core/admin/user/notification/role")
        .testFocusedOn(warBuilder ->
            warBuilder.addPackages(true, TestModelIT.class.getPackageName()))
        .build();
  }

  @Before
  public void setUpCurrentRequester() {
    SessionCacheAccessor sessionCacheAccessor =
        CacheAccessorProvider.getSessionCacheAccessor();
    sessionCacheAccessor.newSessionCache(User.getById("1"));
  }

  @Test
  public void existingResourceShouldBeFound() {
    Optional<Resource> mayBeAResource = Resource.getById("1");
    assertThat(mayBeAResource.isPresent(), is(true));

    Resource resource = mayBeAResource.get();
    assertThat(resource.getId(), is("1"));
    assertThat(resource.getName(), is("Salle Chartreuse"));
    assertThat(resource.getDescription().isEmpty(), is(true));
    assertThat(resource.isValidated(), is(false));
    assertThat(resource.getIdentifier().getComponentInstanceId(), is("myComponent1"));
  }

  @Test
  public void existingResourceValidatorsShouldBeFound() {
    Optional<Resource> mayBeAResource = Resource.getById("1");
    assertThat(mayBeAResource.isPresent(), is(true));
    List<String> expectedValidators = List.of("0", "1", "2");

    Resource resource = mayBeAResource.get();
    var validators = resource.getPossibleValidators();
    assertThat(validators.size(), is(expectedValidators.size()));
    validators.stream()
        .map(User::getId)
        .forEach(u -> assertThat(expectedValidators.contains(u), is(true)));
  }

  @Test
  public void validatingAResourceValidateIt() {
    Optional<Resource> mayBeAResource = Resource.getById("1");
    assertThat(mayBeAResource.isPresent(), is(true));

    Resource resource = mayBeAResource.get();
    assertThat(resource.isValidated(), is(false));

    resource.validate();

    assertThat(resource.isValidated(), is(true));
    assertThat(resource.getValidator().getId(), is("1"));

    assertResourceChangeIsPersisted(resource);
  }

  @Test
  public void addingAValidatorToAResourcePersistIt() {
    User expectedUser = User.getById("10");

    Optional<Resource> mayBeAResource = Resource.getById("1");
    assertThat(mayBeAResource.isPresent(), is(true));

    Resource resource = mayBeAResource.get();
    assertThat(resource.getPossibleValidators().stream()
        .map(User::getId)
        .noneMatch(u -> u.equals(expectedUser.getId())), is(true));

    resource.addAsValidator(expectedUser);

    assertThat(resource.getPossibleValidators().stream()
        .map(User::getId)
        .anyMatch(u -> u.equals(expectedUser.getId())), is(true));

    assertResourceChangeIsPersisted(resource);
  }

  @Test
  public void allRegisteredValidatorsShouldBeFound() {
    List<Validator> registeredValidators = resourceValidators.getAll("myComponent1");
    assertThat(registeredValidators.size(), is(4));

    assertThat(registeredValidators.get(0).getResource().getId(), is("1"));
    assertThat(registeredValidators.get(0).getUser().getId(), is("0"));
    assertThat(registeredValidators.get(1).getResource().getId(), is("1"));
    assertThat(registeredValidators.get(1).getUser().getId(), is("1"));
    assertThat(registeredValidators.get(2).getResource().getId(), is("1"));
    assertThat(registeredValidators.get(2).getUser().getId(), is("2"));

    assertThat(registeredValidators.get(3).getResource().getId(), is("3"));
    assertThat(registeredValidators.get(3).getUser().getId(), is("0"));
  }

  private void assertResourceChangeIsPersisted(Resource expected) {
    Optional<Resource> actualResource = Resource.getById(expected.getId());
    assertThat(actualResource.isPresent(), is(true));

    Resource actual = actualResource.get();
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));
    assertThat(actual.getDescription(), is(expected.getDescription()));
    assertThat(actual.isValidated(), is(expected.isValidated()));
    if (actual.isValidated()) {
      assertThat(actual.getValidator().getId(), is(expected.getValidator().getId()));
      assertThat(actual.getValidationDate(), is(expected.getValidationDate()));
    }

    assertThat(actual.getPossibleValidators(), is(expected.getPossibleValidators()));
  }
}
