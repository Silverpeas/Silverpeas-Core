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
package org.silverpeas.core.webapi.admin.tools;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.silvertrace.SilverTrace;

/**
 * @author Yohann Chastagnier
 */
public class FileSharingTool extends AbstractTool {

  private Boolean isVisible = null;

  public FileSharingTool(final String language, final LookHelper lookHelper) {
    super(language, lookHelper, "fileSharingVisible", "sharingTicket", "FileSharing",
        URLUtil.CMP_FILESHARING);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.admin.tools.AbstractTool#isVisible()
   */
  @Override
  public boolean isVisible() {
    if (isVisible == null) {
      isVisible = super.isVisible();
      if (isVisible) {
        try {
          isVisible =
              !SharingServiceProvider.getSharingTicketService()
                  .getTicketsByUser(getLookHelper().getUserId()).isEmpty();
        } catch (final Exception e) {
          SilverTrace.error("admin", "FileSharingTool.isVisible", "root.CANT_GET_TICKETS", e);
          isVisible = null;
          return false;
        }
      }
    }
    return isVisible;
  }
}
