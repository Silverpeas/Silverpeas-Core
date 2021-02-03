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
