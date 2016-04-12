/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.service;

import org.silverpeas.core.calendar.model.Attendee;
import org.silverpeas.core.calendar.model.ToDoHeader;
import org.silverpeas.core.calendar.test.WarBuilder4Calendar;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the implementation of the ComponentInstanceDeletion interface.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ComponentInstanceDeletionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/calendar/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/calendar/calendar-dataset.sql";

  private static final String COMPONENT_INSTANCE_ID = "inst10";

  private DefaultCalendarService calendar;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Calendar
        .onWarForTestClass(ComponentInstanceDeletionIntegrationTest.class)
        .build();
  }

  @Before
  public void beforeAnyTests() {
    calendar = ServiceProvider.getService(DefaultCalendarService.class);
    assertThat(calendar, notNullValue());
  }

  @Test
  public void deleteAllToDosForAnExistingComponentInstance() {
    Collection<ToDoHeader> todos = calendar.getExternalTodos(null, COMPONENT_INSTANCE_ID, "");
    List<Attendee> attendees = todos.stream()
        .flatMap(todo -> calendar.getToDoAttendees(todo.getId()).stream())
        .collect(Collectors.toList());
    assertThat(todos.isEmpty(), is(false));
    assertThat(attendees.isEmpty(), is(false));

    calendar.delete(COMPONENT_INSTANCE_ID);
    todos = calendar.getExternalTodos(null, COMPONENT_INSTANCE_ID, "");
    attendees = todos.stream()
        .flatMap(todo -> calendar.getToDoAttendees(todo.getId()).stream())
        .collect(Collectors.toList());
    assertThat(todos.isEmpty(), is(true));
    assertThat(attendees.isEmpty(), is(true));
  }

  @Test
  public void deleteAllToDosForANonExistingComponentInstance() {
    Collection<ToDoHeader> todos = calendar.getExternalTodos(null, "toto42", "");
    assertThat(todos.isEmpty(), is(true));

    calendar.delete(COMPONENT_INSTANCE_ID);

    todos = calendar.getExternalTodos(null, COMPONENT_INSTANCE_ID, "");
    assertThat(todos.isEmpty(), is(true));
  }
}
