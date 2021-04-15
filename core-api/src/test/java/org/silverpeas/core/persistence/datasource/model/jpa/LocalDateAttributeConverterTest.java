package org.silverpeas.core.persistence.datasource.model.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MAX_DATE;
import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MIN_DATE;

/**
 * Unit tests on our own JPA {@link LocalDate} converter to ensure the {@link LocalDate#MIN} and
 * {@link LocalDate#MAX} are correctly handled. Indeed, there is a bug between the
 * translation between object of the Java Time API and the Java JDBC API and currently Hibernate
 * doesn't support the min and max values of the date and datetime in the Java Time API.
 */
class LocalDateAttributeConverterTest {

  @Test
  @DisplayName("SQLDateTimeConstants.MIN_DATE should be 0001-01-01")
  void assertMIN_DATEIsTheExpectedOne() {
    assertThat(MIN_DATE.toLocalDate(), is(LocalDate.parse("0001-01-01")));
  }

  @Test
  @DisplayName("SQLDateTimeConstants.MAX_DATE should be 9999-12-31")
  void assertMAX_DATEIsTheExpectedOne() {
    assertThat(MAX_DATE.toLocalDate(), is(LocalDate.parse("9999-12-31")));
  }

  @Test
  @DisplayName("LocalDate.MIN should be converted to SQLDateTimeConstants.MIN_DATE")
  void minLocalDateShouldBeCorrectlyConverted() {
    LocalDateAttributeConverter converter = new LocalDateAttributeConverter();
    Date date = converter.convertToDatabaseColumn(LocalDate.MIN);
    assertThat(date, is(MIN_DATE));
  }

  @Test
  @DisplayName("LocalDate.MAX should be converted to SQLDateTimeConstants.MAX_DATE")
  void maxLocalDateShouldBeCorrectlyConverted() {
    LocalDateAttributeConverter converter = new LocalDateAttributeConverter();
    Date date = converter.convertToDatabaseColumn(LocalDate.MAX);
    assertThat(date, is(MAX_DATE));
  }

  @Test
  @DisplayName("Any LocalDate should be converted into its SQL Date counterpart without any " +
      "Timezone handling")
  void localDateShouldBeConvertedToSQLDate() {
    LocalDate now = LocalDate.now();
    LocalDateAttributeConverter converter = new LocalDateAttributeConverter();
    Date date = converter.convertToDatabaseColumn(now);
    assertThat(date.toLocalDate(), is(now));
  }

  @Test
  @DisplayName("SQLDateTimeConstants.MIN_DATE should be converted to LocalDate.MIN")
  void minSQlDateShouldBeCorrectlyConverted() {
    LocalDateAttributeConverter converter = new LocalDateAttributeConverter();
    LocalDate date = converter.convertToEntityAttribute(MIN_DATE);
    assertThat(date, is(LocalDate.MIN));
  }

  @Test
  @DisplayName("SQLDateTimeConstants.MAX_DATE should be converted to LocalDate.MAX")
  void maxSQLDateShouldBeCorrectlyConverted() {
    LocalDateAttributeConverter converter = new LocalDateAttributeConverter();
    LocalDate date = converter.convertToEntityAttribute(MAX_DATE);
    assertThat(date, is(LocalDate.MAX));
  }

  @Test
  @DisplayName("Any SQL Date should be converted into its LocalDate counterpart without any " +
      "Timezone handling")
  void sqlDateShouldBeConvertedToLocalDate() {
    Date now = new Date(new java.util.Date().getTime());
    LocalDateAttributeConverter converter = new LocalDateAttributeConverter();
    LocalDate date = converter.convertToEntityAttribute(now);
    assertThat(date, is(now.toLocalDate()));
  }
}
