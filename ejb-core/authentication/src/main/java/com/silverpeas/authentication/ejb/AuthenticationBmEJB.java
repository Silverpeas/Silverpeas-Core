/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.authentication.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;

public class AuthenticationBmEJB implements SessionBean {

  public String authenticate(String login, String password, String domainId) {
    LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();

    String authenticationKey = lpAuth.authenticate(login, password, domainId, null);

    return authenticationKey;
  }

  public void ejbCreate() {
  }

  public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
  }

  public void ejbRemove() throws EJBException, RemoteException {
  }

  public void ejbActivate() throws EJBException, RemoteException {
  }

  public void ejbPassivate() throws EJBException, RemoteException {
  }

}