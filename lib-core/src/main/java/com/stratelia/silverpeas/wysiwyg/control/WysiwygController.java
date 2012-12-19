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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.wysiwyg.control;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.util.Charsets;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neysseri
 */
public class WysiwygController {

  private static final OrganizationController ORGANIZATION_CONTROLLER = new OrganizationController();
  public final static String WYSIWYG_CONTEXT = DocumentType.wysiwyg.name();
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
   *
   *
   *
   * @param id type String: for example pubId.
   * @param componentId type String: the id of component.
   * @return imagesList a table of string[N][2] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   */
  public static String[][] searchAllAttachments(String id, String componentId) {
    List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(new ForeignPK(id, componentId), null);
    int nbImages = attachments.size();
    String[][] imagesList = new String[nbImages][2];
    for (int i = 0; i < nbImages; i++) {
      SimpleDocument attD = attachments.get(i);
      String path = attD.getAttachmentPath();
      imagesList[i][0] = path;
      imagesList[i][1] = attD.getFilename();
      SilverTrace.info("wysiwyg", "WysiwygController.searchAllAttachments()",
          "root.MSG_GEN_PARAM_VALUE", imagesList[i][0] + "] [" + imagesList[i][1]);
    }
    return imagesList;
  }

  /**
   * Method declaration Get images of the website
   *
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public static String[][] getWebsiteImages(String path, String componentId) throws WysiwygException {
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
   * @param objectId : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public static String getImagesFileName(String objectId) {
    return objectId + WYSIWYG_IMAGES;
  }

  public static void deleteFileAndAttachment(String componentId, String id) throws WysiwygException {
    ForeignPK foreignKey = new ForeignPK(id, componentId);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(foreignKey, null);
    for (SimpleDocument doc : documents) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc);
    }
  }

  public static void deleteFile(String componentId, String objectId, String language) {
    ForeignPK foreignKey = new ForeignPK(objectId, componentId);
    List<SimpleDocument> files = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(foreignKey, null);
    for (SimpleDocument file : files) {
      if (file != null && file.getFilename().
          equalsIgnoreCase(getWysiwygFileName(objectId, language))) {
        AttachmentServiceFactory.getAttachmentService().removeContent(file, language, false);
      }
    }
  }

  /**
   * Method declaration creation of the file and its attachment.
   *
   *
   * @param textHtml String : contains the text published by the wysiwyg
   * @param fileName String : name of the file
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param id String : for example the id of the publication.
   * @param userId
   */
  public static void createFileAndAttachment(String textHtml, String fileName, String componentId,
      String context, String id, String userId)  {
    createFileAndAttachment(textHtml, fileName, componentId, context, id, userId, true);
  }

  public static void createFileAndAttachment(String textHtml, String fileName, String componentId,
      String context, String id, String userId, boolean indexIt) {
    createFileAndAttachment(textHtml, fileName, componentId, context, id, userId, indexIt, true);
  }

  public static void createFileAndAttachment(String textHtml, String fileName, String componentId,
      String context, String id, String userId, boolean indexIt, boolean invokeCallback)  {
    if (!StringUtil.isDefined(textHtml)) {
      return;
    }
      int iUserId = -1;
      if (userId != null) {
        iUserId = Integer.parseInt(userId);
      }
      SimpleDocumentPK docPk = new SimpleDocumentPK(null, componentId);
      SimpleDocument document = new SimpleDocument(docPk, id, 0, false, userId,
          new SimpleAttachment(fileName, null, fileName, null, textHtml.length(),
          MimeTypes.HTML_MIME_TYPE, userId, new Date(), null));
      document.setDocumentType(DocumentType.valueOf(context));
      AttachmentServiceFactory.getAttachmentService().createAttachment(document,
          new ByteArrayInputStream(textHtml.getBytes(Charsets.UTF_8)), indexIt, invokeCallback);
      if (invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ON_WYSIWYG, iUserId, componentId, id);
      }
  }

  /**
   * Method declaration creation of the file and its attachment.
   *
   *
   * @param textHtml String : contains the text published by the wysiwyg
   * @param componentId String : the id of component.
   * @param id String : for example the id of the publication.
   */
  public static void createFileAndAttachment(String textHtml, String componentId, String id)  {
    String fileName = getWysiwygFileName(id);
    createFileAndAttachment(textHtml, fileName, componentId, WYSIWYG_CONTEXT, id, null);
  }

  /**
   * Index all elements attached to object identified by <id, componentId>
   *
   * @param componentId for example, the id of the application.
   * @param id for example, the id of the publication.
   */
  public static void index(String componentId, String id) {
    ForeignPK foreignPK = new ForeignPK(id, componentId);
    AttachmentServiceFactory.getAttachmentService().indexAllDocuments(foreignPK, null, null);
  }

  /**
   * This method must be synchronized. Quick wysiwyg's saving can generate problems without
   * synchronization !!!
   *
   *
   * @param textHtml
   * @param fileName
   * @param componentId
   * @param context
   * @param objectId
   * @param userId
   */
  private static void updateFileAndAttachment(String textHtml, String fileName,
      String componentId, String context, String objectId, String userId, boolean indexIt) {
    SilverTrace.info("wysiwyg", "WysiwygController.updateFileAndAttachment()",
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + fileName + " context=" + context + "objectId="
        + objectId);
    SimpleDocument document = searchAttachmentDetail(fileName, componentId, context, objectId);
    if (document != null) {
      document.setSize(textHtml.getBytes(Charsets.UTF_8).length);
      document.setDocumentType(DocumentType.valueOf(context));
      AttachmentServiceFactory.getAttachmentService().updateAttachment(document,
          new ByteArrayInputStream(textHtml.getBytes(Charsets.UTF_8)), indexIt, true);
    } else {
      createFileAndAttachment(textHtml, fileName, componentId, context, objectId, userId, indexIt);
    }
  }

  /**
   * Method declaration remove and recreates the file attached
   *
   * @param textHtml String : contains the text published by the wysiwyg
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param userId
   */
  public static void updateFileAndAttachment(String textHtml, String componentId,
      String objectId, String userId){
    updateFileAndAttachment(textHtml, componentId, objectId, userId, true);
  }

  public static void updateFileAndAttachment(String textHtml, String componentId,
      String objectId, String userId, boolean indexIt) {
    updateFileAndAttachment(textHtml, getWysiwygFileName(objectId), componentId, WYSIWYG_CONTEXT,
        objectId, userId, indexIt);
  }

  public static void save(String textHtml, String componentId, String objectId, String userId,
      String language, boolean indexIt) {
    if (I18NHelper.isDefaultLanguage(language)) {
      updateFileAndAttachment(textHtml, componentId, objectId, userId, indexIt);
    } else {
      updateFileAndAttachment(textHtml, getWysiwygFileName(objectId, language), componentId,
          WYSIWYG_CONTEXT, objectId, userId, indexIt);
    }
  }

  /**
   * Method declaration remove the file attached. *
   *
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   */
  public static void deleteWysiwygAttachments(String componentId, String objectId)  {
    try {
      // delete all the attachments
      ForeignPK foreignKey = new ForeignPK(objectId, componentId);
      List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService()
          .listAllDocumentsByForeignKey(foreignKey, null);
      for (SimpleDocument document : documents) {
        AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
      }
    } catch (AttachmentException exc) {
      SilverTrace.error("wysiwyg", "WysiwygController.deleteWysiwygAttachments()",
          "wysiwyg.DELETING_WYSIWYG_ATTACHMENTS_FAILED", exc);
      throw exc;
    }
  }

  /**
   * La méthode deleteWysiwygAttachments efface tous les attachments de la publication donc pour
   * éviter une éventuelle régression, je crée une nouvelle méthode
   *
   * @param spaceId
   * @param componentId
   * @param objectId
   */
  public static void deleteWysiwygAttachmentsOnly(String spaceId, String componentId,
      String objectId) throws WysiwygException {
    try {
      List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(new ForeignPK(objectId, componentId),
          DocumentType.wysiwyg, null);
      for (SimpleDocument wysiwygAttachment : docs) {
        AttachmentServiceFactory.getAttachmentService().deleteAttachment(wysiwygAttachment);
      }
    } catch (Exception exc) {
      throw new WysiwygException("WysiwygController.deleteWysiwygAttachments()",
          SilverpeasException.ERROR, "wysiwyg.DELETING_WYSIWYG_ATTACHMENTS_FAILED", exc);
    }
  }

  /**
   * Loads the content of a Wysiwyg as a String.
   *
   * @param foreignPk
   * @param context
   * @param lang
   * @return
   */
  public static String loadFileAndAttachment(ForeignPK foreignPk, String context, String lang) {
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignPk, DocumentType.valueOf(context), lang);
    if (!docs.isEmpty()) {
      return loadContent(docs.get(0), lang);
    }
    return "";
  }

  public static String loadContent(SimpleDocument doc, String lang) {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {
      AttachmentServiceFactory.getAttachmentService().getBinaryContent(buffer, doc.getPk(), lang);
      return new String(buffer.toByteArray(), Charsets.UTF_8);
    } finally {
      IOUtils.closeQuietly(buffer);
    }
  }

  /**
   * Method declaration return the contents of the file attached.
   *
   *
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @return text : the contents of the file attached.
   */
  public static String loadFileAndAttachment(String componentId, String objectId) {
    return loadFileAndAttachment(new ForeignPK(objectId, componentId), WYSIWYG_CONTEXT, null);
  }

  /**
   * Load wysiwyg content
   *
   * @param componentId
   * @param objectId
   * @param language
   * @return
   */
  public static String load(String componentId, String objectId, String language) {
    String content = null;

    boolean useDefaultLanguage = (language == null || I18NHelper.isDefaultLanguage(language));
    if (!useDefaultLanguage) {
      content = loadFileAndAttachment(new ForeignPK(objectId, componentId), WYSIWYG_CONTEXT,
          language);
    }

    // use default language also if content has not been found in specified language
    if ((!StringUtil.isDefined(content)) || (useDefaultLanguage)) {
      content = loadFileAndAttachment(new ForeignPK(objectId, componentId), WYSIWYG_CONTEXT, null);
    }

    if (content == null) {
      content = "";
    }
    return content;
  }

  /**
   * Get all Silverpeas Files linked by wysiwyg content
   *
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
   *
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
   *
   *
   * @param componentId
   * @param objectId
   * @return
   */
  public static boolean haveGotWysiwyg(String componentId, String objectId) {
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(new ForeignPK(objectId, componentId),
        DocumentType.wysiwyg, null);
    return !docs.isEmpty();
  }

  public static boolean haveGotWysiwyg(String componentId, String objectId, String language) {
      String wysiwygContent = load(componentId, objectId, language);
      return StringUtil.isDefined(wysiwygContent);
  }

  /**
   * Method declaration to search all file attached by primary key of customer object and context of
   * file attached
   *
   *
   * @param fileName String : name of the file
   * @param componentId String : the id of component.
   * @param context String : for example wysiwyg.
   * @param objectId String : for example the id of the publication.
   * @return SimpleDocument
   */
  public static SimpleDocument searchAttachmentDetail(String fileName, String componentId,
      String context, String objectId) {
    ForeignPK foreignKey = new ForeignPK(objectId, componentId);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignKey, DocumentType.valueOf(context), null);
    for (SimpleDocument doc : documents) {
      if (doc.getFilename().equals(fileName)) {
        return doc;
      }
    }
    return null;
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
   *
   * @param cheminFichier the path to the directory containing the file.
   * @param nomFichier the name of the file.
   * @param contenuFichier the content of the file.
   * @ return
   * @throws WysiwygException
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
   * Method declaration
   *
   *
   *
   * @param oldComponentId
   * @param oldObjectId
   * @param componentId
   * @param objectId
   * @param userId
   * @see
   */
  public static void copy(String oldComponentId, String oldObjectId,
      String componentId, String objectId, String userId) {
    SilverTrace.info("wysiwyg", "WysiwygController.copy()", "root.MSG_GEN_ENTER_METHOD");
    // copy the wysiwyg
    ForeignPK foreignKey = new ForeignPK(oldObjectId, oldComponentId);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, null);
    ForeignPK targetPk = new ForeignPK(objectId, componentId);
    for (SimpleDocument doc : documents) {
      AttachmentServiceFactory.getAttachmentService().copyDocument(doc, targetPk);
    }
    List<SimpleDocument> images = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
    for (SimpleDocument image : images) {
      AttachmentServiceFactory.getAttachmentService().copyDocument(image, targetPk);
    }
  }

  /**
   * Usefull to maintain forward compatibility (old URLs to images)
   *
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
    while (languages.hasNext()) {
      String language = languages.next();
      String wysiwyg = load(newComponentId, newObjectId, language);
      if (StringUtil.isDefined(wysiwyg)) {
        wysiwyg = replaceInternalImagesPath(wysiwyg, oldComponentId, oldObjectId, newComponentId,
            newObjectId);
        // overwrite
        createFile(createPath(newComponentId, WYSIWYG_CONTEXT),
            getWysiwygFileName(newObjectId, language), wysiwyg);
      }
    }
  }

  public static String getWysiwygPath(String componentId, String objectId, String language) {
    String path = createPath(componentId, WYSIWYG_CONTEXT);
    return path + getWysiwygFileName(objectId, language);
  }

  public static String getWysiwygPath(String componentId, String objectId) {
    String path = createPath(componentId, WYSIWYG_CONTEXT);
    return path + getWysiwygFileName(objectId);
  }

  public static List<ComponentInstLight> getGalleries() {
    String[] galleryIds = ORGANIZATION_CONTROLLER.getCompoId("gallery");
    List<ComponentInstLight> galleries = new ArrayList<ComponentInstLight>(galleryIds.length);
    for (String componentId : galleryIds) {
      if (StringUtil.getBooleanValue(ORGANIZATION_CONTROLLER.getComponentParameterValue("gallery"
          + componentId, "viewInWysiwyg"))) {
        galleries.add(ORGANIZATION_CONTROLLER.getComponentInstLight("gallery"
            + componentId));
      }
    }
    return galleries;
  }

  /**
   * Gets the components dedicated to file storage
   *
   * @param userId the user identifier is used to retrieve only the authorized components for the
   * user
   * @return a components list
   */
  public static List<ComponentInstLight> getStorageFile(String userId) {
    // instiate all needed objects
    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    // gets all kmelia components
    String[] compoIds = ORGANIZATION_CONTROLLER.getCompoId("kmelia");
    for (String compoId : compoIds) {
      // retain only the components considered as a file storage
      if (StringUtil.getBooleanValue(ORGANIZATION_CONTROLLER.getComponentParameterValue(compoId,
          "publicFiles"))) {
        ComponentInstLight component = ORGANIZATION_CONTROLLER.getComponentInstLight(compoId);
        components.add(component);
      }
    }
    return components;
  }

  /**
   * Index given embedded linked files
   *
   * @param indexEntry index entry to update
   * @param embeddedAttachmentIds embedded linked files ids
   */
  public static void indexEmbeddedLinkedFiles(FullIndexEntry indexEntry,
      List<String> embeddedAttachmentIds) {
    for (String attachmentId : embeddedAttachmentIds) {
      try {
        SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
            searchDocumentById(
            new SimpleDocumentPK(attachmentId), null);
        if (attachment != null) {
          indexEntry.addLinkedFileContent(attachment.getAttachmentPath(), CharEncoding.UTF_8,
              attachment.getContentType(), attachment.getLanguage());
          indexEntry.addLinkedFileId(attachmentId);
        }
      } catch (Exception e) {
        SilverTrace.warn("wisiwyg", "WysiwygController", "root.MSG_GEN_PARAM_VALUE",
            "Erreur dans l'indexation d'un fichier joint lié au contenu wysiwyg - attachmentId:"
            + attachmentId);
      }
    }
  }

  /**
   * To create path Warning: the token separing the repertories is ","
   *
   * @param componentId : type String: the name of component
   * @param context : type String: string made up of the repertories separated by token ","
   */
  public static String createPath(String componentId, String context) {
    String path = null;

    if ((context != null) && !context.equals("null") && (context.length() > 0)) {

      // to create the context
      String strAt = "Attachment,";

      strAt = strAt.concat(context);

      StringTokenizer strToken = new StringTokenizer(strAt, ",");

      // number of elements
      int nElt = strToken.countTokens();

      // to init array
      String[] ctx = new String[nElt];

      int k = 0;

      while (strToken.hasMoreElements()) {
        ctx[k] = (String) strToken.nextElement();
        k++;
      }

      path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    } else {
      String[] ctx = {"Attachment"};

      path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    }

    try {
      File d = new File(path);

      if (!d.exists()) {
        FileFolderManager.createFolder(path);
      }

      return path;
    } catch (Exception e) {
      throw new AttachmentRuntimeException(
          "AttachmentController.createPath(String spaceId, String componentId, String context)",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_CREATE_FILE", e);
    }
  }
}
