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
package org.silverpeas.core.security.token;

import java.io.Serializable;

/**
 * A token in Silverpeas is an atom or a symbol, encoded in a String, that is either used to
 * identify uniquely a resource or to stamp a resource for security reason.
 *
 * There is several ways of using a token in an application, but those uses are usually all related
 * to the security. For example, the tokens can be used to authenticate and identify in a single
 * pass a user or, as another example, to stamp a user session or a web page.
 *
 * @author mmoquillon
 */
public interface Token extends Serializable {

  /**
   * Gets the String representation of this token.
   *
   * @return the value of the token (id est its String representation).
   */
  String getValue();

  /**
   * Is this token defined? A token is defined if it was generated and represents correctly a
   * well-valued token. If it is empty (a none token), then it is considered as undefined.
   *
   * It is expected a token can be never null and a none-token concept is used instead. This method
   * is to check the token is not a none-token, that is to say it is well initialized.
   *
   * @return true if this token is well-initialized, false otherwise.
   */
  boolean isDefined();
}
