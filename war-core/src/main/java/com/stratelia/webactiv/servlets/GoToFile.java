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
package com.stratelia.webactiv.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.AccessForbiddenException;
import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class GoToFile extends GoTo {

  private static final long serialVersionUID = 1L;

  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    boolean isLoggedIn = isUserLogin(req);

    // Check first if attachment exists
    AttachmentDetail attachment = AttachmentController
        .searchAttachmentByPK(new AttachmentPK(objectId));
    if (attachment == null)
      return null;

    String componentId = attachment.getInstanceId();
    String foreignId = attachment.getForeignKey().getId();

    if (isLoggedIn) {
      boolean isAccessAuthorized = false;
      // L'utilisateur est déjà loggué
      if (isUserAllowed(req, componentId)) {
        // L'utilisateur a-t-il le droit de consulter le fichier/la publication
        isAccessAuthorized = true;
        if (componentId.startsWith("kmelia")) {
          try {
            ComponentSecurity security = (ComponentSecurity) Class.forName(
                "com.stratelia.webactiv.kmelia.KmeliaSecurity").newInstance();
            isAccessAuthorized = security.isAccessAuthorized(componentId,
                getUserId(req), foreignId);
          } catch (Exception e) {
            SilverTrace.error("peasUtil", "GoToFile.doPost",
                "root.EX_CLASS_NOT_INITIALIZED",
                "com.stratelia.webactiv.kmelia.KmeliaSecurity", e);
            return null;
          }
        }

        if (isAccessAuthorized) {
          // res.setContentType(attachment.getType());
          /*
           * res.setContentType("application/x-download"); res.setContentLength(new
           * Long(attachment.getSize()).intValue()); res.setHeader("Content-Disposition",
           * "inline; filename=\""+attachment.getLogicalName()+'"'); String filePath =
           * AttachmentController.createPath(componentId, "Images")+attachment.getPhysicalName();
           * write(res, filePath);
           */

          // http://localhost:8000/silverpeas/FileServer/NV_o2o_Participant_FR.exe?ComponentId=toolbox558&UserId=9&SourceFile=1175858831259.exe&MimeType=application/octet-stream&ArchiveIt=Y&PubId=1460&NodeId=0&Directory=Attachment\Images\&logicalName=NV_o2o_Participant_FR.exe
          res.sendRedirect(attachment.getAttachmentURL());
        }
      }

      if (!isAccessAuthorized)
        throw new AccessForbiddenException("GoToFile.getDestination",
            SilverpeasException.WARNING, null);

      return "useless";
    }

    return "ComponentId=" + componentId + "&AttachmentId=" + objectId + "&Mapping=File&ForeignId=" +
        foreignId;
  }
}