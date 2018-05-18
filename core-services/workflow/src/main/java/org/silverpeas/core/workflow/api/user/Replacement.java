/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.api.user;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * A replacement of a user by another one in a given period of time to exercise the
 * responsibilities of the former for all the tasks in which he's implied. A given task could be
 * performed by the substitute if and only if he holds already the role required by the task; in
 * other terms the substitute and the incumbent must plays the same role(s) otherwise the
 * substitute will be unable to exercise the responsibilities of the incumbent.
 * @author mmoquillon
 */
public interface Replacement<T extends Replacement<T>> extends Entity<T, UuidIdentifier> {

  /**
   * Gets all the replacements of the specified user in the specified workflow instance.
   * @param incumbent the user for which the replacements were constructed and persisted.
   * @param workflowInstanceId the unique identifier of a workflow instance.
   * @return a list of replacements. If no such replacements exist, then an empty list is returned.
   */
  @SuppressWarnings("unchecked")
  static List<Replacement> getAllOf(final User incumbent, final String workflowInstanceId) {
    Repository repository = ServiceProvider.getService(Repository.class);
    return repository.findAllByIncumbentAndByWorkflow(incumbent, workflowInstanceId);
  }

  /**
   * Gets all the replacements exercised by the specified user in the specified workflow instance.
   * @param substitute the user exercising the replacements.
   * @param workflowInstanceId the unique identifier of a workflow instance.
   * @return a list of replacements. If no such replacements exist, then an empty list is returned.
   */
  @SuppressWarnings("unchecked")
  static List<Replacement> getAllBy(final User substitute, final String workflowInstanceId) {
    Repository repository = ServiceProvider.getService(Repository.class);
    return repository.findAllBySubstituteAndByWorkflow(substitute, workflowInstanceId);
  }

  /**
   * Prepares the construction of a replacement between the two specified users.
   * @param incumbent the user that have to be replaced.
   * @param substitute the user that will replace the former.
   * @return a constructor of replacements.
   */
  static Constructor between(final User incumbent, final User substitute) {
    Constructor constructor = ServiceProvider.getService(Constructor.class);
    return constructor.between(incumbent, substitute);
  }

  /**
   * Gets the incumbent of responsibilities of tasks that is replaced in a given period of time.
   * @return the user that is replaced.
   */
  User getIncumbent();

  /**
   * Gets the substitute of the incumbent to exercise his responsibilities on some tasks.
   * @return the user to whom this replacement was done.
   */
  User getSubstitute();

  /**
   * Gets the period over which this replacement is enabled.
   * @return a period of time
   */
  Period getPeriod();

  /**
   * Gets the unique identifier of the instance of the workflow in which this replacement is done.
   * @return the unique identifier of a workflow instance.
   */
  String getWorkflowInstanceId();

  /**
   * Saves or updates this replacement within the persistence context.
   * @return the saved or updated replacement.
   */
  @SuppressWarnings("unchecked")
  default Replacement save() {
    return Transaction.performInOne(() -> {
      Repository repository = ServiceProvider.getService(Repository.class);
      return repository.save(this);
    });
  }

  /**
   * Deletes this replacement in the persistence context. If the replacement isn't persisted,
   * nothing is done.
   */
  @SuppressWarnings("unchecked")
  default void delete() {
    Transaction.performInOne(() -> {
      Repository repository = ServiceProvider.getService(Repository.class);
      repository.delete(this);
      return null;
    });
  }

  /**
   * Constructor of a replacement between two users in a given workflow instance and over a given
   * period of time.
   */
  interface Constructor {
    /**
     * Sets the two users concerned by the replacement to construct.
     * @param incumbent the user that is replaced.
     * @param substitute the user that replaces the former.
     * @return itself.
     */
    Constructor between(final User incumbent, final User substitute);

    /**
     * Sets the workflow instance concerned by the replacement to construct.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @return itself.
     */
    Constructor inWorkflow(final String workflowInstanceId);

    /**
     * Sets the period of time the replacement will be performed and constructs it.
     * @param period a period of time.
     * @return the constructed replacement.
     */
    <T extends Replacement> T during(final Period period);
  }

  /**
   * Repository storing the replacements and backing the actual used data source of such
   * replacements. This repository shouldn't be used directly; it is dedicated to the
   * {@link Replacement} objects for use.
   */
  interface Repository {

    /**
     * Saves or updates the specified replacement into this repository.
     * @param replacement the replacement to persist.
     * @return the persisted replacement.
     */
    Replacement save(final Replacement replacement);

    /**
     * Deletes the specified replacement in this repository. If the replacement doesn't exist in
     * the repository, nothing is done.
     * @param replacement the replacement to delete.
     */
    void delete(final Replacement replacement);

    /**
     * Finds all the replacements by the specified replaced user and by the workflow instance
     * identifier.
     * @param user a user in the workflow instance.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @return a list of all persisted replacements of the specified user. If no
     * replacements were set for the specified user in the given workflow instance, then an empty
     * list is returned.
     */
    <T extends Replacement> List<T> findAllByIncumbentAndByWorkflow(final User user, final String workflowInstanceId);

    /**
     * Finds all the replacements by the specified substitute and by the workflow instance
     * identifier.
     * @param user a user in the workflow instance.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @return a list of all persisted replacements that will be done by the specified user. If no
     * replacements were set with the specified user in the given workflow instance, then an empty
     * list is returned.
     */
    <T extends Replacement> List<T> findAllBySubstituteAndByWorkflow(final User user, final String workflowInstanceId);
  }
}
