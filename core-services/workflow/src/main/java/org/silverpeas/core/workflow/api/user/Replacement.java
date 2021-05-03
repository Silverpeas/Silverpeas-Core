/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import java.util.Objects;
import java.util.Optional;

/**
 * A replacement of a user by another one in a given period of time to exercise the
 * responsibilities of the former for all the tasks in which he's implied. A given task could be
 * performed by the substitute if and only if he does already play the role required by the task; in
 * other terms the substitute and the incumbent must plays the same role(s) otherwise the
 * substitute will be unable to exercise the responsibilities of the incumbent.
 * @author mmoquillon
 */
public interface Replacement<T extends Replacement<T>> extends Entity<T, UuidIdentifier> {

  /**
   * Gets the replacement with the specified unique identifier. Each replacement is unique, whatever
   * the workflow in which they were created.
   * @param replacementId the unique identifier of a replacement.
   * @return optionally the replacement with the specified identifier. Nothing if no such
   * replacement exists.
   */
  static <T extends Replacement<T>> Optional<T> get(final String replacementId) {
    Repository repository = ServiceProvider.getSingleton(Repository.class);
    return Optional.ofNullable(repository.findById(replacementId));
  }

  /**
   * Gets all the replacements of the specified user in the specified workflow instance.
   * @param incumbent the user for which the replacements were constructed and persisted.
   * @param workflowInstanceId the unique identifier of a workflow instance.
   * @param <T> the class implementing the {@link Replacement} interface.
   * @return a list of replacements. If no such replacements exist, then an empty list is returned.
   */
  static <T extends Replacement<T>> ReplacementList<T> getAllOf(final User incumbent,
      final String workflowInstanceId) {
    Repository repository = ServiceProvider.getSingleton(Repository.class);
    List<T> replacements = repository.findAllByIncumbentAndByWorkflow(incumbent, workflowInstanceId);
    return new ReplacementList<>(replacements);
  }

  /**
   * Gets all the replacements exercised by the specified user in the specified workflow instance.
   * @param substitute the user exercising the replacements.
   * @param workflowInstanceId the unique identifier of a workflow instance.
   * @param <T> the class implementing the {@link Replacement} interface.
   * @return a list of replacements. If no such replacements exist, then an empty list is returned.
   */
  static <T extends Replacement<T>> ReplacementList<T> getAllBy(final User substitute,
      final String workflowInstanceId) {
    Repository repository = ServiceProvider.getSingleton(Repository.class);
    List<T> replacements = repository.findAllBySubstituteAndByWorkflow(substitute, workflowInstanceId);
    return new ReplacementList<>(replacements);
  }

  /**
   * Gets all the replacements in which the specified users are implied and that are defined in the
   * specified workflow instance.
   * @param incumbent the user to replace.
   * @param substitute the user exercising the replacements.
   * @param workflowInstanceId the unique identifier of a workflow instance.
   * @param <T> the class implementing the {@link Replacement} interface.
   * @return a list of replacements. If no such replacements exist, then an empty list is returned.
   */
  static <T extends Replacement<T>> ReplacementList<T> getAllWith(final User incumbent, final User substitute,
      final String workflowInstanceId) {
    Repository repository = ServiceProvider.getSingleton(Repository.class);
    List<T> replacements = repository.findAllByUsersAndByWorkflow(incumbent, substitute, workflowInstanceId);
    return new ReplacementList<>(replacements);
  }

  /**
   * Gets all the replacements that are defined in the specified workflow instance.
   * @param workflowInstanceId the unique identifier of a workflow instance.
   * @param <T> the class implementing the {@link Replacement} interface.
   * @return a list of all the replacements in the specified workflow instance. If no
   * replacements exist, then an empty list is returned.
   */
  static <T extends Replacement<T>> ReplacementList<T> getAll(final String workflowInstanceId) {
    Repository repository = ServiceProvider.getSingleton(Repository.class);
    List<T> replacements = repository.findAllByWorkflow(workflowInstanceId);
    return new ReplacementList<>(replacements);
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
   * Sets a new substitute to this replacement.
   * @param user the user that will replace the incumbent of some tasks
   * @return itself.
   */
  Replacement<T> setSubstitute(final User user);

  /**
   * Gets the period in days over which this replacement is enabled. The start and end dates of the
   * period are {@link java.time.LocalDate} instances.
   * @return a period in days.
   */
  Period getPeriod();

  /**
   * Sets a new period in days over which this replacement will be enabled. The start date and end
   * date of the period should be {@link java.time.LocalDate} instances.
   * @param period the new period of this replacement.
   * @return itself.
   */
  Replacement<T> setPeriod(final Period period);

  /**
   * Gets the unique identifier of the instance of the workflow in which this replacement is done.
   * @return the unique identifier of a workflow instance.
   */
  String getWorkflowInstanceId();

  /**
   * Saves or updates this replacement within the persistence context. A unique identifier aver
   * all of the workflow instances will be attributed to the newly saved replacement. This
   * identifier can then be used to retrieve later this replacement among its counterparts.
   * @return the saved or updated replacement.
   */
  default T save() {
    return Transaction.performInOne(() -> {
      Repository repository = ServiceProvider.getSingleton(Repository.class);
      return repository.save(this);
    });
  }

  /**
   * Deletes this replacement in the persistence context. If the replacement isn't persisted,
   * nothing is done.
   */
  default void delete() {
    Transaction.performInOne(() -> {
      Repository repository = ServiceProvider.getSingleton(Repository.class);
      repository.delete(this);
      return null;
    });
  }

  /**
   * Is this replacement same as the specified one?
   * <p>
   * This method differs from equality as they don't compare the same thing: the {@code equals}
   * method in Java is a comparator by identity, meaning two objects are compared by their unique
   * identifier (either by their OID for non-persistent object or by their persistence identifier
   * for persistent object). The {@code isSameAs} method is a comparator by value, meaning two
   * objects are compared by their state; so two equal objects (that is referring to a same
   * object) can be different by their state: one representing a given state of the referred object
   * whereas the other represents another state of the referred object.
   * </p>
   * @param replacement the attendees to compare with.
   * @return true if the given replacement is the same has the current one.
   */
  default boolean isSameAs(Replacement<T> replacement) {
    return Objects.equals(getIncumbent().getUserId(), replacement.getIncumbent().getUserId()) &&
        Objects.equals(getSubstitute().getUserId(), replacement.getSubstitute().getUserId()) &&
        Objects.equals(getPeriod(), replacement.getPeriod());
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
     * Sets the period of time the replacement will be performed and constructs it. The period is
     * expressed in days; the start and end date are then {@link java.time.LocalDate} instances.
     * @param period a period of time.
     * @return the constructed replacement.
     */
    <T extends Replacement<T>> T during(final Period period);
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
    <T extends Replacement<T>> T save(final Replacement<T> replacement);

    /**
     * Deletes the specified replacement in this repository. If the replacement doesn't exist in
     * the repository, nothing is done.
     * @param replacement the replacement to delete.
     */
    <T extends Replacement<T>> void delete(final Replacement<T> replacement);

    /**
     * Finds all the replacements by the specified replaced user and by the workflow instance
     * identifier.
     * @param user a user in the workflow instance.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @param <T> the class implementing the {@link Replacement} interface.
     * @return a list of all persisted replacements of the specified user. If no
     * replacements were set for the specified user in the given workflow instance, then an empty
     * list is returned.
     */
    <T extends Replacement<T>> List<T> findAllByIncumbentAndByWorkflow(final User user, final String workflowInstanceId);

    /**
     * Finds all the replacements by the specified substitute and by the workflow instance
     * identifier.
     * @param user a user in the workflow instance.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @param <T> the class implementing the {@link Replacement} interface.
     * @return a list of all persisted replacements that will be done by the specified user. If no
     * replacements were set with the specified user in the given workflow instance, then an empty
     * list is returned.
     */
    <T extends Replacement<T>> List<T> findAllBySubstituteAndByWorkflow(final User user, final String workflowInstanceId);

    /**
     * Finds all the replacements created in the specified workflow instance.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @param <T> the class implementing the {@link Replacement} interface.
     * @return a list of all persisted replacements that were created in the specified workflow
     * instance. If no replacements were set in the given workflow instance, then an empty list
     * is returned.
     */
    <T extends Replacement<T>> List<T> findAllByWorkflow(final String workflowInstanceId);

    /**
     * Finds all the replacements between the two specified users and created in the specified
     * workflow instance.
     * @param incumbent the incumbent in the replacements to get.
     * @param substitute the substitute in the replacements to get.
     * @param workflowInstanceId the unique identifier of a workflow instance.
     * @param <T> the class implementing the {@link Replacement} interface.
     * @return a list of all persisted replacements that were created in the specified workflow
     * instance. If no replacements were set in the given workflow instance, then an empty list
     * is returned.
     */
    <T extends Replacement<T>> List<T> findAllByUsersAndByWorkflow(final User incumbent,
        final User substitute, final String workflowInstanceId);

    /**
     * Gets the replacement with the specified unique identifier. The identifier of a replacement
     * is unique among all over the workflow instances.
     * @param replacementId the unique identifier of a replacement among all of the available
     * workflow instances.
     * @return a {@link Replacement} object or null if no such replacement exists with the specified
     * unique identifier.
     */
    <T extends Replacement<T>> T findById(final String replacementId);
  }
}
