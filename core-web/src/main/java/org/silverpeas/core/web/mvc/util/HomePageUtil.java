/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.web.mvc.util;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.exception.FromModule;
import org.silverpeas.core.exception.SilverpeasException;

import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class HomePageUtil {

  /**
   * Method declaration
   * @param exception
   * @param language
   * @return
   * @see
   */
  public static String getMessageToDisplay(Throwable exception, String language) {
    if (exception == null) {
      return "Internal error : No error in parameter's request";
    }
    Collection<Throwable> exceptions = SilverpeasException.getChainedExceptions(exception);
    for (Throwable toDisplayException : exceptions) {
      return HomePageUtil.getMessageFromException(toDisplayException, language);
    }
    return exception.getMessage();

  }

  /**
   * Method declaration
   * @param exception
   * @param language
   * @return
   * @see
   */
  public static String getMessagesToDisplay(Throwable exception, String language) {

    if (exception == null) {
      return "Internal error : No error in parameter's request";
    }
    Collection<Throwable> exceptions = SilverpeasException.getChainedExceptions(exception);
    StringBuilder result = new StringBuilder();
    for (Throwable toDisplayException : exceptions) {
      if (toDisplayException instanceof java.rmi.RemoteException) {
        continue;
      }
      if (result.length() > 0) {
        result.append("<BR>");
      }
      result.append(HomePageUtil.getMessageFromException(toDisplayException, language));
    }
    return result.toString();
  }

  /**
   * Method declaration
   * @param toDisplayException
   * @param language
   * @return
   * @see
   */
  public static String getMessageFromException(Throwable toDisplayException, String language) {
    if (toDisplayException.getMessage() == null) {
      return toDisplayException.getClass().getName();
    }
    if (toDisplayException instanceof FromModule) {
      return ((FromModule) toDisplayException).getMessageLang(language);
    } else if (toDisplayException instanceof RemoteException) {
      return getMessageFromException(((RemoteException) toDisplayException).detail, language);
    } else {
      // on affiche le label non multilangue de l'exception
      return toDisplayException.getMessage();
    }
  }

  /**
   * Method declaration
   * @param exception
   * @return
   * @see
   */
  public static Throwable getExceptionToDisplay(Throwable exception) {
    Throwable toDisplayException = null;
    if (exception != null) {
      Collection<Throwable> exceptions = SilverpeasException.getChainedExceptions(exception);
      for (Throwable exception1 : exceptions) {
        toDisplayException = exception1;
      }
    }
    return toDisplayException;
  }

  /**
   * Trace the exception in SilverTrace
   * @param exception
   */
  public static void traceException(Throwable exception) {
    Throwable lastEx = getExceptionToDisplay(exception);

    if ((lastEx != null)
        && (lastEx.getMessage() != null)
        && (lastEx.getMessage().contains("Connection reset by peer: socket write error"))
        && (!lastEx.getMessage().contains("SQL"))) {


    } else {
      if (exception instanceof FromModule) {
        ((FromModule) exception).traceException();
      } else {
        Throwable parcNested;
        boolean bFound = false;

        if (exception != null) {
          Collection<Throwable> exceptions = SilverpeasException.getChainedExceptions(exception);

          for (Throwable exception1 : exceptions) {
            parcNested = exception1;
            if (parcNested instanceof FromModule) {
              bFound = true;
              ((FromModule) parcNested).traceException();
            }
          }
        }
        if (bFound == false) {
          SilverTrace.error("util", "HomePageUtil.traceException()",
              "util.MSG_EXCEPTION_NOT_EMBEDED", "", exception);
        }
      }
    }
  }
}
