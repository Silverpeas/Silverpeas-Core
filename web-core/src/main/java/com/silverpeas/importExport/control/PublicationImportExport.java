/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.importExport.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.silverpeas.util.mail.Extractor;
import org.silverpeas.util.mail.Mail;
import org.silverpeas.util.mail.MailExtractor;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.coordinates.model.CoordinateRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang.StringUtils;

public class PublicationImportExport {

  final static ResourceLocator multilang = new ResourceLocator(
      "org.silverpeas.importExport.multilang.importExportBundle", "fr");

  private PublicationImportExport() {
  }

  /**
   * Méthodes permettant de récupérer un objet publication dont les méta-données sont générées à
   * partir des informations du fichier destiné à être attaché à celle ci. Utilisation de l'api POI
   * dans le cas des fichiers MSoffice.
   * @param file - fichier destiné à être attaché à la publication d'où l'on extrait les
   * informations qui iront renseigner les méta-données de la publication à creer
   * @return renvoie un objet PublicationDetail
   */
  public static PublicationDetail convertFileInfoToPublicationDetail(File file, ImportSettings settings) {
    String fileName = file.getName();
    String nomPub = settings.getPublicationName(fileName);
    String description = "";
    String motsClefs = "";
    String content = "";
    Date creationDate = new Date();
    Date lastModificationDate = null;

    if (!settings.mustCreateOnePublicationForAllFiles()) {
      if (FileUtil.isMail(file.getName())) {
        try {
          MailExtractor extractor = Extractor.getExtractor(file);
          Mail mail = extractor.getMail();

          creationDate = mail.getDate();

          // define StringTemplate attributes
          Map<String, String> attributes = new HashMap<String, String>();
          attributes.put("subject", mail.getSubject());
          InternetAddress address = mail.getFrom();
          if (StringUtil.isDefined(address.getPersonal())) {
            attributes.put("fromPersonal", address.getPersonal());
            description += address.getPersonal() + " - ";
          }
          attributes.put("fromAddress", address.getAddress());

          // generate title of publication
          StringTemplate titleST =
              new StringTemplate(multilang.getString("importExport.import.mail.title"));
          titleST.setAttributes(attributes);
          nomPub = titleST.toString();

          // generate description of publication
          StringTemplate descriptionST =
              new StringTemplate(multilang.getString("importExport.import.mail.description"));
          descriptionST.setAttributes(attributes);
          description = descriptionST.toString();
        } catch (Exception e) {
          SilverTrace
              .error("importExport", "PublicationImportExport.convertFileInfoToPublicationDetail",
                  "importExport.EX_CANT_EXTRACT_MAIL_DATA", e);
        }
      } else {
        // it's a classical file (not an email)
        MetaData metaData = null;
        if (settings.isPoiUsed()) {
          // extract title, subject and keywords
          metaData = MetadataExtractor.getInstance().extractMetadata(file.getAbsolutePath());
          if (StringUtil.isDefined(metaData.getTitle())) {
            nomPub = metaData.getTitle();
          }
          if (StringUtil.isDefined(metaData.getSubject())) {
            description = metaData.getSubject();
          }
          if (metaData.getKeywords() != null && metaData.getKeywords().length > 0) {
            motsClefs = StringUtils.join(metaData.getKeywords(), ';');
          }
        }

        if (settings.useFileDates()) {
          // extract creation and last modification dates
          if (metaData == null) {
            metaData = MetadataExtractor.getInstance().extractMetadata(file.getAbsolutePath());
          }
          if (metaData.getCreationDate() != null) {
            creationDate = metaData.getCreationDate();
          }
          if (metaData.getLastSaveDateTime() != null) {
            lastModificationDate = metaData.getLastSaveDateTime();
          }
        }
      }
    } else {
      nomPub = settings.getPublicationForAllFiles().getName();
      description = settings.getPublicationForAllFiles().getDescription();
      motsClefs = settings.getPublicationForAllFiles().getKeywords();
      content = settings.getPublicationForAllFiles().getContent();
    }
    PublicationDetail publication =
        new PublicationDetail("unknown", nomPub, description, creationDate, new Date(), null,
            settings.getUser().getId(), "1", null, motsClefs, content);
    publication.setTargetValidatorId(settings.getTargetValidatorIds());
    publication.setLanguage(settings.getContentLanguage());
    if (lastModificationDate != null) {
      publication.setUpdateDate(lastModificationDate);
      publication.setUpdateDateMustBeSet(true);
    }
    return publication;
  }

  /**
   * Add nodes (coordinatesId) to a publication.
   * @param pubPK
   * @param nodes List of coordinateId.
   */
  public static void addNodesToPublication(PublicationPK pubPK, List<Integer> nodes) {
    for (Integer coordinateId : nodes) {
      getPublicationBm().addFather(pubPK, new NodePK(coordinateId.toString(), pubPK));
    }
  }

  /**
   * @return l'EJB PublicationBm
   * @throws CoordinateRuntimeException
   */
  private static PublicationBm getPublicationBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
  }

  /**
   * Get unbalanced publications
   * @param componentId
   * @return ArrayList of publicationDetail
   */
  public static List<PublicationDetail> getUnbalancedPublications(String componentId) {
    return new ArrayList<PublicationDetail>(getPublicationBm().getOrphanPublications(
        new PublicationPK("useless", componentId)));
  }
}
