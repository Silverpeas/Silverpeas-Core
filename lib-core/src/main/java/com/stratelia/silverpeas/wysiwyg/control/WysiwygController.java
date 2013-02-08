/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.FinderException;
import javax.naming.NamingException;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

import org.apache.commons.io.FileUtils;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;

/**
 * @author neysseri
 */
public class WysiwygController {

  public final static String WYSIWYG_CONTEXT = "wysiwyg";
  public final static String WYSIWYG_IMAGES = "Images";
  public final static String WYSIWYG_WEBSITES = "webSites";

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
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static String[][] searchAllAttachments(String id, String spaceId, String componentId,
      String context) /* throws /*WysiwygException, FinderException, NamingException, SQLException */{
    AttachmentPK foreignKey = new AttachmentPK(id, spaceId, componentId);

    List<AttachmentDetail> vectAttachment =
        AttachmentController.searchAttachmentByPKAndContext(foreignKey, context);
    int nbImages = vectAttachment.size();
    String[][] imagesList = new String[nbImages][2];

    for (int i = 0; i < nbImages; i++) {
      AttachmentDetail attD = vectAttachment.get(i);

      String path =
          FileServerUtils.getUrl(spaceId, componentId, attD.getLogicalName(), attD.
          getPhysicalName(),
          attD.getType(), "Attachment/" + context);

      imagesList[i][0] = path;
      imagesList[i][1] = attD.getLogicalName();
      SilverTrace.info("wysiwyg", "WysiwygController.searchAllAttachments()",
          "root.MSG_GEN_PARAM_VALUE", imagesList[i][0] + "] [" + imagesList[i][1]);
    }
    return imagesList;
  }

  /**
   * Method declaration Get images of the website
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
      Collection<File> listImages = FileFolderManager.getAllImages(path);
      Iterator<File> i = listImages.iterator();
      int nbImages = listImages.size();
      String[][] images = new String[nbImages][2];
      SilverTrace.info("wysiwyg", "WysiwygController.getWebsiteImages()",
          "root.MSG_GEN_PARAM_VALUE", "nbImages=" + nbImages + " path=" + path);
      File image;
      for (int j = 0; j < nbImages; j++) {
        image = i.next();
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
      Collection<File> listPages = FileFolderManager.getAllWebPages(getNodePath(path, componentId));
      Iterator<File> i = listPages.iterator();
      int nbPages = listPages.size();
      String[][] pages = new String[nbPages][2];
      SilverTrace.info("wysiwyg", "WysiwygController.getWebsitePages()",
          "root.MSG_GEN_PARAM_VALUE", "nbPages=" + nbPages + " path=" + path);
      File page;
      for (int j = 0; j < nbPages; j++) {
        page = i.next();
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
   * Returns the node path : for example ....webSite17\\id\\rep1\\rep2\\rep3 returns
   * id\rep1\rep2\rep3
   * @param componentId the component id.
   * @param path the full path.
   * @return the path for the nodes.
   */
  static String finNode(String path, String componentId) {

    int longueur = componentId.length();
    int index = path.lastIndexOf(componentId);
    String chemin = path.substring(index + longueur);
    chemin = ignoreSlashAndAntislash(chemin);
    chemin = supprDoubleAntiSlash(chemin);
    return chemin;
  }

  /**
   * Returns the node path without the id element : for example ....webSite17\\id\\rep1\\rep2\\rep3
   * returns rep1\rep2\rep3
   * @param componentId the component id.
   * @param path the full path.
   * @return the path for the nodes.
   */
  static String finNode2(String path, String componentId) {
    SilverTrace.info("wysiwyg", "WysiwygController.finNode2()", "root.MSG_GEN_PARAM_VALUE",
        "path=" + path);
    String finNode = doubleAntiSlash(path);
    finNode = finNode(finNode, componentId);
    int index = finNode.indexOf('\\');
    if (index < 0) {
      index = finNode.indexOf('/');
    }
    return finNode.substring(index + 1);
  }

  /**
   * Method declaration Get the node of the path of the root Website. For example
   * c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11\\ should return
   * c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3
   * @param currentPath the full path.
   * @param componentId the component id.
   * @return a String with the path of the node.
   * @throws WysiwygException
   */
  static String getNodePath(String currentPath, String componentId) {
    String chemin = currentPath;
    if (chemin != null) {
      chemin = supprAntiSlashFin(chemin);
      int longueur = componentId.length();
      int indexComponent = chemin.lastIndexOf(componentId);
      indexComponent = indexComponent + longueur;
      String finChemin = chemin.substring(indexComponent);
      finChemin = ignoreSlash(finChemin);
      finChemin = ignoreSlashAndAntislash(finChemin);
      int index = -1;
      if (finChemin.contains("/")) {
        index = finChemin.indexOf('/');
      } else if (finChemin.contains("\\")) {
        index = finChemin.indexOf('\\');
      }
      SilverTrace.info("wysiwyg", "WysiwygController.getNodePath()", "root.MSG_GEN_PARAM_VALUE",
          "finChemin = " + finChemin);

      if (index == -1) {
        return chemin;
      } else {
        int indexRacine = chemin.indexOf(finChemin);
        return chemin.substring(0, indexRacine + index);
      }
    }
    return "";
  }

  /* supprAntiSlashFin */
  static String supprAntiSlashFin(String path) {
    /* ex : ....\\id\\rep1\\rep2\\rep3\\ */
    /* res : ....\\id\\rep1\\rep2\\rep3 */
    int longueur = path.length();

    if ("/".equals(path.substring(longueur - 1))) {
      return path.substring(0, longueur - 1);
    }
    return path;
  }

  static String ignoreSlash(String chemin) {
    /* ex : /rep1/rep2/rep3 */
    /* res = rep1/rep2/rep3 */
    String res = chemin;
    boolean ok = false;
    while (!ok) {
      char car = res.charAt(0);
      if (car == '/') {
        res = res.substring(1);
      } else {
        ok = true;
      }
    }
    return res;
  }

  static String supprDoubleAntiSlash(String chemin) {
    StringBuilder res = new StringBuilder("");
    int i = 0;
    while (i < chemin.length()) {
      char car = chemin.charAt(i);
      if (car == '\\' && chemin.charAt(i + 1) == '\\') {
        res.append(car);
        i++;
      } else {
        res.append(car);
      }
      i++;
    }
    return res.toString();
  }

  static String ignoreSlashAndAntislash(String chemin) {
    char firstChar = chemin.charAt(0);
    if (firstChar == '\\' || firstChar == '/') {
      return ignoreSlashAndAntislash(chemin.substring(1));
    }
    return chemin;
  }

  /* doubleAntiSlash */
  static String doubleAntiSlash(String chemin) {
    int i = 0;
    String res = chemin;
    boolean ok = true;
    while (ok) {
      int j = i + 1;
      if ((i < res.length()) && (j < res.length())) {
        char car1 = res.charAt(i);
        char car2 = res.charAt(j);
        if (!((car1 == '\\' && car2 == '\\') || (car1 != '\\' && car2 != '\\'))) {
          String avant = res.substring(0, j);
          String apres = res.substring(j);
          if (!apres.startsWith("\\\\") && !avant.endsWith("\\\\")) {
            res = avant + '\\' + apres;
            i++;
          }
        }
      } else {
        if (i < res.length()) {
          char car = res.charAt(i);
          if (car == '\\') {
            res = res + '\\';
          }
        }
        ok = false;
      }
      i = i + 2;
    }
    return res;
  }

  /**
   * Method declaration built the name of the file to be attached.
   * @param objectId : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public static String getWysiwygFileName(String objectId) {
    return objectId + WYSIWYG_CONTEXT + ".txt";
  }

  public static String getWysiwygFileName(String objectId, String language) {
    if (I18NHelper.isDefaultLanguage(language)) {
      return getWysiwygFileName(objectId);
    }
    return objectId + WYSIWYG_CONTEXT + "_" + language + ".txt";
  }

  /**
   * Method declaration built the name of the images to be attached.
   * @param objectId : for example the id of the publication.
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
    List<AttachmentDetail> files = AttachmentController.searchAttachmentByCustomerPK(foreignKey);
    for (AttachmentDetail file : files) {
      if (file != null
          && file.getPhysicalName().equalsIgnoreCase(getWysiwygFileName(objectId, language))) {
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
    createFileAndAttachment(textHtml, fileName, spaceId, componentId, context, id, userId, indexIt,
        true);
  }

  public static void createFileAndAttachment(String textHtml, String fileName, String spaceId,
      String componentId, String context, String id, String userId, boolean indexIt,
      boolean invokeCallback) throws WysiwygException {
    if (!StringUtil.isDefined(textHtml)) {
      return;
    }
    try {
      int iUserId = -1;

      if (userId != null) {
        iUserId = Integer.parseInt(userId);
      }
      String path = AttachmentController.createPath(componentId, context);
      File file = WysiwygController.createFile(path, fileName, textHtml);
      AttachmentPK atPK = new AttachmentPK(null, spaceId, componentId);

      // create foreignKey with spaceId, componentId and id
      // use AttachmentPK to build the foreign key of customer object.
      AttachmentPK foreignKey = new AttachmentPK(id, spaceId, componentId);

      // create AttachmentDetail Object
      AttachmentDetail ad = new AttachmentDetail(atPK, fileName, fileName, null, "text/html", file.
          length(), context, new java.util.Date(), foreignKey);
      ad.setAuthor(userId);
      AttachmentController.createAttachment(ad, indexIt, invokeCallback);
      if (invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ON_WYSIWYG, iUserId, componentId, id);
      }
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
      String id) throws WysiwygException {
    String fileName = WysiwygController.getWysiwygFileName(id);

    WysiwygController.createFileAndAttachment(textHtml, fileName, spaceId, componentId,
        WYSIWYG_CONTEXT, id, null);
  }

  /**
   * Index all elements attached to object identified by <id, componentId>
   * @param componentId for example, the id of the application.
   * @param id for example, the id of the publication.
   */
  public static void index(String componentId, String id) {
    AttachmentPK foreignPK = new AttachmentPK(id, componentId);
    AttachmentController.attachmentIndexer(foreignPK);
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
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + fileName + " context=" + context + "objectId="
        + objectId);
    AttachmentDetail attD =
        WysiwygController.searchAttachmentDetail(fileName, spaceId, componentId, context,
        objectId);
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
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @throws FinderException
   * @throws NamingException
   * @throws SQLException
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static void deleteWysiwygAttachments(String spaceId, String componentId, String objectId)
      throws WysiwygException {
    try {
      // delete all the attachments
      AttachmentPK foreignKey = new AttachmentPK(objectId, spaceId, componentId);

      AttachmentController.deleteAttachmentByCustomerPK(foreignKey);
      // delete the images directory
      String path =
          AttachmentController.createPath(componentId, WysiwygController.getImagesFileName(
          objectId));

      FileFolderManager.deleteFolder(path);
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
      String objectId) throws WysiwygException /* , FinderException, NamingException, SQLException */{
    try {
      // delete all the attachments
      AttachmentPK foreignKey = new AttachmentPK(objectId, spaceId, componentId);

      AttachmentController.deleteWysiwygAttachmentByCustomerPK(foreignKey);
      // delete the images directory
      String path =
          AttachmentController.createPath(componentId, WysiwygController.getImagesFileName(
          objectId));

      FileFolderManager.deleteFolder(path);
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
   * @return text : the contents of the file attached.
   * @throws WysiwygException
   * @see AttachmentController
   */
  public static String loadFileAndAttachment(String fileName, String spaceId, String componentId,
      String context) throws WysiwygException {
    String path = AttachmentController.createPath(componentId, context);
    try {
      return FileFolderManager.getCode(path, fileName);
    } catch (UtilException e) {
      // There is no document
      throw new WysiwygException("WysiwygController.loadFileAndAttachment()",
          SilverpeasException.WARNING, "wysiwyg.NO_WYSIWYG_DOCUMENT_ASSOCIATED", e);
    }
  }

  /**
   * Method declaration return the contents of the file attached.
   * @param spaceId String : the id of space.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @return text : the contents of the file attached.
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
    String fileName = null;
    boolean useDefaultLanguage = (language == null || I18NHelper.isDefaultLanguage(language));
    if (!useDefaultLanguage) {
      fileName = WysiwygController.getWysiwygFileName(objectId, language);
      content =
          WysiwygController.loadFileAndAttachment(fileName, null, componentId, WYSIWYG_CONTEXT);
    }

    // use default language also if content has not been found in specified language
    if ((!StringUtil.isDefined(content)) || (useDefaultLanguage)) {
      fileName = WysiwygController.getWysiwygFileName(objectId);
      content =
          WysiwygController.loadFileAndAttachment(fileName, null, componentId, WYSIWYG_CONTEXT);
    }

    if (content == null) {
      content = "";
    }
    return content;
  }

  /**
   * Get all Silverpeas Files linked by wysiwyg content (bases on pattern "<a href='/silverpeas/
   * @param content
   * @return
   * @throws WysiwygException
   */
  public static List<String> getEmbeddedAttachmentIds(String content)
      throws WysiwygException {
    List<String> attachmentIds = new ArrayList<String>();

    if (content != null) {
      // 1 - search url with format : /silverpeas/File/####
      Pattern attachmentLinkPattern = Pattern.compile("href=\\\"\\/silverpeas\\/File\\/(.*?)\\\"");
      Matcher linkMatcher = attachmentLinkPattern.matcher(content);
      while (linkMatcher.find()) {
        String fileId = linkMatcher.group(1);
        attachmentIds.add(fileId);
      }

      // 2 - search url with format : /silverpeas/FileServer/....attachmentId=###...
      attachmentLinkPattern = Pattern.compile(
          "href=\\\"\\/silverpeas\\/FileServer\\/(.*?)attachmentId=(\\d*)");
      linkMatcher = attachmentLinkPattern.matcher(content);
      while (linkMatcher.find()) {
        String fileId = linkMatcher.group(2);
        attachmentIds.add(fileId);
      }
    }

    return attachmentIds;
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
   */
  public static boolean haveGotWysiwyg(String spaceId, String componentId, String objectId) {
    String path = AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);

    Iterator<String> languages = I18NHelper.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();

      File file = new File(path + getWysiwygFileName(objectId, language));

      if (file.exists()) {
        return true;
      }
    }

    // check default language
    File file = new File(path + getWysiwygFileName(objectId));
    if (file.exists()) {
      return true;
    }

    return false;
  }

  public static boolean haveGotWysiwyg(String spaceId, String componentId, String objectId,
      String language) {
    try {
      String wysiwygContent = load(componentId, objectId, language);
      if (wysiwygContent == null) {
        return false;
      } else {
        return true;
      }
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
   * @param con
   * @return AttachmentDetail
   * @see AttachmentController
   */
  public static AttachmentDetail searchAttachmentDetail(String fileName, String spaceId,
      String componentId, String context, String objectId, Connection con) {
    AttachmentPK foreignKey = new AttachmentPK(objectId, spaceId, componentId);
    List<AttachmentDetail> vectAttachment =
        AttachmentController.searchAttachmentByPKAndContext(foreignKey, context, con);
    for (AttachmentDetail attD : vectAttachment) {
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
   * @see AttachmentController
   */
  public static AttachmentDetail searchAttachmentDetail(String spaceId, String componentId,
      String context, String objectId) /*
                                        * throws /* WysiwygException, FinderException,
                                        * NamingException, SQLException
                                        */{
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
   * Creation or update of a file
   * @param cheminFichier the path to the directory containing the file.
   * @param nomFichier the name of the file.
   * @param contenuFichier the content of the file. @ return
   * @throws WysiwygException
   */
  protected static File createFile(String cheminFichier, String nomFichier, String contenuFichier)
      throws WysiwygException {
    SilverTrace.info("wysiwyg", "WysiwygController.createFile()", "root.MSG_GEN_ENTER_METHOD",
        "cheminFichier=" + cheminFichier + " nomFichier=" + nomFichier);

    FileFolderManager.createFile(cheminFichier, nomFichier, contenuFichier);
    
    File directory = new File(cheminFichier);
    return FileUtils.getFile(directory, nomFichier);
  }

  /**
   * deleteFile : destruction of a file Param = path name of the file
   * @param directory
   * @param fileName
   * @throws WysiwygException
   */
  public synchronized void deleteFile(String directory, String fileName) throws WysiwygException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder\\File.html */
    File file = new File(directory, fileName);
    if (!file.delete()) {
      throw new WysiwygException("WysiwygController.deleteFile()", SilverpeasException.ERROR,
          "wysiwyg.DELETING_WYSIWYG_DOCUMENT_FAILED", "file = " + directory + fileName);
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
          FileRepositoryManager.getAbsolutePath(oldComponentId, FileRepositoryManager.
          getAttachmentContext(getImagesFileName(oldObjectId)));
      String currentPath = "";
      String nPath = AttachmentController.createPath(componentId, getImagesFileName(objectId));
      String newPath = "";
      AttachmentPK foreignKey = new AttachmentPK(oldObjectId, oldSpaceId, oldComponentId);
      List<AttachmentDetail> vectAttachment =
          AttachmentController.searchAttachmentByPKAndContext(foreignKey,
          getImagesFileName(oldObjectId));
      Map<AttachmentPK, AttachmentPK> imageIds = new HashMap<AttachmentPK, AttachmentPK>();

      for (AttachmentDetail attD : vectAttachment) {
        currentPath = oldPath + attD.getPhysicalName();
        newPath = nPath + attD.getPhysicalName();
        // physically
        FileRepositoryManager.copyFile(currentPath, newPath);
        // logically
        AttachmentDetail newAttd =
            new AttachmentDetail(new AttachmentPK("unknown", spaceId, componentId), attD.
            getPhysicalName(), attD.getLogicalName(), attD.getDescription(), attD.getType(),
            attD.getSize(), getImagesFileName(objectId), new java.util.Date(),
            new AttachmentPK(objectId, spaceId, componentId));
        newAttd.setAuthor(attD.getAuthor());
        AttachmentController.createAttachment(newAttd);

        imageIds.put(attD.getPK(), newAttd.getPK());
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
      String objectId, String userId, String language, Map<AttachmentPK, AttachmentPK> imageIds) {
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
            null, componentId, WYSIWYG_CONTEXT, objectId, userId, true, false);
      }
    } catch (Exception e) {
    }
  }

  private static String replaceInternalImageIds(String wysiwygContent,
      Map<AttachmentPK, AttachmentPK> imageIds) {
    String tmp = wysiwygContent;
    for (Entry<AttachmentPK, AttachmentPK> imageId : imageIds.entrySet()) {
      tmp = replaceInternalImageId(tmp, imageId.getKey(), imageId.getValue());
    }
    return tmp;
  }

  private static String replaceInternalImageId(String wysiwygContent, AttachmentPK oldPK,
      AttachmentPK newPK) {
    String from = "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK.getId() + "/";
    String to = "/componentId/" + newPK.getInstanceId() + "/attachmentId/" + newPK.getId() + "/";
    return wysiwygContent.replaceAll(from, to);
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
    if (wysiwygContent.contains("FileServer")) {
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
    Iterator<String> languages = I18NHelper.getLanguages();
    String language = null;
    String wysiwyg = null;
    while (languages.hasNext()) {
      language = languages.next();
      wysiwyg = load(newComponentId, newObjectId, language);
      if (StringUtil.isDefined(wysiwyg)) {
        wysiwyg = replaceInternalImagesPath(wysiwyg, oldComponentId, oldObjectId, newComponentId,
            newObjectId);
        // overwrite
        createFile(AttachmentController.createPath(newComponentId, WYSIWYG_CONTEXT),
            getWysiwygFileName(newObjectId, language), wysiwyg);
      }
    }
  }

  public static String getWysiwygPath(String componentId, String objectId, String language) {
    String path = AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);
    return path + getWysiwygFileName(objectId, language);
  }

  public static String getWysiwygPath(String componentId, String objectId) {
    String path = AttachmentController.createPath(componentId, WYSIWYG_CONTEXT);
    return path + getWysiwygFileName(objectId);
  }

  public static List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = null;
    OrganizationController orgaController = new OrganizationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (String compoId : compoIds) {
      if ("yes".equalsIgnoreCase(orgaController.getComponentParameterValue("gallery" + compoId,
          "viewInWysiwyg"))) {
        if (galleries == null) {
          galleries = new ArrayList<ComponentInstLight>();
        }
        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoId);
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
    String[] compoIds = controller.getCompoId("kmelia");
    for (String compoId : compoIds) {
      // retain only the components considered as a file storage
      if ("yes".equalsIgnoreCase(controller.getComponentParameterValue(compoId, "publicFiles"))) {
        ComponentInstLight component = controller.getComponentInstLight(compoId);
        components.add(component);
      }
    }
    return components;
  }

  /**
   * Index given embedded linked files
   * @param indexEntry index entry to update
   * @param embeddedAttachmentIds embedded linked files ids
   */
  public static void indexEmbeddedLinkedFiles(FullIndexEntry indexEntry,
      List<String> embeddedAttachmentIds) {
    for (String attachmentId : embeddedAttachmentIds) {
      try {
        AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
            attachmentId));
        if (attachment != null) {
          String attLanguage = attachment.getLanguage();
          String physicalName = null;
          String type = null;

          AttachmentDetailI18N translation = null;
          if (attLanguage != null) {
            translation = (AttachmentDetailI18N) attachment.getTranslation(attLanguage);
            physicalName = translation.getPhysicalName();
            type = translation.getType();
          } else {
            physicalName = attachment.getPhysicalName();
            type = attachment.getType();
          }

          String path;
          if (attachment.getAttachmentGroup() == AttachmentDetail.GROUP_FILE_LINK) {
            /*
             * c'est un lien, le chemin est contenu dans la colonne physicalName(complet) un lien,
             * le chemin est contenu dans la colonne physicalName(complet)
             */
            path = attachment.getPhysicalName();
          } else {
            path = AttachmentController.createPath(attachment.getInstanceId(),
                attachment.getContext()) + File.separator + physicalName;
          }

          indexEntry.addLinkedFileContent(path, null, type, attLanguage);
          indexEntry.addLinkedFileId(attachmentId);
        }
      } catch (Exception e) {
        SilverTrace.warn("wisiwyg", "WysiwygController", "root.MSG_GEN_PARAM_VALUE",
            "Erreur dans l'indexation d'un fichier joint lié au contenu wysiwyg - attachmentId:"
            + attachmentId);
      }
    }
  }
}