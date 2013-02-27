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

/*
 * AuthenticationNT.java
 *
 * Created on 6 aout 2001
 */
package org.silverpeas.authentication;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.silverpeas.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.authentication.exception.AuthenticationException;
import org.silverpeas.authentication.exception.AuthenticationHostException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This class performs the NT authentication by calling the NTRIS service
 * <p/>
 * @author tleroi
 * @version
 */
public class AuthenticationNT extends Authentication {

  protected String m_Host;
  protected int m_Port = 9999;
  protected int m_Timeout = 10000; // timeout after which, without any response
  // from the host, we give up and close the
  // connection
  char START_DELIM = 1;
  char LOGON_COMMAND = '1';
  char GET_USER_INFORMATION_COMMAND = '2';
  char END_DELIM = 2;

  @Override
  public void loadProperties(ResourceLocator settings) {
    m_Host = settings.getString(getServerName() + ".NTRISHost");
    m_Port = Integer.parseInt(settings.getString(getServerName() + ".NTRISPort"));
  }

  @Override
  protected AuthenticationConnection<Socket> openConnection() throws AuthenticationException {
    try {
      InetAddress localAddr = InetAddress.getByName(m_Host); // pick any
      Socket socket = new Socket(localAddr, m_Port);
      socket.setSoTimeout(m_Timeout);
      return new AuthenticationConnection<Socket>(socket);
    } catch (Exception ex) {
      throw new AuthenticationHostException("AuthenticationNT.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          "Host=" + m_Host + ";Port=" + java.lang.Integer.toString(m_Port), ex);
    }
  }

  @Override
  protected void closeConnection(AuthenticationConnection connection) throws AuthenticationException {
    try {
      Socket connector = getSocket(connection);
      if (connector != null) {
        connector.close();
      }
    } catch (Exception ex) {
      throw new AuthenticationHostException("AuthenticationNT.closeConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", "Host=" + m_Host + ";Port="
          + java.lang.Integer.toString(m_Port), ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
                                  AuthenticationCredential credential) throws AuthenticationException {
    String login = credential.getLogin();
    int idx;
    SilverTrace.info(module,
        "AuthenticationNT.doAuthentication()",
        "authentication.MSG_TRY_TO_AUTHENTICATE_USER", "User=" + login);
    if ((idx = login.indexOf("\\")) > 0) {
      login = login.substring(idx + 1);
    }
    String pass = credential.getPassword();
    if (pass == null) {
      pass = "";
    }
    String line;
    try {
      Socket socket = getSocket(connection);
      PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      // gobble up welcome string
      line = reader.readLine();

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
      writer.println(sb);
      line = reader.readLine();
    } catch (Exception ex) {
      throw new AuthenticationHostException("AuthenticationNT.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_NT_ACCESS_ERROR", ex);
    }
    if ("-OK".equalsIgnoreCase(line)) {
      SilverTrace.info(module, "AuthenticationNT.doAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } else if ("-ERR".equalsIgnoreCase(line)) {
      throw new AuthenticationBadCredentialException("AuthenticationNT.doAuthentication()",
          SilverpeasException.ERROR,
          "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "User=" + login);
    } else {
      throw new AuthenticationHostException("AuthenticationNT.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_NT_RETURN_ERROR",
          "Line=" + line);
    }
  }

  private static Socket getSocket(AuthenticationConnection connection) {
    return (Socket) connection.getConnector();
  }
}
