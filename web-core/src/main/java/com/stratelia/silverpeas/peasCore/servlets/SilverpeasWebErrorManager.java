/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.silverpeas.peasCore.servlets;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.webactiv.util.exception.WithNested;
import org.silverpeas.admin.component.exception.ComponentFileFilterException;
import org.silverpeas.admin.space.quota.process.check.exception.DataStorageQuotaException;
import org.silverpeas.util.UnitUtil;

import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;

/**
 * Centralized treatment of transverse exceptions.
 * @author Yohann Chastagnier
 */
public class SilverpeasWebErrorManager {

  private static final String SERVLET_JSP_EXCEPTION_ATTRIBUTE = "javax.servlet.jsp.jspException";

  /**
   * Checks if a DataStorageQuotaException is registred in the servlet request
   * @param request the http servlet request
   * @param language the language of current user
   * @throws DataStorageQuotaException, ComponentFileFilterException
   */
  public static void verifyErrorFromRequest(final HttpServletRequest request, final String language)
      throws DataStorageQuotaException, ComponentFileFilterException {
    final Object servletJspExceptionAttribute =
        request.getAttribute(SERVLET_JSP_EXCEPTION_ATTRIBUTE);
    if (servletJspExceptionAttribute instanceof Throwable) {
      final Throwable throwable = (Throwable) servletJspExceptionAttribute;

      // Data storage quota exception
      final DataStorageQuotaException dataStorageQuotaException =
          retrieveDataStorageQuotaException(throwable);
      if (dataStorageQuotaException != null) {
        dataStorageQuotaException.setLanguage(language);
        throw dataStorageQuotaException;
      }

      // Component file filter exception
      final ComponentFileFilterException componentFileFilterException =
          retrieveComponentFileFilterException(throwable);
      if (componentFileFilterException != null) {
        componentFileFilterException.setLanguage(language);
        throw componentFileFilterException;
      }
    }
  }

  /**
   * Retrieves a formatted exception message if any handled from a given throwable
   * @param throwable
   * @return
   */
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  private static String performExceptionMessage(Throwable throwable, String language) {
    String message = "";
    final SilverpeasTemplate template;

    // Data storage quota exception
    final DataStorageQuotaException dsqe = retrieveDataStorageQuotaException(throwable);
    if (dsqe != null) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("admin/space/quota");
      template
          .setAttribute("maxCountFormated", UnitUtil.formatMemSize(dsqe.getQuota().getMaxCount()));
      template.setAttribute("spaceName", dsqe.getSpace().getName());
      template.setAttribute("isPersonalSpace", dsqe.getSpace().isPersonalSpace());
      template.setAttribute("fromComponentName", dsqe.getFromComponent());
      message = template.applyFileTemplate("dataStorageQuotaExceptionMessage_" + language);
      return message;
    }

    // Component file filter exception
    final ComponentFileFilterException cffe = retrieveComponentFileFilterException(throwable);
    if (cffe != null) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("admin/component/error");
      template.setAttribute("fileFilters", cffe.getComponentFileFilterParameter().getFileFilters());
      template.setAttribute("isGloballySet",
          cffe.getComponentFileFilterParameter().isFileFilterGloballySet());
      template
          .setAttribute("isAuthorized", cffe.getComponentFileFilterParameter().isAuthorization());
      template.setAttribute("forbiddenFileName", cffe.getForbiddenFileName());
      template
          .setAttribute("fromComponentName", cffe.getComponentFileFilterParameter().getComponent());
      message = template.applyFileTemplate("forbiddenFileMessage_" + language);
      return message;
    }

    return message;
  }

  /**
   * Retrieves a unformatted exception message if any handled from a given throwable
   * @param throwable
   * @return
   */
  public static String performAppletAlertExceptionMessage(Throwable throwable, String language) {
    return EncodeHelper.htmlStringToJavaString(performExceptionMessage(throwable, language))
        .replaceAll("(</?b|</?i|</?p)[a-zA-Z=\"'${}\\.0-9 ]*/?>", "").replaceAll("\\n", "\\\\n");
  }

  /**
   * Retrieves a DataStorageQuotaException if any from a given throwable
   * @param throwable
   * @return
   */
  private static DataStorageQuotaException retrieveDataStorageQuotaException(Throwable throwable) {
    return retrieveException(throwable, DataStorageQuotaException.class);
  }

  /**
   * Retrieves a ComponentFileFilterException if any from a given throwable
   * @param throwable
   * @return
   */
  private static ComponentFileFilterException retrieveComponentFileFilterException(
      Throwable throwable) {
    return retrieveException(throwable, ComponentFileFilterException.class);
  }

  /**
   * Retrieves an exception if any from a given throwable
   * @param throwable
   * @param exceptionClass
   * @return
   */
  @SuppressWarnings("unchecked")
  private static <T extends Exception> T retrieveException(Throwable throwable,
      final Class<T> exceptionClass) {
    if (throwable != null) {
      while (throwable != null) {
        if (exceptionClass.isInstance(throwable)) {
          return (T) throwable;
        }

        if (throwable instanceof WithNested) {
          throwable = ((WithNested) throwable).getNested();
        } else if (throwable instanceof RemoteException) {
          throwable = ((RemoteException) throwable).detail;
        } else if (throwable instanceof EJBException) {
          throwable = ((EJBException) throwable).getCausedByException();
        } else {
          throwable = null;
        }
      }
    }
    return null;
  }
}
