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

package org.silverpeas.core.exception;

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
abstract public class SilverpeasTrappedException extends SilverpeasException {
  String gobackPage = "";

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public void setGoBackPage(String gbp) {
    gobackPage = gbp;
  }

  public String getGoBackPage() {
    return gobackPage;
  }
}
