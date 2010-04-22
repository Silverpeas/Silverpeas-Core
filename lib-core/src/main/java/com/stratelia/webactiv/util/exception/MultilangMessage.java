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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.util.exception;

public class MultilangMessage {

  private String message = null;
  private String[] params;

  /**
   * fabrication d'un message multilangue avec un paramètre. Le message est un label multilangue qui
   * correspond, une fois traduit à une chaine contenant un paramètre. Exemple : message =
   * "util.MSG_EJB_INTROUVABLE", param1 = "ejb/NodeHome". Traduction dans fichier properties
   * francais "util.MSG_EJB_INTROUVABLE = L'ejb nommé %1 est introuvable". %1 est le premier
   * paramètre. On imprimera "L'ejb nommé ejb/NodeHome est introuvable".
   */
  public MultilangMessage(String message, String param1) {
    this.message = message;
    params = new String[1];
    params[0] = param1;
  }

  /**
   * meme chose avec deux paramètres. (%1 et %2)
   */
  public MultilangMessage(String message, String param1, String param2) {
    this.message = message;
    params = new String[2];
    params[0] = param1;
    params[1] = param2;
  }

  // remarque : on pourrait continuer avec trois paramètres. Dans ce cas, voir
  // aussi la methode fromString()

  public String getMessage() {
    return message;
  }

  public String[] getParameters() {
    return params;
  }

  /**
   * codage du message multilangue et de ses paramètre sous forme d'une String
   * @return la chaine encodée, qq chose comme [message,param1,param2]
   */
  public String toString() {
    String result = "[" + message;
    for (int i = 0; i < params.length; i++) {
      result += "," + params[i];
    }
    result += "]";
    return result;
  }
}