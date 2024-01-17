/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.web.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.csv.CSVRow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class ExportCSVBuilderTest {

  private static final String LINE_SEPARATOR = "\n";

  @BeforeEach
  public void prepareInjection(@TestManagedMock UserProvider userProvider) {
    final UserPreferences userPreferences = mock(UserPreferences.class);
    when(userPreferences.getZoneId()).thenReturn(ZoneId.of("Europe/Paris"));
    when(userPreferences.getLanguage()).thenReturn("fr");
    final User user = mock(User.class);
    when(user.getUserPreferences()).thenReturn(userPreferences);
    when(userProvider.getCurrentRequester()).thenReturn(user);
  }

  @Test
  void noDataToSend() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    final String result = getCsvFrom(builder);
    assertThat(result, emptyString());
  }

  @Test
  void oneLineWithNullValue() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith((String) null));
    final String result = getCsvFrom(builder);
    assertThat(result, is(LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneEmptyString() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(""));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"\"" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneEmptyQuoteString() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("\""));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"\"\"\"" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneQuoteString() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("What's a \"big\" test!"));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"What's a \"\"big\"\" test!\"" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneInteger() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(26));
    final String result = getCsvFrom(builder);
    assertThat(result, is("26" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneFloat() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(26.56F));
    final String result = getCsvFrom(builder);
    assertThat(result, is("26.56" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneUtcTemporal() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(OffsetDateTime.parse("2016-01-08T18:30:08Z")));
    final String result = getCsvFrom(builder);
    assertThat(result, is("08/01/2016 18:30 (Z)" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneTemporal() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(ZonedDateTime.parse("2016-01-08T18:30:08+01:00[Europe/Paris]")));
    final String result = getCsvFrom(builder);
    assertThat(result, is("08/01/2016 18:30" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneUser() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    final User user = mock(User.class);
    when(user.getLastName()).thenReturn("LastName");
    when(user.getFirstName()).thenReturn("FirstName");
    builder.addLine(aLineWith(user));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"LastName FirstName\"" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneUserWithQuotes() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    final User user = mock(User.class);
    when(user.getLastName()).thenReturn("LastName");
    when(user.getFirstName()).thenReturn("\"First\"Name");
    builder.addLine(aLineWith(user));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"LastName \"\"First\"\"Name\"" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneDate() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(Timestamp.valueOf("2020-01-04 10:45:56.023")));
    final String result = getCsvFrom(builder);
    assertThat(result, is("04/01/2020" + LINE_SEPARATOR));
  }

  @Test
  void oneLineWithOneUnknownType() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith(new UnknownType()));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"toto - 26\"" + LINE_SEPARATOR));
  }

  @Test
  void twoLinesWithNullValue() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith((String) null));
    builder.addLine(aLineWith((String) null));
    final String result = getCsvFrom(builder);
    assertThat(result, is(LINE_SEPARATOR + LINE_SEPARATOR));
  }

  @Test
  void twoLinesWithSeveralEmptyOrNullValues() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("", null));
    builder.addLine(aLineWith(null, ""));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"\";" + LINE_SEPARATOR + ";\"\"" + LINE_SEPARATOR));
  }

  @Test
  void twoLinesWithSeveralValues() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("titi", 38));
    builder.addLine(aLineWith("toto", 26));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"titi\";38" + LINE_SEPARATOR + "\"toto\";26" + LINE_SEPARATOR));
  }

  @Test
  void escapeHTMLNewLine() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("A<br/>B"));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"A" + LINE_SEPARATOR + "B\"" + LINE_SEPARATOR));
  }

  @Test
  void escapeHTMLUppercaseNewLine() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("A<BR/>B"));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"A" + LINE_SEPARATOR + "B\"" + LINE_SEPARATOR));
  }

  @Test
  void escapeHTMLStrangeNewLine() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("A<  Br  /   >B"));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"A" + LINE_SEPARATOR + "B\"" + LINE_SEPARATOR));
  }

  @Test
  void header() throws IOException {
    final ExportCSVBuilder builder = new ExportCSVBuilder();
    builder.addLine(aLineWith("A", "B"));
    builder.setHeader(aLineWith("col1", "col2"));
    final String result = getCsvFrom(builder);
    assertThat(result, is("\"col1\";\"col2\"" + LINE_SEPARATOR + "\"A\";\"B\"" + LINE_SEPARATOR));
  }

  private CSVRow aLineWith(Object... values) {
    final CSVRow row = new CSVRow();
    Stream.of(values).forEach(row::addCell);
    return row;
  }

  private String getCsvFrom(final ExportCSVBuilder builder) throws IOException {
    final HttpServletResponse response = mock(HttpServletResponse.class);
    try (final ByteArrayOutputStream data = new ByteArrayOutputStream();
         final PrintWriter writer = new PrintWriter(data)) {
      Mockito.when(response.getWriter()).thenReturn(writer);
      builder.sendTo(mock(HttpServletRequest.class), response);
      writer.flush();
      final String string = data.toString(Charsets.UTF_8);
      // Removing the BOM for assertions
      if (string.isEmpty()) {
        return string;
      }
      assertThat(string.substring(0 ,1), is("\uFEFF"));
      return string.substring(1);
    }
  }

  private static class UnknownType {

    @Override
    public String toString() {
      final String stringValue = "toto";
      final int intValue = 26;
      return stringValue + " - " + intValue;
    }
  }
}