/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.engine.jdo;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.DatabaseNotFoundException;
import org.exolab.castor.jdo.JDOManager;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.mapping.MappingException;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

/**
 * This class offers services about database persistence. It uses Castor library to read/write
 * process instance information in database
 */
public class WorkflowJDOManager {
  /**
   * ResourceLocator object to retrieve settings in a properties file
   */
  private static SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.workflow.engine.castorSettings");

  /**
   * JDO object used by Castor persistence mechanism
   */
  private static JDOManager jdo = null;

  /**
   * Get a connection to database
   * @return Database object
   */
  static public Database getDatabase() throws WorkflowException {
    return getDatabase(false);
  }

  /**
   * Get a connection to database
   * @return Database object
   */
  static public Database getDatabase(boolean fast) throws WorkflowException {
    String databaseFileURL;
    String castorLogFileURL;
    String processInstanceDBName;

    Database db = null;

    if (db == null) {
      if (jdo == null) {
        // get configuration files url
        databaseFileURL = settings.getString("CastorJDODatabaseFileURL");

        castorLogFileURL = settings.getString("CastorJDOLogFileURL");
        processInstanceDBName = settings.getString("ProcessInstanceDBName");

        // Format these url
        databaseFileURL = "file:///" + databaseFileURL.replace('\\', '/');
        castorLogFileURL = castorLogFileURL.replace('\\', '/');

        // Constructs new JDO object
        try {
          JDOManager.loadConfiguration(databaseFileURL);
          jdo = JDOManager.createInstance(processInstanceDBName);
        } catch (MappingException me) {
          throw new WorkflowException("JDOManager.getDatabase",
              "EX_ERR_CASTOR_DATABASE_MAPPING_ERROR", me);
        }
      }

      try {
        db = jdo.getDatabase();
      } catch (DatabaseNotFoundException dbnfe) {
        throw new WorkflowException("JDOManager.getDatabase",
            "EX_ERR_CASTOR_DATABASE_NOT_FOUND", dbnfe);
      } catch (PersistenceException pe) {
        throw new WorkflowException("JDOManager.getDatabase",
            "EX_ERR_CASTOR_GET_DATABASE", pe);
      }
    }

    return db;
  }

  static public void closeDatabase(Database db) {
    if (db != null) {
      try {
        db.close();
      } catch (PersistenceException pe) {
        SilverTrace.warn("workflowEngine", "JDOManager.closeDatabase",
            "root.EX_CONNECTION_CLOSE_FAILED", pe);
      }
    }
  }
}