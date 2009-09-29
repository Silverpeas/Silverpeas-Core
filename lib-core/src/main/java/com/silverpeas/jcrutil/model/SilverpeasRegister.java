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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.jcrutil.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader;
import org.apache.jackrabbit.core.nodetype.compact.ParseException;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.JcrConstants;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SilverpeasRegister {
  /**
   * Register Silverpeas node types in Jackrabbit
   * 
   * @param cndFileName
   *          the file containing the nodes definitions.
   * @throws NamespaceException
   * @throws UnsupportedRepositoryOperationException
   * @throws AccessDeniedException
   * @throws RepositoryException
   * @throws ParseException
   * @throws FileNotFoundException
   * @throws InvalidNodeTypeDefException
   */
  @SuppressWarnings("unchecked")
  public static void registerNodeTypes(String cndFileName)
      throws NamespaceException, UnsupportedRepositoryOperationException,
      AccessDeniedException, RepositoryException, ParseException,
      FileNotFoundException, InvalidNodeTypeDefException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();

      FileReader fileReader = new FileReader(cndFileName);

      // Create a CompactNodeTypeDefReader
      CompactNodeTypeDefReader cndReader = new CompactNodeTypeDefReader(
          fileReader, cndFileName);

      // Get the List of NodeTypeDef objects
      List ntdList = cndReader.getNodeTypeDefs();
      Workspace ws = session.getWorkspace();

      // Get the NodeTypeManager from the Workspace.
      // Note that it must be cast from the generic JCR NodeTypeManager to the
      // Jackrabbit-specific implementation.
      try {
        ws.getNamespaceRegistry().registerNamespace(
            JcrConstants.SILVERPEAS_PREFIX,
            cndReader.getNamespaceMapping().getURI(
                JcrConstants.SILVERPEAS_PREFIX));
      } catch (NamespaceException e) {
        // The namespace may be already registred
        SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
            "RepositoryAccessServlet error", e);
      }
      NodeTypeManagerImpl ntmgr = (NodeTypeManagerImpl) ws.getNodeTypeManager();
      // Acquire the NodeTypeRegistry
      NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
      // Loop through the prepared NodeTypeDefs
      for (Iterator i = ntdList.iterator(); i.hasNext();) {
        // Get the NodeTypeDef...
        NodeTypeDef ntd = (NodeTypeDef) i.next();
        // ...and register it
        try {
          ntreg.registerNodeType(ntd);
        } catch (InvalidNodeTypeDefException e) {
          // The node type may be already registred
          if(! reRegisterNodeType(ntreg, ntd)) {
          SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
              "RepositoryAccessServlet error", e);
          }
        }
      }
      session.save();
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected static boolean reRegisterNodeType(NodeTypeRegistry ntreg,
      NodeTypeDef ntd) {
    try {
      ntreg.reregisterNodeType(ntd);
      return true;
    } catch (NoSuchNodeTypeException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      return false;
    } catch (InvalidNodeTypeDefException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      return false;
    } catch (RepositoryException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      return false;
    }
  }
}
