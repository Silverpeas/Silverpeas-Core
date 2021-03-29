package org.silverpeas.core.webapi.base;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;

/**
 * Validation of the authorization of a user to access a web endpoint in Silverpeas. This interface
 * requires to be implemented by all of authorization validators in Silverpeas. The validation of
 * the authorization can be only performed against authenticated users.
 * @author mmoquillon
 */
public interface WebAuthorizationValidation {

  /**
   * Gets the context of Silverpeas linked to the current request. This context must be initialized
   * before the functional request processing.
   * @return {@link SilverpeasRequestContext} instance.
   */
  SilverpeasRequestContext getSilverpeasContext();

  /**
   * Validates the authorization of the user to request the web endpoint referred by the Silverpeas
   * context by using the specified user privilege validation service. By default, it checks only
   * the user has a valid account in Silverpeas. Further or more precise authorization treatments
   * are delegated to implementors.
   * <p>
   * This method should be invoked for web services requiring an authorized user to access them.
   * Otherwise, the annotation {@link org.silverpeas.core.webapi.base.annotation.Authorized} can be
   * also used instead at class level.
   * </p>
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the authorization isn't valid (no authorized or
   * authorization failure).
   * @see UserPrivilegeValidator
   */
  default void validateUserAuthorization(final UserPrivilegeValidation validation) {
    final SilverpeasRequestContext context = getSilverpeasContext();
    final User user = context.getUser();
    if (user == null) {
      SilverLogger.getLogger(this)
          .warn("Authorization validation invoked against a non authenticated user!");
      throw new NotAuthorizedException("User no authenticated");
    }
    if (!user.isValidState()) {
      throw new ForbiddenException(
          "User account isn't valid: account " + user.getState().getName());
    }
  }
}
