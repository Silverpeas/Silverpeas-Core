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
package org.silverpeas.core.util;

import org.apache.commons.lang3.SerializationException;
import org.junit.Test;
import org.silverpeas.core.util.SerializationUtil;

import java.io.Serializable;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Yohann Chastagnier
 */
public class SerializationUtilTest {

  private static long commonTime = java.sql.Timestamp.valueOf("2015-01-01 11:32:36.365").getTime();

  @Test
  public void serializeAsStringNullObject() {
    String serializedValue = SerializationUtil.serializeAsString(null);
    assertThat(serializedValue, is("rO0ABXA="));
  }

  @Test
  public void deserializeFromStringNullObject() {
    String deserializedValue = SerializationUtil.deserializeFromString("rO0ABXA=");
    assertThat(deserializedValue, nullValue());
  }

  @Test(expected = SerializationException.class)
  public void serializeAsStringNotSerializableObject() {
    SerializationUtil.serializeAsString(new NotSerializableClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void deserializeFromStringNullParameter() {
    String deserializedValue = SerializationUtil.deserializeFromString(null);
    assertThat(deserializedValue, nullValue());
  }

  @Test
  public void serializeAsString() {
    String serializedValue = SerializationUtil.serializeAsString("value");
    assertThat(serializedValue, is("rO0ABXQABXZhbHVl"));

    serializedValue = SerializationUtil.serializeAsString(new SerializableClass());
    assertThat(serializedValue,
        is("rO0ABXNyAEBvcmcuc2lsdmVycGVhcy5jb3JlLnV0aWwuU2VyaWFsaXphdGlvblV0aWxUZXN0JFNlcmlhbGl6" +
            "YWJsZUNsYXNzVJ0TJNg44nYCAAJMAARkYXRldAAQTGphdmEvdXRpbC9EYXRlO0wAC3N0cmluZ1ZhbHVldAA" +
            "STGphdmEvbGFuZy9TdHJpbmc7eHBzcgAOamF2YS51dGlsLkRhdGVoaoEBS1l0GQMAAHhwdwgAAAFKpQ3bDX" +
            "h0ABBzZXJpYWxpemVkU3RyaW5n"));
  }

  @Test
  public void deserializeFromString() {
    String deserializedValue = SerializationUtil.deserializeFromString("rO0ABXQABXZhbHVl");
    assertThat(deserializedValue, is("value"));

    SerializableClass deserializedInstance = SerializationUtil.deserializeFromString(
        "rO0ABXNyAEBvcmcuc2lsdmVycGVhcy5jb3JlLnV0aWwuU2VyaWFsaXphdGlvblV0aWxUZXN0JFNlcmlhbGl6" +
            "YWJsZUNsYXNzVJ0TJNg44nYCAAJMAARkYXRldAAQTGphdmEvdXRpbC9EYXRlO0wAC3N0cmluZ1ZhbHVldAA" +
            "STGphdmEvbGFuZy9TdHJpbmc7eHBzcgAOamF2YS51dGlsLkRhdGVoaoEBS1l0GQMAAHhwdwgAAAFKpQ3bDX" +
            "h0ABBzZXJpYWxpemVkU3RyaW5n");
    assertThat(deserializedInstance.toString(), is("serializedString|" + commonTime));
  }

  /**
   * @author Yohann Chastagnier
   */
  public static class SerializableClass implements Serializable {
    private static final long serialVersionUID = 6097050519496876662L;

    private String stringValue = "serializedString";
    private Date date = new Date(commonTime);

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
    private Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
      }
    });

    @Override
    public String toString() {
      return stringValue + "|" + thread.toString();
    }
  }
}