/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.web.util;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 16/07/13
 */
public class IFrameAjaxTransportUtilTest {

  private static final String JSON_STRING =
      "{\"id\":\"an_id\"," + "\"instanceId\":\"an_instanceId\",\"fileName\":\"a_filename\"}";

  @Test
  public void testPackJSonDataWithHtmlContainerWithObject() throws IOException {
    String result =
        IFrameAjaxTransportUtil.packObjectToJSonDataWithHtmlContainer(new ObjectEntity());
    assertThat(result, is("<textarea data-type='application/json'>{\"id\":\"objectId\"," +
        "\"length\":156}</textarea>"));
  }

  @Test
  public void testPackJSonDataWithHtmlContainerWithObjects() throws IOException {
    String result = IFrameAjaxTransportUtil.packObjectToJSonDataWithHtmlContainer(
        Arrays.asList(new ObjectEntity(), new ObjectEntity()));
    assertThat(result, is("<textarea data-type='application/json'>[{\"id\":\"objectId\"," +
        "\"length\":156},{\"id\":\"objectId\",\"length\":156}]</textarea>"));
  }

  @Test
  public void testPackJSonDataWithHtmlContainerWithJSonObject() throws IOException {
    String result = IFrameAjaxTransportUtil
        .packJSonDataWithHtmlContainer(new JSONObject().put("id", "an_id").put("length", 425));
    assertThat(result, is("<textarea data-type='application/json'>{\"id\":\"an_id\"," +
        "\"length\":425}</textarea>"));
  }

  @Test
  public void testPackJSonDataWithHtmlContainerWithJSonObjects() throws IOException {
    String result = IFrameAjaxTransportUtil.packJSonDataWithHtmlContainer(Arrays
        .asList(new JSONObject().put("id", "an_id").put("length", 425),
            new JSONObject().put("id", "an_other_id").put("length", 426)));
    assertThat(result, is("<textarea data-type='application/json'>[{\"id\":\"an_id\"," +
        "\"length\":425},{\"id\":\"an_other_id\",\"length\":426}]</textarea>"));
  }

  @Test
  public void testPackJSonDataWithHtmlContainerWithJSonString() {
    String result = IFrameAjaxTransportUtil.packJSonDataWithHtmlContainer(JSON_STRING);
    assertThat(result, is("<textarea data-type='application/json'>" + JSON_STRING + "</textarea>"));
  }

  private class ObjectEntity {
    private String id = "objectId";
    private long length = 156;

    public String getId() {
      return id;
    }

    public long getLength() {
      return length;
    }
  }
}
