package com.stratelia.webactiv.util.publication.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.RecordSet;
import com.silverpeas.notation.ejb.NotationBm;
import com.silverpeas.notation.ejb.NotationBmHome;
import com.silverpeas.notation.ejb.NotationRuntimeException;
import com.silverpeas.notation.model.Notation;
import com.silverpeas.notation.model.NotationPK;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.tagcloud.ejb.TagCloudBm;
import com.silverpeas.tagcloud.ejb.TagCloudBmHome;
import com.silverpeas.tagcloud.ejb.TagCloudRuntimeException;
import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;
import com.silverpeas.tagcloud.model.TagCloudUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.ejb.Publication;
import com.stratelia.webactiv.util.publication.ejb.PublicationDAO;
import com.stratelia.webactiv.util.publication.ejb.PublicationFatherDAO;
import com.stratelia.webactiv.util.publication.ejb.PublicationHome;
import com.stratelia.webactiv.util.publication.ejb.PublicationI18NDAO;
import com.stratelia.webactiv.util.publication.ejb.ValidationStepsDAO;
import com.stratelia.webactiv.util.publication.info.InfoDAO;
import com.stratelia.webactiv.util.publication.info.SeeAlsoDAO;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoLinkDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationI18N;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import com.stratelia.webactiv.util.publication.model.ValidationStep;

/*
 * CVS Informations
 *
 * $Id: PublicationBmEJB.java,v 1.47 2009/03/31 07:58:52 neysseri Exp $
 *
 * $Log: PublicationBmEJB.java,v $
 * Revision 1.47  2009/03/31 07:58:52  neysseri
 * eShop : Livraison 2
 *
 * Revision 1.46  2009/02/24 14:01:35  neysseri
 * Evolution eShop
 *
 * Revision 1.45  2009/01/23 12:50:55  neysseri
 * Livraison eShop : Intégration tagCloud
 *
 * Revision 1.44  2008/10/15 08:19:48  neysseri
 * Utilisation systématique de la table seealso au lieu de infodetail_link
 *
 * Revision 1.43  2008/10/10 11:22:37  neysseri
 * Ajout méthode getPublicationPKsByStatus(String status, List componentIds)
 * Optimisations suite visite DSR
 *
 * Revision 1.42.4.1  2008/10/10 11:18:49  neysseri
 * no message
 *
 * Revision 1.42  2008/08/29 14:11:21  neysseri
 * Ajout de la méthode changePublicationsOrder(List ids, NodePK nodePK)
 *
 * Revision 1.41  2008/08/26 05:52:25  neysseri
 * no message
 *
 * Revision 1.40  2008/07/08 14:33:01  neysseri
 * no message
 *
 * Revision 1.39  2008/06/23 11:25:01  neysseri
 * no message
 *
 * Revision 1.38  2008/06/05 11:34:25  neysseri
 * no message
 *
 * Revision 1.37  2008/05/22 12:43:59  neysseri
 * no message
 *
 * Revision 1.36  2008/05/16 10:50:39  neysseri
 * no message
 *
 * Revision 1.35.2.2  2008/05/16 10:46:30  neysseri
 * no message
 *
 * Revision 1.35.2.1  2008/05/09 14:38:04  neysseri
 * no message
 *
 * Revision 1.35  2008/04/16 13:09:55  neysseri
 * no message
 *
 * Revision 1.34.2.1  2008/04/03 09:10:24  neysseri
 * no message
 *
 * Revision 1.34  2008/03/26 13:15:50  neysseri
 * no message
 *
 * Revision 1.33  2008/03/11 15:44:15  neysseri
 * no message
 *
 * Revision 1.32  2007/12/10 08:26:35  neysseri
 * no message
 *
 * Revision 1.31  2007/12/03 14:06:26  neysseri
 * no message
 *
 * Revision 1.30.2.3  2007/11/20 15:39:31  neysseri
 * no message
 *
 * Revision 1.30.2.2  2007/11/07 10:31:16  neysseri
 * no message
 *
 * Revision 1.30.2.1  2007/10/31 16:59:33  neysseri
 * no message
 *
 * Revision 1.30  2007/08/13 08:04:19  neysseri
 * no message
 *
 * Revision 1.29  2007/08/08 14:55:55  neysseri
 * no message
 *
 * Revision 1.28  2007/04/20 14:17:15  neysseri
 * no message
 *

 * Revision 1.27.2.1  2007/03/29 08:45:33  neysseri
 * no message
 *
 * Revision 1.27  2007/02/27 08:36:31  neysseri
 * Ajout méthode getDetailBetweenDate()
 *
 * Revision 1.26  2007/02/21 18:16:23  neysseri
 * Intégration FCKEditor dans formTemplate
 *
 * Revision 1.25.4.1  2007/02/16 16:36:00  sfariello
 * modif pour utilisation dans le Blog
 *
 * Revision 1.25.2.1  2006/12/20 17:37:29  dlesimple
 * Modif méthode getDetailsNotInFatherPK
 *
 * Revision 1.25  2006/12/01 15:01:37  neysseri
 * no message
 *
 * Revision 1.24  2006/10/27 16:07:26  neysseri
 * Indexation du contenu XML champ par champ
 *
 * Revision 1.23  2006/09/18 07:07:36  neysseri
 * Le wysiwyg n'est plus indexé via son contenu mais par le fichier wysiwyg.txt
 *
 * Revision 1.22  2006/08/10 15:46:19  neysseri
 * Ajout d'une vignette à l'indexation. Est restituée par le moteur de recherche.
 *
 * Revision 1.21.2.1  2006/07/24 15:05:42  neysseri
 * no message
 *
 * Revision 1.21  2006/06/23 13:14:55  neysseri
 * no message
 *
 * Revision 1.20.2.1  2006/06/23 12:47:14  neysseri
 * no message
 *
 * Revision 1.20  2006/03/20 15:47:09  neysseri
 * no message
 *
 * Revision 1.19.2.1  2006/03/10 19:02:21  neysseri
 * no message
 *
 * Revision 1.19  2005/12/23 16:10:28  dlesimple
 * Indexation champ auteur
 *
 * Revision 1.18  2005/12/09 12:23:34  neysseri
 * no message
 *
 * Revision 1.17  2005/12/02 13:59:27  neysseri
 * Ajout de deux méthodes :
 * - removeImage(PublicationPK pubPK)
 * - getPublicationsByStatus(String status, List componentIds)
 *
 * Revision 1.16  2005/05/19 14:54:15  neysseri
 * Possibilité de supprimer les Voir Aussi
 *
 * Revision 1.15  2005/04/14 18:13:45  neysseri
 * no message
 *
 * Revision 1.14  2005/04/07 18:14:30  neysseri
 * no message
 *
 * Revision 1.13  2005/02/23 19:13:55  neysseri
 * intégration Import/Export
 *
 * Revision 1.12.2.1  2005/02/08 18:00:26  tleroi
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/22 15:34:59  neysseri
 * nettoyage eclipse
 *
 * Revision 1.11  2004/02/06 18:47:04  neysseri
 * Now, the module publication is responsible of the wysiwyg indexation.
 * It is used (like kmelia).
 *
 * Revision 1.10  2003/12/05 15:36:49  neysseri
 * no message
 *
 * Revision 1.9  2003/11/25 09:56:37  neysseri
 * no message
 *
 * Revision 1.8  2003/08/26 09:39:27  neysseri
 * New method added.
 * This method permits to know if a publication already exists in a given instance.
 * This is a based-name search.
 *
 * Revision 1.7  2003/07/02 20:24:57  cbonin
 * Indexation nom & prenom de l'auteur
 *
 * Revision 1.6  2003/06/30 23:52:53  neysseri
 * no message
 *
 * Revision 1.5  2003/05/15 19:30:32  neysseri
 * no message
 *
 * Revision 1.4  2003/02/28 15:50:44  neysseri
 * no message
 *
 * Revision 1.3  2002/12/18 07:39:27  neysseri
 * Bug fixing about links between publications
 *
 * Revision 1.2  2002/11/28 16:49:25  neysseri
 * QuickInfo in PDC merging
 *
 * Revision 1.1.1.1.18.3  2002/11/28 10:46:37  abudnikau
 * no message
 *
 * Revision 1.1.1.1.18.2  2002/11/27 13:38:21  abudnikau
 * no message
 *
 * Revision 1.1.1.1.18.1  2002/11/26 13:33:30  abudnikau
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.39  2002/07/30 07:26:00  nchaix
 * Merge branche B200006
 *
 * Revision 1.38.16.1  2002/07/22 10:04:29  mnikolaenko
 * no message
 *
 * Revision 1.38  2002/05/17 15:09:55  nchaix
 * Merge de la branche bug001 sur la branche principale
 *
 * Revision 1.37.4.1  2002/05/17 09:47:17  dwenzek
 * Coorection bug : aucun document n'est indexé sur solaris
 *
 * Revision 1.37  2002/02/04 12:06:44  neysseri
 * Ajout d'une méthode permettant d'obtenir la liste
 * des publications référencées par au moins un père
 *
 * Revision 1.36  2002/01/11 12:40:30  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class PublicationBmEJB implements SessionBean,
    PublicationBmBusinessSkeleton {

  private String dbName = JNDINames.PUBLICATION_DATASOURCE;
  private SimpleDateFormat formatter = new java.text.SimpleDateFormat(
      "yyyy/MM/dd");
  private static final ResourceLocator publicationSettings = new ResourceLocator(
      "com.stratelia.webactiv.util.publication.publicationSettings", "fr");

  public PublicationDetail getDetail(PublicationPK pubPK)
      throws RemoteException {
    if (pubPK.getInstanceId() == null) {
      // Cas des liens simplifiés
      // On ne connait que l'id de la publication
      // Tous les attributs d'une primaryKey sont obligatoires pour faire
      // un findByPrimaryKey.
      // On est donc obligé de faire une recherche directement dans la base
      // avant
      // pour récuperer l'instanceId !
      Connection con = null;
      try {
        con = getConnection();
        pubPK = PublicationDAO.selectByPrimaryKey(con, pubPK);
        return pubPK.pubDetail;
      } catch (SQLException e) {
        throw new PublicationRuntimeException("PublicationBmEJB.getDetail()",
            SilverpeasRuntimeException.ERROR,
            "publication.GETTING_PUBLICATION_HEADER_FAILED", "pubId = "
                + pubPK.getId(), e);
      } finally {
        freeConnection(con);
      }
    }
    PublicationDetail result = null;
    Publication pub = findPublication(pubPK);
    try {
      result = pub.getDetail();
      return result;
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_HEADER_FAILED", "pubId = "
              + pubPK.getId(), re);
    }
  }

  private void setTranslations(Connection con, PublicationDetail publi) {
    try {
      PublicationI18N translation = new PublicationI18N(publi.getLanguage(),
          publi.getName(), publi.getDescription(), publi.getKeywords());
      publi.addTranslation(translation);
      List translations = PublicationI18NDAO
          .getTranslations(con, publi.getPK());
      publi.setTranslations(translations);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.setTranslations()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_TRANSLATIONS_FAILED", "pubId = "
              + publi.getPK().getId(), e);
    }
  }

  private void setTranslations(Connection con, Collection publis) {
    if (publis != null && publis.size() > 0) {
      PublicationDetail publi = null;
      Iterator it = publis.iterator();
      while (it.hasNext()) {
        publi = (PublicationDetail) it.next();
        setTranslations(con, publi);
      }
    }
  }

  public PublicationPK createPublication(PublicationDetail detail)
      throws RemoteException {
    PublicationDetail pubDetail = null;
    try {
      int indexOperation = detail.getIndexOperation();
      SilverTrace.info("publication", "PublicationBmEJB.createPublication()",
          "root.MSG_GEN_PARAM_VALUE", "indexOperation = " + indexOperation);

      Publication pub = getPublicationHome().create(detail);
      pubDetail = pub.getDetail();

      pubDetail.setIndexOperation(indexOperation);
      createIndex(pubDetail);

      if (SilverpeasSettings.readBoolean(publicationSettings, "useTagCloud",
          false))
        createTagCloud(pubDetail);
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.createPublication()",
          SilverpeasRuntimeException.ERROR,
          "publication.CREATING_PUBLICATION_FAILED", "detail = "
              + detail.toString(), re);
    }
    return pubDetail.getPK();
  }

  public void movePublication(PublicationPK pubPK, NodePK nodePK,
      boolean indexIt) throws RemoteException {
    Publication publi = findPublication(pubPK);
    try {
      deleteIndex(pubPK);

      publi.move(nodePK);

      if (indexIt)
        createIndex(pubPK);
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.movePublication()",
          SilverpeasRuntimeException.ERROR,
          "publication.MOVING_PUBLICATION_FAILED", "pubId = " + pubPK.getId(),
          re);
    }
  }

  public void changePublicationsOrder(List ids, NodePK nodePK)
      throws RemoteException {
    if (ids == null || ids.size() == 0)
      return;

    Connection con = null;

    try {
      con = getConnection();
      String id = null;
      PublicationPK pubPK = new PublicationPK("unknown", nodePK.getInstanceId());
      for (int i = 0; i < ids.size(); i++) {
        id = (String) ids.get(i);
        pubPK.setId(id);

        PublicationFatherDAO.updateOrder(con, pubPK, nodePK, i);
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.changePublicationsOrder()",
          SilverpeasRuntimeException.ERROR,
          "publication.SORTING_PUBLICATIONS_FAILED", "pubIds = "
              + ids.toString(), e);
    } finally {
      freeConnection(con);
    }

  }

  public void changePublicationOrder(PublicationPK pubPK, NodePK nodePK,
      int direction) throws RemoteException {
    // get all publications in given node
    List publications = (List) getDetailsByFatherPK(nodePK,
        "P.pubUpdateDate desc");

    // find given publication
    int index = getIndexOfPublication(pubPK.getId(), publications);

    // remove publication in list
    PublicationDetail publication = (PublicationDetail) publications
        .remove(index);

    index = index + direction;

    // prevent indexOutOfBound
    if (index < 0)
      index = 0;
    else if (index > publications.size())
      index = publications.size();

    // insert publication at the right place
    publications.add(index, publication);

    // change all publications order
    PublicationDetail publiToOrder = null;
    Connection con = null;
    try {
      con = getConnection();
      for (int p = 0; p < publications.size(); p++) {
        publiToOrder = (PublicationDetail) publications.get(p);

        PublicationFatherDAO.updateOrder(con, publiToOrder.getPK(), nodePK, p);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.changePublicationOrder()",
          SilverpeasRuntimeException.ERROR,
          "publication.MOVING_PUBLICATION_FAILED", "pubId = " + pubPK.getId(),
          e);
    } finally {
      freeConnection(con);
    }
  }

  private int getIndexOfPublication(String pubId, List publications) {
    SilverTrace.debug("publication",
        "PublicationBmEJB.getIndexOfPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    PublicationDetail publi = null;
    int index = 0;
    if (publications != null) {
      for (int i = 0; i < publications.size(); i++) {
        publi = (PublicationDetail) publications.get(i);
        if (pubId.equals(publi.getPK().getId())) {
          SilverTrace.debug("publication",
              "PublicationBmEJB.getIndexOfPublication()",
              "root.MSG_GEN_EXIT_METHOD", "index = " + index);
          return index;
        }
        index++;
      }
    }
    SilverTrace.debug("publication",
        "PublicationBmEJB.getIndexOfPublication()", "root.MSG_GEN_EXIT_METHOD",
        "index = " + index);
    return index;
  }

  public void removePublication(PublicationPK pubPK) throws RemoteException {
    PublicationHome pubHome = getPublicationHome();
    try {
      pubHome.remove(pubPK);
      // deleteAttachments(pubPK);
      deleteIndex(pubPK);
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.removePublication()",
          SilverpeasRuntimeException.ERROR,
          "publication.DELETING_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId(), re);
    }
  }

  public void removeImage(PublicationPK pubPK) throws RemoteException {
    Publication publication = findPublication(pubPK);
    try {
      publication.removeImage();
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeImage()",
          SilverpeasRuntimeException.ERROR,
          "publication.DELETING_IMAGE_FAILED", "pubId = " + pubPK.getId(), re);
    }
  }

  public void setDetail(PublicationDetail detail) throws RemoteException {
    Publication pub = findPublication(detail.getPK());

    try {
      int indexOperation = detail.getIndexOperation();

      pub.setDetail(detail);

      if (detail.isRemoveTranslation()) {
        // remove wysiwyg content
        WysiwygController.deleteFile(detail.getPK().getInstanceId(), detail
            .getPK().getId(), detail.getLanguage());

        // remove xml content
        String infoId = detail.getInfoId();
        SilverTrace.info("publication", "PublicationBmEJB.setDetail()",
            "root.MSG_GEN_PARAM_VALUE", "infoId = " + infoId);
        if (StringUtil.isDefined(infoId) && !isInteger(infoId)) {
          String xmlFormShortName = infoId;

          PublicationTemplate pubTemplate = PublicationTemplateManager
              .getPublicationTemplate(detail.getPK().getInstanceId() + ":"
                  + xmlFormShortName);

          RecordSet set = pubTemplate.getRecordSet();
          DataRecord data = set.getRecord(detail.getPK().getId(), detail
              .getLanguage());
          set.delete(data);
        }
      }

      SilverTrace.info("publication", "PublicationBmEJB.setDetail()",
          "root.MSG_GEN_PARAM_VALUE", "indexOperation = " + indexOperation);

      if (indexOperation == IndexManager.ADD
          || indexOperation == IndexManager.READD) {
        createIndex(detail.getPK(), true, indexOperation);
        // createWysiwygIndex(detail.getPK());
      } else if (indexOperation == IndexManager.REMOVE) {
        deleteIndex(detail.getPK());
      }

      if (SilverpeasSettings.readBoolean(publicationSettings, "useTagCloud",
          false))
        updateTagCloud(detail);
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.setDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.UPDATING_PUBLICATION_HEADER_FAILED", "detail = "
              + detail.toString(), re);
    }
  }

  public List getValidationSteps(PublicationPK pubPK) throws RemoteException {
    Connection con = null;

    try {
      con = getConnection();

      return ValidationStepsDAO.getSteps(con, pubPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getValidationSteps()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_VALIDATION_STEPS_FAILED", pubPK
              .toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public ValidationStep getValidationStepByUser(PublicationPK pubPK,
      String userId) throws RemoteException {
    Connection con = null;

    try {
      con = getConnection();

      return ValidationStepsDAO.getStepByUser(con, pubPK, userId);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getValidationStepByUser()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_VALIDATION_STEP_FAILED", pubPK
              .toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void addValidationStep(ValidationStep step) throws RemoteException {
    Connection con = null;

    try {
      con = getConnection();

      ValidationStepsDAO.addStep(con, step);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.addValidationStep()",
          SilverpeasRuntimeException.ERROR,
          "publication.ADDING_PUBLICATION_VALIDATION_STEP_FAILED", step
              .getPubPK().toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void removeValidationSteps(PublicationPK pubPK) throws RemoteException {
    Connection con = null;

    try {
      con = getConnection();

      ValidationStepsDAO.removeSteps(con, pubPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.removeValidationSteps()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_PUBLICATION_VALIDATION_STEPS_FAILED", pubPK
              .toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void addFather(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      pub.addFather(fatherPK);
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.addFather()",
          SilverpeasRuntimeException.ERROR,
          "publication.ADDING_FATHER_TO_PUBLICATION_FAILED", "pubId = "
              + pubPK.getId() + " and fatherId = " + fatherPK.getId(), re);
    }
  }

  public void removeFather(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      pub.removeFather(fatherPK);
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeFather()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHER_TO_PUBLICATION_FAILED", "pubId = "
              + pubPK.getId() + " and fatherId = " + fatherPK.getId(), re);
    }
  }

  public void removeFather(NodePK fatherPK) throws RemoteException {
    Connection con = getConnection();

    try {
      PublicationPK pubPK = new PublicationPK("useless", fatherPK);

      PublicationFatherDAO.removeFatherToPublications(con, pubPK, fatherPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeFather()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHER_TO_ALL_PUBLICATIONS_FAILED",
          "fatherId = " + fatherPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void removeFathers(PublicationPK pubPK, Collection fatherIds)
      throws RemoteException {
    Connection con = getConnection();

    try {
      PublicationFatherDAO.removeFathersToPublications(con, pubPK, fatherIds);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeFathers()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHERS_TO_ALL_PUBLICATIONS_FAILED",
          "fatherIds = " + fatherIds.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void removeAllFather(PublicationPK pubPK) throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      pub.removeAllFather();
      deleteIndex(pubPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.removeAllFather()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHERS_TO_PUBLICATION_FAILED", "pubId = "
              + pubPK.getId(), e);
    }
  }

  public Collection getOrphanPublications(PublicationPK pubPK)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection pubDetails = PublicationDAO.getOrphanPublications(con, pubPK);
      if (I18NHelper.isI18N)
        setTranslations(con, pubDetails);
      return pubDetails;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getOrphanPublications()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getNotOrphanPublications(PublicationPK pubPK)
      throws RemoteException {
    Connection con = getConnection();
    try {
      Collection pubDetails = PublicationDAO.getNotOrphanPublications(con,
          pubPK);
      if (I18NHelper.isI18N)
        setTranslations(con, pubDetails);
      return pubDetails;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getNotOrphanPublications()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteOrphanPublicationsByCreatorId(PublicationPK pubPK,
      String creatorId) throws RemoteException {
    Connection con = getConnection();

    try {
      PublicationDAO.deleteOrphanPublicationsByCreatorId(con, pubPK, creatorId);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.deleteOrphanPublicationsByCreatorId()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED",
          "creatorId = " + creatorId, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getUnavailablePublicationsByPublisherId(
      PublicationPK pubPK, String publisherId, String nodeId)
      throws RemoteException {
    Connection con = getConnection();
    try {
      Collection pubDetails = PublicationDAO
          .getUnavailablePublicationsByPublisherId(con, pubPK, publisherId,
              nodeId);
      if (I18NHelper.isI18N)
        setTranslations(con, pubDetails);
      return pubDetails;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getUnavailablePublicationsByPublisherId()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "publisherId = "
              + publisherId, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getAllFatherPK(PublicationPK pubPK) throws RemoteException {
    Collection result = null;
    // Publication pub = findPublication(pubPK);
    Connection con = getConnection();

    try {
      // result = pub.getAllFatherPK();
      result = PublicationFatherDAO.getAllFatherPK(con, pubPK);
      return result;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getAllFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_FATHERS_FAILED", "pubId = "
              + pubPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getAlias(PublicationPK pubPK) throws RemoteException {
    Connection con = getConnection();
    try {
      return PublicationFatherDAO.getAlias(con, pubPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getAlias()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_FATHERS_FAILED", "pubId = "
              + pubPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void addAlias(PublicationPK pubPK, List aliases)
      throws RemoteException {
    Connection con = getConnection();
    try {
      Alias alias = null;
      for (int f = 0; aliases != null && f < aliases.size(); f++) {
        alias = (Alias) aliases.get(f);

        PublicationFatherDAO.addAlias(con, pubPK, alias);

        PublicationDAO.invalidateLastPublis(alias.getInstanceId());
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.addAlias()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_FATHERS_FAILED", "pubId = "
              + pubPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void removeAlias(PublicationPK pubPK, List aliases)
      throws RemoteException {
    Connection con = getConnection();
    try {
      Alias alias = null;
      for (int f = 0; aliases != null && f < aliases.size(); f++) {
        alias = (Alias) aliases.get(f);
        PublicationFatherDAO.removeAlias(con, pubPK, alias);

        PublicationDAO.invalidateLastPublis(alias.getInstanceId());
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeAlias()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_FATHERS_FAILED", "pubId = "
              + pubPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByFatherPK(NodePK fatherPK)
      throws RemoteException {
    return getDetailsByFatherPK(fatherPK, null);
  }

  public Collection getDetailsByFatherPK(NodePK fatherPK, String sorting)
      throws RemoteException {
    return getDetailsByFatherPK(fatherPK, sorting, true);
  }

  public Collection getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod) throws RemoteException {
    Connection con = getConnection();

    try {
      Collection publis = PublicationDAO.selectByFatherPK(con, fatherPK,
          sorting, filterOnVisibilityPeriod);
      if (I18NHelper.isI18N)
        setTranslations(con, publis);
      return publis;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "fatherPK = "
              + fatherPK.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId) throws RemoteException {
    Connection con = getConnection();

    try {
      Collection publis = PublicationDAO.selectByFatherPK(con, fatherPK,
          sorting, filterOnVisibilityPeriod, userId);
      if (I18NHelper.isI18N)
        setTranslations(con, publis);
      return publis;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "fatherPK = "
              + fatherPK.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsNotInFatherPK(NodePK fatherPK)
      throws RemoteException {
    return getDetailsNotInFatherPK(fatherPK, null);
  }

  public Collection getDetailsNotInFatherPK(NodePK fatherPK, String sorting)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection detailList = PublicationDAO.selectNotInFatherPK(con, fatherPK,
          sorting);
      if (I18NHelper.isI18N)
        setTranslations(con, detailList);
      return detailList;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsNotInFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "fatherPK = "
              + fatherPK.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByBeginDateDescAndStatus(PublicationPK pk,
      String status, int nbPubs) throws RemoteException {
    Connection con = getConnection();

    try {
      Collection detailList = null;
      List result = new ArrayList();

      detailList = PublicationDAO.selectByBeginDateDescAndStatus(con, pk,
          status);
      Iterator it = detailList.iterator();
      int i = 0;
      PublicationDetail pubDetail = null;

      while (it.hasNext() && i < nbPubs) {
        pubDetail = (PublicationDetail) it.next();
        result.add(pubDetail);
        i++;
      }

      if (I18NHelper.isI18N)
        setTranslations(con, result);
      return result;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByBeginDateDescAndStatus()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "status = " + status, e);
    } finally {
      freeConnection(con);
    }

  }

  public Collection getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(
      PublicationPK pk, String status, int nbPubs, String fatherId)
      throws RemoteException {
    Connection con = getConnection();
    Collection detailList = null;

    try {
      detailList = PublicationDAO
          .selectByBeginDateDescAndStatusAndNotLinkedToFatherId(con, pk,
              status, fatherId, nbPubs);
      if (I18NHelper.isI18N)
        setTranslations(con, detailList);
      return detailList;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "fatherId = " + fatherId,
          e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByBeginDateDesc(PublicationPK pk, int nbPubs)
      throws RemoteException {
    Connection con = getConnection();
    try {
      Collection detailList = null;
      List result = new ArrayList();

      detailList = PublicationDAO.selectByBeginDateDesc(con, pk);
      Iterator it = detailList.iterator();
      int i = 0;
      PublicationDetail pubDetail = null;

      while (it.hasNext() && i < nbPubs) {
        pubDetail = (PublicationDetail) it.next();
        result.add(pubDetail);
        i++;
      }
      if (I18NHelper.isI18N)
        setTranslations(con, result);
      return result;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByBeginDateDesc()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "nbPubs = " + nbPubs, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getAllModelsDetail() throws RemoteException {
    Connection con = getConnection();

    try {
      return InfoDAO.getAllModelsDetail(con);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getAllModelsDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_MODELS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public ModelDetail getModelDetail(ModelPK modelPK) throws RemoteException {
    Connection con = getConnection();

    try {
      return InfoDAO.getModelDetail(con, modelPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getModelDetail()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_MODEL_FAILED",
          "modelPK = " + modelPK.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void createInfoDetail(PublicationPK pubPK, ModelPK modelPK,
      InfoDetail infos) throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      pub.createInfoDetail(modelPK, infos);
      if (infos != null)
        createIndex(pubPK, false, infos.getIndexOperation());
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.createInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.CREATING_PUBLICATION_DETAIL_FAILED", "pubId = "
              + pubPK.getId() + ", modelPK = " + modelPK.toString()
              + ", infos = " + infos.toString(), e);
    }
  }

  public void createInfoModelDetail(PublicationPK pubPK, ModelPK modelPK,
      InfoDetail infos) throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      pub.createInfoModelDetail(modelPK, infos);
      if (infos != null)
        createIndex(pubPK, false, infos.getIndexOperation());
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.createInfoModelDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.CREATING_PUBLICATION_DETAIL_FAILED", "pubId = "
              + pubPK.getId() + ", modelPK = " + modelPK.toString()
              + ", infos = " + infos.toString(), e);
    }
  }

  public InfoDetail getInfoDetail(PublicationPK pubPK) throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      return pub.getInfoDetail();
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_DETAIL_FAILED", "pubId = "
              + pubPK.getId(), e);
    }
  }

  public void updateInfoDetail(PublicationPK pubPK, InfoDetail infos)
      throws RemoteException {
    Publication pub = findPublication(pubPK);

    Connection con = null;
    try {
      if (isInteger(pub.getDetail().getInfoId())
          && !"0".equals(pub.getDetail().getInfoId())) {
        pub.updateInfoDetail(infos);
        if (infos != null)
          createIndex(pubPK, false, infos.getIndexOperation());
      } else {
        // XML Template
        // Only infoLinks are used
        Collection links = infos.getInfoLinkList();
        if (links != null) {
          con = getConnection();
          Iterator i = links.iterator();
          InfoLinkDetail link = null;
          PublicationPK targetPK = null;
          while (i.hasNext()) {
            link = (InfoLinkDetail) i.next();
            targetPK = new PublicationPK(link.getTargetId(), pubPK
                .getInstanceId());
            SeeAlsoDAO.addLink(con, pubPK, targetPK);
          }
        }
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.updateInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.UPDATING_INFO_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteInfoLinks(PublicationPK pubPK, List pubIds)
      throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      pub.deleteInfoLinks(pubIds);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.deleteInfoLinks()",
          SilverpeasRuntimeException.ERROR,
          "publication.UPDATING_INFO_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    }
  }

  public CompletePublication getCompletePublication(PublicationPK pubPK)
      throws RemoteException {
    Publication pub = findPublication(pubPK);

    try {
      return pub.getCompletePublication();
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getCompletePublication()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_FAILED", "pubId = " + pubPK.getId(),
          e);
    }
  }

  public Collection searchByKeywords(String query, PublicationPK pubPK)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection resultList = PublicationDAO
          .searchByKeywords(con, query, pubPK);
      if (I18NHelper.isI18N)
        setTranslations(con, resultList);
      return resultList;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.searchByKeywords()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "query = " + query, e);
    } finally {
      freeConnection(con);
    }

  }

  public Collection getPublications(Collection publicationPKs)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection publications = PublicationDAO.selectByPublicationPKs(con,
          publicationPKs);
      if (I18NHelper.isI18N)
        setTranslations(con, publications);
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublications()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "publicationPKs = "
              + publicationPKs.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getPublicationsByStatus(String status, PublicationPK pubPK)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection publications = PublicationDAO.selectByStatus(con, pubPK,
          status);
      if (I18NHelper.isI18N)
        setTranslations(con, publications);
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationsByStatus()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "status = " + status, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getPublicationPKsByStatus(String status, List componentIds)
      throws RemoteException {
    Connection con = getConnection();
    try {
      return PublicationDAO.selectPKsByStatus(con, componentIds, status);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationPKsByStatus()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "status = " + status
              + ", componentIds = " + componentIds.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getPublicationsByStatus(String status, List componentIds)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection publications = PublicationDAO.selectByStatus(con,
          componentIds, status);
      if (I18NHelper.isI18N)
        setTranslations(con, publications);
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationsByStatus()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "status = " + status
              + ", componentIds = " + componentIds.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public int getNbPubInFatherPKs(Collection fatherPKs) throws RemoteException {
    Connection con = getConnection();
    try {
      return PublicationDAO.getNbPubInFatherPKs(con, fatherPKs);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getNbPubInFatherPKs()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED", "fatherPKs = "
              + fatherPKs.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Hashtable getDistribution(String instanceId, String statusSubQuery,
      boolean checkVisibility) throws RemoteException {
    Connection con = getConnection();
    try {
      return PublicationDAO.getDistribution(con, instanceId, statusSubQuery,
          checkVisibility);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDistribution()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED", "instanceId = "
              + instanceId, e);
    } finally {
      freeConnection(con);
    }
  }

  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath)
      throws RemoteException {
    Connection con = getConnection();
    try {
      return PublicationDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getNbPubByFatherPath()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED", "fatherPath = "
              + fatherPath, e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getDetailsByFatherIds(ArrayList fatherIds,
      PublicationPK pubPK, boolean filterOnVisibilityPeriod)
      throws RemoteException {
    return getDetailsByFatherIdsAndStatusList(fatherIds, pubPK, null, null,
        filterOnVisibilityPeriod);
  }

  public Collection getDetailsByFatherIds(ArrayList fatherIds,
      PublicationPK pubPK) throws RemoteException {
    return getDetailsByFatherIdsAndStatus(fatherIds, pubPK, null, null);
  }

  public Collection getDetailsByFatherIds(ArrayList fatherIds,
      PublicationPK pubPK, String sorting) throws RemoteException {
    return getDetailsByFatherIdsAndStatus(fatherIds, pubPK, sorting, null);
  }

  public Collection getDetailsByFatherIdsAndStatus(ArrayList fatherIds,
      PublicationPK pubPK, String sorting, String status)
      throws RemoteException {
    ArrayList statusList = null;
    if (status != null) {
      statusList = new ArrayList();
      statusList.add(status);
    }
    return getDetailsByFatherIdsAndStatusList(fatherIds, pubPK, sorting,
        statusList);
  }

  public Collection getDetailsByFatherIdsAndStatusList(ArrayList fatherIds,
      PublicationPK pubPK, String sorting, ArrayList status)
      throws RemoteException {
    return getDetailsByFatherIdsAndStatusList(fatherIds, pubPK, sorting,
        status, true);
  }

  public Collection getDetailsByFatherIdsAndStatusList(ArrayList fatherIds,
      PublicationPK pubPK, String sorting, ArrayList status,
      boolean filterOnVisibilityPeriod) throws RemoteException {
    Connection con = getConnection();
    try {
      Collection detailList = PublicationDAO.selectByFatherIds(con, fatherIds,
          pubPK, sorting, status, filterOnVisibilityPeriod);
      if (I18NHelper.isI18N)
        setTranslations(con, detailList);
      return detailList;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByFatherIdsAndStatus()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "fatherIds = "
              + fatherIds.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getPubPKsInFatherPKs(Collection fatherPKs)
      throws RemoteException {
    Connection con = getConnection();
    try {
      return PublicationFatherDAO.getPubPKsInFatherPKs(con, fatherPKs);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPubPKsInFatherPKs()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_PK_FAILED", "fatherPKs = "
              + fatherPKs.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getPubPKsInFatherPK(NodePK fatherPK) throws RemoteException {
    Connection con = getConnection();

    try {
      return PublicationFatherDAO.getPubPKsInFatherPK(con, fatherPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPubPKsInFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_PK_FAILED", "fatherPK = "
              + fatherPK.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  public void processWysiwyg(PublicationPK pubPK) throws RemoteException {
    createIndex(pubPK);
  }

  // internal methods

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private PublicationHome getPublicationHome() {
    try {
      PublicationHome pubHome = (PublicationHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PUBLICATION_EJBHOME, PublicationHome.class);

      return pubHome;
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationHome()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          re);
    }

  }

  /**
   * Method declaration
   * 
   * 
   * @param pubPK
   * 
   * @return
   * 
   * @see
   */
  private Publication findPublication(PublicationPK pubPK) {
    PublicationHome pubHome = getPublicationHome();

    try {
      return (pubHome.findByPrimaryKey(pubPK));
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.findPublication()",
          SilverpeasRuntimeException.ERROR,
          "publication.PUBLICATION_UNFINDABLE", "pubId = " + pubPK.getId(), re);
    }
  }

  private Publication findPublicationByName(PublicationPK pubPK, String name) {
    PublicationHome pubHome = getPublicationHome();

    try {
      return (pubHome.findByName(pubPK, name));
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.findPublicationByName()",
          SilverpeasRuntimeException.ERROR,
          "publication.PUBLICATION_UNFINDABLE", "pubName = " + name, re);
    }
  }

  private Publication findPublicationByNameAndNodeId(PublicationPK pubPK,
      String name, int nodeId) {
    PublicationHome pubHome = getPublicationHome();

    try {
      return (pubHome.findByNameAndNodeId(pubPK, name, nodeId));
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.findPublicationByNameAndNodeId()",
          SilverpeasRuntimeException.ERROR,
          "publication.PUBLICATION_UNFINDABLE", "pubName = " + name
              + ", nodeId=" + nodeId, re);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);

      return con;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * 
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * ***************************************************************************
   * ************************* INDEXING METHODS
   * *********************************
   * *******************************************************************
   */
  private FullIndexEntry updateIndexEntryWithModelContent(
      FullIndexEntry indexEntry, Collection textList) {
    SilverTrace.info("publication",
        "PublicationBmEJB.updateIndexEntryWithModelContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
    if (textList != null) {
      Iterator it = textList.iterator();

      while (it.hasNext()) {
        InfoTextDetail textDetail = (InfoTextDetail) it.next();

        indexEntry.addTextContent(textDetail.getContent());
      }
    }
    return indexEntry;
  }

  /**
   * Method declaration
   * 
   * 
   * @param indexEntry
   * @param infoDetail
   * 
   * @return
   * 
   * @see
   */
  private FullIndexEntry updateIndexEntryWithInfoDetail(
      FullIndexEntry indexEntry, InfoDetail infoDetail) {
    SilverTrace.info("publication",
        "PublicationBmEJB.updateIndexEntryWithInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString()
            + ", infoDetail = " + infoDetail.toString());
    if (infoDetail != null) {
      // Index the text includes in the model
      Collection textList = infoDetail.getInfoTextList();
      indexEntry = this.updateIndexEntryWithModelContent(indexEntry, textList);
    }
    return indexEntry;
  }

  private FullIndexEntry updateIndexEntryWithWysiwygContent(
      FullIndexEntry indexEntry, PublicationDetail pubDetail) {
    PublicationPK pubPK = pubDetail.getPK();
    SilverTrace.info("publication",
        "PublicationBmEJB.updateIndexEntryWithWysiwygContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString()
            + ", pubPK = " + pubPK.toString());
    try {
      if (pubPK != null) {
        Iterator languages = pubDetail.getLanguages();
        while (languages.hasNext()) {
          String language = (String) languages.next();
          String wysiwygContent = WysiwygController.load(pubPK.getInstanceId(),
              pubPK.getId(), language);
          if (StringUtil.isDefined(wysiwygContent)) {
            String wysiwygPath = WysiwygController.getWysiwygPath(pubPK
                .getInstanceId(), pubPK.getId(), language);
            indexEntry.addFileContent(wysiwygPath, null, "text/html", language);
          }
        }
        /*
         * String wysiwygContent =
         * WysiwygController.loadFileAndAttachment(pubPK.getSpace(),
         * pubPK.getComponentName(), pubPK.getId()); if (wysiwygContent != null)
         * { String wysiwygPath =
         * WysiwygController.getWysiwygPath(pubPK.getInstanceId(),
         * pubPK.getId()); indexEntry.addFileContent(wysiwygPath, null,
         * "text/html", "fr"); }
         */
      }
    } catch (Exception e) {
      // No wysiwyg associated
    }
    return indexEntry;
  }

  private FullIndexEntry updateIndexEntryWithXMLFormContent(
      FullIndexEntry indexEntry, PublicationDetail pubDetail) {
    SilverTrace.info("publication",
        "PublicationBmEJB.updateIndexEntryWithXMLFormContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString()
            + ", pubDetail.getInfoId() = " + pubDetail.getInfoId());
    if (!isInteger(pubDetail.getInfoId())) {
      try {
        PublicationTemplate pub = PublicationTemplateManager
            .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":"
                + pubDetail.getInfoId());

        RecordSet set = pub.getRecordSet();
        set.indexRecord(pubDetail.getPK().getId(), pubDetail.getInfoId(),
            indexEntry);
      } catch (Exception e) {
        SilverTrace.error("publication",
            "PublicationBmEJB.updateIndexEntryWithXMLFormContent()", "", e);
      }
    }
    return indexEntry;
  }

  private static boolean isInteger(String id) {
    try {
      Integer.parseInt(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Called on : - createPublication()
   */
  public void createIndex(PublicationDetail pubDetail) {
    SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "pubDetail.getIndexOperation() = "
            + pubDetail.getIndexOperation());
    if (pubDetail.getIndexOperation() == IndexManager.ADD
        || pubDetail.getIndexOperation() == IndexManager.READD) {
      SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
          "root.MSG_GEN_PARAM_VALUE", "pubDetail = " + pubDetail.toString());
      try {
        FullIndexEntry indexEntry = getFullIndexEntry(pubDetail);
        if (indexEntry != null)
          IndexEngineProxy.addIndexEntry(indexEntry);
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationBmEJB.createIndex()",
            "root.MSG_GEN_ENTER_METHOD", "pubDetail = " + pubDetail.toString()
                + ", indexEngineBm.addIndexEntry() failed !", e);
      }
    }
  }

  /**
   * Called on : - createPublication() - updatePublication() -
   * createInfoDetail() - createInfoModelDetail() - updateInfoDetail()
   */
  public void createIndex(PublicationPK pubPK) {
    createIndex(pubPK, true);
  }

  private void createIndex(PublicationPK pubPK, boolean processWysiwygContent,
      int indexOperation) {
    SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "processWysiwygContent = "
            + processWysiwygContent + ", indexOperation = " + indexOperation);
    if (indexOperation == IndexManager.ADD
        || indexOperation == IndexManager.READD) {
      SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
          "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
      try {
        CompletePublication completePublication = this
            .getCompletePublication(pubPK);
        FullIndexEntry indexEntry = null;
        PublicationDetail pubDetail = null;
        InfoDetail infoDetail = null;

        if (completePublication != null) {
          pubDetail = completePublication.getPublicationDetail();
          if (pubDetail != null) {
            // Index the Publication Header
            indexEntry = getFullIndexEntry(pubDetail);

            // Index the Publication Content
            infoDetail = completePublication.getInfoDetail();
            indexEntry = this.updateIndexEntryWithInfoDetail(indexEntry,
                infoDetail);

            if (processWysiwygContent) {
              indexEntry = updateIndexEntryWithWysiwygContent(indexEntry,
                  pubDetail);
              indexEntry = updateIndexEntryWithXMLFormContent(indexEntry,
                  pubDetail);
            }

            IndexEngineProxy.addIndexEntry(indexEntry);
          }
        }
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationBmEJB.createIndex()",
            "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
                + ", indexEngineBm.addIndexEntry() failed !", e);
      }
    }
  }

  private void createIndex(PublicationPK pubPK, boolean processWysiwygContent) {
    createIndex(pubPK, processWysiwygContent, IndexManager.ADD);
  }

  private FullIndexEntry getFullIndexEntry(PublicationDetail pubDetail) {
    FullIndexEntry indexEntry = null;

    if (pubDetail != null) {
      // Index the Publication Header
      indexEntry = new FullIndexEntry(pubDetail.getPK().getComponentName(),
          "Publication", pubDetail.getPK().getId());
      indexEntry.setIndexId(true);

      Iterator languages = pubDetail.getLanguages();
      while (languages.hasNext()) {
        String language = (String) languages.next();
        PublicationI18N translation = (PublicationI18N) pubDetail
            .getTranslation(language);

        indexEntry.setTitle(translation.getName(), language);
        indexEntry.setPreview(translation.getDescription(), language);
        indexEntry.setKeywords(translation.getKeywords() + " "
            + pubDetail.getAuthor(), language);
      }

      // indexEntry.setTitle(pubDetail.getName());
      // indexEntry.setKeyWords(pubDetail.getKeywords()+" "+pubDetail.getAuthor());
      // indexEntry.setPreView(pubDetail.getDescription());

      indexEntry.setLang("fr");
      indexEntry.setCreationDate(pubDetail.getUpdateDate());
      if (pubDetail.getBeginDate() != null) {
        indexEntry.setStartDate(formatter.format(pubDetail.getBeginDate()));
      }
      if (pubDetail.getEndDate() != null) {
        indexEntry.setEndDate(formatter.format(pubDetail.getEndDate()));
      }
      indexEntry.setCreationUser(pubDetail.getCreatorId());
      // index creator's full name
      if (publicationSettings.getString("indexAuthorName").equals("true")) {
        try {
          Admin admin = new Admin();
          UserDetail ud = admin.getUserDetail(pubDetail.getCreatorId());
          if (ud != null) {
            indexEntry.addTextContent(ud.getDisplayedName());
          }
        } catch (AdminException e) {
          // unable to find user detail, ignore and don't index creator's full
          // name
        }
      }

      indexEntry.setThumbnail(pubDetail.getImage());
      indexEntry.setThumbnailMimeType(pubDetail.getImageMimeType());
      indexEntry.setThumbnailDirectory(publicationSettings
          .getString("imagesSubDirectory"));
    }

    return indexEntry;
  }

  /**
   * Called on : - deletePublication()
   */
  public void deleteIndex(PublicationPK pubPK) throws RemoteException {
    SilverTrace.info("publication", "PublicationBmEJB.deleteIndex()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(pubPK.getComponentName(),
        "Publication", pubPK.getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);

    // Suppression du nuage de tags lors de la suppression de l'index (et pas
    // lors de l'envoi de la publication dans la corbeille).
    if (SilverpeasSettings.readBoolean(publicationSettings, "useTagCloud",
        false))
      deleteTagCloud(pubPK);

    // idem pour les notations
    if (SilverpeasSettings.readBoolean(publicationSettings, "useNotation",
        false))
      deleteNotation(pubPK);
  }

  /**
   * Method declaration
   * 
   * 
   * @param pubPK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getAllPublications(PublicationPK pubPK, String sorting)
      throws RemoteException {
    Connection con = getConnection();

    try {
      Collection publications = PublicationDAO.selectAllPublications(con,
          pubPK, sorting);

      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getAllPublications()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection getAllPublications(PublicationPK pubPK)
      throws RemoteException {
    return getAllPublications(pubPK, null);
  }

  public PublicationDetail getDetailByName(PublicationPK pubPK, String pubName)
      throws RemoteException {
    SilverTrace.info("publication", "PublicationBmEJB.getDetailByName()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
            + ", pubName = " + pubName);
    PublicationDetail result = null;
    Publication pub = findPublicationByName(pubPK, pubName);

    try {
      result = pub.getDetail();
      return result;
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailByName()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_HEADER_FAILED", "pubPK = "
              + pubPK.toString() + ", pubName = " + pubName, re);
    }
  }

  public PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK,
      String pubName, int nodeId) throws RemoteException {
    SilverTrace.info("publication",
        "PublicationBmEJB.getDetailByNameAndNodeId()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
            + ", pubName = " + pubName + ", nodeId=" + nodeId);
    PublicationDetail result = null;
    Publication pub = findPublicationByNameAndNodeId(pubPK, pubName, nodeId);

    try {
      result = pub.getDetail();
      return result;
    } catch (Exception re) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailByNameAndNodeId()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_HEADER_FAILED", "pubPK = "
              + pubPK.toString() + ", pubName = " + pubName + ", nodeId="
              + nodeId, re);
    }
  }

  public Collection getDetailBetweenDate(String beginDate, String endDate,
      String instanceId) throws RemoteException {
    Connection con = getConnection();

    try {
      Collection detailList = null;
      ArrayList result = new ArrayList();

      detailList = PublicationDAO.selectBetweenDate(con, beginDate, endDate,
          instanceId);
      Iterator it = detailList.iterator();
      int i = 0;
      PublicationDetail pubDetail = null;

      while (it.hasNext() && i < detailList.size()) {
        pubDetail = (PublicationDetail) it.next();
        result.add(pubDetail);
        i++;
      }
      if (I18NHelper.isI18N)
        setTranslations(con, result);
      return result;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailBetweenDate()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * @return The bean managing tagclouds.
   */
  private TagCloudBm getTagCloudBm() {
    try {
      TagCloudBmHome tagCloudBmHome = (TagCloudBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.TAGCLOUDBM_EJBHOME, TagCloudBmHome.class);
      TagCloudBm tagCloudBm = tagCloudBmHome.create();
      return tagCloudBm;
    } catch (Exception e) {
      throw new TagCloudRuntimeException("PublicationBmEJB.getTagCloudBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Create the tagclouds corresponding to the publication detail.
   * 
   * @param pubDetail
   *          The detail of the publication.
   * @throws RemoteException
   */
  private void createTagCloud(PublicationDetail pubDetail)
      throws RemoteException {
    String keywords = pubDetail.getKeywords();
    if (keywords != null) {
      TagCloudBm tagCloudBm = getTagCloudBm();
      TagCloud tagCloud = new TagCloud(pubDetail.getInstanceId(), pubDetail
          .getId(), TagCloud.TYPE_PUBLICATION);
      StringTokenizer st = new StringTokenizer(keywords, " ");
      String tag;
      String tagKey;
      ArrayList tagList = new ArrayList();
      while (st.hasMoreElements()) {
        tag = (String) st.nextElement();
        tagKey = TagCloudUtil.getTag(tag);
        if (!tagList.contains(tagKey)) {
          tagCloud.setTag(tagKey);
          tagCloud.setLabel(tag.toLowerCase());
          tagCloudBm.createTagCloud(tagCloud);
          tagList.add(tagKey);
        }
      }
    }
  }

  /**
   * Delete the tagclouds corresponding to the publication key.
   * 
   * @param pubPK
   *          The primary key of the publication.
   * @throws RemoteException
   */
  private void deleteTagCloud(PublicationPK pubPK) throws RemoteException {
    getTagCloudBm().deleteTagCloud(
        new TagCloudPK(pubPK.getId(), pubPK.getInstanceId()),
        TagCloud.TYPE_PUBLICATION);
  }

  /**
   * Update the tagclouds corresponding to the publication detail.
   * 
   * @param pubDetail
   *          The detail of the publication.
   * @throws RemoteException
   */
  private void updateTagCloud(PublicationDetail pubDetail)
      throws RemoteException {
    deleteTagCloud(pubDetail.getPK());
    createTagCloud(pubDetail);
  }

  /**
   * @return The bean managing notations.
   */
  private NotationBm getNotationBm() {
    try {
      NotationBmHome notationBmHome = (NotationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.NOTATIONBM_EJBHOME, NotationBmHome.class);
      NotationBm notationBm = notationBmHome.create();
      return notationBm;
    } catch (Exception e) {
      throw new NotationRuntimeException("PublicationBmEJB.getNotationBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private void deleteNotation(PublicationPK pubPK) throws RemoteException {
    getNotationBm().deleteNotation(
        new NotationPK(pubPK.getId(), pubPK.getInstanceId(),
            Notation.TYPE_PUBLICATION));
  }

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public PublicationBmEJB() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbCreate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param sc
   * 
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }

}
