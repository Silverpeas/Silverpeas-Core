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
package org.silverpeas.core.web.util;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.MultiPartElement;
import org.apache.ecs.xhtml.textarea;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * This class provides some tools to more effectively use the technique of ajax iframe transport.
 * User: Yohann Chastagnier
 * Date: 16/07/13
 */
public class IFrameAjaxTransportUtil {

  public static final String X_REQUESTED_WITH = "X-Requested-With";
  public static final String AJAX_IFRAME_TRANSPORT = "IFrame";

  /**
   * Packaging an Web Application Exception as JSon String and then as HTML to be performed by
   * IFrame Ajax Transport Javascript Plugin.
   * @param wae
   * @return
   */
  public static WebApplicationException createWebApplicationExceptionWithJSonErrorInHtmlContainer(
      WebApplicationException wae) throws IOException {
    String messageKey = MessageManager.getRegistredKey();
    final String errorEntity;
    if (StringUtil.isDefined(messageKey)) {
      errorEntity = packJSonObjectWithHtmlContainer(o -> o.put("iframeMessageKey", messageKey));
    } else {
      errorEntity = packObjectToJSonDataWithHtmlContainer(wae.getResponse().getEntity());
    }
    return new WebApplicationException(
        Response.status(wae.getResponse().getStatus()).type(MediaType.TEXT_HTML_TYPE)
            .entity(errorEntity).build());
  }

  /**
   * Packaging an object as JSon String and then as HTML to be performed by IFrame Ajax Transport
   * Javascript Plugin.
   * @param object
   * @return
   */
  public static String packObjectToJSonDataWithHtmlContainer(Object object) throws IOException {
    String json = StringUtil.EMPTY;
    if (object != null) {
      json = JSONCodec.encode(object);
    }
    return packJSonDataWithHtmlContainer(json);
  }

  /**
   * Packaging an object list as JSon String and then as HTML to be performed by IFrame Ajax
   * Transport Javascript Plugin.
   * @param objects
   * @return
   */
  public static String packObjectToJSonDataWithHtmlContainer(List<Object> objects)
      throws IOException {
    String jsonArray = StringUtil.EMPTY;
    if (objects != null && !objects.isEmpty()) {
      jsonArray = JSONCodec.encode(objects);
    }
    return packJSonDataWithHtmlContainer(jsonArray);
  }

  /**
   * Packaging a JSon object as HTML to be performed by IFrame Ajax Transport Javascript
   * Plugin.
   * @param jsonObjectBuilder a dynamic builder of a JSON object.
   * @return
   */
  public static String packJSonObjectWithHtmlContainer(
      Function<JSONCodec.JSONObject, JSONCodec.JSONObject> jsonObjectBuilder) {
    String json = StringUtil.EMPTY;
    if (jsonObjectBuilder != null) {
      try {
        json = JSONCodec.encodeObject(jsonObjectBuilder);
      } catch (Exception ex) {
      }
    }
    return packJSonDataWithHtmlContainer(json);
  }

  /**
   * Packaging a JSon object list as HTML to be performed by IFrame Ajax Transport Javascript
   * Plugin.
   * @param jsonArrayBuilder a dynamic builder of a JSON array of JSON objects.
   * @return
   */
  public static String packJSonArrayWithHtmlContainer(
      Function<JSONCodec.JSONArray, JSONCodec.JSONArray> jsonArrayBuilder) {
    String json = StringUtil.EMPTY;
    if (jsonArrayBuilder != null) {
      try {
        json = JSONCodec.encodeArray(jsonArrayBuilder);
        if (json.trim().equals("[]")) {
          json = StringUtil.EMPTY;
        }
      } catch(Exception ex) {

      }
    }
    return packJSonDataWithHtmlContainer(json);
  }

  /**
   * Packaging a JSon string as HTML to be performed by IFrame Ajax Transport Javascript Plugin.
   * @param jsonString
   * @return
   */
  protected static String packJSonDataWithHtmlContainer(String jsonString) {
    ElementContainer xhtmlcontainer = new ElementContainer();
    MultiPartElement response = new textarea();
    response.addAttribute("data-type", MediaType.APPLICATION_JSON);
    if (StringUtil.isDefined(jsonString)) {
      response.addElementToRegistry(jsonString);
    }
    return xhtmlcontainer.addElement(response).toString();
  }
}
