/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.calendar;

import com.silverpeas.annotation.DateInterval;
import org.silverpeas.core.annotation.constraint.DateRange;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.calendar.CalendarEvent.*;
import static org.silverpeas.core.date.Date.*;
import static org.silverpeas.core.date.DateTime.*;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= "/spring-calendar.xml")
public class CalendarEventValidationTest {

  private static final String errormsg = DateInterval.class.getAnnotation(DateRange.class).message();
  private static final DateTime afterNow = new DateTime(new java.util.Date(now().getTime()
      + 86400000));
  private static final DateTime beforeNow = new DateTime(new java.util.Date(now().getTime()
      - 86400000));

  @Inject
  private Validator validator;

  private DateTime afterNow() {
    return afterNow.clone();
  }

  private static DateTime beforeNow() {
    return beforeNow.clone();
  }


  public CalendarEventValidationTest() {
  }

  @Before
  public void setUp() {
    assertNotNull("The validator isn't injected!", validator);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void anEventAtAStartingDateTimeIsValide() {
    CalendarEvent event = anEventAt(now());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void anEventAtAStartingDateIsValide() {
    CalendarEvent event = anEventAt(today());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void anEventWithAnEndingDateTimeAfterTheStartDateTimeIsValide() {
    CalendarEvent event = anEventAt(now(), afterNow());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void anEventWithAnEndingDateAfterTheStartDateIsValide() {
    CalendarEvent event = anEventAt(today(), tomorrow());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void anEventWithAnEndingDateTimeAfterTheStartDateIsValide() {
    CalendarEvent event = anEventAt(now(), tomorrow());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void anEventWithAnEndingDateAfterTheStartDateTimeIsValide() {
    CalendarEvent event = anEventAt(today(), afterNow());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void anEventWithAnEndingDateTimeBeforeTheStartDateTimeIsInvalide() {
    CalendarEvent event = anEventAt(now(), beforeNow());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<CalendarEvent> constraintViolation : violations) {
      assertThat((CalendarEvent) constraintViolation.getInvalidValue(), is(event));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void anEventWithAnEndingDateBeforeTheStartDateTimeIsInvalide() {
    CalendarEvent event = anEventAt(now(), yesterday());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<CalendarEvent> constraintViolation : violations) {
      assertThat((CalendarEvent) constraintViolation.getInvalidValue(), is(event));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void anEventWithAnEndingDateTimeBeforeTheStartDateIsInvalide() {
    CalendarEvent event = anEventAt(today(), beforeNow());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<CalendarEvent> constraintViolation : violations) {
      assertThat((CalendarEvent) constraintViolation.getInvalidValue(), is(event));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void anEventWithAnEndingDateBeforeTheStartDateIsInvalide() {
    CalendarEvent event = anEventAt(today(), yesterday());
    Set<ConstraintViolation<CalendarEvent>> violations = validator.validate(event);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<CalendarEvent> constraintViolation : violations) {
      assertThat((CalendarEvent) constraintViolation.getInvalidValue(), is(event));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }
}