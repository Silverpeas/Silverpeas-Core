/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Yohann Chastagnier
 */
public class ResponseContentProducer {

  /**
   * Hidden constructor.
   */
  private ResponseContentProducer(){
  }

  static void produce(WebComponentRequestContext context, Path pathToPerform) {
    String contentType = pathToPerform.getProduces().value()[0];
    context.getResponse().setHeader("Content-Type", contentType + "; charset=UTF-8");
    if (contentType.equals(MediaType.APPLICATION_JSON)) {
      handleJson(context, pathToPerform);
    } else {
      throw new IllegalArgumentException("only JSON producing is handled");
    }
  }

  private static void handleJson(final WebComponentRequestContext context,
      final Path pathToPerform) {
    Object content = invoke(context, pathToPerform);
    String responseContent = "";
    if (content instanceof String) {
      responseContent = (String) content;
    } else if (content != null) {
      responseContent = JSONCodec.encode(content);
    }
    if (StringUtil.isDefined(responseContent)) {
      try {
        context.getResponse().getWriter().append(responseContent);
      } catch (IOException e) {
        throw new WebApplicationException(e, Response.Status.SERVICE_UNAVAILABLE);
      }
    }
  }

  private static Object invoke(final WebComponentRequestContext context, final Path pathToPerform) {
    try {
      return pathToPerform.getResourceMethod().invoke(context.getController(), context);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new WebApplicationException(
          "error during " + pathToPerform.getResourceMethod().getName() + " invocation", e,
          Response.Status.SERVICE_UNAVAILABLE);
    }
  }
}
