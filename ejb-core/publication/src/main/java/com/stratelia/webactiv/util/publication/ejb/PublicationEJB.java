/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.publication.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.InfoDAO;
import com.stratelia.webactiv.util.publication.info.SeeAlsoDAO;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoLinkDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoPK;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationI18N;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

/*
 * CVS Informations
 *
 * $Id: PublicationEJB.java,v 1.22 2008/10/15 08:19:48 neysseri Exp $
 *
 * $Log: PublicationEJB.java,v $
 * Revision 1.22  2008/10/15 08:19:48  neysseri
 * Utilisation systématique de la table seealso au lieu de infodetail_link
 *
 * Revision 1.21  2008/06/27 07:01:58  neysseri
 * Ajout encodage problème apostrophe Word
 *
 * Revision 1.20  2008/03/26 13:15:50  neysseri
 * no message
 *
 * Revision 1.19  2008/03/11 15:44:15  neysseri
 * no message
 *
 * Revision 1.18  2007/12/10 08:26:35  neysseri
 * no message
 *
 * Revision 1.17  2007/12/03 14:06:26  neysseri
 * no message
 *
 * Revision 1.16.6.4  2007/11/20 15:39:31  neysseri
 * no message
 *
 * Revision 1.16.6.3  2007/11/09 13:29:41  neysseri
 * no message
 *
 * Revision 1.16.6.2  2007/11/05 16:03:26  neysseri
 * no message
 *
 * Revision 1.16.6.1  2007/10/31 16:59:33  neysseri
 * no message
 *
 * Revision 1.16  2007/02/27 15:03:34  neysseri
 * no message
 *
 * Revision 1.15  2006/12/01 15:01:37  neysseri
 * no message
 *
 * Revision 1.14  2006/10/24 14:37:08  neysseri
 * Modifications apportées pour l'évolution "Publication Toujours Visible" :
 * Ajout de la notion de clone
 *
 * Revision 1.13.4.2  2006/10/20 16:18:10  neysseri
 * *** empty log message ***
 *
 * Revision 1.13.4.1  2006/10/06 16:00:30  neysseri
 * *** empty log message ***
 *
 * Revision 1.13  2006/07/06 14:17:19  neysseri
 * no message
 *
 * Revision 1.12  2006/06/23 13:14:56  neysseri
 * no message
 *
 * Revision 1.11.4.1  2006/06/23 12:47:14  neysseri
 * no message
 *
 * Revision 1.11  2006/01/05 11:01:59  neysseri
 * Ajout de l'attribut targetValidatorId : utilisateur devant valider la publication
 *
 * Revision 1.10  2005/12/02 13:11:51  neysseri
 * Ajout d'un méthode pour supprimer l'image
 *
 * Revision 1.9  2005/09/13 12:59:19  dlesimple
 * Ajout champ Auteur
 *
 * Revision 1.8  2005/05/19 14:54:15  neysseri
 * Possibilité de supprimer les Voir Aussi
 *
 * Revision 1.7  2005/02/23 19:13:55  neysseri
 * intégration Import/Export
 *
 * Revision 1.6.2.2  2005/02/17 17:33:15  neysseri
 * no message
 *
 * Revision 1.6.2.1  2005/02/08 18:00:39  tleroi
 * *** empty log message ***
 *
 * Revision 1.6  2004/06/22 15:34:59  neysseri
 * nettoyage eclipse
 *
 * Revision 1.5  2004/02/06 18:48:38  neysseri
 * Some useless methods removed !
 *
 * Revision 1.4  2004/01/09 13:48:49  neysseri
 * Adding of two new attributes beginHour and endHour to extends visibility period
 *
 * Revision 1.3  2003/08/26 09:39:27  neysseri
 * New method added.
 * This method permits to know if a publication already exists in a given instance.
 * This is a based-name search.
 *
 * Revision 1.2  2002/12/18 07:39:27  neysseri
 * Bug fixing about links between publications
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.22  2002/07/30 07:26:00  nchaix
 * Merge branche B200006
 *
 * Revision 1.21.4.1  2002/07/22 10:04:16  mnikolaenko
 * no message
 *
 * Revision 1.21  2002/06/27 08:56:33  mguillem
 * Merge branche V2001_fevs01
 *
 * Revision 1.20.14.2  2002/05/28 11:13:14  gshakirin
 * Adding validatorId and validateDate fields
 *
 * Revision 1.20.14.1  2002/05/28 11:09:12  gshakirin
 * Adding validatorId and validateDate fields
 *
 * Revision 1.20  2002/04/16 10:02:55  santonio
 * ajout d'une methode pour transformer les carecteres speciaux comme € et ’
 *
 * Revision 1.19  2002/03/11 14:15:53  mhguig
 * mofif pour modificateur
 *
 * Revision 1.18  2002/01/11 12:40:47  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class PublicationEJB implements EntityBean {
  private EntityContext context;
  private PublicationPK pk;
  private InfoPK infoPK;
  private String name;
  private String description;
  private Date creationDate;
  private Date beginDate;
  private Date endDate;
  private String creatorId;
  private int importance;
  private String version;
  private String keywords;
  private String content;
  private String status;
  private String image;
  private String imageMimeType;
  private Date updateDate;
  private String updaterId;
  private Date validateDate;
  private String validatorId;
  private String beginHour;
  private String endHour;
  private String author;
  private String targetValidatorId;
  private String cloneId;
  private String cloneStatus;
  private String lang;

  private List translations;

  private boolean isModified = false;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public PublicationEJB() {
  }

  /**
   * Get the attributes of THIS publication
   * 
   * @return a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public PublicationDetail getDetail() throws SQLException {
    PublicationDetail pubDetail = new PublicationDetail(pk, name, description,
        creationDate, beginDate, endDate, creatorId, importance, version,
        keywords, content, status, image, imageMimeType, updateDate, updaterId,
        author);
    pubDetail.setBeginHour(beginHour);
    pubDetail.setEndHour(endHour);
    pubDetail.setTargetValidatorId(targetValidatorId);
    pubDetail.setInfoId(infoPK.getId());
    pubDetail.setCloneId(cloneId);
    pubDetail.setCloneStatus(cloneStatus);
    pubDetail.setLanguage(lang);

    pubDetail.setTranslations(translations);

    return pubDetail;
  }

  public void removeImage() {
    image = null;
    imageMimeType = null;
    isModified = true;
  }

  /**
   * Update the attributes of the publication
   * 
   * @param pubDetail
   *          the PublicationDetail which contains updated data
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public void setDetail(PublicationDetail pubDetail) {
    if (pubDetail.getPK().equals(pk)) {
      String oldName = name;
      String oldDesc = description;
      String oldKeywords = keywords;
      String oldLang = lang;

      if (pubDetail.getName() != null) {
        name = pubDetail.getName();
      }
      if (pubDetail.getDescription() != null) {
        description = pubDetail.getDescription();
      }
      if (pubDetail.getCreationDate() != null) {
        creationDate = pubDetail.getCreationDate();
      }
      beginDate = pubDetail.getBeginDate();
      endDate = pubDetail.getEndDate();
      if (pubDetail.getCreatorId() != null) {
        creatorId = pubDetail.getCreatorId();
      }
      if (pubDetail.getImportance() != 0) {
        importance = pubDetail.getImportance();
      }
      if (pubDetail.getVersion() != null) {
        version = pubDetail.getVersion();
      }
      if (pubDetail.getKeywords() != null) {
        keywords = pubDetail.getKeywords();
      }
      if (pubDetail.getContent() != null) {
        content = pubDetail.getContent();
      }
      if (pubDetail.getStatus() != null) {
        status = pubDetail.getStatus();
      }
      if (pubDetail.getImage() != null) {
        image = pubDetail.getImage();
      }
      if (pubDetail.getImageMimeType() != null) {
        imageMimeType = pubDetail.getImageMimeType();

      }
      /*
       * if(pubDetail.getUpdaterId() != null) { updaterId =
       * pubDetail.getCreatorId(); }
       */
      updaterId = pubDetail.getUpdaterId();
      if (pubDetail.isUpdateDateMustBeSet())
        updateDate = new Date();
      isModified = true;
      if (pubDetail.getValidatorId() != null) {
        validatorId = pubDetail.getValidatorId();
      }

      if (pubDetail.getValidateDate() != null) {
        validateDate = new Date();
      }
      beginHour = pubDetail.getBeginHour();
      endHour = pubDetail.getEndHour();
      if (pubDetail.getAuthor() != null) {
        author = pubDetail.getAuthor();
      }

      targetValidatorId = pubDetail.getTargetValidatorId();

      if (pubDetail.getInfoId() != null)
        infoPK.setId(pubDetail.getInfoId());

      cloneId = pubDetail.getCloneId();
      cloneStatus = pubDetail.getCloneStatus();

      if (pubDetail.getLanguage() != null)
        lang = pubDetail.getLanguage();

      if (pubDetail.isRemoveTranslation()) {
        Connection con = getConnection();
        try {
          // Remove of a translation is required
          if (oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            // Default language = translation
            List translations = PublicationI18NDAO.getTranslations(con, pk);

            if (translations != null && translations.size() > 0) {
              PublicationI18N translation = (PublicationI18N) translations
                  .get(0);

              lang = translation.getLanguage();
              name = translation.getName();
              description = translation.getDescription();
              keywords = translation.getKeywords();

              PublicationI18NDAO.removeTranslation(con, translation.getId());
            }
          } else {
            PublicationI18NDAO.removeTranslation(con, pubDetail
                .getTranslationId());

            name = oldName;
            description = oldDesc;
            keywords = oldKeywords;
            lang = oldLang;
          }
        } catch (SQLException e) {
          throw new PublicationRuntimeException("PublicationEJB.setDetail()",
              SilverpeasRuntimeException.ERROR,
              "publication.CANNOT_MANAGE_TRANSLATIONS", e);
        } finally {
          freeConnection(con);
        }
      } else {
        // Add or update a translation
        if (pubDetail.getLanguage() != null) {
          if (oldLang == null) {
            // translation for the first time
            lang = I18NHelper.defaultLanguage;
          }

          if (oldLang != null
              && !oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            PublicationI18N translation = new PublicationI18N(pubDetail);
            String translationId = pubDetail.getTranslationId();
            Connection con = getConnection();
            try {
              if (translationId != null && !translationId.equals("-1")) {
                PublicationI18NDAO.updateTranslation(con, translation);
              } else {
                PublicationI18NDAO.addTranslation(con, translation);
              }
            } catch (Exception e) {
              throw new PublicationRuntimeException(
                  "PublicationEJB.setDetail()",
                  SilverpeasRuntimeException.ERROR,
                  "publication.CANNOT_MANAGE_TRANSLATIONS", e);
            } finally {
              freeConnection(con);
            }
            name = oldName;
            description = oldDesc;
            keywords = oldKeywords;
            lang = oldLang;
          }
        }
      }

      getTranslations();
    } else {
      throw new PublicationRuntimeException("PublicationEJB.setDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.PUBDETAIL_PK_NOT_SAME_THAN_THIS_OBJECT");
    }
  }

  private void getTranslations() {
    PublicationI18N translation = new PublicationI18N(lang, name, description,
        keywords);
    translations = new ArrayList();
    translations.add(translation);
    Connection con = getConnection();
    try {
      translations.addAll(PublicationI18NDAO.getTranslations(con, pk));
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.getTranslations()",
          SilverpeasRuntimeException.ERROR,
          "publication.CANNOT_GET_TRANSLATIONS", e);
    } finally {
      freeConnection(con);
    }
  }

  private void createTranslations(Connection con, PublicationDetail publication)
      throws SQLException, UtilException {
    if (publication.getTranslations() != null) {
      Iterator translations = publication.getTranslations().values().iterator();
      PublicationI18N translation = null;
      while (translations.hasNext()) {
        translation = (PublicationI18N) translations.next();
        if (publication.getLanguage() != null
            && !publication.getLanguage().equals(translation.getLanguage())) {
          translation.setObjectId(publication.getPK().getId());
          PublicationI18NDAO.addTranslation(con, translation);
        }
      }
    }
  }

  /**
   * Add a new father to this publication
   * 
   * @param fatherPK
   *          the father NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void addFather(NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.addFather()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();

    try {
      PublicationFatherDAO.addFather(con, pk, fatherPK);
    } finally {
      freeConnection(con);
    }
  }

  public void move(NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.move()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();

    try {
      PublicationDAO.changeInstanceId(con, pk, fatherPK.getInstanceId());

      pk.setComponentName(fatherPK.getInstanceId());

      PublicationFatherDAO.removeAllFather(con, pk);

      PublicationFatherDAO.addFather(con, pk, fatherPK);

      isModified = true;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Remove a father to this publication
   * 
   * @param fatherPK
   *          the father NodePK to remove
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void removeFather(NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.removeFather()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeFather(con, pk, fatherPK);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Remove all fathers to this publication - this publication will be linked to
   * no Node
   * 
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void removeAllFather() throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.removeAllFather()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      PublicationFatherDAO.removeAllFather(con, pk);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Get all fathers of this publication
   * 
   * @return A collection of NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see java.util.Collection
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Collection getAllFatherPK() throws SQLException {
    return getAllFatherPK(null);
  }

  public Collection getAllFatherPK(String sorting) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.getAllFatherPK()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      Collection result = PublicationFatherDAO.getAllFatherPK(con, pk, sorting);

      return result;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create or update info to this publication
   * 
   * @param modelPK
   *          The modelPk corresponding to the choosen model
   * @param infos
   *          An InfoDetail which contains info to add to the publication
   * @see com.stratelia.webactiv.util.publication.info.model.ModelPK
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void createInfoDetail(ModelPK modelPK, InfoDetail infos)
      throws SQLException, UtilException {
    SilverTrace.info("publication", "PublicationEJB.createInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD", "modelId = " + modelPK.getId());
    Connection con = getConnection();

    try {
      InfoPK iPK = InfoDAO.createInfo(con, modelPK, this.pk);

      if (infos != null) {
        infos.setPK(iPK);
        InfoDAO.addInfoItems(con, infos);
        updateDate = new Date();
      }
      infoPK = iPK;
      isModified = true;
      ejbStore();
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create only info associated to a model
   * 
   * @param modelPK
   *          The modelPk corresponding to the choosen model
   * @param infos
   *          An InfoDetail which contains info to add to the publication
   * @see com.stratelia.webactiv.util.publication.info.model.ModelPK
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void createInfoModelDetail(ModelPK modelPK, InfoDetail infos)
      throws SQLException, UtilException {
    SilverTrace.info("publication", "PublicationEJB.createInfoModelDetail()",
        "root.MSG_GEN_ENTER_METHOD", "modelId = " + modelPK.getId());
    Connection con = getConnection();

    try {
      // The publication have already some info attached (model, attached files,
      // links...)
      if (!infoPK.getId().equals("0")) {
        // Update the model
        InfoDAO.updateInfo(con, modelPK, infoPK);
        if (infos != null) {
          infos.setPK(infoPK);
          // Add info
          InfoDAO.addInfoItems(con, infos);
          updateDate = new Date();
          isModified = true;
        }
      } else {
        // Creation from A to Z
        createInfoDetail(modelPK, infos);
      }
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Get all info associated to this publication
   * 
   * @return All info are in a InfoDetail object
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public InfoDetail getInfoDetail() throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.getInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      InfoDetail result = InfoDAO.getInfoDetailByInfoPK(con, infoPK);
      return result;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Update info associated to this publication
   * 
   * @param infos
   *          An InfoDetail which contains info to update to the publication
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void updateInfoDetail(InfoDetail infos) throws SQLException,
      UtilException {
    SilverTrace.info("publication", "PublicationEJB.updateInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    InfoDetail old = getInfoDetail();

    // update or create textDetail ?
    ArrayList newText = new ArrayList();
    ArrayList oldText = new ArrayList();

    InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoTextList(), old
        .getInfoTextList(), newText, oldText);

    // update or create imageDetail ?
    ArrayList newImage = new ArrayList();
    ArrayList oldImage = new ArrayList();

    InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoImageList(), old
        .getInfoImageList(), newImage, oldImage);

    // update or create linkDetail ?
    // update or create imageDetail ?
    ArrayList newLink = new ArrayList();
    ArrayList oldLink = new ArrayList();

    InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoLinkList(), old
        .getInfoLinkList(), newLink, oldLink);

    Connection con = getConnection();

    try {
      if (this.infoPK.getId().equals("0")) {
        createInfoDetail(new ModelPK("0", this.pk), new InfoDetail(infoPK,
            newText, newImage, newLink, ""));
      } else {
        InfoDAO.updateInfoItems(con, new InfoDetail(infoPK, oldText, oldImage,
            oldLink, ""), infoPK);
        InfoDAO.addInfoItems(con, new InfoDetail(infoPK, newText, newImage,
            newLink, ""));
        updateDate = new Date();
        isModified = true;
      }
    } finally {
      freeConnection(con);
    }
  }

  public void deleteInfoLinks(List pubIds) throws SQLException {
    Connection con = getConnection();

    try {
      String pubId = null;
      PublicationPK targetPK = null;
      for (int p = 0; p < pubIds.size(); p++) {
        pubId = (String) pubIds.get(p);
        targetPK = new PublicationPK(pubId, this.pk.getInstanceId());
        SeeAlsoDAO.deleteLink(con, this.pk, targetPK);
      }
      isModified = true;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Get all info on publication (parameters, model and info)
   * 
   * @return A completePublication
   * @see com.stratelia.webactiv.util.publication.model.CompletePublication
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public CompletePublication getCompletePublication() throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.getCompletePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      // get detail
      PublicationDetail pubDetail = getDetail();

      // get infos
      InfoDetail infoDetail = InfoDAO.getInfoDetailByInfoPK(con, infoPK);
      // get model
      ModelDetail modelDetail = InfoDAO.getModelDetail(con, infoPK);

      // Get links
      List links = SeeAlsoDAO.getLinks(con, pubDetail.getPK());
      ForeignPK link = null;
      InfoLinkDetail infoLink = null;
      for (int l = 0; l < links.size(); l++) {
        link = (ForeignPK) links.get(l);
        infoLink = new InfoLinkDetail(null, "useless", "useless", link.getId());
        infoDetail.getInfoLinkList().add(infoLink);
      }

      return new CompletePublication(pubDetail, modelDetail, infoDetail);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create a new Publication object
   * 
   * @param pubDetail
   *          the PublicationDetail which contains data
   * @return the PublicationPK of the new Publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception javax.ejb.CreateException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public PublicationPK ejbCreate(PublicationDetail pubDetail) {
    SilverTrace.info("publication", "PublicationEJB.ejbCreate()",
        "root.MSG_GEN_ENTER_METHOD", "pubDetail = " + pubDetail.toString());
    Connection con = getConnection();

    try {
      int id = 0;

      // Transform the 'special' caracters
      /*
       * pubDetail.setName(Encode.transformStringForBD(pubDetail.getName()));
       * pubDetail
       * .setDescription(Encode.transformStringForBD(pubDetail.getDescription
       * ()));
       * pubDetail.setKeywords(Encode.transformStringForBD(pubDetail.getKeywords
       * ()));
       * pubDetail.setAuthor(Encode.transformStringForBD(pubDetail.getAuthor
       * ()));
       */

      try {
        id = DBUtil.getNextId(pubDetail.getPK().getTableName(), "pubId");
      } catch (Exception ex) {
        throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
            SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
      }
      pubDetail.getPK().setId(String.valueOf(id));
      try {
        PublicationDAO.insertRow(con, pubDetail);
      } catch (Exception ex) {
        throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_INSERT_ENTITY_ATTRIBUTES", ex);
      }
      if (I18NHelper.isI18N) {
        try {
          createTranslations(con, pubDetail);
        } catch (Exception ex) {
          throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
              SilverpeasRuntimeException.ERROR,
              "root.EX_CANT_INSERT_TRANSLATIONS", ex);
        }
      }
    } finally {
      freeConnection(con);
    }

    pk = pubDetail.getPK();
    if (pubDetail.getInfoId() == null || pubDetail.getInfoId().length() == 0)
      infoPK = new InfoPK("0", pk);
    else
      infoPK = new InfoPK(pubDetail.getInfoId(), pk);
    name = pubDetail.getName();
    description = pubDetail.getDescription();
    creationDate = pubDetail.getCreationDate();
    beginDate = pubDetail.getBeginDate();
    endDate = pubDetail.getEndDate();
    creatorId = pubDetail.getCreatorId();
    importance = pubDetail.getImportance();
    version = pubDetail.getVersion();
    keywords = pubDetail.getKeywords();
    content = pubDetail.getContent();
    status = pubDetail.getStatus();
    image = pubDetail.getImage();
    imageMimeType = pubDetail.getImageMimeType();
    updateDate = new Date();
    updaterId = pubDetail.getUpdaterId();
    beginHour = pubDetail.getBeginHour();
    endHour = pubDetail.getEndHour();
    author = pubDetail.getAuthor();
    targetValidatorId = pubDetail.getTargetValidatorId();
    cloneId = pubDetail.getCloneId();
    cloneStatus = pubDetail.getCloneStatus();
    lang = pubDetail.getLanguage();

    getTranslations();

    return pk;
  }

  /**
   * Method declaration
   * 
   * 
   * @param pubDetail
   * 
   * @throws CreateException
   * 
   * @see
   */
  public void ejbPostCreate(PublicationDetail pubDetail) throws CreateException {
  }

  /**
   * Create an instance of a Publication object
   * 
   * @param pk
   *          the PK of the Publication to instanciate
   * @return the PublicationPK of the instanciated Publication if it exists in
   *         database
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception javax.ejb.FinderException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public PublicationPK ejbFindByPrimaryKey(PublicationPK pk)
      throws FinderException {
    Connection con = getConnection();

    try {
      PublicationPK primary = PublicationDAO.selectByPrimaryKey(con, pk);
      if (primary != null) {
        return primary;
      } else {
        throw new PublicationRuntimeException(
            "PublicationEJB.ejbFindByPrimaryKey()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
            "PubId = " + pk.getId());
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationEJB.ejbFindByPrimaryKey()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
          "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create an instance of a Publication object
   * 
   * @param pk
   *          the PK where the Publication is instanciated
   * @param name
   *          the publication's name to instanciate
   * @return the PublicationPK of the instanciated Publication if it exists in
   *         database
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception javax.ejb.FinderException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public PublicationPK ejbFindByName(PublicationPK pk, String name)
      throws FinderException {
    Connection con = getConnection();

    try {
      PublicationPK primary = PublicationDAO.selectByPublicationName(con, pk,
          name);
      if (primary != null) {
        return primary;
      } else {
        throw new PublicationRuntimeException("PublicationEJB.ejbFindByName()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
            "name = " + name);
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbFindByName()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
          "name = " + name, e);
    } finally {
      freeConnection(con);
    }
  }

  public PublicationPK ejbFindByNameAndNodeId(PublicationPK pk, String name,
      int nodeId) throws FinderException {
    Connection con = getConnection();

    try {
      PublicationPK primary = PublicationDAO.selectByPublicationNameAndNodeId(
          con, pk, name, nodeId);
      if (primary != null) {
        return primary;
      } else {
        throw new PublicationRuntimeException(
            "PublicationEJB.ejbFindByNameAndNodeId()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
            "name = " + name + ", nodeId=" + nodeId);
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationEJB.ejbFindByNameAndNodeId()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
          "name = " + name + ", nodeId=" + nodeId, e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Load publication attributes from database
   * 
   * @since 1.0
   */
  public void ejbLoad() {
    SilverTrace.info("publication", "PublicationEJB.ejbLoad()",
        "root.MSG_GEN_ENTER_METHOD");
    if (pk == null) {
      return;
    }
    PublicationDetail pubDetail = null;
    Connection con = null;

    try {
      if (pk.pubDetail != null) {
        pubDetail = pk.pubDetail;
      } else {
        con = getConnection();
        pubDetail = PublicationDAO.loadRow(con, pk);
      }
      infoPK = new InfoPK(pubDetail.getInfoId(), pk);
      name = pubDetail.getName();
      description = pubDetail.getDescription();
      creationDate = pubDetail.getCreationDate();
      beginDate = pubDetail.getBeginDate();
      endDate = pubDetail.getEndDate();
      creatorId = pubDetail.getCreatorId();
      importance = pubDetail.getImportance();
      version = pubDetail.getVersion();
      keywords = pubDetail.getKeywords();
      content = pubDetail.getContent();
      status = pubDetail.getStatus();
      image = pubDetail.getImage();
      imageMimeType = pubDetail.getImageMimeType();
      updateDate = pubDetail.getUpdateDate();
      updaterId = pubDetail.getUpdaterId();
      beginHour = pubDetail.getBeginHour();
      endHour = pubDetail.getEndHour();
      author = pubDetail.getAuthor();
      targetValidatorId = pubDetail.getTargetValidatorId();
      cloneId = pubDetail.getCloneId();
      cloneStatus = pubDetail.getCloneStatus();
      lang = pubDetail.getLanguage();

      getTranslations();

      isModified = false;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbLoad()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Store publication attributes into database
   * 
   * @since 1.0
   */
  public void ejbStore() {
    if (!isModified) {
      return;
    }
    if (pk == null) {
      return;
    }
    // Transform the 'special' caracters
    /*
     * name = Encode.transformStringForBD(name); description =
     * Encode.transformStringForBD(description); keywords =
     * Encode.transformStringForBD(keywords); auhor =
     * Encode.transformStringForBD(author);
     */

    PublicationDetail detail = new PublicationDetail(pk, name, description,
        creationDate, beginDate, endDate, creatorId, importance, version,
        keywords, content, status, image, imageMimeType, updateDate, updaterId,
        validateDate, validatorId, author);
    detail.setBeginHour(beginHour);
    detail.setEndHour(endHour);
    detail.setTargetValidatorId(targetValidatorId);
    detail.setCloneId(cloneId);
    detail.setCloneStatus(cloneStatus);
    detail.setLanguage(lang);
    detail.setInfoId(infoPK.getId());

    Connection con = getConnection();
    try {
      PublicationDAO.storeRow(con, detail);
      isModified = false;
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbStore()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Delete this Publication and all info associated
   * 
   * @since 1.0
   */
  public void ejbRemove() {
    SilverTrace.info("publication", "PublicationEJB.ejbRemove()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      // delete links from another publication to removed publication
      InfoDAO.deleteInfoLinkByTargetLink(con, infoPK, pk.getId());

      SeeAlsoDAO.deleteLinksByObjectId(con, pk);
      SeeAlsoDAO.deleteLinksByTargetId(con, pk);

      // delete all info associated from database
      InfoDAO.deleteInfoDetailByInfoPK(con, infoPK);

      // delete translations
      PublicationI18NDAO.removeTranslations(con, pk);

      // delete publication from database
      PublicationDAO.deleteRow(con, pk);
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbRemove()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_DELETE_ENTITY",
          "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbActivate() {
    pk = (PublicationPK) context.getPrimaryKey();
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbPassivate() {
    pk = null;
  }

  /**
   * Method declaration
   * 
   * 
   * @param ec
   * 
   * @see
   */
  public void setEntityContext(EntityContext ec) {
    context = ec;
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void unsetEntityContext() {
    context = null;
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.PUBLICATION_DATASOURCE);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

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

  public String getValidatorId() {
    return validatorId;
  }

  public Date getValidateDate() {
    return validateDate;
  }
}