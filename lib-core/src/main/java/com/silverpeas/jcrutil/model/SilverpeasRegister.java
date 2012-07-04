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

package com.silverpeas.jcrutil.model;

import com.silverpeas.jcrutil.BasicDaoFactory;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import java.io.FileReader;
import java.io.IOException;

public class SilverpeasRegister {

  /**
   * Register Silverpeas node types in Jackrabbit
   * @param cndFileName the file containing the nodes definitions.
   * @throws NamespaceException
   * @throws UnsupportedRepositoryOperationException
   * @throws RepositoryException
   * @throws ParseException
   * @throws IOException
   */
  public static void registerNodeTypes(String cndFileName)
      throws RepositoryException, ParseException, IOException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      FileReader fileReader = new FileReader(cndFileName);
      CndImporter.registerNodeTypes(fileReader, session);
      session.save();
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
}
