/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.exception;

import org.silverpeas.core.silvertrace.SilverTrace;

import javax.ejb.EJBException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * SilverpeasException est la racine de la hiérarchie d'exception silverpeas. Toutes les classes
 * d'exception spécifiques aux differents modules doivent dériver (directement ou non) de
 * SilverpeasException. La page d'erreur globale à l'application ne saura traiter correctement que
 * les SilverpeasException. Les autres exception (ou error ou runtime) provoqueront l'affichage
 * d'une page d'erreur imprévue. Le message que l'on donne à l'exception est très important, il doit
 * etre affiché à l'utilisateur. C'est pourquoi le label est multilangue. Chaque classe heritant de
 * SilverpeasException doit surdefinir la fonction getModule qui retourne le nom du module (le meme
 * nom que celui defini dans Silvertrace)
 */
abstract public class SilverpeasException extends Exception implements WithNested, FromModule {

  public static final int FATAL = SilverTrace.TRACE_LEVEL_FATAL;
  public static final int ERROR = SilverTrace.TRACE_LEVEL_ERROR;
  public static final int WARNING = SilverTrace.TRACE_LEVEL_WARN;
  private static final long serialVersionUID = -981770983716177578L;
  private int errorLevel = ERROR;
  private Exception nested = null;
  private String extraParams = "";
  private String callingClass = "NoClass";

  public SilverpeasException(Exception exception) {
    this("noClass", ERROR, "", null, exception);
  }

  // Constructors
  // ------------
  /**
   * Fabriquation d'une exception silverpeas avec un message multilangue. Il n'y a pas d'exception
   * ayant provoqué celle-ci (getNested() renvera null). Le niveau d'importance est ERROR.
   *
   * @param message Le label multilangue. Ex : "impossibleDeFabriquerUneConnexionBDD". Le label est
   * traduit dans des fichiers de properties.
   * @deprecated
   */
  public SilverpeasException(String message) {
    this("noClass", ERROR, message, null, null);
  }

  /**
   * Fabriquation d'une exception silverpeas avec un message multilangue et une exception à
   * imbriquer. Le niveau d'importance est ERROR.
   *
   * @param message Le label multilangue. Ex : "impossibleDeFabriquerUneConnexionBDD". Le label est
   * traduit dans des fichiers de properties.
   * @param nested L'exception qui a provoqué le problème. nested peut etre une SilverpeasException
   * ou une exception technique (SQLException, RemoteException...)
   * @deprecated
   */
  public SilverpeasException(String message, Exception nested) {
    this("noClass", ERROR, message, null, nested);
  }

  /**
   * Fabriquation d'une exception silverpeas avec un message multilangue, une eception à imbriquer
   * et un niveau d'importance.
   *
   * @param message Le label multilangue. Ex : "impossibleDeFabriquerUneConnexionBDD". Le label est
   * traduit dans des fichiers de properties.
   * @param nested L'exception qui a provoqué le problème. nested peut etre une SilverpeasException
   * ou une exception technique (SQLException, RemoteException...)
   * @param errorLevel Le niveau de critissicité de l'erreur (FATAL, ERROR, ou WARNING)
   * @deprecated
   */
  public SilverpeasException(String message, Exception nested, int errorLevel) {
    this("noClass", errorLevel, message, null, nested);
  }

  public SilverpeasException(String callingClass, int errorLevel, String message) {
    this(callingClass, errorLevel, message, null, null);
  }

  public SilverpeasException(String callingClass, int errorLevel,
      String message, String extraParams) {
    this(callingClass, errorLevel, message, extraParams, null);
  }

  public SilverpeasException(String callingClass, int errorLevel,
      String message, Exception nested) {
    this(callingClass, errorLevel, message, null, nested);
  }

  public SilverpeasException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(message);
    this.callingClass = callingClass;
    this.errorLevel = errorLevel;
    this.extraParams = extraParams;
    this.nested = nested;

    // Trace the exception as INFO or FATAL
    if (errorLevel == FATAL) {
      SilverTrace.fatal(getModule(), callingClass, message, extraParams, this);
    } else {

    }
  }

  // WithNested methods
  // ------------------
  /**
   * retourne l'exception qui a provoqué la creation de celle-ci. Permet l'encapsulation des
   * exception technique.
   *
   * @return L'exception précédente qui a provoqué celle-ci, null s'il n'y en a pas.
   */
  @Override
  public Exception getNested() {
    return nested;
  }

  // Throwable methods overloaded
  // ----------------------------
  @Override
  public void printStackTrace() {
    this.printStackTrace(System.out);
  }

  @Override
  public void printStackTrace(java.io.PrintStream s) {
    if (getNested() == null) {
      s.println(this.getMessageLang());
      super.printStackTrace(s);
    } else {
      Throwable e = null;
      s.println(this.getMessageLang());
      Collection<Throwable> exceptions = SilverpeasException
          .getChainedExceptions(getNested());
      for (Iterator<Throwable> i = exceptions.iterator(); i.hasNext();) {
        e = i.next();
        if (!i.hasNext()) {
          s.println("nested (Deepest) : ");
        } else {
          if (e instanceof FromModule) {
            s.println("nested : " + ((FromModule) e).getMessageLang());
          } else {
            s.println("nested : " + e.getMessage());
          }
        }
      }
      if (e != null) {
        e.printStackTrace(s);
      }
    }
  }

  @Override
  public void printStackTrace(java.io.PrintWriter w) {
    if (getNested() == null) {
      w.println(this.getMessageLang());
      super.printStackTrace(w);
    } else {
      Throwable e = null;

      w.println(this.getMessageLang());
      Collection<Throwable> exceptions = SilverpeasException
          .getChainedExceptions(getNested());
      for (Iterator<Throwable> i = exceptions.iterator(); i.hasNext();) {
        e = i.next();
        if (!i.hasNext()) {
          w.println("nested (Deepest) : ");
        } else {
          if (e instanceof FromModule) {
            w.println("nested : " + ((FromModule) e).getMessageLang());
          } else {
            w.println("nested : " + e.getMessage());
          }
        }
      }
      if (e != null) {
        e.printStackTrace(w);
      }
    }
  }

  // FromModule methods
  // ------------------
  /**
   * This function must be defined by the Classes that herit from this one
   *
   * @return The SilverTrace's module name
   *
   */
  @Override
  abstract public String getModule();

  @Override
  public String getMessageLang() {
    return SilverTrace.getTraceMessage(getMessage());
  }

  @Override
  public String getMessageLang(String language) {
    return SilverTrace.getTraceMessage(getMessage(), language);
  }

  @Override
  public void traceException() {
    switch (errorLevel) {
      case FATAL:
        SilverTrace.fatal(getModule(), callingClass, getMessage(), extraParams,
            this);
        break;
      case ERROR:
        SilverTrace.error(getModule(), callingClass, getMessage(), extraParams,
            this);
        break;
      case WARNING:
        SilverTrace.warn(getModule(), callingClass, getMessage(), extraParams,
            this);
        break;
      default:

    }
  }

  /**
   * retourne le niveau de critissicité de l'exception
   *
   * @return le niveau d'erreur, FATAL, ERROR, ou WARNING
   */
  @Override
  public int getErrorLevel() {
    return errorLevel;
  }

  public String getExtraInfos() {
    return extraParams;
  }

  // Specific methods
  // ----------------
  /**
   * méthode utilitaire dont le role est de représenter les exceptions encapsulées sous forme d'une
   * collection. L'exception courante se trouvera en première position de la collection. Les
   * eventuelles imbrications succéssive suivront dans la liste.
   *
   * @param e L'exception de plus haut niveau
   * @return une collection de Throwable, qui contiendra au moins l'exception passee en parametre.
   * Et plus si cette exception en imbrique d'autre.
   */
  public static Collection<Throwable> getChainedExceptions(Throwable e) {
    List<Throwable> result = new ArrayList<Throwable>();
    Throwable throwable = e;
    while (throwable != null) {
      result.add(throwable);
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
    return result;
  }
}