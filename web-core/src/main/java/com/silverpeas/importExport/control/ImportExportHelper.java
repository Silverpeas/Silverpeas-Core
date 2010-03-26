/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.importExport.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author neysseri
 */
public class ImportExportHelper {

  public static boolean isVersioningUsed(ComponentInst componentInst) {
    String versioningParam = componentInst.getParameterValue("versionControl");
    if (versioningParam != null && !("").equals(versioningParam)
        && !("no").equals(versioningParam.toLowerCase()))
      return true;
    else
      return false;
  }

  public static boolean isDraftUsed(ComponentInst componentInst) {
    String param = componentInst.getParameterValue("draft");
    return (StringUtil.isDefined(param) && !("no").equals(param.toLowerCase()));
  }

  public static boolean isIndexable(PublicationDetail pubDetail) {
    return pubDetail.isIndexable();
  }
}
