package org.silverpeas.core.persistence.datasource.model.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MAX_TIMESTAMP;
import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MIN_TIMESTAMP;

/**
 * Unit tests on our own JPA {@link Instant} converter to ensure the {@link Instant#MIN} and {@link
 * Instant#MAX} are correctly handled. Indeed, there is a bug between the translation between object
 * of the Java Time API and the Java JDBC API and currently Hibernate doesn't support the min and
 * max values of the date and datetime in the Java Time API.
 */
class InstantAttributeConverterTest {

  @Test
  @DisplayName("SQLDateTimeConstants.MIN_TIMESTAMP should be 0001-01-01 at midnight")
  void assertMIN_TIMSTAMPIsTheExpectedOne() {
    assertThat(MIN_TIMESTAMP.toLocalDateTime().toInstant(ZoneOffset.UTC),
        is(Instant.parse("0001-01-01T00:00:00.0000000Z")));
  }

  @Test
  @DisplayName("SQLDateTimeConstants.MAX_TIMESTAMP should be 9999-12-31 at midnight")
  void assertMAX_TIMESTAMPIsTheExpectedOne() {
    assertThat(MAX_TIMESTAMP.toLocalDateTime().toInstant(ZoneOffset.UTC),
        is(Instant.parse("9999-12-31T00:00:00.0000000Z")));
  }

  @Test
  @DisplayName("INSTANT.MIN should be converted to SQLDateTimeConstants.MIN_TIMESTAMP")
  void minInstantShouldBeCorrectlyConverted() {
    InstantAttributeConverter converter = new InstantAttributeConverter();
    Timestamp date = converter.convertToDatabaseColumn(Instant.MIN);
    assertThat(date, is(MIN_TIMESTAMP));
  }

  @Test
  @DisplayName("Instant.MAX should be converted to SQLDateTimeConstants.MAX_TIMESTAMP")
  void maxInstantShouldBeCorrectlyConverted() {
    InstantAttributeConverter converter = new InstantAttributeConverter();
    Timestamp date = converter.convertToDatabaseColumn(Instant.MAX);
    assertThat(date, is(MAX_TIMESTAMP));
  }

  @Test
  @DisplayName("Any Instant should be converted into its SQL Timestamp counterpart without any " +
      "Timezone handling")
  void instantShouldBeConvertedToSQLTimestamp() {
    Instant now = Instant.now();
    InstantAttributeConverter converter = new InstantAttributeConverter();
    Timestamp date = converter.convertToDatabaseColumn(now);
    assertThat(date.toInstant(), is(now));
  }

  @Test
  @DisplayName("SQLDateTimeConstants.MIN_TIMESTAMP should be converted to Instant.MIN")
  void minSQlInstantShouldBeCorrectlyConverted() {
    InstantAttributeConverter converter = new InstantAttributeConverter();
    Instant date = converter.convertToEntityAttribute(MIN_TIMESTAMP);
    assertThat(date, is(Instant.MIN));
  }

  @Test
  @DisplayName("SQLDateTimeConstants.MAX_TIMESTAMP should be converted to Instant.MAX")
  void maxSQLInstantShouldBeCorrectlyConverted() {
    InstantAttributeConverter converter = new InstantAttributeConverter();
    Instant date = converter.convertToEntityAttribute(MAX_TIMESTAMP);
    assertThat(date, is(Instant.MAX));
  }

  @Test
  @DisplayName("Any SQL Timestamp should be converted into its Instant counterpart without any " +
      "Timezone handling")
  void sqlTimestampShouldBeConvertedToInstant() {
    Timestamp now = new Timestamp(new java.util.Date().getTime());
    InstantAttributeConverter converter = new InstantAttributeConverter();
    Instant date = converter.convertToEntityAttribute(now);
    assertThat(date, is(now.toInstant()));
  }
}
