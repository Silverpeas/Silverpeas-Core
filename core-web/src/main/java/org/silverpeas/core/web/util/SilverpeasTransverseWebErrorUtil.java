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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.admin.component.exception.ComponentFileFilterException;
import org.silverpeas.core.admin.space.quota.process.check.exception.DataStorageQuotaException;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;

import javax.servlet.ServletRequest;

/**
 * Centralized treatment of web transverse exceptions.
 * @author Yohann Chastagnier
 */
public class SilverpeasTransverseWebErrorUtil extends SilverpeasTransverseErrorUtil {

  private static final String SERVLET_JSP_EXCEPTION_ATTRIBUTE = "javax.servlet.jsp.jspException";

  /**
   * Checks if a DataStorageQuotaException is registered in the servlet request
   * @param request the http servlet request
   * @param language the language of current user
   * @throws DataStorageQuotaException, ComponentFileFilterException
   */
  public static void verifyErrorFromRequest(final ServletRequest request, final String language)
      throws DataStorageQuotaException, ComponentFileFilterException {
    final Object servletJspExceptionAttribute =
        request.getAttribute(SERVLET_JSP_EXCEPTION_ATTRIBUTE);
    if (servletJspExceptionAttribute instanceof Exception) {
      throwTransverseErrorIfAny((Exception) servletJspExceptionAttribute, language);
    }
  }

  /**
   * Retrieves an unformatted exception message if any handled from a given throwable
   * @param exception
   * @return
   */
  public static String performAppletAlertExceptionMessage(Exception exception, String language) {
    return EncodeHelper.htmlStringToJavaString(performExceptionMessage(exception, language))
        .replaceAll("(</?b|</?i|</?p)[a-zA-Z=\"'${}\\.0-9 ]*/?>", "").replaceAll("\\n", "\\\\n");
  }
}
