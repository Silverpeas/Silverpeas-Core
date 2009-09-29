/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.invoker.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

import com.sun.portal.container.ContainerLogger;
import com.sun.portal.portletcontainer.invoker.ResponseProperties;

/**
 * InvokerUtil has utility methods needed by the Window Invoker
 * 
 */
public class InvokerUtil {

  public static String MARKUP_HEADERS = "com.sun.portal.portletcontainer.markupHeaders";

  // Create a logger for this class
  private static Logger logger = ContainerLogger.getLogger(InvokerUtil.class,
      "com.silverpeas.portlets.PCCTXLogMessages");

  /**
   * Sets the response properties like cookies and headers in the
   * HttpServletResponse and sets the markup header in Session so that it will
   * be set in the <head> tag in header.jsp
   * 
   * @param request
   *          the HttpServletRequest object
   * @param response
   *          the HttpServletResponse object
   * @param responseProperties
   *          the ResponseProperties responseProperties object
   */
  public static void setResponseProperties(HttpServletRequest request,
            HttpServletResponse response, ResponseProperties responseProperties) {
        if(responseProperties != null) {
            List<Cookie> cookies = responseProperties.getCookies();
            for (Cookie cookie : cookies) {
                response.addCookie(cookie);
            }

            Map<String, List<String>> responseHeaders = responseProperties.getResponseHeaders();
            Set<Map.Entry<String, List<String>>> entries = responseHeaders.entrySet();
            for(Map.Entry<String, List<String>> mapEntry : entries) {
                String headerName = mapEntry.getKey();
                for(String headerValue : mapEntry.getValue()) {
                    response.addHeader(headerName, headerValue);
                }
            }
            
            HttpSession session = request.getSession(true);
            session.removeAttribute(MARKUP_HEADERS);
            List<Element> markupHeadElements = responseProperties.getMarkupHeadElements();
            if(!markupHeadElements.isEmpty()) {
                List<String> markupHeaders = new ArrayList<String>();
                for(Element markupHeadElement : markupHeadElements) {
                    String elementValue = convertElementToString(markupHeadElement);
                    if(elementValue != null) {
                        markupHeaders.add(elementValue);
                    }
                }
                session.setAttribute(MARKUP_HEADERS, markupHeaders);
            }
        }
    }

  /**
   * Clears the response properties
   * 
   * @param responseProperties
   *          the ResponseProperties responseProperties object
   */
  public static void clearResponseProperties(
      ResponseProperties responseProperties) {
    if (responseProperties != null) {
      responseProperties.clear();
    }
  }

  private static String convertElementToString(Element element) {
    try {
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StringWriter sw = new StringWriter();
      transformer.transform(new DOMSource(element), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException ex) {
      if (logger.isLoggable(Level.WARNING)) {
        logger.log(Level.WARNING, "PSPCD_CSPPD0010", ex.toString());
      }
    }
    return null;
  }
}
