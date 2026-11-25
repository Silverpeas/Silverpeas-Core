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
package org.silverpeas.core.contribution.template.publication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.kernel.test.UnitTest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the values of the fields of a data record, used to know whether the data of a
 * form have been modified.
 * @author mmoquillon
 */
@UnitTest
class DataRecordValuesTest {

  private static final String TITLE = "title";
  private static final String PHONES = "phones";

  private final Map<String, String> fieldValues = new HashMap<>();
  private DataRecord record;

  @BeforeEach
  void setUpARecordWithASimpleFieldAndARepeatableOne() throws FormException {
    RecordTemplate template = mock(RecordTemplate.class);
    FieldTemplate titleTemplate = fieldTemplate(1);
    FieldTemplate phonesTemplate = fieldTemplate(2);
    when(template.getFieldTemplate(TITLE)).thenReturn(titleTemplate);
    when(template.getFieldTemplate(PHONES)).thenReturn(phonesTemplate);

    record = mock(DataRecord.class);
    when(record.getTemplate()).thenReturn(template);
    when(record.getFieldNames()).thenReturn(new String[]{TITLE, PHONES});
    Field title = field(TITLE, 0);
    Field firstPhone = field(PHONES, 0);
    Field secondPhone = field(PHONES, 1);
    when(record.getField(TITLE, 0)).thenReturn(title);
    when(record.getField(PHONES, 0)).thenReturn(firstPhone);
    when(record.getField(PHONES, 1)).thenReturn(secondPhone);

    fieldValues.put(TITLE + 0, "Manager");
    fieldValues.put(PHONES + 0, "0102030405");
  }

  @Test
  void theValuesOfAnUnmodifiedRecordAreEqual() throws FormException {
    Map<String, String> before = DataRecordValues.of(record);
    Map<String, String> after = DataRecordValues.of(record);
    assertThat(after, is(before));
  }

  @Test
  void anUndefinedValueIsEqualToAnEmptyOne() throws FormException {
    Map<String, String> before = DataRecordValues.of(record);
    fieldValues.put(PHONES + 1, "");
    Map<String, String> after = DataRecordValues.of(record);
    assertThat(after, is(before));
  }

  @Test
  void aModifiedValueOfASimpleFieldIsDetected() throws FormException {
    Map<String, String> before = DataRecordValues.of(record);
    fieldValues.put(TITLE + 0, "Director");
    Map<String, String> after = DataRecordValues.of(record);
    assertThat(after, is(not(before)));
  }

  @Test
  void aModifiedOccurrenceOfARepeatableFieldIsDetected() throws FormException {
    Map<String, String> before = DataRecordValues.of(record);
    fieldValues.put(PHONES + 1, "0607080910");
    Map<String, String> after = DataRecordValues.of(record);
    assertThat(after, is(not(before)));
  }

  @Test
  void aResetValueIsDetected() throws FormException {
    Map<String, String> before = DataRecordValues.of(record);
    fieldValues.put(TITLE + 0, null);
    Map<String, String> after = DataRecordValues.of(record);
    assertThat(after, is(not(before)));
  }

  private static FieldTemplate fieldTemplate(final int maximumNumberOfOccurrences) {
    FieldTemplate template = mock(FieldTemplate.class);
    when(template.getMaximumNumberOfOccurrences()).thenReturn(maximumNumberOfOccurrences);
    return template;
  }

  private Field field(final String name, final int occurrence) {
    Field field = mock(Field.class);
    when(field.getName()).thenReturn(name);
    when(field.getOccurrence()).thenReturn(occurrence);
    when(field.getStringValue()).thenAnswer(i -> fieldValues.get(name + occurrence));
    return field;
  }
}
