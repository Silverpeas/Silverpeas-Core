/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.exception;

import org.silverpeas.core.util.logging.SilverLogger;

/**
 * Deprecated exception. Please use instead {@link org.silverpeas.core.SilverpeasRuntimeException}
 * <br>
 * SilverpeasRuntimeException est la racine de la hiérarchie des exception runtime silverpeas. Les
 * exceptions Runtime ne sont pas obligatoirement traitées par le développeur. C'est pourquoi leur
 * utilisation doit se faire après reflexion. La principale application des
 * SilverpeasRuntimeException est l'utilisation au sein des EJB. En effet une RuntimeException qui
 * traverse le conteneur annule la transaction en cours, et sera encapsulée dans une
 * RemoteException. Le message que l'on donne à l'exception est très important, il doit etre affiché
 * à l'utilisateur. C'est pourquoi le label est multilangue. Chaque classe heritant de
 * SilverpeasRuntimeException doit surdefinir la fonction getModule qui retourne le nom du module
 * (le meme nom que celui defini dans Silvertrace)
 * @deprecated
 */
@Deprecated
 public abstract class SilverpeasRuntimeException extends RuntimeException implements WithNested,
    FromModule {

  public static final int FATAL = 0x00000005;
  public static final int ERROR = 0x00000004;
  public static final int WARNING = 0x00000003;
  private int errorLevel = ERROR;
  private Throwable nested = null;
  private String extraParams = "";
  private String callingClass = "NoClass";

  public SilverpeasRuntimeException(String callingClass, int errorLevel, String message) {
    this(callingClass, errorLevel, message, null, null);
  }

  public SilverpeasRuntimeException(String callingClass, int errorLevel, String message,
      String extraParams) {
    this(callingClass, errorLevel, message, extraParams, null);
  }

  public SilverpeasRuntimeException(String callingClass, int errorLevel, String message,
      Exception nested) {
    this(callingClass, errorLevel, message, null, nested);
  }

  public SilverpeasRuntimeException(String callingClass, int errorLevel, String message,
      String extraParams, Throwable nested) {
    super(message, nested);
    this.callingClass = callingClass;
    this.errorLevel = errorLevel;
    this.extraParams = extraParams;
    this.nested = nested;
    // Trace the exception as INFO or FATAL
    if (errorLevel == FATAL) {
      SilverLogger.getLogger(this).error(this);
    }
  }

  /**
   * retourne l'exception qui a provoqué la creation de celle-ci. Permet l'encapsulation des
   * exception technique.
   *
   * @return L'exception précédente qui a provoqué celle-ci, null s'il n'y en a pas.
   */
  @Override
  public Throwable getNested() {
    return nested;
  }

  /**
   * This function must be defined by the Classes that herit from this one
   *
   * @return The module name
   *
   */

  @Override
  public String getMessageLang() {
    return getMessage();
  }

  @Override
  public String getMessageLang(String language) {
    return getMessage();
  }

  @Override
  public void traceException() {
    switch (errorLevel) {
      case FATAL | ERROR:
        SilverLogger.getLogger(this).error(this);
        break;
      case WARNING:
        SilverLogger.getLogger(this).warn(this);
        break;
      default:
        break;
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
}