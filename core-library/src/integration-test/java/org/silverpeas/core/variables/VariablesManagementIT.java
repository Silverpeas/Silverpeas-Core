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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.variables;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests on the management of {@link Variable} with their
 * {@link VariableScheduledValue}.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class VariablesManagementIT {

  static final String TABLE_CREATION_SCRIPT = "/org/silverpeas/core/variables/create_table.sql";

  static final String INITIALIZATION_SCRIPT =
      "/org/silverpeas/core/variables/variables-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(INITIALIZATION_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(VariablesManagementIT.class)
        .addAdministrationFeatures()
        .addSilverpeasExceptionBases()
        .addJpaPersistenceFeatures()
        .addPackages(true, "org.silverpeas.core.variables")
        .addClasses(PublicationTemplateManager.class)
        .addAsResource(TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void setUpTest() throws Exception {
    User user = mock(User.class);
    when(user.getId()).thenReturn("26");
    CacheServiceProvider.clearAllThreadCaches();
    ((SessionCacheService) CacheServiceProvider.getSessionCacheService()).newSessionCache(user);
    OperationContext.fromUser(user);
  }

  @Test
  public void empty() {
    // just to test the war with the tests is deployed correctly
  }

  @Test
  public void createANonValuedVariable() {
    final String label = "A Label";
    final String description = "A short description about the variable";
    Variable variable = new Variable(label, description);
    assertThat(variable.getVariableValues().isEmpty(), is(true));

    Variable savedVariable = variable.save();

    savedVariable = Variable.getById(savedVariable.getId());
    assertThat(savedVariable.getLabel(), is(label));
    assertThat(savedVariable.getDescription(), is(description));
    assertThat(savedVariable.getVariableValues().isEmpty(), is(true));
  }

  @Test
  public void createAValuedVariableInAFixedPeriod() throws SQLException {
    final String label = "A Label";
    final String description = "A short description about the variable";
    final LocalDate yesterday = LocalDate.now().minusDays(1);
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    Variable variable = new Variable(label, description);
    VariableScheduledValue value =
        new VariableScheduledValue("prout prout", Period.between(yesterday, tomorrow));
    Variable savedVariable = Transaction.performInOne(() -> {
      variable.getVariableValues().add(value);
      assertThat(variable.getVariableValues().size(), is(1));
      return variable.save();
    });

    savedVariable = Variable.getById(savedVariable.getId());
    assertThat(savedVariable.getLabel(), is(label));
    assertThat(savedVariable.getDescription(), is(description));
    assertThat(savedVariable.getVariableValues().size(), is(1));
    assertThat(savedVariable.getVariableValues().getCurrent().isPresent(), is(true));
    assertThat(savedVariable.getVariableValues().getCurrent().get().getValue(),
        is(value.getValue()));
  }

  @Test
  public void createAValuedVariableEndingAtAFixedDate() {
    final String label = "A Label";
    final String description = "A short description about the variable";
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    Variable variable = new Variable(label, description);
    VariableScheduledValue value =
        new VariableScheduledValue("prout prout", Period.between(LocalDate.MIN, tomorrow));
    Variable savedVariable = Transaction.performInOne(() -> {
      variable.getVariableValues().add(value);
      assertThat(variable.getVariableValues().size(), is(1));
      return variable.save();
    });

    savedVariable = Variable.getById(savedVariable.getId());
    assertThat(savedVariable.getLabel(), is(label));
    assertThat(savedVariable.getDescription(), is(description));
    assertThat(savedVariable.getVariableValues().size(), is(1));
    assertThat(savedVariable.getVariableValues().getCurrent().isPresent(), is(true));
    assertThat(savedVariable.getVariableValues().getCurrent().get().getValue(),
        is(value.getValue()));
  }

  @Test
  public void createAValuedVariableStartingAtAFixedDate() {
    final String label = "A Label";
    final String description = "A short description about the variable";
    final LocalDate yesterday = LocalDate.now().minusDays(1);
    Variable variable = new Variable(label, description);
    VariableScheduledValue value =
        new VariableScheduledValue("prout prout", Period.between(yesterday, LocalDate.MAX));
    Variable savedVariable = Transaction.performInOne(() -> {
      variable.getVariableValues().add(value);
      assertThat(variable.getVariableValues().size(), is(1));
      return variable.save();
    });

    savedVariable = Variable.getById(savedVariable.getId());
    assertThat(savedVariable.getLabel(), is(label));
    assertThat(savedVariable.getDescription(), is(description));
    assertThat(savedVariable.getVariableValues().size(), is(1));
    assertThat(savedVariable.getVariableValues().getCurrent().isPresent(), is(true));
    assertThat(savedVariable.getVariableValues().getCurrent().get().getValue(),
        is(value.getValue()));
  }

  @Test
  public void createAValuedVariableInANonFixedPeriod() {
    final String label = "A Label";
    final String description = "A short description about the variable";
    Variable variable = new Variable(label, description);
    VariableScheduledValue value =
        new VariableScheduledValue("prout prout", Period.between(LocalDate.MIN, LocalDate.MAX));
    Variable savedVariable = Transaction.performInOne(() -> {
      variable.getVariableValues().add(value);
      assertThat(variable.getVariableValues().size(), is(1));
      return variable.save();
    });

    savedVariable = Variable.getById(savedVariable.getId());
    assertThat(savedVariable.getLabel(), is(label));
    assertThat(savedVariable.getDescription(), is(description));
    assertThat(savedVariable.getVariableValues().size(), is(1));
    assertThat(savedVariable.getVariableValues().getCurrent().isPresent(), is(true));
    assertThat(savedVariable.getVariableValues().getCurrent().get().getValue(),
        is(value.getValue()));
  }

  @Test
  public void findTheCurrentVariableValueAmongSeverals() {
    final String label = "A Label";
    final String description = "A short description about the variable";
    final LocalDate yesterday = LocalDate.now().minusDays(1);
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    Variable variable = new Variable(label, description);
    variable.getVariableValues()
        .add(new VariableScheduledValue("prout prout",
            Period.between(yesterday.minusDays(2), yesterday)));
    variable.getVariableValues()
        .add(new VariableScheduledValue("prout", Period.between(yesterday, tomorrow)));
    variable.getVariableValues()
        .add(new VariableScheduledValue("prout only",
            Period.between(tomorrow, tomorrow.plusDays(2))));
    Variable savedVariable = variable.save();
    assertThat(savedVariable.getVariableValues().size(), is(3));

    savedVariable = Variable.getById(savedVariable.getId());
    assertThat(savedVariable.getLabel(), is(label));
    assertThat(savedVariable.getDescription(), is(description));
    assertThat(savedVariable.getVariableValues().size(), is(3));
    assertThat(savedVariable.getVariableValues().getCurrent().isPresent(), is(true));
    assertThat(savedVariable.getVariableValues().getCurrent().get().getValue(), is("prout"));
  }

  @Test
  public void findCurrentNextAndPreviousScheduledValues() {
    final String variableId = "2";
    final LocalDate today = LocalDate.now();
    Variable variable = Variable.getById(variableId);
    assertThat(variable.getVariableValues().getCurrent().isPresent(), is(false));
    assertThat(variable.getVariableValues().getNext().isPresent(), is(false));
    assertThat(variable.getVariableValues().getPrevious().isPresent(), is(true));
    assertThat(variable.getVariableValues().getPrevious().get().getPeriod().endsBefore(today),
        is(true));
  }

  @Test
  public void addANewVariableValueForAGivenPeriod() {
    final String variableId = "2";
    final String newValue = "A New Value 2";
    final LocalDate today = LocalDate.now();
    final LocalDate yesterday = today.minusDays(1);
    final LocalDate tomorrow = today.plusDays(1);

    Variable variable = Variable.getById(variableId);
    int valueCount = variable.getVariableValues().size();
    assertThat(variable.getVariableValues().getCurrent().isPresent(), is(false));

    VariableScheduledValue newVariableValue =
        new VariableScheduledValue(newValue, Period.between(yesterday, tomorrow));
    newVariableValue = variable.getVariableValues().addAndSave(newVariableValue);
    assertThat(newVariableValue.getId(), notNullValue());
    assertThat(newVariableValue.isPersisted(), is(true));

    variable = Variable.getById(variableId);
    assertThat(variable.getVariableValues().size(), is(valueCount + 1));
    assertThat(variable.getVariableValues().getCurrent().isPresent(), is(true));
    assertThat(variable.getVariableValues().getCurrent().get().getPeriod().includes(today),
        is(true));
    assertThat(variable.getVariableValues().getCurrent().get().getValue(), is(newValue));
  }

  @Test
  public void updateAnExistingVariableValue() {
    final String variableId = "3";
    final String newValue = "A New Value 3";
    final LocalDate today = LocalDate.now();
    final LocalDate yesterday = today.minusDays(1);
    final LocalDate tomorrow = today.plusDays(1);

    Variable variable = Variable.getById(variableId);
    assertThat(variable.getVariableValues().size(), is(1));

    Optional<VariableScheduledValue> optionalVariableValue =
        variable.getVariableValues().get(variableId);
    if (optionalVariableValue.isPresent()) {
      VariableScheduledValue variableValue = optionalVariableValue.get();
      variableValue.updateFrom(
          new VariableScheduledValue(newValue, Period.between(yesterday, tomorrow)));

      variable = Variable.getById(variableId);
      assertThat(variable.getVariableValues().size(), is(1));
      assertThat(variable.getVariableValues().getCurrent().isPresent(), is(true));
      assertThat(variable.getVariableValues().getCurrent().get().getPeriod().getStartDate(),
          is(yesterday));
      assertThat(variable.getVariableValues().getCurrent().get().getPeriod().getEndDate(),
          is(tomorrow));
      assertThat(variable.getVariableValues().getCurrent().get().getValue(), is(newValue));
    } else {
      fail("The variable " + variableId + " should have at least one variable value");
    }
  }

  @Test
  public void deleteAnExistingVariableValue() {
    final String variableId = "3";
    final Variable variable = Variable.getById(variableId);
    assertThat(variable.getVariableValues().size(), is(1));

    variable.getVariableValues().getCurrent().ifPresent(v -> {
      variable.getVariableValues().remove(v.getId());
      variable.save();
    });

    Variable actual = Variable.getById(variableId);
    assertThat(actual.getVariableValues().isEmpty(), is(true));
  }

  @Test
  public void getAllTheCurrentVariableValues() {
    List<VariableScheduledValue> currentValues = VariableScheduledValue.getCurrentOnes();
    assertThat(currentValues.size(), is(1));
    assertThat(currentValues.get(0).getId(), is("3"));
    assertThat(currentValues.get(0).getVariable().getId(), is("3"));
  }

  //@Test
  public void getAllTheCurrentValueOfVariableHavingSeveralCurrentValues() {
    final String variableId = "3";
    final LocalDate yesterday = LocalDate.now().minusDays(1);
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    final Variable variable = Variable.getById(variableId);
    VariableScheduledValue newVariableValue =
        new VariableScheduledValue("toto chez les papoos", Period.between(yesterday, tomorrow));
    VariableScheduledValue newValue = variable.getVariableValues().addAndSave(newVariableValue);

    List<VariableScheduledValue> currentValues = VariableScheduledValue.getCurrentOnes();
    assertThat(currentValues.size(), is(1));
    assertThat(currentValues.get(0).getId(), is(newValue.getId()));
    assertThat(currentValues.get(0).getVariable().getId(), is(variableId));
  }
}
