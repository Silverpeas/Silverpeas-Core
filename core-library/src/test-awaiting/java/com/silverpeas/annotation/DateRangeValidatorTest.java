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

package com.silverpeas.annotation;

import org.silverpeas.core.annotation.constraint.DateRange;
import org.silverpeas.core.date.DateTime;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.inject.Inject;
import javax.validation.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.date.Date.*;
import static org.silverpeas.core.date.DateTime.*;

/**
 * Unit tests on the DateRangeValidator validation.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-calendar.xml")
public class DateRangeValidatorTest {

  private static final String errormsg = DateInterval.class.getAnnotation(DateRange.class).message();
  private static final DateTime afterNow = new DateTime(new java.util.Date(now().getTime()
      + 86400000));
  private static final DateTime beforeNow = new DateTime(new java.util.Date(now().getTime()
      - 86400000));
  @Inject
  private Validator validator;

  @Before
  public void setUp() {
    assertNotNull(validator);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void aStartDateEqualToTheEndDateIsValid() {
    DateInterval interval = new DateInterval(today(), today());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateBeforeTheEndDateIsValid() {
    DateInterval interval = new DateInterval(today(), tomorrow());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateAfterTheEndDateIsInvalid() {
    DateInterval interval = new DateInterval(today(), yesterday());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat((DateInterval) constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateTimeEqualToTheEndDateTimeIsValid() {
    DateInterval interval = new DateInterval(now(), now());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateTimeBeforeTheEndDateTimeIsValid() {
    DateInterval interval = new DateInterval(today(), afterNow());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateTimeAfterTheEndDateTimeIsInvalid() {
    DateInterval interval = new DateInterval(today(), beforeNow());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat((DateInterval) constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateEqualToTheEndDateTimeIsValid() {
    DateInterval interval = new DateInterval(today(), now());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateTimeEqualToTheEndDateIsValid() {
    DateInterval interval = new DateInterval(now(), today());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateBeforeTheEndDateTimeIsValid() {
    DateInterval interval = new DateInterval(today(), afterNow());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateTimeBeforeTheEndDateIsValid() {
    DateInterval interval = new DateInterval(beforeNow(), today());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void aStartDateAfterTheEndDateTimeIsInvalid() {
    DateInterval interval = new DateInterval(tomorrow(), now());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat((DateInterval) constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateTimeAfterTheEndDateIsInvalid() {
    DateInterval interval = new DateInterval(afterNow(), today());
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertFalse(violations.isEmpty());
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat((DateInterval) constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  private DateTime afterNow() {
    return afterNow.clone();
  }

  private static DateTime beforeNow() {
    return beforeNow.clone();
  }
}