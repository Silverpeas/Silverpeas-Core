/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.http;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.SettingBundle;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpRequestTest {
  private static final String HTTP_PARAMETER = "paramName";

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private HttpServletRequest httpServletRequestMock;
  private HttpRequest httpRequest;

  @Before
  public void setup() throws Exception{
    httpServletRequestMock = mock(HttpServletRequest.class);
    when(httpServletRequestMock.getMethod()).thenReturn("GET");

    httpRequest = HttpRequest.decorate(httpServletRequestMock);
    SettingBundle generalSettings =
        reflectionRule.mockField(httpRequest, SettingBundle.class, "generalSettings");
    when(generalSettings.getString("tempPath")).thenReturn(
        File.createTempFile("prefix", "suffix").getPath());
  }

  /*
  TESTS around
  {@link HttpRequest#getParameterAsBoolean(String)}.
   */

  @Test
  public void getParameterAsBooleanForNullValue() {
    assertThat(getParameterAsBoolean(), is(false));
  }

  @Test
  public void getParameterAsBooleanForEmptyValue() {
    setHttpParameterValue("");
    assertThat(getParameterAsBoolean(), is(false));
  }

  @Test
  public void getParameterAsBooleanForNotDefinedValue() {
    setHttpParameterValue("    ");
    assertThat(getParameterAsBoolean(), is(false));
  }

  @Test
  public void getParameterAsBooleanForNotBooleanValue() {
    setHttpParameterValue("toto");
    assertThat(getParameterAsBoolean(), is(false));
  }

  @Test
  public void getParameterAsBooleanForFalseValue() {
    setHttpParameterValue("false");
    assertThat(getParameterAsBoolean(), is(false));
  }

  @Test
  public void getParameterAsBooleanForTrueValue() {
    setHttpParameterValue("true");
    assertThat(getParameterAsBoolean(), is(true));
  }

  @Test
  public void getParameterAsBooleanForYesValue() {
    setHttpParameterValue("yes");
    assertThat(getParameterAsBoolean(), is(true));
  }

  @Test
  public void getParameterAsBooleanForOneValue() {
    setHttpParameterValue("1");
    assertThat(getParameterAsBoolean(), is(true));
  }

  private boolean getParameterAsBoolean() {
    return httpRequest.getParameterAsBoolean(HTTP_PARAMETER);
  }

  /*
  TESTS around
  {@link HttpRequest#getParameterAsInteger(String)}.
   */

  @Test
  public void getParameterAsIntegerForNullValue() {
    assertThat(getParameterAsInteger(), nullValue());
  }

  @Test
  public void getParameterAsIntegerForEmptyValue() {
    setHttpParameterValue("");
    assertThat(getParameterAsInteger(), nullValue());
  }

  @Test
  public void getParameterAsIntegerForNotDefinedValue() {
    setHttpParameterValue("    ");
    assertThat(getParameterAsInteger(), nullValue());
  }

  @Test
  public void getParameterAsIntegerForNotIntegerValue() {
    setHttpParameterValue("toto");
    assertThat(getParameterAsInteger(), nullValue());
  }

  @Test
  public void getParameterAsIntegerForDecimalValue() {
    setHttpParameterValue("1.1");
    assertThat(getParameterAsInteger(), nullValue());
  }

  @Test
  public void getParameterAsIntegerForMinusValue() {
    setHttpParameterValue("-1");
    assertThat(getParameterAsInteger(), is(-1));
  }

  @Test
  public void getParameterAsIntegerForZeroValue() {
    setHttpParameterValue("0");
    assertThat(getParameterAsInteger(), is(0));
  }

  @Test
  public void getParameterAsIntegerForPositiveValue() {
    setHttpParameterValue("1");
    assertThat(getParameterAsInteger(), is(1));
  }

  private Integer getParameterAsInteger() {
    return httpRequest.getParameterAsInteger(HTTP_PARAMETER);
  }

  /*
  TESTS around
  {@link HttpRequest#getParameterAsLong(String)}.
   */

  @Test
  public void getParameterAsLongForNullValue() {
    assertThat(getParameterAsLong(), nullValue());
  }

  @Test
  public void getParameterAsLongForEmptyValue() {
    setHttpParameterValue("");
    assertThat(getParameterAsLong(), nullValue());
  }

  @Test
  public void getParameterAsLongForNotDefinedValue() {
    setHttpParameterValue("    ");
    assertThat(getParameterAsLong(), nullValue());
  }

  @Test
  public void getParameterAsLongForNotLongValue() {
    setHttpParameterValue("toto");
    assertThat(getParameterAsLong(), nullValue());
  }

  @Test
  public void getParameterAsLongForDecimalValue() {
    setHttpParameterValue("1.1");
    assertThat(getParameterAsLong(), nullValue());
  }

  @Test
  public void getParameterAsLongForMinusValue() {
    setHttpParameterValue("-1");
    assertThat(getParameterAsLong(), is(-1L));
  }

  @Test
  public void getParameterAsLongForZeroValue() {
    setHttpParameterValue("0");
    assertThat(getParameterAsLong(), is(0L));
  }

  @Test
  public void getParameterAsLongForPositiveValue() {
    setHttpParameterValue("1");
    assertThat(getParameterAsLong(), is(1L));
  }

  private Long getParameterAsLong() {
    return httpRequest.getParameterAsLong(HTTP_PARAMETER);
  }

  /*
  TESTS around
  {@link HttpRequest#getParameterAsEnum(String, Class)}.
   */

  @Test
  public void getParameterAsEnumWithoutCreatorAnnotationForNullValue() {
    EnumWithoutCreationAnnotation enumValue = getParameterAsEnumWithoutCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithCreatorAnnotationForNullValue() {
    EnumWithCreationAnnotation enumValue = getParameterAsEnumWithCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithoutCreatorAnnotationForEmptyValue() {
    setHttpParameterValue("");
    EnumWithoutCreationAnnotation enumValue = getParameterAsEnumWithoutCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithCreatorAnnotationForEmptyValue() {
    setHttpParameterValue("");
    EnumWithCreationAnnotation enumValue = getParameterAsEnumWithCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithoutCreatorAnnotationForNotDefinedValue() {
    setHttpParameterValue("   ");
    EnumWithoutCreationAnnotation enumValue = getParameterAsEnumWithoutCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithCreatorAnnotationForNotDefinedValue() {
    setHttpParameterValue("   ");
    EnumWithCreationAnnotation enumValue = getParameterAsEnumWithCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithoutCreatorAnnotationForUnknownValue() {
    setHttpParameterValue("VALUE_UNKNOWN");
    EnumWithoutCreationAnnotation enumValue = getParameterAsEnumWithoutCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithCreatorAnnotationForUnknownValue() {
    setHttpParameterValue("VALUE_UNKNOWN");
    EnumWithCreationAnnotation enumValue = getParameterAsEnumWithCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithoutCreatorAnnotationForLowercaseValue() {
    setHttpParameterValue("value_b");
    EnumWithoutCreationAnnotation enumValue = getParameterAsEnumWithoutCreationAnnotation();
    assertThat(enumValue, nullValue());
  }

  @Test
  public void getParameterAsEnumWithCreatorAnnotationForLowercaseValue() {
    setHttpParameterValue("value_b");
    EnumWithCreationAnnotation enumValue = getParameterAsEnumWithCreationAnnotation();
    assertThat(enumValue, is(EnumWithCreationAnnotation.VALUE_B));
  }

  @Test
  public void getParameterAsEnumWithoutCreatorAnnotationForUppercaseValue() {
    setHttpParameterValue("VALUE_A");
    EnumWithoutCreationAnnotation enumValue = getParameterAsEnumWithoutCreationAnnotation();
    assertThat(enumValue, is(EnumWithoutCreationAnnotation.VALUE_A));
  }

  @Test
  public void getParameterAsEnumWithCreatorAnnotationForUppercaseValue() {
    setHttpParameterValue("VALUE_A");
    EnumWithCreationAnnotation enumValue = getParameterAsEnumWithCreationAnnotation();
    assertThat(enumValue, is(EnumWithCreationAnnotation.VALUE_A));
  }

  private EnumWithoutCreationAnnotation getParameterAsEnumWithoutCreationAnnotation() {
    return httpRequest.getParameterAsEnum(HTTP_PARAMETER, EnumWithoutCreationAnnotation.class);
  }

  private EnumWithCreationAnnotation getParameterAsEnumWithCreationAnnotation() {
    return httpRequest.getParameterAsEnum(HTTP_PARAMETER, EnumWithCreationAnnotation.class);
  }

  /*
  TOOLS
   */

  private void setHttpParameterValue(String value) {
    when(httpServletRequestMock.getParameter(HTTP_PARAMETER)).thenReturn(value);
  }
}