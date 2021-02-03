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

package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.webapi.base.UserPrivilegeValidator;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author silveryocha
 */

@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class StubbedUserPrivilegeValidator extends UserPrivilegeValidator {

  @Override
  public void validateUserAuthorizationOnAttachment(final HttpServletRequest request,
      final User user, final SimpleDocument doc) {
    // Nothing is checked
  }
}
