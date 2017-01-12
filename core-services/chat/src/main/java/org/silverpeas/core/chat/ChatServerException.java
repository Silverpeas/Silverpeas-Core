package org.silverpeas.core.chat;

import org.silverpeas.core.SilverpeasRuntimeException;

/**
 * Thrown when an error need external intervention to be fixed
 *
 * @author remipassmoilesel
 */
public class ChatServerException extends SilverpeasRuntimeException {

  /**
   * Error while creating user
   */
  public static final String USER_CREATION_FAIL = "USER_CREATION_FAIL";
  /**
   * Error while deleting user
   */
  public static final String USER_DELETION_FAIL = "USER_DELETION_FAIL";

  /**
   * Error while accessing the profile of the user.
   */
  public static final String USER_ACCESS_FAIL = "USER_ACCESS_FAIL";

  /**
   * Relationship already exist
   */
  public static final String RELATIONSHIP_ALREADY_EXIST = "RELATIONSHIP_ALREADY_EXIST";
  /**
   * Error while creating relationship
   */
  public static final String RELATIONSHIP_CREATION_FAIL = "RELATIONSHIP_CREATION_FAIL";
  /**
   * Error while deleting relationship
   */
  public static final String RELATIONSHIP_DELETION_FAIL = "RELATIONSHIP_DELETION_FAIL";

  public ChatServerException(final String message) {
    super(message);
  }

  public ChatServerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ChatServerException(final Throwable cause) {
    super(cause);
  }
}
