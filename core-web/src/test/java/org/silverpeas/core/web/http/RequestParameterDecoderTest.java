/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.web.http;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
public class RequestParameterDecoderTest {

  private Date TODAY = DateUtil.getNow();
  private HttpRequest httpRequestMock;

  @BeforeEach
  public void setup() throws ParseException {
    httpRequestMock = mock(HttpRequest.class);

    when(httpRequestMock.getParameter(anyString())).then(invocation -> {
      String parameterName = (String) invocation.getArguments()[0];
      if (StringUtil.isDefined(parameterName)) {
        switch (parameterName) {
          case "aString":
            return "&lt;a&gt;aStringValue&lt;/a&gt;";
          case "aStringToUnescape":
            return "&lt;a&gt;aStringValueToUnescape&lt;/a&gt;";
          case "anUri":
            return "/an/uri/";
          case "anOffsetDateTime":
            return "2009-01-02T23:54:26Z";
          case "anInteger":
            return "1";
          case "anIntegerFromAnnotation":
            return "2";
          case "aLongFromAnnotation":
            return "20";
          case "aLong":
            return "10";
          case "aBoolean":
            return "true";
          case "aBooleanFromAnnotation":
            return "false";
          case "anEnum":
            return EnumWithoutCreationAnnotation.VALUE_A.name();
        }
      }
      return null;
    });
    when(httpRequestMock.getParameterAsRequestFile(anyString())).then(invocation -> {
      String parameterName = (String) invocation.getArguments()[0];
      if (StringUtil.isDefined(parameterName)) {
        if (parameterName.equals("aRequestFile")) {
          FileItem fileItem = mock(FileItem.class);
          when(fileItem.getName()).thenReturn("fileName");
          when(fileItem.getSize()).thenReturn(26L);
          when(fileItem.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
          when(fileItem.getInputStream()).thenReturn(FileUtils.openInputStream(getImageResource()));
          return new RequestFile(fileItem);
        }
      }
      return null;
    });
    when(httpRequestMock.getParameterAsDate(anyString())).then(invocation -> {
      String parameterName = (String) invocation.getArguments()[0];
      if (StringUtil.isDefined(parameterName)) {
        if (parameterName.equals("aDate")) {
          return TODAY;
        }
      }
      return null;
    });
    when(httpRequestMock.getParameterValues(anyString())).then((Answer<String[]>) invocation -> {
      String parameterName = (String) invocation.getArguments()[0];
      if (StringUtil.isDefined(parameterName)) {
        if (parameterName.equals("strings")) {
          return new String[]{"string_1", "string_2", "string_2"};
        } else if (parameterName.equals("integers")) {
          return new String[]{"1", "2", "2"};
        }
      }
      return null;
    });
  }

  private File getImageResource() throws Exception {
    return new File(
        RequestParameterDecoderTest.class.getClassLoader().getResource("image.gif").toURI());
  }

  @Test
  public void decode() throws Exception {
    PoJo result = RequestParameterDecoder.decode(httpRequestMock, PoJo.class);
    assertThat(result.getaStringWithoutAnnotation(), nullValue());
    try {
      assertThat(IOUtils.toByteArray(result.getaRequestFile().getInputStream()), is(FileUtils.readFileToByteArray(getImageResource())));
    } finally {
      IOUtils.closeQuietly(result.getaRequestFile().getInputStream());
    }
    assertThat(result.getaStringNotInParameter(), nullValue());
    assertThat(result.getaString(), is("&lt;a&gt;aStringValue&lt;/a&gt;"));
    assertThat(result.getaStringNotInParameterToUnescape(), isEmptyString());
    assertThat(result.getaStringToUnescape(), is("<a>aStringValueToUnescape</a>"));
    assertThat(result.getAnIntegerNotInParameter(), nullValue());
    assertThat(result.getAnInteger(), is(1));
    assertThat(result.getaPrimitiveIntegerNotInParameter(), is(-1));
    assertThat(result.getaPrimitiveInteger(), is(2));
    assertThat(result.getaLongNotInParameter(), nullValue());
    assertThat(result.getaLong(), is(10L));
    assertThat(result.getaPrimitiveLong(), is(20L));
    assertThat(result.getaBooleanNotInParameter(), nullValue());
    assertThat(result.getaBoolean(), is(true));
    assertThat(result.isaPrimitiveBoolean(), is(false));
    assertThat(result.getaDateNotInParameter(), nullValue());
    assertThat(result.getaDate(), is(TODAY));
    assertThat(result.getAnEnumNotInParameter(), nullValue());
    assertThat(result.getAnEnum(), is(EnumWithoutCreationAnnotation.VALUE_A));
    assertThat(result.getAnUriNotInParameter(), nullValue());
    assertThat(result.getAnUri(), is(URI.create("/an/uri/")));
    assertThat(result.getStrings(), containsInAnyOrder("string_1", "string_2"));
    assertThat(result.getIntegers(), containsInAnyOrder(1, 2, 2));
    assertThat(result.getOffsetDateTime().toString(), is("2009-01-02T23:54:26Z"));
  }

  @Test
  public void decodeWithNotHandledType() {
    assertThrows(RuntimeException.class,
        () -> RequestParameterDecoder.decode(httpRequestMock, PoJoWithNotHandledType.class));
  }
}