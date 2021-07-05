/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.variables;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.test.extension.RequesterProvider;
import org.silverpeas.core.test.extension.TestManagedMock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv
class VariableTest {

  @TestManagedMock
  VariableScheduledValueRepository valuesRepository;

  @TestManagedMock
  VariablesRepository variablesRepository;

  @RequesterProvider
  User getCurrentUser() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("1");
    when(user.getFirstName()).thenReturn("John");
    when(user.getLastName()).thenReturn("Doo");
    return user;
  }

  @BeforeEach
  void setUpMocks() {
    Transaction transaction = new Transaction();
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(Transaction.class)).thenReturn(transaction);
  }

  @Test
  void propertiesOfAVariableWithoutValueAreCorrectlySet() {
    Variable variable = new Variable("Var1", "My variable var1");
    assertThat(variable.getLabel(), is("Var1"));
    assertThat(variable.getDescription(), is("My variable var1"));
    assertThat(variable.getNumberOfValues(), is(0));
    assertThat(variable.getVariableValues().isEmpty(), is(true));
  }

  @Test
  void propertiesOfAVariableWithTwoValuesAreCorrectlySet() {
    Variable variable = new Variable("Var1", "My variable var1");
    VariableScheduledValue value1 = new VariableScheduledValue("value1", Period.indefinite());
    VariableScheduledValue value2 = new VariableScheduledValue("value2", Period.indefinite());
    variable.getVariableValues().add(value1);
    variable.getVariableValues().add(value2);

    assertThat(variable.getLabel(), is("Var1"));
    assertThat(variable.getDescription(), is("My variable var1"));
    assertThat(variable.getNumberOfValues(), is(2));
    assertThat(variable.getVariableValues().size(), is(2));
    assertThat(variable.getVariableValues().contains(value1), is(true));
    assertThat(variable.getVariableValues().contains(value2), is(true));
  }

  @Test
  void saveAVariableWithoutAnyValue() {
    Variable variable = new Variable("Var1", "My variable var1");
    variable.save();
    verify(variablesRepository).save(variable);
  }

  @Test
  void saveAVariableWithValue() {
    Variable variable = new Variable("Var1", "My variable var1");
    VariableScheduledValue value = new VariableScheduledValue("value", Period.indefinite());
    variable.getVariableValues().add(value);
    variable.save();
    verify(variablesRepository).save(variable);
  }

  @Test
  void saveExplicitlyAValueOfAVariable() {
    Variable variable = new Variable("Var1", "My variable var1");
    VariableScheduledValue value = new VariableScheduledValue("value", Period.indefinite());
    variable.getVariableValues().addAndSave(value);
    verify(valuesRepository).save(value);
  }
}
