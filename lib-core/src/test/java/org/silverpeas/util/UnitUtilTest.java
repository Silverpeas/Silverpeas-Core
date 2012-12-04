package org.silverpeas.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.util.UnitUtil.convertTo;
import static org.silverpeas.util.UnitUtil.formatValue;

import java.math.BigDecimal;

import org.junit.Test;

public class UnitUtilTest {

  @Test
  public void testConvertTo_BigDecimal() {

    assertThat(
        convertTo(new BigDecimal(String.valueOf("1")), UnitUtil.memUnit.B, UnitUtil.memUnit.B),
        is(new BigDecimal(String.valueOf("1"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("1")), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is(new BigDecimal(String.valueOf("1024"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("2")), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is(new BigDecimal(String.valueOf("2048"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("2.4")), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is(new BigDecimal(String.valueOf("2457.6"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("1")), UnitUtil.memUnit.MB, UnitUtil.memUnit.B),
        is(new BigDecimal(String.valueOf("1048576"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("10")), UnitUtil.memUnit.MB, UnitUtil.memUnit.B),
        is(new BigDecimal(String.valueOf("10485760"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("1024")), UnitUtil.memUnit.B, UnitUtil.memUnit.KB),
        is(new BigDecimal(String.valueOf("1"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("1048576")), UnitUtil.memUnit.B,
            UnitUtil.memUnit.MB), is(new BigDecimal(String.valueOf("1"))));

    assertThat(
        convertTo(new BigDecimal(String.valueOf("1073741824")), UnitUtil.memUnit.B,
            UnitUtil.memUnit.GB), is(new BigDecimal(String.valueOf("1"))));
  }

  @Test
  public void testConvertTo_Long() {

    assertThat(convertTo(1L, UnitUtil.memUnit.KB, UnitUtil.memUnit.B), is(1024L));

    assertThat(convertTo(1L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(0L));
    assertThat(convertTo(512L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(0L));
    assertThat(convertTo(513L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(1L));
  }

  @Test
  public void testFormatValue_BigDecimal() {

    assertThat(
        formatValue(new BigDecimal(String.valueOf("1")), UnitUtil.memUnit.B, UnitUtil.memUnit.B),
        is("1 Octets"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("1")), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is("1024 Octets"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("2")), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is("2048 Octets"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("2.4")), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is("2457.6 Octets"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("1")), UnitUtil.memUnit.MB, UnitUtil.memUnit.B),
        is("1048576 Octets"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("10")), UnitUtil.memUnit.MB, UnitUtil.memUnit.B),
        is("10485760 Octets"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("1024")), UnitUtil.memUnit.B, UnitUtil.memUnit.KB),
        is("1 Ko"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("1048576")), UnitUtil.memUnit.B,
            UnitUtil.memUnit.MB), is("1 Mo"));

    assertThat(
        formatValue(new BigDecimal(String.valueOf("1073741824")), UnitUtil.memUnit.B,
            UnitUtil.memUnit.GB), is("1 Gb"));
  }

  @Test
  public void testFormatValue_Long() {

    assertThat(formatValue(1L, UnitUtil.memUnit.KB, UnitUtil.memUnit.B), is("1024 Octets"));

    assertThat(formatValue(1L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is("0 Ko"));
    assertThat(formatValue(512L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is("0 Ko"));
    assertThat(formatValue(513L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is("1 Ko"));
  }

  @Test
  public void testFormatValue_BigDecimal_bis() {

    assertThat(formatValue(new BigDecimal(String.valueOf("1024")), UnitUtil.memUnit.KB), is("1 Ko"));

    assertThat(formatValue(new BigDecimal(String.valueOf("1048576")), UnitUtil.memUnit.MB),
        is("1 Mo"));

    assertThat(formatValue(new BigDecimal(String.valueOf("1073741824")), UnitUtil.memUnit.GB),
        is("1 Gb"));
  }

  @Test
  public void testFormatValue_Long_bis() {
    assertThat(formatValue(513L, UnitUtil.memUnit.KB), is("1 Ko"));
  }
}
