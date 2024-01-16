/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core;

import java.text.MessageFormat;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

/**
 * It defines the patterns of the common messages to pass with Silverpeas exceptions when an error
 * is occurring during a treatment in Silverpeas.
 * @author mmoquillon
 */
public class SilverpeasExceptionMessages {

  private static final MessageFormat ADDING_FAILURE = new MessageFormat("Fail to add {0} {1}");
  private static final MessageFormat UPDATE_FAILURE = new MessageFormat("Fail to update {0} {1}");
  private static final MessageFormat DELETION_FAILURE = new MessageFormat("Fail to delete {0} {1}");
  private static final MessageFormat REMOVE_FAILURE = new MessageFormat("Fail to remove {0} {1}");
  private static final MessageFormat GETTING_FAILURE = new MessageFormat("Fail to get {0} {1}");
  private static final MessageFormat MOVE_FAILURE =
      new MessageFormat("Fail to move {0} {1} to {2} {3}");
  private static final MessageFormat RESTORATION_FAILURE =
      new MessageFormat("Fail to restore {0} {1}");
  private static final MessageFormat VALIDATION_FAILURE =
      new MessageFormat("Fail to validate {0} {1}");

  private static final MessageFormat INDEXATION_FAILURE =
      new MessageFormat("Fail to index {0} {1}");
  private static final MessageFormat UNINDEXATION_FAILURE =
      new MessageFormat("Fail to unindex {0} {1}");

  private static final MessageFormat CONNECION_OPENING_FAILURE =
      new MessageFormat("Fail to open connection to {0}");
  private static final MessageFormat CONNECION_CLOSING_FAILURE =
      new MessageFormat("Fail to close connection to {0}");
  private static final MessageFormat CONNECTION_FAILURE =
      new MessageFormat("Fail to connect/access {0}");

  private static final MessageFormat UNAVAILABLE_RESOURCE = new MessageFormat("{0} {1} not found");
  private static final MessageFormat UNDEFINED_RESOURCE =
      new MessageFormat("{0} identifier not defined");

  private static final MessageFormat RENDERING_FAILURE =
      new MessageFormat("Fail to render {0} {1}");

  private static final MessageFormat OPENING_FILE_FAILURE =
      new MessageFormat("Fail to open file {0}");
  private static final MessageFormat CLOSING_FILE_FAILURE =
      new MessageFormat("Fail to close file {0}");

  private static final MessageFormat SUBSCRIPTION_FAILURE =
      new MessageFormat("Fail to subscribe to {0} {1}");
  private static final MessageFormat UNSUBSCRIPTION_FAILURE =
      new MessageFormat("Fail to unsubscribe to {0} {1}");


  private SilverpeasExceptionMessages() {

  }


  /**
   * Computes a message about the failure to add a given resource with the specified
   * identifier.
   * <p>
   * This message is for when an exception has been caught during the adding of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnAdding(String resource, Object id) {
    return ADDING_FAILURE.format(new Object[]{resource, id == null ? "" : id});
  }

  /**
   * Computes a message about the failure to update a given resource with the specified
   * identifier.
   * <p>
   * This message is for when an exception has been caught during the update of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnUpdate(String resource, Object id) {
    return UPDATE_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to remove a given resource with the specified
   * identifier.
   * <p>
   * This message is for when an exception has been caught during the remove of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnRemoving(String resource, Object id) {
    return REMOVE_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to delete a given resource with the specified
   * identifier.
   * <p>
   * This message is for when an exception has been caught during the deletion of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnDeleting(String resource, Object id) {
    return DELETION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to move a given resource with the specified
   * identifier to the specified target with the specified identifier.
   * <p>
   * This message is for when an exception has been caught during the move of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @param target a resource into which the resource is moved (a bin, a folder, a space, ...)
   * @param targetId an identifier of the target (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnMoving(String resource, Object id, String target, Object targetId) {
    return MOVE_FAILURE.format(new Object[]{resource, id, target, targetId});
  }

  /**
   * Computes a message about the failure to restore a given resource with the specified
   * identifier.
   * <p>
   * This message is for when an exception has been caught during the restoration of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnRestoring(String resource, Object id) {
    return RESTORATION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to get a given resource with the specified
   * identifier.
   * <p>
   * This message is for when an exception has been caught during the getting of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnGetting(String resource, Object id) {
    return GETTING_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to open a connection against a given service.
   * <p>
   * This message is for when an exception has been caught while opening a connection to the
   * given service.
   * </p>
   * @param service the service concerned by the connection.
   * @return an exception message.
   */
  public static String failureOnOpeningConnectionTo(String service) {
    return CONNECION_OPENING_FAILURE.format(service);
  }

  /**
   * Computes a message about the failure to close a connection with a given service.
   * <p>
   * This message is for when an exception has been caught while closing a connection with the
   * given service.
   * </p>
   * @param service the service concerned by the connection.
   * @return an exception message.
   */
  public static String failureOnClosingConnectionTo(String service) {
    return CONNECION_CLOSING_FAILURE.format(service);
  }

  /**
   * Computes a message about the failure of a connection with a given service.
   * <p>
   * This message is for when an exception has been caught while connecting or accessing an
   * external service.
   * </p>
   * @param service the service concerned by the connection.
   * @return an exception message.
   */
  public static String failureOnConnecting(String service) {
    return CONNECTION_FAILURE.format(service);
  }

  /**
   * Computes a message about the failure to validate the correctness of a given resource with the
   * specified identifier.
   * <p>
   * This message is for when an exception has been caught during the validation of the correctness
   * of a resource in Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnValidating(String resource, Object id) {
    return VALIDATION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to index the given resource with the
   * specified identifier.
   * <p>
   * This message is for when an exception has been caught during the indexation of a resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnIndexing(String resource, Object id) {
    return INDEXATION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to unindex the given resource with the
   * specified identifier.
   * <p>
   * This message is for when an exception has been caught during the deletion on the index
   * of a resource in Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnUnindexing(String resource, Object id) {
    return UNINDEXATION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to render the given resource with the
   * specified identifier.
   * <p>
   * This message is for when an exception has been caught during the rendering of resource in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String failureOnRendering(String resource, Object id) {
    return RENDERING_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure to open the given resource with the
   * specified identifier.
   * <p>
   * This message is for when an exception has been caught during the opening of file in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @return an exception message.
   */
  public static String failureOnOpeningFile(String resource) {
    return OPENING_FILE_FAILURE.format(new Object[]{resource});
  }

  /**
   * Computes a message about the failure to close the given resource with the
   * specified identifier.
   * <p>
   * This message is for when an exception has been caught during the closing of file in
   * Silverpeas.
   * </p>
   * @param resource the resource concerned by the failure; for example a user, a file, ...
   * @return an exception message.
   */
  public static String failureOnClosingFile(String resource) {
    return CLOSING_FILE_FAILURE.format(new Object[]{resource});
  }

  /**
   * Computes a message about the failure on the subscription of a user to a given resource.
   * @param resource the resource concerned by the subscription attempt.
   * @param id the unique identifier of the resource.
   * @return an exception message.
   */
  public static String failureOnSubscribing(String resource, String id) {
    return SUBSCRIPTION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about the failure on the subscription cancelling of a user to a given
   * resource.
   * @param resource the resource concerned by the unsubscription attempt.
   * @param id the unique identifier of the resource.
   * @return an exception message.
   */
  public static String failureOnUnsubscribing(String resource, String id) {
    return UNSUBSCRIPTION_FAILURE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about an unknown resource with the specified identifier.
   * <p>
   * Usually, this message is passed into an exception when the resource required to perform a
   * treatment doesn't exist or isn't found in a peculiar context. It's not necessary resulted from
   * an exception (in that case, we prefer {@code failureOnGetting} message).
   * </p>
   * @param resource the resource concerned by the issue; for example a user, a file, ...
   * @param id an identifier of the resource (a name, a unique identifier, ...)
   * @return an exception message.
   */
  public static String unknown(String resource, Object id) {
    return UNAVAILABLE_RESOURCE.format(new Object[]{resource, id});
  }

  /**
   * Computes a message about an asked undefined resource. A resource is undefined when its
   * identifier is either empty or null.
   * @param resource the resource concerned by the issue; for example a user, a file, ...
   * @return an exception message.
   */
  public static String undefined(String resource) {
    return UNDEFINED_RESOURCE.format(new Object[]{resource});
  }

  /**
   * In charge to produce lighter error messages but with a bit of contextualization.
   */
  public static class LightExceptionMessage {
    private final Class<?> source;
    private final Exception exception;

    public LightExceptionMessage(final Object source, final Exception exception) {
      this(source.getClass(), exception);
    }

    public LightExceptionMessage(final Class<?> source, final Exception exception) {
      this.source = source;
      this.exception = exception;
    }

    /**
     * Merges the given message with the context in order to produce an error on a single line.
     * @param message a functional error message to merge with the context.
     * @return a string representing the merged message.
     */
    public String singleLineWith(final String message) {
      final String currentClass = source.getName();
      return format("{0} -> {1}", message, Stream.of(exception.getStackTrace())
          .filter(s -> s.getClassName().equals(currentClass))
          .findFirst()
          .orElseGet(() -> exception.getStackTrace()[0]));
    }
  }
}
  