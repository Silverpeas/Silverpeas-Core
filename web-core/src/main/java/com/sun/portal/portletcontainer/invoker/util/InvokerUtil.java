/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.invoker.util;

import com.sun.portal.container.ContainerLogger;
import com.sun.portal.portletcontainer.invoker.ResponseProperties;
import org.w3c.dom.Element;

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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * InvokerUtil has utility methods needed by the Window Invoker
 */
public class InvokerUtil {

  public static String MARKUP_HEADERS = "com.sun.portal.portletcontainer.markupHeaders";

  // Create a logger for this class
  private static Logger logger = ContainerLogger.getLogger(InvokerUtil.class,
      "org.silverpeas.portlets.PCCTXLogMessages");

  /**
   * Sets the response properties like cookies and headers in the HttpServletResponse and sets the
   * markup header in Session so that it will be set in the <head> tag in header.jsp
   * @param request the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @param responseProperties the ResponseProperties responseProperties object
   */
  public static void setResponseProperties(HttpServletRequest request,
      HttpServletResponse response, ResponseProperties responseProperties) {
    if (responseProperties != null) {
      List<Cookie> cookies = responseProperties.getCookies();
      for (Cookie cookie : cookies) {
        response.addCookie(cookie);
      }

      Map<String, List<String>> responseHeaders = responseProperties.getResponseHeaders();
      Set<Map.Entry<String, List<String>>> entries = responseHeaders.entrySet();
      for (Map.Entry<String, List<String>> mapEntry : entries) {
        String headerName = mapEntry.getKey();
        for (String headerValue : mapEntry.getValue()) {
          response.addHeader(headerName, headerValue);
        }
      }

      HttpSession session = request.getSession(true);
      session.removeAttribute(MARKUP_HEADERS);
      List<Element> markupHeadElements = responseProperties.getMarkupHeadElements();
      if (!markupHeadElements.isEmpty()) {
        List<String> markupHeaders = new ArrayList<String>();
        for (Element markupHeadElement : markupHeadElements) {
          String elementValue = convertElementToString(markupHeadElement);
          if (elementValue != null) {
            markupHeaders.add(elementValue);
          }
        }
        session.setAttribute(MARKUP_HEADERS, markupHeaders);
      }
    }
  }

  /**
   * Clears the response properties
   * @param responseProperties the ResponseProperties responseProperties object
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
