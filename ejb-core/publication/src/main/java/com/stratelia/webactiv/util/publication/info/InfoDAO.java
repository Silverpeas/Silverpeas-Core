package com.stratelia.webactiv.util.publication.info;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImagePK;
import com.stratelia.webactiv.util.publication.info.model.InfoLinkDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoLinkPK;
import com.stratelia.webactiv.util.publication.info.model.InfoPK;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextPK;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

/*
 * CVS Informations
 * 
 * $Id: InfoDAO.java,v 1.13 2006/09/13 13:07:31 neysseri Exp $
 * 
 * $Log: InfoDAO.java,v $
 * Revision 1.13  2006/09/13 13:07:31  neysseri
 * Extension de la taille du htmlDisplayer et htmlEditor grâce à plusieurs lignes en BdD
 *
 * Revision 1.12  2006/07/10 16:23:12  neysseri
 * no message
 *
 * Revision 1.11  2006/06/23 13:14:55  neysseri
 * no message
 *
 * Revision 1.10.6.1  2006/06/23 12:47:14  neysseri
 * no message
 *
 * Revision 1.10  2005/05/19 14:54:16  neysseri
 * Possibilité de supprimer les Voir Aussi
 *
 * Revision 1.9  2004/06/22 15:34:59  neysseri
 * nettoyage eclipse
 *
 * Revision 1.8  2004/02/06 18:48:03  neysseri
 * Attachments no more implemented by submodule info.
 *
 * Revision 1.7  2003/11/25 08:30:19  cbonin
 * no message
 *
 * Revision 1.6  2003/11/24 10:34:03  cbonin
 * no message
 *
 * Revision 1.5  2003/06/21 00:35:37  neysseri
 * no message
 *
 * Revision 1.4  2003/01/15 10:07:24  scotte
 * Correction : pb de deplacement des contenus des champs des modèles sous Oracle
 *
 * Revision 1.3  2002/12/20 09:17:17  cbonin
 * Report Bug OCISI :
 * utilisation du File.separator au lieu de "\"
 *
 * Revision 1.2  2002/12/18 07:39:27  neysseri
 * Bug fixing about links between publications
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.16  2002/08/05 09:45:09  neysseri
 * Correction du bug HHB sur les méthodes :
 * - deleteInfoTextByInfoPK()
 * - deleteInfoImageByInfoPK()
 *
 * Les requetes de suppression n'était pas correctes du tout !!!
 *
 * Revision 1.15  2002/04/03 09:03:00  neysseri
 * Suppression de l'appel de la requete
 * qui récupère les attachments (remplacé pas le module attachment)
 *
 * Revision 1.14  2002/01/11 12:40:55  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class InfoDAO {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public InfoDAO() {
  }

  // return a ModelDetail collection of available models

  /**
   * Method declaration
   * 
   * 
   * @param con
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getAllModelsDetail(Connection con)
      throws SQLException {
    ResultSet rs = null;
    ModelPK mPK = new ModelPK("unknown");
    String selectStatement = "select id, name, description, imageName, htmlDisplayer, htmlEditor from "
        + mPK.getTableName() + " order by id asc, partId asc";
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);

      String id = "";
      String name = "";
      String description = "";
      String imageName = "";
      String htmlDisplayer = "";
      String htmlEditor = "";
      String memId = "-1";
      ModelDetail modelDetail = null;
      ArrayList list = new ArrayList();
      while (rs.next()) {
        id = new Integer(rs.getInt(1)).toString();
        if (!id.equals(memId)) {
          // this is a new model
          if (name.length() > 0) {
            // we must add the new model object
            modelDetail = new ModelDetail(memId, name, description, imageName,
                htmlDisplayer, htmlEditor);
            list.add(modelDetail);
          }

          name = rs.getString(2);
          description = rs.getString(3);
          imageName = rs.getString(4);
          htmlDisplayer = rs.getString(5);
          htmlEditor = rs.getString(6);
          memId = id;
        } else {
          htmlDisplayer += rs.getString(5);
          htmlEditor += rs.getString(6);
        }
      }
      if (name.length() > 0) {
        // we must add the last model object
        modelDetail = new ModelDetail(memId, name, description, imageName,
            htmlDisplayer, htmlEditor);
        list.add(modelDetail);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  // return the ModelDetail associated to the infoPK

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static ModelDetail getModelDetail(Connection con, InfoPK infoPK)
      throws SQLException {
    SilverTrace.info("publication", "InfoDAO.getModelDetail(infoPK)",
        "root.MSG_GEN_PARAM_VALUE", "Info = " + infoPK.getId());

    if (!isInteger(infoPK.getId()))
      return null;

    ResultSet rs = null;
    ModelPK mPK = new ModelPK("unknown", infoPK);
    ModelDetail detail = null;
    String selectStatement = "select M.id, M.name, M.description, M.imageName, M.htmlDisplayer, M.htmlEditor "
        + "from "
        + mPK.getTableName()
        + " M, "
        + infoPK.getTableName()
        + " I "
        + "where I.modelId = M.Id "
        + "and I.infoId = ? "
        + "order by M.partId asc";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      rs = prepStmt.executeQuery();
      /*
       * if (rs.next()) { String id = new Integer(rs.getInt(1)).toString();
       * String name = rs.getString(2); String description = rs.getString(3);
       * String imageName = rs.getString(4); String htmlDisplayer =
       * rs.getString(5); String htmlEditor = rs.getString(6);
       * 
       * detail = new ModelDetail(id, name, description, imageName,
       * htmlDisplayer, htmlEditor); }
       */
      boolean firstModelPartReaden = false;
      String htmlDisplayer = "";
      String htmlEditor = "";
      String id = null;
      String name = null;
      String description = null;
      String imageName = null;
      while (rs.next()) {
        if (!firstModelPartReaden) {
          // It's the first part of the model
          id = new Integer(rs.getInt(1)).toString();
          name = rs.getString(2);
          description = rs.getString(3);
          imageName = rs.getString(4);
          htmlDisplayer = rs.getString(5);
          htmlEditor = rs.getString(6);

          firstModelPartReaden = true;
        } else {
          htmlDisplayer += rs.getString(5);
          htmlEditor += rs.getString(6);
        }
      }
      if (firstModelPartReaden) {
        detail = new ModelDetail(id, name, description, imageName,
            htmlDisplayer, htmlEditor);
        SilverTrace.info("publication", "InfoDAO.getModelDetail()",
            "root.MSG_GEN_PARAM_VALUE", "detail != null");
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return detail;
  }

  // return a ModelDetail

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param modelPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static ModelDetail getModelDetail(Connection con, ModelPK modelPK)
      throws SQLException {
    SilverTrace.info("publication", "InfoDAO.getModelDetail(modelPK)",
        "root.MSG_GEN_PARAM_VALUE", "Model = " + modelPK.getId());

    ModelDetail modelDetail = null;
    ResultSet rs = null;
    String selectStatement = "select id, name, description, imageName, htmlDisplayer, htmlEditor "
        + "from "
        + modelPK.getTableName()
        + " where id="
        + modelPK.getId()
        + " order by partId asc";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      /*
       * if (rs.next()) { String id = new Integer(rs.getInt(1)).toString();
       * String name = rs.getString(2); String description = rs.getString(3);
       * String imageName = rs.getString(4); String htmlDisplayer =
       * rs.getString(5); String htmlEditor = rs.getString(6);
       * 
       * modelDetail = new ModelDetail(id, name, description, imageName,
       * htmlDisplayer, htmlEditor); }
       */
      String htmlDisplayer = "";
      String htmlEditor = "";
      String id = null;
      String name = null;
      String description = null;
      String imageName = null;
      boolean firstModelPartReaden = false;
      while (rs.next()) {
        if (!firstModelPartReaden) {
          // It's the first part of the model
          id = new Integer(rs.getInt(1)).toString();
          name = rs.getString(2);
          description = rs.getString(3);
          imageName = rs.getString(4);
          htmlDisplayer = rs.getString(5);
          htmlEditor = rs.getString(6);

          firstModelPartReaden = true;
        } else {
          htmlDisplayer += rs.getString(5);
          htmlEditor += rs.getString(6);
        }
      }
      if (firstModelPartReaden) {
        modelDetail = new ModelDetail(id, name, description, imageName,
            htmlDisplayer, htmlEditor);
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException("InfoDAO.getModelDetail(modelPK)",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_MODEL_FAILED",
          "modelPK = " + modelPK.toString(), e);
    } finally {
      DBUtil.close(rs, stmt);
    }

    SilverTrace.info("publication", "InfoDAO.getModelDetail(modelPK)",
        "root.MSG_GEN_PARAM_VALUE", "ModelDetail = " + modelDetail);

    return modelDetail;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pubPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static InfoPK hasInfo(Connection con, PublicationPK pubPK)
      throws SQLException {
    SilverTrace.info("publication", "InfoDAO.hasInfo",
        "root.MSG_GEN_PARAM_VALUE", "Pub = " + pubPK.getId());

    InfoPK infoPK = new InfoPK("0", pubPK);

    String selectStatement = "select I.infoId " + "from "
        + infoPK.getTableName() + " I, " + pubPK.getTableName() + " P "
        + "where P.infoId = I.infoId " + "and P.pubId=" + pubPK.getId();
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      if (rs.next()) {
        int infoId = rs.getInt(1);

        infoPK = new InfoPK("unknown", pubPK);
        infoPK.setId(new Integer(infoId).toString());
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    SilverTrace.info("publication", "InfoDAO.hasInfo",
        "root.MSG_GEN_PARAM_VALUE", "InfoId = " + infoPK.getId());

    return infoPK;
  }

  // create the info reference
  // match the info with a model

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param modelPK
   * @param pubPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static InfoPK createInfo(Connection con, ModelPK modelPK,
      PublicationPK pubPK) throws SQLException {
    InfoPK infoPK = null;

    infoPK = hasInfo(con, pubPK);

    SilverTrace.info("publication", "InfoDAO.createInfo",
        "root.MSG_GEN_PARAM_VALUE", "Pub = " + pubPK.getId());
    if (infoPK != null && !infoPK.getId().equals("0")) {
      SilverTrace.info("publication", "InfoDAO.createInfo",
          "root.MSG_GEN_PARAM_VALUE", "Pub = " + pubPK.getId()
              + ", infos existent");
      return infoPK;
    } else {
      SilverTrace.info("publication", "InfoDAO.createInfo",
          "root.MSG_GEN_PARAM_VALUE", "Pub = " + pubPK.getId()
              + ", creation infos");
      infoPK = new InfoPK("unknown", modelPK);

      /* Recherche de la nouvelle PK de la table Info */
      String newId = null;
      String tableName = infoPK.getTableName();

      try {
        newId = new Integer(DBUtil.getNextId(tableName, new String("infoId")))
            .toString();
      } catch (Exception ex) {
        throw new PublicationRuntimeException("InfoDAO.createInfo()",
            SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
      }
      String insertStatement = "insert into " + infoPK.getTableName()
          + " values ( ? , ? , ? , ?)";
      PreparedStatement prepStmt = null;
      SilverTrace.info("publication", "InfoDAO.createInfo",
          "root.MSG_GEN_PARAM_VALUE", "InsertStatement = " + insertStatement
              + " (" + new Integer(newId).toString() + ", "
              + new Integer(modelPK.getId()).toString() + ", null, "
              + pubPK.getComponentName() + " )");

      try {
        prepStmt = con.prepareStatement(insertStatement);
        prepStmt.setInt(1, new Integer(newId).intValue());
        prepStmt.setInt(2, new Integer(modelPK.getId()).intValue());
        prepStmt.setString(3, null);
        prepStmt.setString(4, pubPK.getComponentName());
        prepStmt.executeUpdate();
      } finally {
        DBUtil.close(prepStmt);
      }
      infoPK.setId(newId);
    }

    return infoPK;
  }

  // update the info reference
  // match the info with a model

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param modelPK
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void updateInfo(Connection con, ModelPK modelPK, InfoPK infoPK)
      throws SQLException {
    SilverTrace.info("publication", "InfoDAO.updateInfo",
        "root.MSG_GEN_PARAM_VALUE", "Info = " + infoPK.getId());

    String updateStatement = "update " + infoPK.getTableName()
        + " set infoId = ? , modelId = ? , infoContent = ?, instanceId = ?"
        + " where infoId = ? ";
    PreparedStatement prepStmt = null;
    SilverTrace.info("publication", "InfoDAO.updateInfo",
        "root.MSG_GEN_PARAM_VALUE", "UpdateStatement = " + updateStatement
            + " (" + new Integer(infoPK.getId()).toString() + ", "
            + new Integer(modelPK.getId()).toString() + ", null, "
            + infoPK.getComponentName() + " )");

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      prepStmt.setInt(2, new Integer(modelPK.getId()).intValue());
      prepStmt.setString(3, null);
      prepStmt.setString(4, infoPK.getComponentName());
      prepStmt.setInt(5, new Integer(infoPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteInfo(Connection con, InfoPK infoPK)
      throws SQLException {

    String deleteStatement = "delete from " + infoPK.getTableName()
        + " where infoId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      // prepStmt.setString(1, infoPK.getId());
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  // match the info with a model

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infos
   * 
   * @throws SQLException
   * @throws UtilException
   * 
   * @see
   */
  public static void addInfoItems(Connection con, InfoDetail infos)
      throws SQLException, UtilException {
    SilverTrace.info("publication", "InfoDAO.addInfoItems",
        "root.MSG_GEN_PARAM_VALUE", "Info = " + infos.getPK().getId());

    // Treatement of the infoText
    Collection textList = infos.getInfoTextList();

    if (textList != null) {
      Iterator textListIterator = textList.iterator();

      while (textListIterator.hasNext()) {
        InfoTextDetail infoText = (InfoTextDetail) textListIterator.next();

        addInfoText(con, infos.getPK(), infoText);
      }
    }

    // Treatement of the infoImage
    Collection imageList = infos.getInfoImageList();

    if (imageList != null) {
      Iterator imageListIterator = imageList.iterator();

      while (imageListIterator.hasNext()) {
        InfoImageDetail infoImage = (InfoImageDetail) imageListIterator.next();

        addInfoImage(con, infos.getPK(), infoImage);
      }
    }

    // Treatement of the infoLink
    Collection linkList = infos.getInfoLinkList();

    if (linkList != null) {
      Iterator linkListIterator = linkList.iterator();

      while (linkListIterator.hasNext()) {
        InfoLinkDetail infoLink = (InfoLinkDetail) linkListIterator.next();

        addInfoLink(con, infos.getPK(), infoLink);
      }
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infos
   * @param infoPK
   * 
   * @throws SQLException
   * @throws UtilException
   * 
   * @see
   */
  public static void updateInfoItems(Connection con, InfoDetail infos,
      InfoPK infoPK) throws SQLException, UtilException {
    SilverTrace.info("publication", "InfoDAO.updateInfoItems",
        "root.MSG_GEN_PARAM_VALUE", "Info = " + infoPK.getId());

    // Treatement of the infoText
    if (infos.getInfoTextList() != null) {
      Collection textList = infos.getInfoTextList();
      Iterator textListIterator = textList.iterator();

      while (textListIterator.hasNext()) {
        InfoTextDetail infoText = (InfoTextDetail) textListIterator.next();

        updateInfoText(con, infoText, infoPK);
      }
    }

    // Treatement of the infoImage
    if (infos.getInfoImageList() != null) {
      Collection imageList = infos.getInfoImageList();
      Iterator imageListIterator = imageList.iterator();

      while (imageListIterator.hasNext()) {
        InfoImageDetail infoImage = (InfoImageDetail) imageListIterator.next();

        updateInfoImage(con, infoPK, infoImage);
      }
    }

    // Treatement of the infoLink
    Collection linkList = infos.getInfoLinkList();

    if (linkList != null) {
      Iterator linkListIterator = linkList.iterator();

      while (linkListIterator.hasNext()) {
        InfoLinkDetail infoLink = (InfoLinkDetail) linkListIterator.next();

        addInfoLink(con, infoPK, infoLink);
      }
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteInfoItems(Connection con, InfoPK infoPK)
      throws SQLException {
    // Treatement of the infoText
    deleteInfoTextByInfoPK(con, infoPK);
    // Treatement of the infoImage
    deleteInfoImageByInfoPK(con, infoPK);
    // Treatement of the infoLink
    deleteInfoLinkByInfoPK(con, infoPK);
  }

  // match the info with a model

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * @param infoText
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void addInfoText(Connection con, InfoPK infoPK,
      InfoTextDetail infoText) throws SQLException {
    String newId = null;
    InfoTextPK infotextPK = new InfoTextPK("unknown", infoPK);
    String tableName = infotextPK.getTableName();

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = new Integer(DBUtil.getNextId(tableName, new String("infoTextId")))
          .toString();
    } catch (Exception ex) {
      throw new PublicationRuntimeException("InfoDAO.addInfoText()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
    }

    String insertStatement = "insert into " + tableName
        + " values ( ? , ? , ? , ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, new Integer(newId).intValue());
      prepStmt.setInt(2, new Integer(infoPK.getId()).intValue());
      prepStmt.setString(3, infoText.getContent());
      prepStmt.setInt(4, new Integer(infoText.getOrder()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoText
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void updateInfoText(Connection con, InfoTextDetail infoText,
      InfoPK infoPK) throws SQLException {
    InfoTextPK infotextPK = new InfoTextPK("unknown", infoPK);
    String tableName = infotextPK.getTableName();

    String updateStatement = "update " + tableName + " set "
        + " infoTextContent = ?  where infoTextId = ? "
        + " and infoId in ( select infoId from " + infoPK.getTableName()
        + " where  instanceId = ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setString(1, infoText.getContent());
      prepStmt.setInt(2, new Integer(infoText.getId()).intValue());
      prepStmt.setString(3, infoPK.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void deleteInfoTextByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {
    InfoTextPK infotextPK = new InfoTextPK("unknown", infoPK);

    // String deleteQuery = "delete from " + infotextPK.getTableName() +
    // " where infoId in (" + " select infoId from " + infoPK.getTableName() +
    // " where instanceId ='" + infoPK.getComponentName() + "')";
    String deleteQuery = "delete from " + infotextPK.getTableName()
        + " where infoId = " + infoPK.getId();
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * @param infoImage
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void addInfoImage(Connection con, InfoPK infoPK,
      InfoImageDetail infoImage) throws SQLException {
    String newId = null;
    InfoImagePK infoImagePK = new InfoImagePK("unknown", infoPK);
    String tableName = infoImagePK.getTableName();

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = new Integer(DBUtil
          .getNextId(tableName, new String("infoImageId"))).toString();
    } catch (Exception ex) {
      throw new PublicationRuntimeException("InfoDAO.addInfoImage()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
    }

    String insertStatement = "insert into " + tableName
        + " values ( ? , ? , ? , ? , ? , ? , ? , ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, new Integer(newId).intValue());
      prepStmt.setInt(2, new Integer(infoPK.getId()).intValue());
      prepStmt.setString(3, infoImage.getPhysicalName());
      prepStmt.setString(4, infoImage.getLogicalName());
      prepStmt.setString(5, infoImage.getDescription());
      prepStmt.setString(6, infoImage.getType());
      prepStmt.setInt(7, new Long(infoImage.getSize()).intValue());
      prepStmt.setInt(8, new Integer(infoImage.getOrder()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * @param infoImage
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void updateInfoImage(Connection con, InfoPK infoPK,
      InfoImageDetail infoImage) throws SQLException {
    InfoImagePK infoImagePK = new InfoImagePK("unknown", infoPK);
    String tableName = infoImagePK.getTableName();

    String updateStatement = "update "
        + tableName
        + " set infoId = ? , "
        + "infoImagePhysicalName = ? , infoImageLogicalName = ? , infoImageDescription = ? , infoImageType = ? , infoImageSize = ? , infoImageDisplayOrder = ? "
        + " where infoImageId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      prepStmt.setString(2, infoImage.getPhysicalName());
      prepStmt.setString(3, infoImage.getLogicalName());
      prepStmt.setString(4, infoImage.getDescription());
      prepStmt.setString(5, infoImage.getType());
      prepStmt.setInt(6, new Long(infoImage.getSize()).intValue());
      prepStmt.setInt(7, new Integer(infoImage.getOrder()).intValue());
      prepStmt.setInt(8, new Integer(infoImage.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteInfoImageByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {
    InfoImagePK infoImagePK = new InfoImagePK("unknown", infoPK);

    // String deleteQuery = "delete from " + tableName +
    // " where infoId in ( select infoId from " + infoPK.getTableName() +
    // " where  instanceId = ? )";
    String deleteQuery = "delete from " + infoImagePK.getTableName()
        + " where infoId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteQuery);
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * @param infoLink
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void addInfoLink(Connection con, InfoPK infoPK,
      InfoLinkDetail infoLink) throws SQLException {
    String newId = null;
    InfoLinkPK infoLinkPK = new InfoLinkPK("unknown", infoPK);
    String tableName = infoLinkPK.getTableName();

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = new Integer(DBUtil.getNextId(tableName, new String("infoLinkId")))
          .toString();
    } catch (Exception ex) {
      throw new PublicationRuntimeException("InfoDAO.addInfoLink()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
    }

    String insertStatement = "insert into " + tableName
        + " values ( ? , ? , ? , ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, new Integer(newId).intValue());
      prepStmt.setInt(2, new Integer(infoPK.getId()).intValue());
      prepStmt.setInt(3, new Integer(infoLink.getTargetId()).intValue());
      prepStmt.setInt(4, new Integer(infoLink.getOrder()).intValue());

      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * @param linkIds
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteInfoLinksByPKs(Connection con, InfoPK infoPK,
      Collection linkIds) throws SQLException {
    Iterator iterator = linkIds.iterator();
    String linkId = "";
    InfoLinkPK linkPK = null;

    while (iterator.hasNext()) {
      linkId = (String) iterator.next();
      linkPK = new InfoLinkPK(linkId, infoPK);
      deleteInfoLinkByPK(con, linkPK);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param linkPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void deleteInfoLinkByPK(Connection con, InfoLinkPK linkPK)
      throws SQLException {
    String tableName = linkPK.getTableName();
    String deleteStatement = "delete from " + tableName
        + " where infoLinkId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, new Integer(linkPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteInfoLinks(Connection con, InfoPK infoPK, List pubIds)
      throws SQLException {
    Iterator iterator = pubIds.iterator();
    String pubId = "";

    while (iterator.hasNext()) {
      pubId = (String) iterator.next();
      deleteInfoLink(con, infoPK, pubId);
    }
  }

  public static void deleteInfoLink(Connection con, InfoPK infoPK, String pubId)
      throws SQLException {
    InfoLinkPK infoLinkPK = new InfoLinkPK("unknown", infoPK);
    String tableName = infoLinkPK.getTableName();

    String deleteStatement = "delete from " + tableName
        + " where infoId = ? AND pubId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(infoPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(pubId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void deleteInfoLinkByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {
    InfoLinkPK linkPK = new InfoLinkPK("unknown", infoPK);
    String tableName = linkPK.getTableName();
    String deleteStatement = "delete from " + tableName + " where infoId  = ?";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * @param targetLink
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteInfoLinkByTargetLink(Connection con, InfoPK infoPK,
      String targetLink) throws SQLException {
    InfoLinkPK linkPK = new InfoLinkPK("unknown", infoPK);
    String tableName = linkPK.getTableName();
    String deleteStatement = "delete from " + tableName + " where pubId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, new Integer(targetLink).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static InfoDetail getInfoDetailByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {
    Collection textList = null;
    Collection imageList = null;
    Collection linkList = null;
    if (isInteger(infoPK.getId())) {
      textList = getAllInfoTextByInfoPK(con, infoPK);
      imageList = getAllInfoImageByInfoPK(con, infoPK);
      linkList = getAllInfoLinkByInfoPK(con, infoPK);
    } else {
      textList = new ArrayList();
      imageList = new ArrayList();
      linkList = new ArrayList();
    }

    return new InfoDetail(infoPK, textList, imageList, linkList, null);
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
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteInfoDetailByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {
    if (isInteger(infoPK.getId())) {
      deleteInfoItems(con, infoPK);
      deleteInfo(con, infoPK);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static Collection getAllInfoTextByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {
    ResultSet rs = null;
    InfoTextPK infoTextPK = new InfoTextPK("unknown", infoPK);
    String tableName = infoTextPK.getTableName();
    String selectStatement = "select * from " + tableName + " where infoId = "
        + infoPK.getId() + " order  by (infoTextDisplayOrder) ASC";
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      String id = "";
      String content = "";
      String displayOrder = "";
      ArrayList list = new ArrayList();
      while (rs.next()) {
        id = new Integer(rs.getInt(1)).toString();
        content = rs.getString(3);
        displayOrder = new Integer(rs.getInt(4)).toString();
        InfoTextDetail infoTextDetail = new InfoTextDetail(infoPK,
            displayOrder, id, content);

        list.add(infoTextDetail);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static Collection getAllInfoImageByInfoPK(Connection con,
      InfoPK infoPK) throws SQLException {
    ResultSet rs = null;
    InfoImagePK infoImagePK = new InfoImagePK("unknown", infoPK);
    String tableName = infoImagePK.getTableName();
    String selectStatement = "select * from " + tableName + " where infoId = "
        + infoPK.getId() + " order  by (infoImageDisplayOrder) ASC";
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);

      String id = "";
      String physicalName = "";
      String logicalName = "";
      String description = "";
      String type = "";
      int size;
      String displayOrder = "";
      ArrayList list = new ArrayList();
      while (rs.next()) {
        id = new Integer(rs.getInt(1)).toString();
        physicalName = rs.getString(3);
        logicalName = rs.getString(4);
        description = rs.getString(5);
        type = rs.getString(6);
        size = rs.getInt(7);
        displayOrder = new Integer(rs.getInt(8)).toString();
        InfoImageDetail infoImageDetail = new InfoImageDetail(infoPK,
            displayOrder, id, physicalName, logicalName, description, type,
            size);

        list.add(infoImageDetail);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param infoPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static Collection getAllInfoLinkByInfoPK(Connection con, InfoPK infoPK)
      throws SQLException {

    ResultSet rs = null;
    InfoLinkPK infoLinkPK = new InfoLinkPK("unknown", infoPK);
    String tableName = infoLinkPK.getTableName();
    String selectStatement = "select * from " + tableName
        + " where infoId  = ?  order by (infoLinkDisplayOrder) ASC";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);

    try {
      prepStmt.setInt(1, new Integer(infoPK.getId()).intValue());
      rs = prepStmt.executeQuery();

      String id = "";
      String targetId = "";
      String displayOrder = "";
      ArrayList list = new ArrayList();
      while (rs.next()) {
        id = new Integer(rs.getInt(1)).toString();
        targetId = new Integer(rs.getInt(3)).toString();
        displayOrder = new Integer(rs.getInt(4)).toString();
        InfoLinkDetail infoLinkDetail = new InfoLinkDetail(infoPK,
            displayOrder, id, targetId);

        list.add(infoLinkDetail);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}
