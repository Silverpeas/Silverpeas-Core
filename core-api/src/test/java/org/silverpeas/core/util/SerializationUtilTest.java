/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util;

import org.apache.commons.lang3.SerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yohann Chastagnier
 */
@Execution(ExecutionMode.SAME_THREAD)
public class SerializationUtilTest {

  private static final long REFERENCE_DATE_TIME =
      ZonedDateTime.of(2015, 1, 1, 11, 32, 36, 365_000_000, ZoneId.of("Europe/Paris"))
          .toEpochSecond();

  private static final String SERIALIZED_OBJECT =
      "rO0ABXNyAEBvcmcuc2lsdmVycGVhcy5jb3JlLnV0aWwuU2VyaWFsaXphdGlvblV0aWxUZXN0JFNlcmlhbGl6YWJsZUNsYXNzVJ0TJNg44nYCAAJMAARkYXRldAAQTGphdmEvdXRpbC9EYXRlO0wAC3N0cmluZ1ZhbHVldAASTGphdmEvbGFuZy9TdHJpbmc7eHBzcgAOamF2YS51dGlsLkRhdGVoaoEBS1l0GQMAAHhwdwgAAAAAVKUiRHh0ABBzZXJpYWxpemVkU3RyaW5n";

  private static final String SERIALIZED_NULL = "rO0ABXA=";

  @Test
  public void serializeAsStringNullObject() {
    String serializedValue = SerializationUtil.serializeAsString(null);
    assertThat(serializedValue, is(SERIALIZED_NULL));
  }

  @Test
  public void deserializeFromStringNullObject() {
    String deserializedValue = SerializationUtil.deserializeFromString(SERIALIZED_NULL);
    assertThat(deserializedValue, nullValue());
  }

  @Test
  public void serializeAsStringNotSerializableObject() {
    assertThrows(SerializationException.class,
        () -> SerializationUtil.serializeAsString(new NotSerializableClass()));
  }

  @Test
  public void deserializeFromStringNullParameter() {
    assertThrows(IllegalArgumentException.class, () -> {
      String deserializedValue = SerializationUtil.deserializeFromString(null);
      assertThat(deserializedValue, nullValue());
    });
  }

  @Test
  public void serializeAsString() {
    String serializedValue = SerializationUtil.serializeAsString("value");
    assertThat(serializedValue, is("rO0ABXQABXZhbHVl"));

    serializedValue = SerializationUtil.serializeAsString(new SerializableClass());
    assertThat(serializedValue, is(SERIALIZED_OBJECT));
  }

  @Test
  public void deserializeFromString() {
    String deserializedValue = SerializationUtil.deserializeFromString("rO0ABXQABXZhbHVl");
    assertThat(deserializedValue, is("value"));

    SerializableClass deserializedInstance =
        SerializationUtil.deserializeFromString(SERIALIZED_OBJECT);
    assertThat(deserializedInstance.toString(), is("serializedString|" + REFERENCE_DATE_TIME));
  }

  /**
   * @author Yohann Chastagnier
   */
  public static class SerializableClass implements Serializable {
    private static final long serialVersionUID = 6097050519496876662L;

    private String stringValue = "serializedString";
    private Date date = new Date(REFERENCE_DATE_TIME);

    @Override
    public String toString() {
      return stringValue + "|" + date.getTime();
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  public static class NotSerializableClass implements Serializable {
    private static final long serialVersionUID = 6097050519496876662L;

    private String stringValue = "serializedString";
    private Thread thread = new Thread(() -> {
    });

    @Override
    public String toString() {
      return stringValue + "|" + thread.toString();
    }
  }
}