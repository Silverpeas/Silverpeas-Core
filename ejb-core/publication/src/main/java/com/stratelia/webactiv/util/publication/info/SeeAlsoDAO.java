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
package com.stratelia.webactiv.util.publication.info;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

/*
 * CVS Informations
 * 
 * $Id: SeeAlsoDAO.java,v 1.2 2007/02/27 15:03:34 neysseri Exp $
 * 
 * $Log: SeeAlsoDAO.java,v $
 * Revision 1.2  2007/02/27 15:03:34  neysseri
 * no message
 *
 * Revision 1.1  2006/12/01 15:01:37  neysseri
 * no message
 *
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
public class SeeAlsoDAO {
  private static String SEEALSO_TABLENAME = "SB_SeeAlso_Link";

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SeeAlsoDAO() {
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
  public static void addLink(Connection con, WAPrimaryKey objectPK,
      WAPrimaryKey targetPK) throws SQLException {
    int newId = -1;

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = DBUtil.getNextId(SEEALSO_TABLENAME, "id");
    } catch (Exception ex) {
      throw new PublicationRuntimeException("SeeAlsoDAO.addLink()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
    }

    String insertStatement = "insert into " + SEEALSO_TABLENAME
        + " values ( ? , ? , ? , ? , ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(3, objectPK.getInstanceId());
      prepStmt.setInt(4, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(5, targetPK.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLink(Connection con, WAPrimaryKey objectPK,
      WAPrimaryKey targetPK) throws SQLException {
    String deleteStatement = "delete from "
        + SEEALSO_TABLENAME
        + " where objectId = ? AND objectInstanceId = ? AND targetId = ? AND targetInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      prepStmt.setInt(3, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(4, targetPK.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLinksByObjectId(Connection con, WAPrimaryKey objectPK)
      throws SQLException {
    String deleteStatement = "delete from " + SEEALSO_TABLENAME
        + " where objectId = ? AND objectInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLinksByTargetId(Connection con, WAPrimaryKey targetPK)
      throws SQLException {
    String deleteStatement = "delete from " + SEEALSO_TABLENAME
        + " where targetId = ? AND targetInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(2, targetPK.getInstanceId());
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
  public static List getLinks(Connection con, WAPrimaryKey objectPK)
      throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select targetId, targetInstanceId from "
        + SEEALSO_TABLENAME + " where objectId  = ? AND objectInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      rs = prepStmt.executeQuery();

      String targetId = "";
      String targetInstanceId = "";
      List list = new ArrayList();
      while (rs.next()) {
        targetId = Integer.toString(rs.getInt(1));
        targetInstanceId = rs.getString(2);
        ForeignPK targetPK = new ForeignPK(targetId, targetInstanceId);

        list.add(targetPK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}