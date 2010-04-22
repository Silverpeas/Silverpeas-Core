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

package com.stratelia.silverpeas.wysiwyg.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.FinderException;
import javax.naming.NamingException;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * @author neysseri
 */
/*
 * CVS Informations $Id: WysiwygController.java,v 1.22 2009/03/26 14:16:24 neysseri Exp $ $Log:
 * WysiwygController.java,v $ Revision 1.22 2009/03/26 14:16:24 neysseri Le copier/coller de contenu
 * Wysiwyg n'était pas correct. Problème de réécriture d'URL des images. Revision 1.21 2009/02/20
 * 07:51:23 neysseri Ajout d'un paramètre IndexIt permettant de spécifier si l'indexation autonome
 * doit être effectué lors de la création/modification Par défaut = true Sinon passez False et
 * utiliser le mécanisme de Callback Revision 1.20 2008/05/21 14:01:25 neysseri no message Revision
 * 1.19.2.2 2008/05/06 09:40:35 ehugonnet Renommage du calcul du context Revision 1.19.2.1
 * 2008/04/29 09:04:16 ehugonnet Extraction de getContext d'AttachmentController dans
 * FileRepositoryManager Revision 1.19 2008/03/26 13:16:30 neysseri no message Revision 1.18
 * 2008/02/26 15:21:13 dlesimple Suppression de spaceId ds getWysiwyg Revision 1.17 2007/12/03
 * 13:25:43 neysseri no message Revision 1.16.2.5 2007/11/21 11:44:07 neysseri no message Revision
 * 1.16.2.4 2007/11/20 15:39:45 neysseri no message Revision 1.16.2.3 2007/11/07 10:31:37 neysseri
 * no message Revision 1.16.2.2 2007/11/05 16:03:46 neysseri no message Revision 1.16.2.1 2007/10/31
 * 17:14:34 neysseri no message Revision 1.16 2007/06/25 11:53:11 cbonin correction bug liens si
 * UNIX Revision 1.15 2007/04/20 14:22:56 neysseri no message Revision 1.14.2.1 2007/03/29 14:13:27
 * neysseri no message Revision 1.14 2006/09/18 07:10:02 neysseri Suppression des méthodes
 * d'indexation. Ce sont les modules utilisants le wysiwyg qui doivent indexer le contenu et pas le
 * wysiwyg lui-même ! Revision 1.13 2005/10/20 10:53:30 neysseri no message Revision 1.12 2005/10/13
 * 19:15:05 neysseri no message Revision 1.11 2005/07/07 18:04:01 neysseri Nettoyage sources
 * Revision 1.10 2005/07/04 09:50:45 dlesimple Tiny MCE v1.45 Revision 1.9 2005/04/22 12:15:51
 * neysseri Added : - deleteFileAndAttachment() Updated : - updateFileAndAttachment() -->
 * synchronized Revision 1.8 2005/04/21 11:01:31 neysseri Ajout du créateur du Wysiwyg (transmis à
 * attachment) Revision 1.7 2005/04/07 18:15:11 neysseri no message Revision 1.6.2.1 2005/03/11
 * 19:19:55 sdevolder *** empty log message *** Revision 1.6 2004/07/26 08:29:28 neysseri no message
 * Revision 1.5 2004/07/23 16:33:11 neysseri Bug copier/coller Revision 1.4 2004/02/06 18:49:52
 * neysseri Now, the both indexation methods are deprecated ! Revision 1.3 2003/09/19 13:12:43
 * neysseri no message Revision 1.2 2002/10/09 07:41:03 neysseri no message Revision 1.1.1.1.6.1
 * 2002/09/27 08:07:49 abudnikau Remove debug Revision 1.1.1.1 2002/08/06 14:47:55 nchaix no message
 * Revision 1.11 2002/04/17 08:06:30 nchaix no message Revision 1.10 2002/01/31 12:13:35 neysseri no
 * message Revision 1.9 2002/01/18 16:55:10 neysseri Stabilisation Lot 2 : Exception et Silvertraces
 */

public class WysiwygController {

  public static String WYSIWYG_CONTEXT = "wysiwyg";
  public static String WYSIWYG_IMAGES = "Images";
  public static String WYSIWYG_WEBSITES = "webSites";

  /**
   * the constructor.
   */
  public WysiwygController() {
  }

  /**
   * Method declaration turn over all the files attached according to the parameters id, spaceId,
   * componentId, context.
   * @param id type String: for example pubId.
   * @param spaceId type String: the id of space.
   * @param componentId type String: the id of component.
   * @param context type String: for example images
   * @return imagesList a table of string[N][2] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static String[][] searchAllAttachments(String id, String spaceId, String componentId,
      String context) /* throws /*WysiwygException, FinderException, NamingException, SQLException */
  {
    AttachmentPK foreignKey = new AttachmentPK(id, spaceId, componentId);

    Vector vectAttachment =
        AttachmentController.searchAttachmentByPKAndContext(foreignKey, context);
    int nbImages = vectAttachment.size();
    String[][] imagesList = new String[nbImages][2];

    for (int i = 0; i < nbImages; i++) {
      AttachmentDetail attD = (AttachmentDetail) vectAttachment.elementAt(i);

      String path =
          FileServerUtils.getUrl(spaceId, componentId, attD.getLogicalName(), attD
          .getPhysicalName(), attD.getType(), "Attachment/" + context);

      imagesList[i][0] = path;
      imagesList[i][1] = attD.getLogicalName();
      SilverTrace.info("wysiwyg", "WysiwygController.searchAllAttachments()",
          "root.MSG_GEN_PARAM_VALUE", imagesList[i][0] + "] [" + imagesList[i][1]);
    }
    return imagesList;
  }

  /*
   * ============================================ WEBSITES FUNCTIONS
   * ==================================== /** Method declaration Get images of the website
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public static String[][] getWebsiteImages(String path, String componentId)
      throws WysiwygException {
    /* chemin du repertoire = c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep */
    try {
      Collection listImages = FileFolderManager.getAllImages(path);
      Iterator i = listImages.iterator();
      int nbImages = listImages.size();
      String[][] images = new String[nbImages][2];
      SilverTrace.info("wysiwyg", "WysiwygController.getWebsiteImages()",
          "root.MSG_GEN_PARAM_VALUE", "nbImages=" + nbImages + " path=" + path);
      File image;
      for (int j = 0; j < nbImages; j++) {
        image = (File) i.next();
        SilverTrace.info("wysiwyg", "WysiwygController.getWebsiteImages()",
            "root.MSG_GEN_PARAM_VALUE", "image=" + image.getAbsolutePath());
        images[j][0] = finNode2(image.getAbsolutePath(), componentId).replace('\\', '/');
        images[j][1] = image.getName();
      }
      return images;
    } catch (Exception e) {
      throw new WysiwygException("WebSiteSessionController.getWebsiteImages()",
          SilverpeasException.ERROR, "wysisyg.EX_GET_ALL_IMAGES_FAIL", e);
    }
  }

  /**
   * Method declaration Get html pages of the website
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N][2] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public static String[][] getWebsitePages(String path, String componentId) throws WysiwygException {
    /* chemin du repertoire = c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep */
    try {
      Collection listPages = FileFolderManager.getAllWebPages(getNodePath(path, componentId));
      Iterator i = listPages.iterator();
      int nbPages = listPages.size();
      String[][] pages = new String[nbPages][2];
      SilverTrace.info("wysiwyg", "WysiwygController.getWebsitePages()",
          "root.MSG_GEN_PARAM_VALUE", "nbPages=" + nbPages + " path=" + path);
      File page;
      for (int j = 0; j < nbPages; j++) {
        page = (File) i.next();
        SilverTrace.info("wysiwyg", "WysiwygController.getWebsitePages()",
            "root.MSG_GEN_PARAM_VALUE", "page=" + page.getAbsolutePath());
        pages[j][0] = finNode2(page.getAbsolutePath(), componentId).replace('\\', '/');
        pages[j][1] = page.getName();
      }
      return pages;
    } catch (Exception e) {
      throw new WysiwygException("WebSiteSessionController.getWebsitePages()",
          SilverpeasException.ERROR, "wysisyg.EX_GET_ALL_PAGES_FAIL", e);
    }
  }

  /**
   * /* finNode
   * @param scc
   * @param path
   * @return
   */
  private static String finNode(String path, String componentId) {
    /* ex : ....webSite17\\id\\rep1\\rep2\\rep3 */
    /* res : id\rep1\rep2\rep3 */
    int longueur = componentId.length();
    int index = path.lastIndexOf(componentId);
    String chemin = path.substring(index + longueur);
    chemin = ignoreAntiSlash(chemin);
    chemin = supprDoubleAntiSlash(chemin);
    return chemin;
  }

  /**
   * finNode2
   */
  private static String finNode2(String path, String componentId) {
    /* ex : ....webSite17\id\rep1\rep2\rep3 */
    /* res : rep1\rep2\rep3 */
    SilverTrace.info("wysiwyg", "WysiwygController.finNode2()", "root.MSG_GEN_PARAM_VALUE",
        "path=" + path);
    String finNode = doubleAntiSlash(path);
    finNode = finNode(finNode, componentId);
    int index = finNode.indexOf("\\");
    if (index == -1) // on est sous systeme UNIX : il n'y a que des Slash
    {
      /*
       * NEWD CBO 25/06/2007 int longueur = componentId.length(); int index2 =
       * path.lastIndexOf(componentId); finNode = path.substring(index2 + longueur); finNode =
       * ignoreAntiSlash(finNode); index = finNode.indexOf("/"); NEWF CBO
       */

      index = finNode.indexOf("/");
    }
    return finNode.substring(index + 1);
  }

  /**
   * Method declaration Get the node of the path of the root Website
   * @param path type String: for example: ex :
   * c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11\\
   * @param componentId
   * @return a String with the path of the node: ex:
   * c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3
   * @throws WysiwygException
   */
  private static String getNodePath(String currentPath, String componentId) {
    /* retourne la liste des noeuds par lesquels on passe */
    /* ex : c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11\\ */
    /* res = c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3 */

    String nodePath = "";
    String deb;
    String finChemin;

    String chemin = currentPath;
    if (chemin != null) {
      chemin = supprAntiSlashFin(chemin); /*
                                           * c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\
                                           * rep1\\rep11
                                           */

      int longueur = componentId.length();
      int index = chemin.lastIndexOf(componentId);

      /* index de fin de webSite15 dans le chemin */
      index = index + longueur;
      /* finChemin = "\\id\\rep\\ ..." */
      finChemin = chemin.substring(index);
      /* saute les antiSlash, finChemin = id\\rep\\ ... */
      finChemin = ignoreSlash(finChemin);

      index = finChemin.indexOf("/");
      SilverTrace.info("wysiwyg", "WysiwygController.getNodePath()", "root.MSG_GEN_PARAM_VALUE",
          "finChemin = " + finChemin);

      if (index == -1) {
        nodePath = chemin; /* la racine id */
      } else {
        /* deb */
        int indexRacine = chemin.indexOf(finChemin);
        int indexAntiSlashSuivant = finChemin.indexOf("/");
        deb = chemin.substring(0, indexRacine + indexAntiSlashSuivant);
        nodePath = deb; /* ajoute la racine */
        /* finChemin = \rep\\ ... */
        finChemin = finChemin.substring(index + 1);
        /* saute les antiSlash s'il y en a, finChemin = rep\\rep1\\rep2\\ ... */
        finChemin = ignoreSlash(finChemin);
        SilverTrace.info("wysiwyg", "WysiwygController.getNodePath()", "root.MSG_GEN_PARAM_VALUE",
            "nodePath = " + nodePath);
      } // fin else
    } // fin if
    return nodePath;
  }

  /* supprAntiSlashFin */
  private static String supprAntiSlashFin(String path) {
    /* ex : ....\\id\\rep1\\rep2\\rep3\\ */
    /* res : ....\\id\\rep1\\rep2\\rep3 */

    int longueur = path.length();
    /*
     * if (path.substring(longueur - 2).equals("\\\\")) return path.substring(0, longueur - 2); else
     */
    if (path.substring(longueur - 1).equals("/"))
      return path.substring(0, longueur - 1);
    else
      return path;
  }

  private static String ignoreSlash(String chemin) {
    /* ex : /rep1/rep2/rep3 */
    /* res = rep1/rep2/rep3 */
    String res = chemin;
    boolean ok = false;
    while (!ok) {
      char car = res.charAt(0);
      if (car == '/') {
        res = res.substring(1);
      } else
        ok = true;
    }
    return res;
  }

  /* supprDoubleAntiSlash */
  private static String supprDoubleAntiSlash(String chemin) {
    /* ex : id\\rep1\\rep11\\rep111 */
    /* res = id\rep1\rep11\re111 */
    String res = "";
    int i = 0;
    while (i < chemin.length()) {
      char car = chemin.charAt(i);
      if (car == '\\') {
        res = res + car;
        i++;
      } else
        res = res + car;
      i++;
    }
    return res;
  }

  /* ignoreAntiSlash */
  private static String ignoreAntiSlash(String chemin) {
    /* ex : \\\rep1\\rep2\\rep3 */
    /* res = rep1\\rep2\\re3 */
    String res = chemin;
    boolean ok = false;
    while (!ok) {
      char car = res.charAt(0);
      /*
       * NEWD CBO 25/06/2007 if (car == '\\') { NEWF CBO
       */
      if (car == '\\' || car == '/') {
        res = res.substring(1);
      } else
        ok = true;
    }
    return res;
  }

  /* doubleAntiSlash */
  private static String doubleAntiSlash(String chemin) {
    int i = 0;
    String res = chemin;
    boolean ok = true;
    while (ok) {
      int j = i + 1;
      if ((i < res.length()) && (j < res.length())) {
        char car1 = res.charAt(i);
        char car2 = res.charAt(j);
        if ((car1 == '\\' && car2 == '\\') ||
            (car1 != '\\' && car2 != '\\')) {
        } else {
          String avant = res.substring(0, j);
          String apres = res.substring(j);
          if ((apres.startsWith("\\\\")) ||
              (avant.endsWith("\\\\"))) {
          } else {
            res = avant + '\\' + apres;
            i++;
          }
        }
      } else {
        if (i < res.length()) {
          char car = res.charAt(i);
          if (car == '\\')
            res = res + '\\';
        }
        ok = false;
      }
      i = i + 2;
    }
    return res;
  }

  /*
   * ============================================ END WEBSITES FUNCTIONS
   * ==================================== /** Method declaration built the name of the file to be
   * attached.
   * @param id String : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public static String getWysiwygFileName(String objectId) {
    return objectId + WYSIWYG_CONTEXT + ".txt";
  }

  public static String getWysiwygFileName(String objectId, String language) {
    if (I18NHelper.isDefaultLanguage(language))
      return getWysiwygFileName(objectId);
    else
      return objectId + WYSIWYG_CONTEXT + "_" + language + ".txt";
  }

  /**
   * Method declaration built the name of the images to be attached.
   * @param context String : for example Images.
   * @param id String : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public static String getImagesFileName(String objectId) {
    String fileName = objectId + WYSIWYG_IMAGES;

    return fileName;
  }

  public static void deleteFileAndAttachment(String componentId, String id) throws WysiwygException {
    AttachmentPK foreignKey = new AttachmentPK(id, "useless", componentId);
    AttachmentController.deleteAttachmentByCustomerPK(foreignKey);
  }

  public static void deleteFile(String componentId, String objectId, String language) {
    AttachmentPK foreignKey = new AttachmentPK(objectId, "useless", componentId);
    Vector files = AttachmentController.searchAttachmentByCustomerPK(foreignKey);
    Iterator f = files.iterator();
    while (f.hasNext()) {
      AttachmentDetail file = (AttachmentDetail) f.next();
      if (file != null &&
          file.getPhysicalName().equalsIgnoreCase(getWysiwygFileName(objectId, language))) {
        AttachmentController.deleteAttachment(file);
      }
    }
  }

  /**
   * Method declaration creation of the file and its attachment.
   * @param textHtml String : contains the text published by the wysiwyg
   * @param fileName String : name of the file
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param id String : for example the id of the publication.
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static void createFileAndAttachment(String textHtml, String fileName, String spaceId,
      String componentId, String context, String id, String userId) throws WysiwygException {
    createFileAndAttachment(textHtml, fileName, spaceId, componentId, context, id, userId, true);
  }

  public static void createFileAndAttachment(String textHtml, String fileName, String spaceId,
      String componentId, String context, String id, String userId, boolean indexIt)
      throws WysiwygException {
    try {
      int iUserId = -1;

      if (userId != null)
        iUserId = Integer.parseInt(userId);

      // create path
      String path = AttachmentController.createPath(componentId, context);

      // create file
      File f = WysiwygController.createFile(path, fileName, textHtml);

      // create AttachmentPK with spaceId and componentId
      AttachmentPK atPK = new AttachmentPK(null, spaceId, componentId);

      // create foreignKey with spaceId, componentId and id
      // use AttachmentPK to build the foreign key of customer object.
      AttachmentPK foreignKey = new AttachmentPK(id, spaceId, componentId);

      // create AttachmentDetail Object
      AttachmentDetail ad =
          new AttachmentDetail(atPK, fileName, fileName, null, "text/html", f.length(), context,
          new java.util.Date(), foreignKey);
      ad.setAuthor(userId);

      AttachmentController.createAttachment(ad, indexIt);

      CallBackManager.invoke(CallBackManager.ACTION_ON_WYSIWYG, iUserId, componentId, id);
    } catch (Exception exc) {
      throw new WysiwygException("WysiwygController.createFileAndAttachment()",
          SilverpeasException.ERROR, "wysiwyg.CREATING_WYSIWYG_DOCUMENT_FAILED", exc);
    }
  }

  /**
   * Method declaration creation of the file and its attachment.
   * @param textHtml String : contains the text published by the wysiwyg
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param id String : for example the id of the publication.
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static void createFileAndAttachment(String textHtml, String spaceId, String componentId,
      String id) throws WysiwygException /* , FinderException, NamingException, SQLException */
  {
    String fileName = WysiwygController.getWysiwygFileName(id);

    WysiwygController.createFileAndAttachment(textHtml, fileName, spaceId, componentId,
        WYSIWYG_CONTEXT, id, null);
  }

  /**
   * This method must be synchronized. Quick wysiwyg's saving can generate problems without
   * synchronization !!!
   * @param textHtml
   * @param fileName
   * @param spaceId
   * @param componentId
   * @param context
   * @param objectId
   * @param userId
   * @throws WysiwygException
   */
  private static synchronized void updateFileAndAttachment(String textHtml, String fileName,
      String spaceId, String componentId, String context, String objectId, String userId,
      boolean indexIt) throws WysiwygException {
    SilverTrace.info("wysiwyg", "WysiwygController.updateFileAndAttachment()",
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + fileName + " context=" + context + "objectId=" +
        objectId);
    AttachmentDetail attD =
        WysiwygController.searchAttachmentDetail(fileName, spaceId, componentId, context, objectId);
    if (attD != null) {
      AttachmentController.deleteAttachment(attD);
    }
    WysiwygController.createFileAndAttachment(textHtml, fileName, spaceId, componentId, context,
        objectId, userId, indexIt);
  }

  /**
   * Method declaration remove and recreates the file attached
   * @param textHtml String : contains the text published by the wysiwyg
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static void updateFileAndAttachment(String textHtml, String spaceId, String componentId,
      String objectId, String userId) throws WysiwygException {
    updateFileAndAttachment(textHtml, spaceId, componentId, objectId, userId, true);
  }

  public static void updateFileAndAttachment(String textHtml, String spaceId, String componentId,
      String objectId, String userId, boolean indexIt) throws WysiwygException {
    String fileName = WysiwygController.getWysiwygFileName(objectId);

    WysiwygController.updateFileAndAttachment(textHtml, fileName, spaceId, componentId,
        WYSIWYG_CONTEXT, objectId, userId, indexIt);
  }

  public static void save(String textHtml, String spaceId, String componentId, String objectId,
      String userId, String language, boolean indexIt) throws WysiwygException {
    if (I18NHelper.isDefaultLanguage(language)) {
      WysiwygController.updateFileAndAttachment(textHtml, spaceId, componentId, objectId, userId,
          indexIt);
    } else {
      String fileName = WysiwygController.getWysiwygFileName(objectId, language);
      WysiwygController.updateFileAndAttachment(textHtml, fileName, spaceId, componentId,
          WYSIWYG_CONTEXT, objectId, userId, indexIt);
    }
  }

  /**
   * Method declaration remove the file attached
   * @param fileName String : name of the file
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param objectId String : for example the id of the publication.
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static void deleteWysiwygAttachments(String spaceId, String componentId, String objectId)
      throws WysiwygException /* , FinderException, NamingException, SQLException */
  {
    try {
      // delete all the attachments
      AttachmentPK foreignKey = new AttachmentPK(objectId, spaceId, componentId);

      AttachmentController.deleteAttachmentByCustomerPK(foreignKey);
      // delete the images directory
      String path =
          AttachmentController.createPath(componentId, WysiwygController
          .getImagesFileName(objectId));

      WysiwygController.deletePath(path);
    } catch (Exception exc) {
      throw new WysiwygException("WysiwygController.deleteWysiwygAttachments()",
          SilverpeasException.ERROR, "wysiwyg.DELETING_WYSIWYG_ATTACHMENTS_FAILED", exc);
    }
  }

  /**
   * La méthode deleteWysiwygAttachments efface tous les attachments de la publication donc pour
   * éviter une éventuelle régression, je crée une nouvelle méthode
   * @param spaceId
   * @param componentId
   * @param objectId
   * @throws WysiwygException
   */
  public static void deleteWysiwygAttachmentsOnly(String spaceId, String componentId,
      String objectId) throws WysiwygException /* , FinderException, NamingException, SQLException */
  {
    try {
      // delete all the attachments
      AttachmentPK foreignKey = new AttachmentPK(objectId, spaceId, componentId);

      AttachmentController.deleteWysiwygAttachmentByCustomerPK(foreignKey);
      // delete the images directory
      String path =
          AttachmentController.createPath(componentId, WysiwygController
          .getImagesFileName(objectId));

      WysiwygController.deletePath(path);
    } catch (Exception exc) {
      throw new WysiwygException("WysiwygController.deleteWysiwygAttachments()",
          SilverpeasException.ERROR, "wysiwyg.DELETING_WYSIWYG_ATTACHMENTS_FAILED", exc);
    }
  }

  /**
   * Method declaration return the contents of the file attached.
   * @param fileName String : name of the file
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param objectId String : for example the id of the publication.
   * @return text : the contents of the file attached.
   * @throws Exception
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static String loadFileAndAttachment(String fileName, String spaceId, String componentId,
      String context) throws WysiwygException {
    String text = null;
    String path = AttachmentController.createPath(componentId, context);

    try {
      text = FileFolderManager.getCode(path, fileName);
    } catch (UtilException e) {
      // There is no document
      throw new WysiwygException("WysiwygController.loadFileAndAttachment()",
          SilverpeasException.WARNING, "wysiwyg.NO_WYSIWYG_DOCUMENT_ASSOCIATED");
    }
    return text;
  }

  /**
   * Method declaration return the contents of the file attached.
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param objectId String : for example the id of the publication.
   * @return text : the contents of the file attached.
   * @throws Exception
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static String loadFileAndAttachment(String spaceId, String componentId, String objectId)
      throws WysiwygException {
    String fileName = WysiwygController.getWysiwygFileName(objectId);

    return WysiwygController.loadFileAndAttachment(fileName, null, componentId, WYSIWYG_CONTEXT);
  }

  /**
   * @deprecated
   * @param spaceId
   * @param componentId
   * @param objectId
   * @param language
   * @return
   * @throws WysiwygException
   */
  public static String load(String spaceId, String componentId, String objectId, String language)
      throws WysiwygException {

    return load(componentId, objectId, language);
  }

  /**
   * Load wysiwyg content
   * @param componentId
   * @param objectId
   * @param language
   * @return
   * @throws WysiwygException
   */
  public static String load(String componentId, String objectId, String language)
      throws WysiwygException {
    String content = null;
    if (language == null || I18NHelper.isDefaultLanguage(language)) {
      String fileName = WysiwygController.getWysiwygFileName(objectId);
      content =
          WysiwygController.loadFileAndAttachment(fileName, null, componentId, WYSIWYG_CONTEXT);
    } else {
      String fileName = WysiwygController.getWysiwygFileName(objectId, language);
      content =
          WysiwygController.loadFileAndAttachment(fileName, null, componentId, WYSIWYG_CONTEXT);
    }
    if (content == null)
      content = "";
    return content;
  }

  /**
   * Method declaration return the contents of the file.
   * @param fileName String : name of the file
   * @param path String : the path of the file
   * @return text : the contents of the file attached.
   * @throws WysiwygException
   */
  public static String loadFileWebsite(String path, String fileName) throws WysiwygException {
    String text = null;
    try {
      text = FileFolderManager.getCode(path, fileName);
    } catch (UtilException e) {
      // There is no document
      throw new WysiwygException("WysiwygController.loadFileWebsite()",
          SilverpeasException.WARNING, "wysiwyg.NO_WYSIWYG_DOCUMENT_ASSOCIATED");
    }
    return text;
  }

  /**
   * Method declaration
   * @param spaceId
   * @param componentId
   * @param objectId
   * @return
   * @throws Exception
   * @throws FinderException
   * @throws NamingException
   * @see
   */
  public static boolean haveGotWysiwyg(String spaceId, String componentId, String objectId) {
    String path = AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);

    Iterator languages = I18NHelper.getLanguages();
    while (languages.hasNext()) {
      String language = (String) languages.next();

      File file = new File(path + getWysiwygFileName(objectId, language));

      if (file.exists())
        return true;
    }

    // check default language
    File file = new File(path + getWysiwygFileName(objectId));
    if (file.exists())
      return true;

    return false;
  }

  public static boolean haveGotWysiwyg(String spaceId, String componentId, String objectId,
      String language) {
    try {
      String wysiwygContent = load(componentId, objectId, language);
      if (wysiwygContent == null)
        return false;
      else
        return true;
    } catch (WysiwygException we) {
      return false;
    }
  }

  public static AttachmentDetail searchAttachmentDetail(String fileName, String spaceId,
      String componentId, String context, String objectId) {
    return searchAttachmentDetail(fileName, spaceId, componentId, context, objectId, null);
  }

  /**
   * Method declaration to search all file attached by primary key of customer object and context of
   * file attached
   * @param fileName String : name of the file
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param objectId String : for example the id of the publication.
   * @return AttachmentDetail
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static AttachmentDetail searchAttachmentDetail(String fileName, String spaceId,
      String componentId, String context, String objectId, Connection con) {
    AttachmentPK foreignKey = new AttachmentPK(objectId, spaceId, componentId);
    Vector vectAttachment =
        AttachmentController.searchAttachmentByPKAndContext(foreignKey, context, con);
    int nbFiles = vectAttachment.size();

    for (int i = 0; i < nbFiles; i++) {
      AttachmentDetail attD = (AttachmentDetail) vectAttachment.elementAt(i);

      if (attD.getLogicalName().equals(fileName)) {
        return attD;
      }
    }
    return null;
  }

  /**
   * Method declaration to search all file attached by primary key of customer object and context of
   * file attached
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param objectId String : for example the id of the publication.
   * @return AttachmentDetail
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static AttachmentDetail searchAttachmentDetail(String spaceId, String componentId,
      String context, String objectId) /*
                                        * throws /* WysiwygException, FinderException,
                                        * NamingException, SQLException
                                        */
  {
    String fileName = WysiwygController.getWysiwygFileName(objectId);

    return WysiwygController.searchAttachmentDetail(fileName, spaceId, componentId, context,
        objectId);
  }

  /**
   * updateWebsite : creation or update of a file of a website Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   * contenuFichier = code du fichier : "<HTML><TITLE>...."
   */
  public static void updateWebsite(String cheminFichier, String nomFichier, String contenuFichier)
      throws WysiwygException {
    SilverTrace.info("wysiwyg", "WysiwygController.updateWebsite()", "root.MSG_GEN_PARAM_VALUE",
        "cheminFichier=" + cheminFichier + " nomFichier=" + nomFichier);
    createFile(cheminFichier, nomFichier, contenuFichier);
  }

  /**
   * createFile : creation or update of a file Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   * contenuFichier = code du fichier : "<HTML><TITLE>...."
   */
  protected static File createFile(String cheminFichier, String nomFichier, String contenuFichier)
      throws WysiwygException {
    File directory = new File(cheminFichier);
    SilverTrace.info("wysiwyg", "WysiwygController.createFile()", "root.MSG_GEN_PARAM_VALUE",
        "cheminFichier=" + cheminFichier + " nomFichier=" + nomFichier);

    try {
      if (directory.isDirectory()) {

        /* Creation of a new file under the good tree structure */
        File file = new File(directory, nomFichier);

        /* writing of the contents of the file */
        /* if the file were already existing: rewrite of the contents */
        FileWriter file_write = new FileWriter(file);
        BufferedWriter flux_out = new BufferedWriter(file_write);

        flux_out.write(contenuFichier);
        flux_out.close();
        file_write.close();
        return file;
      } else {
        throw new WysiwygException("WysiwygController.createFile()", SilverpeasException.ERROR,
            "wysiwyg.TARGET_DIRECTORY_ON_SERVER_DOES_NOT_EXIST");
      }
    } catch (IOException exc) {
      throw new WysiwygException("WysiwygController.createFile()", SilverpeasException.ERROR,
          "wysiwyg.CREATING_WYSIWYG_DOCUMENT_FAILED");
    }
  }

  /**
   * deleteFile : destruction of a file Param = path name of the file
   */
  public synchronized void deleteFile(String directory, String fileName) throws WysiwygException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder\\File.html */

    /* Creation of the object File */
    File file = new File(directory, fileName);
    boolean result = file.delete();

    if (!result) {
      throw new WysiwygException("WysiwygController.deleteFile()", SilverpeasException.ERROR,
          "wysiwyg.DELETING_WYSIWYG_DOCUMENT_FAILED", "file = " + directory + fileName);
    }
  }

  /**
   * to delete path and file in ths directory
   * @param path: type String: the path to deleted
   * @exception: java.lang.Exception
   * @author Jean-Claude Groccia
   * @version 1.0
   */
  private static void deletePath(String path) throws WysiwygException {
    SilverTrace.info("wysiwyg", "WysiwygController.deletePath()", "root.MSG_GEN_ENTER_METHOD",
        "path = " + path);

    try {
      File d = new File(path);

      if (!d.exists()) {
        FileFolderManager.deleteFolder(path);
      }
    } catch (Exception e) {
      throw new WysiwygException("WysiwygController.deletePath()", SilverpeasException.ERROR,
          "wysiwyg.DELETING_DIRECTORY_ON_SERVER_FAILED", "path = " + path);
    }
  }

  /**
   * Method declaration
   * @param oldSpaceId
   * @param oldComponentId
   * @param oldObjectId
   * @param spaceId
   * @param componentId
   * @param objectId
   * @see
   */
  public static void copy(String oldSpaceId, String oldComponentId, String oldObjectId,
      String spaceId, String componentId, String objectId, String userId) {
    SilverTrace.info("wysiwyg", "WysiwygController.copy()", "root.MSG_GEN_ENTER_METHOD");
    try {
      // copy the wysiwyg
      AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);

      // copy the attachments
      // String oldPath = AttachmentController.createPath(oldSpaceId, oldComponentId,
      // getImagesFileName(oldObjectId));
      String oldPath =
          FileRepositoryManager.getAbsolutePath(oldComponentId, FileRepositoryManager
          .getAttachmentContext(getImagesFileName(oldObjectId)));
      String currentPath = "";
      String nPath = AttachmentController.createPath(componentId, getImagesFileName(objectId));
      String newPath = "";
      AttachmentPK foreignKey = new AttachmentPK(oldObjectId, oldSpaceId, oldComponentId);
      Vector vectAttachment =
          AttachmentController.searchAttachmentByPKAndContext(foreignKey,
          getImagesFileName(oldObjectId));
      int nbImages = vectAttachment.size();
      Hashtable<String, String> imageIds = new Hashtable<String, String>();

      for (int i = 0; i < nbImages; i++) {
        currentPath = oldPath;
        AttachmentDetail attD = (AttachmentDetail) vectAttachment.elementAt(i);

        currentPath += attD.getPhysicalName();
        newPath = nPath + attD.getPhysicalName();
        // physically
        FileRepositoryManager.copyFile(currentPath, newPath);
        // logically
        AttachmentDetail newAttd =
            new AttachmentDetail(new AttachmentPK("unknown", spaceId, componentId), attD
            .getPhysicalName(), attD.getLogicalName(), attD.getDescription(), attD.getType(),
            attD.getSize(), getImagesFileName(objectId), new java.util.Date(),
            new AttachmentPK(objectId, spaceId, componentId));
        newAttd.setAuthor(attD.getAuthor());
        AttachmentController.createAttachment(newAttd);

        imageIds.put(attD.getPK().getId(), newAttd.getPK().getId());
      }

      Iterator<String> languages = I18NHelper.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();

        copyFile(oldComponentId, oldObjectId, componentId, objectId, userId, language, imageIds);
      }
    } catch (Exception e) {
    }
  }

  private static void copyFile(String oldComponentId, String oldObjectId, String componentId,
      String objectId, String userId, String language, Hashtable<String, String> imageIds) {
    SilverTrace.info("wysiwyg", "WysiwygController.copyFile()", "root.MSG_GEN_ENTER_METHOD");
    try {
      // copy the wysiwyg
      AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);
      String wysiwygContent = load(oldComponentId, oldObjectId, language);
      if (StringUtil.isDefined(wysiwygContent)) {
        String newStr =
            replaceInternalImagesPath(wysiwygContent, oldComponentId, oldObjectId, componentId,
            objectId);
        newStr = replaceInternalImageIds(newStr, imageIds);

        createFileAndAttachment(newStr, WysiwygController.getWysiwygFileName(objectId, language),
            null, componentId, WYSIWYG_CONTEXT, objectId, userId);
      }
    } catch (Exception e) {
    }
  }

  private static String replaceInternalImageIds(String wysiwygContent,
      Hashtable<String, String> imageIds) {
    String tmp = wysiwygContent;
    Enumeration<String> oldImageIds = imageIds.keys();
    while (oldImageIds.hasMoreElements()) {
      String oldImageId = oldImageIds.nextElement();
      String newImageId = imageIds.get(oldImageId);

      tmp = replaceInternalImageId(tmp, oldImageId, newImageId);
    }
    return tmp;
  }

  private static String replaceInternalImageId(String wysiwygContent, String oldAttachmentId,
      String newAttachmentId) {
    return wysiwygContent.replaceAll("attachmentId=" + oldAttachmentId + "\"", "attachmentId=" +
        newAttachmentId + "\"");
  }

  /**
   * Usefull to maintain forward compatibility (old URLs to images)
   * @param wysiwygContent
   * @param oldComponentId
   * @param oldObjectId
   * @param componentId
   * @param objectId
   * @return
   */
  private static String replaceInternalImagesPath(String wysiwygContent, String oldComponentId,
      String oldObjectId, String componentId, String objectId) {
    String newStr = "";
    if (wysiwygContent.indexOf("FileServer") != -1) {
      // search and replace
      // SpaceId=WA8
      // ComponentId=kmelia178
      // Directory=Attachment/8Images
      // Directory=Attachment%2F8Images : since integration of the new Wysiwyg editor(FCKEditor)
      // Directory=Attachment\8Images : since integration of the new Wysiwyg editor(FCKEditor)
      // Directory=Attachment%5C8Images : since integration of the new Wysiwyg editor(FCKEditor)
      // String sp = "SpaceId=" + oldSpaceId;
      String co = "ComponentId=" + oldComponentId;
      String di = "Directory=Attachment/" + getImagesFileName(oldObjectId);
      String diBis = "Directory=Attachment%2F" + getImagesFileName(oldObjectId);
      String diTer = "Directory=Attachment\\" + getImagesFileName(oldObjectId);
      String diQua = "Directory=Attachment%5C" + getImagesFileName(oldObjectId);

      int begin = 0;
      int end = 0;

      // search for "ComponentId=" and replace
      begin = 0;
      end = wysiwygContent.indexOf(co, begin);
      while (end != -1) {
        newStr += wysiwygContent.substring(begin, end);
        newStr += "ComponentId=" + componentId;
        begin = end + co.length();
        end = wysiwygContent.indexOf(co, begin);
      }
      newStr += wysiwygContent.substring(begin, wysiwygContent.length());
      wysiwygContent = newStr;
      newStr = "";

      // search for "Directory=Attachment/" and replace
      begin = 0;
      end = wysiwygContent.indexOf(di, begin);
      while (end != -1) {
        newStr += wysiwygContent.substring(begin, end);
        newStr += "Directory=Attachment/" + getImagesFileName(objectId);
        begin = end + di.length();
        end = wysiwygContent.indexOf(di, begin);
      }
      newStr += wysiwygContent.substring(begin, wysiwygContent.length());
      wysiwygContent = newStr;
      newStr = "";

      // search for "Directory=Attachment%2F" and replace
      begin = 0;
      end = wysiwygContent.indexOf(diBis, begin);
      while (end != -1) {
        newStr += wysiwygContent.substring(begin, end);
        newStr += "Directory=Attachment%2F" + getImagesFileName(objectId);
        begin = end + diBis.length();
        end = wysiwygContent.indexOf(diBis, begin);
      }
      newStr += wysiwygContent.substring(begin, wysiwygContent.length());
      wysiwygContent = newStr;
      newStr = "";

      // search for "Directory=Attachment\" and replace
      begin = 0;
      end = wysiwygContent.indexOf(diTer, begin);
      while (end != -1) {
        newStr += wysiwygContent.substring(begin, end);
        newStr += "Directory=Attachment\\" + getImagesFileName(objectId);
        begin = end + diTer.length();
        end = wysiwygContent.indexOf(diTer, begin);
      }
      newStr += wysiwygContent.substring(begin, wysiwygContent.length());
      wysiwygContent = newStr;
      newStr = "";

      // search for "Directory=Attachment%5C" and replace
      begin = 0;
      end = wysiwygContent.indexOf(diQua, begin);
      while (end != -1) {
        newStr += wysiwygContent.substring(begin, end);
        newStr += "Directory=Attachment%5C" + getImagesFileName(objectId);
        begin = end + diQua.length();
        end = wysiwygContent.indexOf(diQua, begin);
      }
      newStr += wysiwygContent.substring(begin, wysiwygContent.length());
    } else {
      newStr = wysiwygContent;
    }

    return newStr;
  }

  public static void wysiwygPlaceHaveChanged(String oldComponentId, String oldObjectId,
      String newComponentId, String newObjectId) throws WysiwygException {
    Iterator languages = I18NHelper.getLanguages();
    String language = null;
    String wysiwyg = null;
    while (languages.hasNext()) {
      language = (String) languages.next();
      wysiwyg = load(newComponentId, newObjectId, language);

      if (StringUtil.isDefined(wysiwyg)) {
        wysiwyg =
            replaceInternalImagesPath(wysiwyg, oldComponentId, oldObjectId, newComponentId,
            newObjectId);

        // overwrite
        createFile(AttachmentController.createPath(newComponentId, WYSIWYG_CONTEXT),
            getWysiwygFileName(newObjectId, language), wysiwyg);
      }
    }
  }

  /*
   * public static void moveWysiwyg(String fromComponentId, String fromObjectId, String
   * toComponentId, String toObjectId) throws WysiwygException { Iterator languages =
   * I18NHelper.getLanguages(); String language = null; String wysiwyg = null; while
   * (languages.hasNext()) { language = (String) languages.next(); wysiwyg = load(fromComponentId,
   * fromObjectId, language); wysiwyg = replaceInternalImagesPath(wysiwyg, fromComponentId,
   * fromObjectId, toComponentId, toObjectId);
   * createFile(AttachmentController.createPath(fromComponentId, WYSIWYG_CONTEXT),
   * getWysiwygFileName(fromObjectId, language), wysiwyg); } }
   */

  public static String getWysiwygPath(String componentId, String objectId, String language) {
    String path = AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);
    return path + getWysiwygFileName(objectId, language);
  }

  public static String getWysiwygPath(String componentId, String objectId) {
    String path = AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);
    return path + getWysiwygFileName(objectId);
  }

  public static List getGalleries() {
    List galleries = null;
    OrganizationController orgaController = new OrganizationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (int c = 0; c < compoIds.length; c++) {
      if ("yes".equalsIgnoreCase(orgaController.getComponentParameterValue("gallery" + compoIds[c],
          "viewInWysiwyg"))) {
        if (galleries == null)
          galleries = new ArrayList();

        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoIds[c]);
        galleries.add(gallery);
      }
    }
    return galleries;
  }

  /**
   * Gets the components dedicated to file storage
   * @param userId the user identifier is used to retrieve only the authorized components for the
   * user
   * @return a components list
   */
  public static List<ComponentInstLight> getStorageFile(String userId) {
    // instiate all needed objects
    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    OrganizationController controller = new OrganizationController();
    // gets all kmelia components
    CompoSpace[] compoIds = controller.getCompoForUser(userId, "kmelia");
    for (CompoSpace compoSpace : compoIds) {
      // retain only the components considered as a file storage
      if ("yes".equalsIgnoreCase(controller.getComponentParameterValue(compoSpace.getComponentId(),
          "publicFiles"))) {
        ComponentInstLight component =
            controller.getComponentInstLight(compoSpace.getComponentId());
        components.add(component);
      }
    }
    return components;
  }

}