/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.web.mock;

import com.silverpeas.accesscontrol.AbstractAccessController;
import com.silverpeas.accesscontrol.AccessControlContext;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.util.Default;
import org.silverpeas.attachment.model.SimpleDocument;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author ehugonnet
 */
@Named("simpleDocumentAccessController")
@Default
public class SimpleDocumentAccessControllerMock extends AbstractAccessController<SimpleDocument> {

  @Named("componentAccessController")
  @Inject
  private AccessController<String> componentAccessController;

  private boolean authorization = true;

  public void setAuthorization(boolean isUsersAuthorized) {
    authorization = isUsersAuthorized;
  }

  @Override
  public boolean isUserAuthorized(final String userId, final SimpleDocument object,
      final AccessControlContext context) {
    return componentAccessController.isUserAuthorized(userId, null) && authorization;
  }
}