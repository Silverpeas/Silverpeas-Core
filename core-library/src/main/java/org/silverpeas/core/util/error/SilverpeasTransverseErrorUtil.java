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
package org.silverpeas.core.util.error;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.exception.WithNested;
import org.silverpeas.core.admin.component.exception.ComponentFileFilterException;
import org.silverpeas.core.admin.space.quota.process.check.exception.DataStorageQuotaException;
import org.silverpeas.core.util.UnitUtil;

import javax.ejb.EJBException;
import java.rmi.RemoteException;

/**
 * Centralized treatment of transverse exceptions :
 * {@link DataStorageQuotaException}
 * {@link ComponentFileFilterException}
 * @author Yohann Chastagnier
 */
public class SilverpeasTransverseErrorUtil {

  /**
   * Stops a runtime transverse exception exists
   * @param exception
   */
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static <E extends Exception> void stopTransverseErrorIfAny(E exception) throws E {

    // Data storage quota exception
    final DataStorageQuotaException dataStorageQuotaException =
        retrieveDataStorageQuotaException(exception);
    if (dataStorageQuotaException != null) {
      return;
    }

    // Component file filter exception
    final ComponentFileFilterException componentFileFilterException =
        retrieveComponentFileFilterException(exception);
    if (componentFileFilterException != null) {
      return;
    }

    throw exception;
  }

  /**
   * Checks from a exception if a runtime transverse exception has to be thrown
   * @param exception the http servlet request
   * @param language the language of current user
   */
  public static <E extends Exception> void throwTransverseErrorIfAny(E exception,
      final String language) throws DataStorageQuotaException, ComponentFileFilterException {

    // Data storage quota exception
    final DataStorageQuotaException dataStorageQuotaException =
        retrieveDataStorageQuotaException(exception);
    if (dataStorageQuotaException != null) {
      dataStorageQuotaException.setLanguage(language);
      throw dataStorageQuotaException;
    }

    // Component file filter exception
    final ComponentFileFilterException componentFileFilterException =
        retrieveComponentFileFilterException(exception);
    if (componentFileFilterException != null) {
      componentFileFilterException.setLanguage(language);
      throw componentFileFilterException;
    }
  }

  /**
   * Retrieves a formatted exception message if any handled from a given exception
   * @param exception
   * @return
   */
  @SuppressWarnings({"exceptionResultOfMethodCallIgnored", "ThrowableResultOfMethodCallIgnored"})
  public static <E extends Exception> String performExceptionMessage(E exception, String language) {
    String message = "";
    final SilverpeasTemplate template;

    // Data storage quota exception
    final DataStorageQuotaException dsqe = retrieveDataStorageQuotaException(exception);
    if (dsqe != null) {
      dsqe.setLanguage(language);
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
    final ComponentFileFilterException cffe = retrieveComponentFileFilterException(exception);
    if (cffe != null) {
      cffe.setLanguage(language);
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
   * Retrieves a DataStorageQuotaException if any from a given exception
   * @param exception
   * @return
   */
  private static <E extends Exception> DataStorageQuotaException retrieveDataStorageQuotaException(
      E exception) {
    return retrieveException(exception, DataStorageQuotaException.class);
  }

  /**
   * Retrieves a ComponentFileFilterException if any from a given exception
   * @param exception
   * @return
   */
  private static <E extends Exception> ComponentFileFilterException
  retrieveComponentFileFilterException(
      E exception) {
    return retrieveException(exception, ComponentFileFilterException.class);
  }

  /**
   * Retrieves an exception if any from a given exception
   * @param exception
   * @param exceptionClass
   * @return
   */
  @SuppressWarnings("unchecked")
  private static <E extends Exception, T extends Exception> E retrieveException(T exception,
      final Class<E> exceptionClass) {
    Throwable throwable = exception;
    if (throwable != null) {
      while (throwable != null) {
        if (exceptionClass.isInstance(throwable)) {
          return (E) throwable;
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
