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

import com.silverpeas.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.MultiPartElement;
import org.apache.ecs.xhtml.textarea;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.silverpeas.notification.message.MessageManager;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

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
      errorEntity =
          packJSonDataWithHtmlContainer(new JSONObject().put("iframeMessageKey", messageKey));
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
    if (object != null) {
      return packJSonDataWithHtmlContainer(new ObjectMapper().writeValueAsString(object));
    }
    return packJSonDataWithHtmlContainer(StringUtil.EMPTY);
  }

  /**
   * Packaging an object list as JSon String and then as HTML to be performed by IFrame Ajax
   * Transport Javascript Plugin.
   * @param objects
   * @return
   */
  public static String packObjectToJSonDataWithHtmlContainer(List<Object> objects)
      throws IOException {
    if (CollectionUtils.isNotEmpty(objects)) {
      ObjectMapper mapper = new ObjectMapper();
      JSONArray jsonArray = new JSONArray();
      for (Object object : objects) {
        jsonArray.put(mapper.writeValueAsString(object));
      }
      return packJSonDataWithHtmlContainer(jsonArray.toString());
    }
    return packJSonDataWithHtmlContainer(StringUtil.EMPTY);
  }

  /**
   * Packaging a JSon object as HTML to be performed by IFrame Ajax Transport Javascript
   * Plugin.
   * @param jsonObject
   * @return
   */
  public static String packJSonDataWithHtmlContainer(JSONObject jsonObject) {
    if (jsonObject != null) {
      return packJSonDataWithHtmlContainer(jsonObject.toString());
    }
    return packJSonDataWithHtmlContainer(StringUtil.EMPTY);
  }

  /**
   * Packaging a JSon object list as HTML to be performed by IFrame Ajax Transport Javascript
   * Plugin.
   * @param jsonObjects
   * @return
   */
  public static String packJSonDataWithHtmlContainer(List<JSONObject> jsonObjects) {
    if (CollectionUtils.isNotEmpty(jsonObjects)) {
      JSONArray jsonArray = new JSONArray();
      for (JSONObject jsonObject : jsonObjects) {
        jsonArray.put(jsonObject);
      }
      return packJSonDataWithHtmlContainer(jsonArray.toString());
    }
    return packJSonDataWithHtmlContainer(StringUtil.EMPTY);
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
