/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
/*
 * @author Norbert CHAIX
 * @version 1.0
 */
package org.silverpeas.core.admin.service;


import org.silverpeas.core.admin.user.model.GroupDetail;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnAdding;

public class GroupAlreadyExistsAdminException extends AdminException {
  private static final long serialVersionUID = -768011721788564784L;

  public GroupAlreadyExistsAdminException(final GroupDetail group) {
    super(failureOnAdding("group",
        "with specificId " + group.getSpecificId() + " and domainId " + group.getDomainId()),
        (String) null);
  }
}
