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
import org.silverpeas.admin.space.quota.process.check.exception.DataStorageQuotaException;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.util.UnitUtil;

import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;

/**
 * Provide methods to manage quota errors
 * @author Yohann Chastagnier
 */
public class QuotaErrorManager {

  private static final String SERVLET_JSP_EXCEPTION_ATTRIBUTE = "javax.servlet.jsp.jspException";

  /**
   * Checks if a DataStorageQuotaException is registred in the servlet request
   * @param request the http servlet request
   * @throws QuotaException
   */
  public static void checkQuotaErrorFromRequest(final HttpServletRequest request)
      throws DataStorageQuotaException {
    final Object servletJspExceptionAttribute =
        request.getAttribute(SERVLET_JSP_EXCEPTION_ATTRIBUTE);
    if (servletJspExceptionAttribute instanceof Throwable) {
      final DataStorageQuotaException dataStorageQuotaException =
          retrieveDataStorageQuotaException((Throwable) servletJspExceptionAttribute);
      if (dataStorageQuotaException != null) {
        throw dataStorageQuotaException;
      }
    }
  }

  /**
   * Retrieves a formated QuotaException message if any from a given throwable
   * @param throwable
   * @return
   */
  public static String performQuotaExceptionMessage(
      Throwable throwable, String language) {
    String message  = "";
    final SilverpeasTemplate template;
    final DataStorageQuotaException dsqe = retrieveDataStorageQuotaException(throwable);
    if (dsqe != null) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("admin/space/quota");
      template
          .setAttribute("maxCountFormated", UnitUtil.formatMemSize(dsqe.getQuota().getMaxCount()));
      template.setAttribute("spaceName", dsqe.getSpace().getName());
      template.setAttribute("isPersonalSpace", dsqe.getSpace().isPersonalSpace());
      template.setAttribute("fromComponentName", dsqe.getFromComponent());
      message =  EncodeHelper
          .htmlStringToJavaString(
              template.applyFileTemplate("dataStorageQuotaExceptionMessage_" + language));
    }
    return message.replaceAll("(</?b|</?i|</?p)[a-zA-Z=\"'${}\\.0-9]*>","");
  }

  /**
   * Retrieves a DataStorageQuotaException if any from a given throwable
   * @param throwable
   * @return
   */
  private static DataStorageQuotaException retrieveDataStorageQuotaException(
      Throwable throwable) {
    if (throwable != null) {
      while (throwable != null) {
        if (throwable instanceof DataStorageQuotaException) {
          return (DataStorageQuotaException) throwable;
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
