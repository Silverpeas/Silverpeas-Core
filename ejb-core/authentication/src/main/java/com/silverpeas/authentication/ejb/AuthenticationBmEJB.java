/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.authentication.ejb;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.authentication.AuthenticationCredential;
import org.silverpeas.authentication.AuthenticationService;

@Stateless(name = "AuthenticationBm", description =
    "EJB to allow for remote authentication on Silverpeas.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class AuthenticationBmEJB implements AuthenticationBm {

  @Override
  public String authenticate(String login, String password, String domainId) {
    AuthenticationService authenticator = new AuthenticationService();
    AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(login)
        .withAsPassword(password).withAsDomainId(domainId);
    return authenticator.authenticate(credential);
  }
}