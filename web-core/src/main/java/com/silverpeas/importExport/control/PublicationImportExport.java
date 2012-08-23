/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.importExport.control;

import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PublicationImportExport {

  final static MetadataExtractor metadataExtractor = new MetadataExtractor();

  private PublicationImportExport() {
  }

  /**
   * Méthodes permettant de récupérer un objet publication dont les méta-données sont générées à
   * partir des informations du fichier destiné à être attaché à celle ci. Utilisation de l'api POI
   * dans le cas des fichiers MSoffice.
   *
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport
   * @param file - fichier destiné à être attaché à la publication d'où l'on extrait les
   * informations qui iront renseigner les méta-données de la publication à creer
   * @param isPOIUsed
   * @return renvoie un objet PublicationDetail
   */
  public static PublicationDetail convertFileInfoToPublicationDetail(UserDetail userDetail,
      File file, boolean isPOIUsed) {
    String fileName = file.getName();
    String nomPub = fileName;
    String description = "";
    String motsClefs = "";
    String content = "";

    if (isPOIUsed) {
      try {
        MetaData metaData = metadataExtractor.extractMetadata(file.getAbsolutePath());
        if (StringUtil.isDefined(metaData.getTitle())) {
          nomPub = metaData.getTitle();
        }
        if (StringUtil.isDefined(metaData.getSubject())) {
          description = metaData.getSubject();
        }
        if (StringUtil.isDefined(metaData.getKeywords())) {
          motsClefs = metaData.getKeywords();
        }
        content = "fichier(s) importé(s)";
      } catch (Exception ex) {
        // on estime que l'exception est dû au fait que nous ne sommes pas en présence d'un
        // fichier OLE2 (office)
        nomPub = fileName;
        description = nomPub;
        motsClefs = nomPub;
        content = nomPub;
      }
    }
    return new PublicationDetail("unknown", nomPub, description, new Date(), new Date(), null,
        userDetail.getId(), "5", null, motsClefs, content);
  }

  /**
   * Add nodes (coordinatesId) to a publication.
   *
   * @param pubPK
   * @param nodes List of coordinateId.
   */
  public static void addNodesToPublication(PublicationPK pubPK, List<Integer> nodes) {
    try {
      for (Integer coordinateId : nodes) {
        getPublicationBm().addFather(pubPK, new NodePK(coordinateId.toString(), pubPK));
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
    try {
      PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(
          JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
      return publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException("ImportExport.getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Get unbalanced publications
   *
   * @param componentId
   * @return ArrayList of publicationDetail
   */
  public static List<PublicationDetail> getUnbalancedPublications(String componentId) {
    try {
      return new ArrayList<PublicationDetail>(getPublicationBm().
          getOrphanPublications(new PublicationPK("useless", componentId)));
    } catch (Exception e) {
      throw new PublicationRuntimeException("CoordinateImportExport.getUnbalancedPublications()",
          SilverpeasRuntimeException.ERROR,
          "importExport.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_PUBLICATIONS_NON_CLASSEES", e);
    }
  }
}
