/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.annotation.constraint;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.variables.VariablesManagementIT;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the DateRangeValidator validation.
 */
@RunWith(Arquillian.class)
public class DateRangeValidatorIT {

  private static final String errormsg = DateInterval.class.getAnnotation(DateRange.class).message();

  @Inject
  private Validator validator;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(VariablesManagementIT.class)
        .addAdministrationFeatures()
        .addSilverpeasExceptionBases()
        .addJpaPersistenceFeatures()
        .addPublicationTemplateFeatures()
        .addPackages(true, "org.silverpeas.core.annotation.constraint")
        .build();
  }

  @Before
  public void setUp() {
    assertThat(validator, notNullValue());
  }

  @Test
  public void aStartDateEqualToTheEndDateIsValid() {
    LocalDate today = LocalDate.now();
    DateInterval interval = new DateInterval(today, today);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void aStartDateBeforeTheEndDateIsValid() {
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);
    DateInterval interval = new DateInterval(today, tomorrow);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void aStartDateAfterTheEndDateIsInvalid() {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    DateInterval interval = new DateInterval(today, yesterday);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(false));
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat(constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateTimeEqualToTheEndDateTimeIsInvalid() {
    OffsetDateTime now = OffsetDateTime.now();
    DateInterval interval = new DateInterval(now, now);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(false));
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat(constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateTimeBeforeTheEndDateTimeIsValid() {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime oneDayAfterNow = now.plusDays(1);
    DateInterval interval = new DateInterval(now, oneDayAfterNow);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void aStartDateTimeAfterTheEndDateTimeIsInvalid() {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime oneDayBeforeNow = now.minusDays(1);
    DateInterval interval = new DateInterval(now, oneDayBeforeNow);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(false));
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat(constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateAndAnEndDateTimeIsInvalid() {
    LocalDate today = LocalDate.now();
    OffsetDateTime now = OffsetDateTime.now();
    DateInterval interval = new DateInterval(today, now);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(false));
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat(constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

  @Test
  public void aStartDateTimeAndAnEndDateIsInValid() {
    LocalDate today = LocalDate.now();
    OffsetDateTime now = OffsetDateTime.now();
    DateInterval interval = new DateInterval(now, today);
    Set<ConstraintViolation<DateInterval>> violations = validator.validate(interval);
    assertThat(violations.isEmpty(), is(false));
    assertThat(violations.size(), is(1));
    for (ConstraintViolation<DateInterval> constraintViolation : violations) {
      assertThat(constraintViolation.getInvalidValue(), is(interval));
      assertThat(constraintViolation.getMessage(), is(errormsg));
    }
  }

}