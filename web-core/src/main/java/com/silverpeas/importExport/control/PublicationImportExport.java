/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.silverpeas.util.MSdocumentPropertiesManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.coordinates.model.CoordinateRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import org.apache.poi.hpsf.SummaryInformation;

import java.io.File;
import java.util.Date;
import java.util.List;

public class PublicationImportExport {

  private PublicationImportExport() {
  }

  /**
   * Méthodes permettant de récupérer un objet publication dont les méta-données sont générées à
   * partir des informations du fichier destiné à être attaché à celle ci. Utilisation de l'api POI
   * dans le cas des fichiers MSoffice.
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport
   * @param file - fichier destiné à être attaché à la publication d'où l'on extrait les
   * informations qui iront renseigner les méta-données de la publication à creer
   * @return renvoie un objet PublicationDetail
   */
  public static PublicationDetail convertFileInfoToPublicationDetail(UserDetail userDetail,
      File file, boolean isPOIUsed) {

    // For reading the properties in an Office document
    MSdocumentPropertiesManager MSdpManager = new MSdocumentPropertiesManager();

    PublicationDetail pubDetail = null;
    SummaryInformation si = null;
    String nomPub = null;
    String description = null;
    String motsClefs = null;
    String content = null;
    String fileName = file.getName();
    String poiTitle = null;
    String poiSubject = null;
    String poiKeywords = null;
    if (isPOIUsed) {
      try {
        si = MSdpManager.getSummaryInformation(file.getAbsolutePath());
        poiTitle = si.getTitle();
        poiSubject = si.getSubject();
        poiKeywords = si.getKeywords();
        nomPub = ((poiTitle == null) || (poiTitle.trim().length() == 0) ? fileName : poiTitle);// si
        // le
        // champs
        // corespondant
        // est
        // vide,
        // on
        // affecte
        // le
        // nom
        // physique
        // du
        // fichier
        description = ((poiSubject == null) || (poiSubject.trim().length() == 0) ? "" : poiSubject);
        motsClefs =
            ((poiKeywords == null) || (poiKeywords.trim().length() == 0) ? "" : poiKeywords);
        content = "fichier(s) importé(s)";
      } catch (Exception ex) {
        // on estime que l'exception est dû au fait que nous ne sommes pas en présence d'un
        // fichier OLE2 (office)
        nomPub = fileName;
        description = nomPub;
        motsClefs = nomPub;
        content = nomPub;
      }
    } else {
      nomPub = fileName;
      description = "";
      motsClefs = "";
      content = "";
    }
    pubDetail =
        new PublicationDetail("unknown"/* id */, nomPub/* nom */, description/* description */,
        new Date()/* date de création */, new Date()/* date de début de validité */,
        null/* date de fin de validité */, userDetail.getId()/* id user */,
        "5"/* importance */, null/* version de la publication */, motsClefs/* keywords */,
        content);
    return pubDetail;
  }

  /**
   * Add nodes (coordinatesId) to a publication
   * @param PublicationDetail , List of coordinateId
   * @return nothing
   */
  public static void addNodesToPublication(PublicationPK pubPK, List nodes) {
    try {
      for (Object node : nodes) {
        Integer coordinateId = (Integer) node;
        getPublicationBm().addFather(pubPK, new NodePK(coordinateId.toString(),
            pubPK));
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException("CoordinateImportExport.addNodesToPublication()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.ATTACHING_NODES_TO_PUBLICATION_FAILED", e);
    }
  }

  /**
   * @return l'EJB PublicationBm
   * @throws CoordinateRuntimeException
   */
  private static PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
          PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException("ImportExport.getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return publicationBm;
  }

  /**
   * Get unbalanced publications
   * @param componentId
   * @return ArrayList of publicationDetail
   */
  public static List<PublicationDetail> getUnbalancedPublications(String componentId) {
    PublicationPK pk = new PublicationPK("useless", componentId);
    List<PublicationDetail> publications = null;
    try {
      publications = (List<PublicationDetail>) getPublicationBm().getOrphanPublications(pk);
    } catch (Exception e) {
      throw new PublicationRuntimeException("CoordinateImportExport.getUnbalancedPublications()",
          SilverpeasRuntimeException.ERROR,
          "importExport.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_PUBLICATIONS_NON_CLASSEES", e);
    }
    return publications;
  }
}
