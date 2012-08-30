package org.silverpeas.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.util.UnitUtil.convertTo;

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
}
