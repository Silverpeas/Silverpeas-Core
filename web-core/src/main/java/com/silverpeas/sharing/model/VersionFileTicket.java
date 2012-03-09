/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import java.rmi.RemoteException;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Ticket for files with versions.
 */
@Entity
@DiscriminatorValue("Versionned")
public class VersionFileTicket extends Ticket {

  private static final long serialVersionUID = 1L;

  public Document getDocument() {
    try {
      return new VersioningUtil().getDocument(new DocumentPK((int) getSharedObjectId(),
          getComponentId()));
    } catch (RemoteException e) {
      SilverTrace.error("fileSharing", "Ticket.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }
}
