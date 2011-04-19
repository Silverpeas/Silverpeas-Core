/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

/*
 * AuthenticationNT.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class performs the NT authentification by calling the NTRIS service
 * @author tleroi
 * @version
 */
public class AuthenticationNT extends Authentication {
  protected String m_Host;
  protected int m_Port = 9999;

  protected Socket m_Client;
  protected PrintWriter m_Out = null;
  protected BufferedReader m_In = null;
  protected int m_Timeout = 10000; // timeout after which, without any response
  // from the host, we give up and close the
  // connection

  char START_DELIM = 1;
  char LOGON_COMMAND = '1';
  char GET_USER_INFORMATION_COMMAND = '2';
  char END_DELIM = 2;

  public void init(String authenticationServerName, ResourceLocator propFile) {
    // Lecture du fichier de proprietes
    m_Host = propFile.getString(authenticationServerName + ".NTRISHost");
    m_Port = Integer.parseInt(propFile.getString(authenticationServerName
        + ".NTRISPort"));
  }

  protected void openConnection() throws AuthenticationException {
    try {
      InetAddress localAddr = InetAddress.getByName(m_Host); // pick any
      m_Client = new Socket(localAddr, m_Port);
      m_Out = new PrintWriter(m_Client.getOutputStream(), true);
      m_In = new BufferedReader(
          new InputStreamReader(m_Client.getInputStream()));
      m_Client.setSoTimeout(m_Timeout);
    } catch (Exception ex) {
      m_Client = null;
      m_Out = null;
      m_In = null;
      throw new AuthenticationHostException(
          "AuthenticationNT.openConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", "Host=" + m_Host + ";Port="
          + Integer.toString(m_Port), ex);
    }
  }

  protected void closeConnection() throws AuthenticationException {
    try {
      if (m_Client != null) {
        m_Client.close();
      }
      m_Client = null;
      m_Out = null;
      m_In = null;
    } catch (Exception ex) {
      throw new AuthenticationHostException(
          "AuthenticationNT.closeConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_CLOSE_FAILED", "Host=" + m_Host + ";Port="
          + Integer.toString(m_Port), ex);
    }
  }

  protected void internalAuthentication(String login, String passwd)
      throws AuthenticationException {
    String line = null;
    String pass;
    int idx;

    SilverTrace.info("authentication",
        "AuthenticationNT.internalAuthentication()",
        "authentication.MSG_TRY_TO_AUTHENTICATE_USER", "User=" + login);
    if ((idx = login.indexOf("\\")) > 0) {
      login = login.substring(idx + 1);
    }
    if (passwd == null)
      pass = "";
    else
      pass = passwd;
    try {
      // gobble up welcome string
      line = m_In.readLine();

      // start delim + LOGON Command + start delim + <user> + start delim +
      // <passwd> + end delim
      // 
      StringBuffer sb = new StringBuffer(1 + 1 + 1 + login.length() + 1
          + pass.length() + 1);
      sb.append(START_DELIM);
      sb.append(LOGON_COMMAND);
      sb.append(START_DELIM);
      sb.append("");
      sb.append(START_DELIM);
      sb.append(login);
      sb.append(START_DELIM);
      sb.append(pass);
      sb.append(END_DELIM);
      m_Out.println(sb);
      line = m_In.readLine();
    } catch (Exception ex) {
      throw new AuthenticationHostException(
          "AuthenticationNT.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_NT_ACCESS_ERROR", ex);
    }
    if (line.equalsIgnoreCase("-OK")) {
      SilverTrace.info("authentication",
          "AuthenticationNT.internalAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } else if (line.equalsIgnoreCase("-ERR")) {
      throw new AuthenticationBadCredentialException(
          "AuthenticationNT.internalAuthentication()",
          SilverpeasException.ERROR,
          "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "User=" + login);
    } else {
      throw new AuthenticationHostException(
          "AuthenticationNT.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_NT_RETURN_ERROR",
          "Line=" + line);
    }
  }
}
