package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.user.model.SilverpeasRole;

import java.util.stream.Stream;

/**
 * The predefined roles in a space to use in an inheritance of access rights.
 * @author mmoquillon
 */
public final class InheritableSpaceRoles {

  public static final SilverpeasRole ADMIN = SilverpeasRole.ADMIN;
  public static final SilverpeasRole PUBLISHER = SilverpeasRole.PUBLISHER;
  public static final SilverpeasRole WRITER = SilverpeasRole.WRITER;
  public static final SilverpeasRole READER = SilverpeasRole.READER;

  /**
   * Gets all the roles in a space that can be inherited by a component instance in that space.
   * @return an array with all inheritable space roles.
   */
  public static SilverpeasRole[] getAllRoles() {
    return new SilverpeasRole[]{ADMIN, PUBLISHER, WRITER, READER};
  }

  /**
   * Is the specified user role is a predefined space role.
   * @param role a role a user can play in Silverpeas.
   * @return true if the given role is supported by a collaborative space.
   */
  public static boolean isASpaceRole(SilverpeasRole role) {
    return Stream.of(getAllRoles()).anyMatch(r -> r == role);
  }

  private InheritableSpaceRoles() {
  }
}
