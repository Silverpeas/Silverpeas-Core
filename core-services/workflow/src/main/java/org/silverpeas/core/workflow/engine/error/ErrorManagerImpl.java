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

package org.silverpeas.core.workflow.engine.error;

import java.util.Vector;

import org.silverpeas.core.workflow.api.ErrorManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.error.WorkflowError;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.engine.jdo.WorkflowJDOManager;
import org.silverpeas.core.silvertrace.SilverTrace;

import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;

/* Castor library */
import org.exolab.castor.jdo.*;

import javax.inject.Singleton;

/**
 * The workflow engine services relate to error management.
 */
@Singleton
public class ErrorManagerImpl implements ErrorManager {
  /**
   * Save an error
   */
  public WorkflowError saveError(ProcessInstance instance, GenericEvent event,
      HistoryStep step, Exception exception) {
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // build workflowError object
      WorkflowErrorImpl error = new WorkflowErrorImpl(instance, event, step,
          exception);

      // Make error persistent
      db.create(error);

      // Commit transaction
      db.commit();

      return error;
    } catch (WorkflowException we) {
      SilverTrace.warn("workflowEngine", "ErrorManagerImpl",
          "workflowEngine.EX_PROBLEM_SAVE_ERROR", we);
      return null;
    } catch (PersistenceException pe) {
      SilverTrace.warn("workflowEngine", "ErrorManagerImpl",
          "workflowEngine.EX_PROBLEM_SAVE_ERROR", pe);
      return null;
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Get all the errors that occured for a given instance
   */
  public WorkflowError[] getErrorsOfInstance(String instanceId) {
    Database db = null;
    OQLQuery query;
    QueryResults results;
    Vector<WorkflowError> errors = new Vector<>();

    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase(true);
      db.begin();

      query =
          db
              .getOQLQuery("SELECT error FROM WorkflowErrorImpl error"
                  + " WHERE instanceId = $1");

      // Execute the query
      query.bind(instanceId);
      results = query.execute(org.exolab.castor.jdo.Database.READONLY);

      // get the instance if any
      while (results.hasMore()) {
        WorkflowError error = (WorkflowError) results.next();
        errors.add(error);
      }

      db.commit();

      return errors.toArray(new WorkflowError[errors.size()]);
    } catch (PersistenceException pe) {
      SilverTrace.warn("workflowEngine", "ErrorManagerImpl",
          "workflowEngine.EX_PROBLEM_GETTING_ERRORS", pe);
      return new WorkflowError[0];
    } catch (WorkflowException we) {
      SilverTrace.warn("workflowEngine", "ErrorManagerImpl",
          "workflowEngine.EX_PROBLEM_GETTING_ERRORS", we);
      return new WorkflowError[0];
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Remove all the errors that occured for a given instance Must be called when instance is removed
   */
  public void removeErrorsOfInstance(String instanceId) {
    Database db = null;
    OQLQuery query;
    QueryResults results;

    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase(true);
      db.begin();

      query =
          db
              .getOQLQuery("SELECT error FROM WorkflowErrorImpl error"
                  + " WHERE instanceId = $1");

      // Execute the query
      query.bind(instanceId);
      results = query.execute();

      // get the instance if any
      while (results.hasMore()) {
        WorkflowErrorImpl error = (WorkflowErrorImpl) results.next();
        db.remove(error);
      }

      db.commit();
    } catch (PersistenceException | WorkflowException pe) {
      SilverTrace.warn("workflowEngine", "ErrorManagerImpl",
          "workflowEngine.EX_PROBLEM_REMOVE_ERRORS", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }
}