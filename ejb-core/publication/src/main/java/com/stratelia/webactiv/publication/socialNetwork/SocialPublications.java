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
package com.stratelia.webactiv.publication.socialNetwork;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.provider.SocialPublicationsInterface;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmEJB;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationWithStatus;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocialPublications implements SocialPublicationsInterface  {
  private static int firstIndex = 0;
  
  /**
   * @param user
   * @return the list of publications that the user has been created or updated
   * @see UserDetail
   * @see SocialInformation
   * @see SocialInformationPublication
   **/



  @Override
  public List getSocialInformationsList(String userId , int limit ,int offset) throws SilverpeasException {
   
     List<SocialInformationPublication> publications = null ;
     //limit==nbElements
     //offset=firstIndex
    try {
      publications = getEJB().
          getAllPublicationsWithStatusbyUserid(userId, offset, limit);
    

    } catch (RemoteException ex) {
      Logger.getLogger(SocialPublications.class.getName()).log(Level.SEVERE, null, ex);
    }
    return publications;
  }

 private PublicationBm getEJB() {
    PublicationBm currentPublicationBm = null;
    try {
      PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
          PublicationBmHome.class);
      currentPublicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationHome()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          e);
    }
    return currentPublicationBm;
  }
}
