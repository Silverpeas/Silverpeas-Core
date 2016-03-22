/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package org.silverpeas.look.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.look.web.LookResourceURIs.DISPLAY_BASE_URI;

import javax.ws.rs.Path;

import org.silverpeas.core.webapi.look.delegate.LookWebDelegate;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.look.LookHelper;

/**
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(DISPLAY_BASE_URI)
@Authenticated
public class DisplayResourceMock extends org.silverpeas.look.web.DisplayResource {

  LookWebDelegate lookWebServiceMock = null;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.AbstractAdminResource#getLookServices()
   */
  @Override
  protected LookWebDelegate getLookDelegate() {
    if (lookWebServiceMock == null) {
      lookWebServiceMock = mock(LookWebDelegate.class);

      // getHelper
      when(lookWebServiceMock.getHelper()).thenReturn(mock(LookHelper.class));
    }
    return lookWebServiceMock;
  }
}
