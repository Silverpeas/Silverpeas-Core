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
package org.silverpeas.wysiwyg.control;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.util.Charsets;
import org.silverpeas.wysiwyg.WysiwygException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central service to manage Wysiwyg.
 */
public class WysiwygController {

  public final static String WYSIWYG_CONTEXT = DocumentType.wysiwyg.name();
  public final static String WYSIWYG_IMAGES = "Images";
  public final static String WYSIWYG_WEBSITES = "webSites";

  /**
   * This method loads the content of the wysiwyg file directly from the filesystem for backward
   * compatibility.
   *
   * @param foreignPK the primary key of the object to which this wysiwyg is attached.
   * @param language the language of he wysiwyg content.
   * @return the content of the wysiwyg.
   */
  private static String loadFromFileSystemDirectly(ForeignPK foreignPK, String language) throws
      IOException {
    File wysiwygFile = new File(getLegacyWysiwygPath(WYSIWYG_CONTEXT, foreignPK.getInstanceId()),
        getWysiwygFileName(foreignPK.getId(), language));
    if (!wysiwygFile.exists() || !wysiwygFile.isFile()) {
      wysiwygFile = new File(getLegacyWysiwygPath(WYSIWYG_CONTEXT, foreignPK.getInstanceId()),
          getOldWysiwygFileName(foreignPK.getId()));
    }
    String content = "";
    if (wysiwygFile.exists() && wysiwygFile.isFile()) {
      content = FileUtils.readFileToString(wysiwygFile);
    }
    return content;
  }

  private static String getLegacyWysiwygPath(String context, String componentId) {
    String path;
    if (StringUtil.isDefined(context)) {
      String strAt = "Attachment,";
      strAt = strAt.concat(context);
      String[] ctx = StringUtil.split(strAt, ',');
      path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    } else {
      String[] ctx = {"Attachment"};
      path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    }
    return path;
  }

  public WysiwygController() {
  }

  /**
   * Turn over all the images attached according to the parameters id, componentId.
   *
   * @param id the id of the object to which this wysiwyg is attached.
   * @param componentId the id of component.
   * @return imagesList a table of string[N][2] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   */
  public static String[][] getImages(String id, String componentId) {
    List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(new ForeignPK(id, componentId), DocumentType.image, null);
    int nbImages = attachments.size();
    String[][] imagesList = new String[nbImages][2];
    for (int i = 0; i < nbImages; i++) {
      SimpleDocument attD = attachments.get(i);
      String path = attD.getAttachmentPath();
      imagesList[i][0] = path;
      imagesList[i][1] = attD.getFilename();
      SilverTrace.info("wysiwyg", "WysiwygController.getImages()",
          "root.MSG_GEN_PARAM_VALUE", imagesList[i][0] + "] [" + imagesList[i][1]);
    }
    return imagesList;
  }

  /**
   * Get images of the website.
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
    } catch (UtilException e) {
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
    } catch (UtilException e) {
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
    chemin = suppressLeadingSlashesOrAntislashes(chemin);
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
      chemin = suppressFinalSlash(chemin);
      int indexComponent = chemin.lastIndexOf(componentId) + componentId.length();
      String finChemin = suppressLeadingSlashesOrAntislashes(chemin.substring(indexComponent));
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
      }
      return chemin.substring(0, chemin.indexOf(finChemin) + index);
    }
    return "";
  }

  /* supprAntiSlashFin */
  static String suppressFinalSlash(String path) {
    if (path.endsWith("/")) {
      return suppressFinalSlash(path.substring(0, path.length() - 1));
    }
    return path;
  }

  static String ignoreLeadingSlash(String chemin) {
    if (chemin.startsWith("/")) {
      return ignoreLeadingSlash(chemin.substring(1));
    }
    return chemin;
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

  static String suppressLeadingSlashesOrAntislashes(String chemin) {
    if (chemin.startsWith("\\") || chemin.startsWith("/")) {
      return suppressLeadingSlashesOrAntislashes(chemin.substring(1));
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
   * Build the name of the file to be attached.
   *
   * @param objectId: for example the id of the publication.
   * @return the name of the file
   */
  public static String getOldWysiwygFileName(String objectId) {
    return objectId + WYSIWYG_CONTEXT + ".txt";
  }

  public static String getWysiwygFileName(String objectId, String currentLanguage) {
    String language = I18NHelper.checkLanguage(currentLanguage);
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

  public static void deleteFileAndAttachment(String componentId, String id) {
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
   * Creation of the file and its attachment.
   *
   * @param textHtml String : contains the text published by the wysiwyg.
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param context the context images/wysiwyg....
   * @param userId the user creating the wysiwyg.
   * @param contentLanguage the language of the content of the wysiwyg.
   */
  public static void createFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      String context, String userId, String contentLanguage) {
    createFileAndAttachment(textHtml, foreignKey, DocumentType.valueOf(context), userId,
        contentLanguage, true, true);
  }

  private static void createFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      DocumentType context, String userId, String contentLanguage, boolean indexIt,
      boolean invokeCallback) {
    String fileName = getWysiwygFileName(foreignKey.getId(), contentLanguage);
    if (!StringUtil.isDefined(textHtml)) {
      return;
    }
    int iUserId = -1;
    if (userId != null) {
      iUserId = Integer.parseInt(userId);
    }
    String language = I18NHelper.checkLanguage(contentLanguage);
    SimpleDocumentPK docPk = new SimpleDocumentPK(null, foreignKey.getInstanceId());
    SimpleDocument document = new SimpleDocument(docPk, foreignKey.getId(), 0, false, userId,
        new SimpleAttachment(fileName, language, fileName, null, textHtml.length(),
        MimeTypes.HTML_MIME_TYPE, userId, new Date(), null));
    document.setDocumentType(context);
    AttachmentServiceFactory.getAttachmentService().createAttachment(document,
        new ByteArrayInputStream(textHtml.getBytes(Charsets.UTF_8)), indexIt, invokeCallback);
    if (invokeCallback) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_ON_WYSIWYG, iUserId, foreignKey.getInstanceId(),
          foreignKey.getId());
    }
    AttachmentServiceFactory.getAttachmentService().unlock(new UnlockContext(document.getId(),
        userId, document.getLanguage()));
  }

  /**
   * Method declaration creation of the file and its attachment.
   *
   *
   * @param textHtml String : contains the text published by the wysiwyg
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param userId the author of the content.
   * @param contentLanguage the language of the content.
   */
  public static void createFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      String userId, String contentLanguage) {
    createFileAndAttachment(textHtml, foreignKey, WYSIWYG_CONTEXT, userId, contentLanguage);
  }

  /**
   * Method declaration creation of the file and its attachment.
   *
   *
   * @param textHtml String : contains the text published by the wysiwyg
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param userId the author of the content.
   * @param contentLanguage the language of the content.
   */
  public static void createUnindexedFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      String userId, String contentLanguage) {
    createFileAndAttachment(textHtml, foreignKey, DocumentType.wysiwyg, userId, contentLanguage,
        false, false);
  }

  /**
   * Add all elements attached to object identified by the given index into the given index
   *
   * @param indexEntry the index of the related resource.
   * @param pk the primary key of the container of the wysiwyg.
   * @param language the language.
   */
  public static void addToIndex(FullIndexEntry indexEntry, ForeignPK pk, String language) {
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(pk, DocumentType.wysiwyg, language);
    if (!docs.isEmpty()) {
      for (SimpleDocument wysiwyg : docs) {
        String wysiwygPath = wysiwyg.getAttachmentPath();
        indexEntry.addFileContent(wysiwygPath, null, MimeTypes.HTML_MIME_TYPE, language);
        String wysiwygContent = loadContent(docs.get(0), language);
        // index embedded linked attachment (links presents in wysiwyg content)
        List<String> embeddedAttachmentIds = getEmbeddedAttachmentIds(wysiwygContent);
        indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
      }
    }
  }

  /**
   * This method must be synchronized. Quick wysiwyg's saving can generate problems without
   * synchronization !!!
   *
   *
   * @param textHtml
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param context
   * @param userId
   */
  private static void saveFile(String textHtml, WAPrimaryKey foreignKey, DocumentType context,
      String userId, String language, boolean indexIt) {
    String fileName = getWysiwygFileName(foreignKey.getId(), language);
    SilverTrace.info("wysiwyg", "WysiwygController.updateFileAndAttachment()",
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + fileName + " context=" + context + "objectId="
        + foreignKey.getId());
    SimpleDocument document = searchAttachmentDetail(foreignKey, context, language);
    if (document != null) {
      document.setLanguage(I18NHelper.checkLanguage(language));
      document.setSize(textHtml.getBytes(Charsets.UTF_8).length);
      document.setDocumentType(context);
      AttachmentServiceFactory.getAttachmentService().updateAttachment(document,
          new ByteArrayInputStream(textHtml.getBytes(Charsets.UTF_8)), indexIt, true);
    } else {
      createFileAndAttachment(textHtml, foreignKey, context, userId, language, indexIt, true);
    }
  }

  /**
   * Method declaration remove and recreates the file attached
   *
   * @param textHtml String : contains the text published by the wysiwyg
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param userId
   * @param language the language of the content.
   */
  public static void updateFileAndAttachment(String textHtml, String componentId,
      String objectId, String userId, String language) {
    updateFileAndAttachment(textHtml, componentId, objectId, userId, language, true);
  }

  public static void updateFileAndAttachment(String textHtml, String componentId,
      String objectId, String userId, String language, boolean indexIt) {
    saveFile(textHtml, new ForeignPK(objectId, componentId), DocumentType.wysiwyg, userId, language,
        indexIt);
  }

  public static void save(String textHtml, String componentId, String objectId, String userId,
      String language, boolean indexIt) {
    saveFile(textHtml, new ForeignPK(objectId, componentId), DocumentType.wysiwyg, userId, language,
        indexIt);
  }

  /**
   * Method declaration remove the file attached. *
   *
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   */
  public static void deleteWysiwygAttachments(String componentId, String objectId) {
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
   * @throws org.silverpeas.wysiwyg.WysiwygException
   */
  public static void deleteWysiwygAttachmentsOnly(String spaceId, String componentId,
      String objectId) throws WysiwygException {
    try {
      ForeignPK foreignKey = new ForeignPK(objectId, componentId);
      List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, null);
      for (SimpleDocument wysiwygAttachment : docs) {
        AttachmentServiceFactory.getAttachmentService().deleteAttachment(wysiwygAttachment);
      }
      docs = AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKeyAndType(
          foreignKey, DocumentType.image, null);
      for (SimpleDocument document : docs) {
        AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
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
  public static String loadFileAndAttachment(ForeignPK foreignPk, DocumentType context, String lang) {
    SimpleDocument document = searchAttachmentDetail(foreignPk, context, lang);
    if (document != null) {
      return loadContent(document, lang);
    }
    return "";
  }

  public static String loadContent(SimpleDocument doc, String lang) {
    return loadContent(doc.getPk(), lang);
  }

  private static String loadContent(SimpleDocumentPK pk, String lang) {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {
      AttachmentServiceFactory.getAttachmentService().getBinaryContent(buffer, pk, lang);
      return new String(buffer.toByteArray(), Charsets.UTF_8);
    } finally {
      IOUtils.closeQuietly(buffer);
    }
  }

  /**
   * Load wysiwyg content.
   *
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param language the language of the content.
   * @return text : the contents of the file attached.
   */
  public static String load(String componentId, String objectId, String language) {
    String currentLanguage = I18NHelper.checkLanguage(language);
    String content = loadFileAndAttachment(new ForeignPK(objectId, componentId),
        DocumentType.wysiwyg, currentLanguage);
    if (!StringUtil.isDefined(content)) {
      try {
        content = loadFromFileSystemDirectly(new ForeignPK(objectId, componentId), currentLanguage);
      } catch (IOException ex) {
        SilverTrace.error("wysiwyg", "WysiwygController.load()", "Error loading content", ex);
      }
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
   */
  public static List<String> getEmbeddedAttachmentIds(String content) {
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
    try {
      return FileFolderManager.getCode(path, fileName);
    } catch (UtilException e) {
      // There is no document
      throw new WysiwygException("WysiwygController.loadFileWebsite()",
          SilverpeasException.WARNING, "wysiwyg.NO_WYSIWYG_DOCUMENT_ASSOCIATED");
    }
  }

  public static boolean haveGotWysiwygToDisplay(String componentId, String objectId,
      String language) {
    String wysiwygContent = load(componentId, objectId, language);
    if (!StringUtil.isDefined(wysiwygContent) && I18NHelper.isI18N) {
      Iterator<String> iter = I18NHelper.getLanguages();
      while (iter.hasNext() && !StringUtil.isDefined(wysiwygContent)) {
        wysiwygContent = load(componentId, objectId, iter.next());
      }
    }
    return StringUtil.isDefined(wysiwygContent);
  }

  public static boolean haveGotWysiwyg(String componentId, String objectId, String language) {
    String wysiwygContent = load(componentId, objectId, language);
    return StringUtil.isDefined(wysiwygContent);
  }

  /**
   * Search all file attached by primary key of customer object and context of file attached
   *
   * @param foreignKey the id of the attached object.
   * @param context String : for example wysiwyg.
   * @return SimpleDocument
   */
  private static SimpleDocument searchAttachmentDetail(WAPrimaryKey foreignKey, DocumentType context,
      String lang) {
    String language = I18NHelper.checkLanguage(lang);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignKey, context, language);
    if (!documents.isEmpty()) {
      return documents.get(0);
    }
    return null;
  }

  /**
   * updateWebsite : creation or update of a file of a website Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   * contenuFichier = code du fichier : "<HTML><TITLE>...."
   *
   * @param cheminFichier
   * @param contenuFichier
   * @param nomFichier
   */
  public static void updateWebsite(String cheminFichier, String nomFichier, String contenuFichier) {
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
   * @return the created file.
   */
  protected static File createFile(String cheminFichier, String nomFichier, String contenuFichier) {
    SilverTrace.info("wysiwyg", "WysiwygController.createFile()", "root.MSG_GEN_ENTER_METHOD",
        "cheminFichier=" + cheminFichier + " nomFichier=" + nomFichier);
    FileFolderManager.createFile(cheminFichier, nomFichier, contenuFichier);
    File directory = new File(cheminFichier);
    return FileUtils.getFile(directory, nomFichier);
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
  public static void copy(String oldComponentId, String oldObjectId, String componentId,
      String objectId, String userId) {
    SilverTrace.info("wysiwyg", "WysiwygController.copy()", "root.MSG_GEN_ENTER_METHOD");
    ForeignPK foreignKey = new ForeignPK(oldObjectId, oldComponentId);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, null);
    ForeignPK targetPk = new ForeignPK(objectId, componentId);
    for (SimpleDocument doc : documents) {
      doc.getFile().setCreatedBy(userId);
      SimpleDocumentPK pk = AttachmentServiceFactory.getAttachmentService().copyDocument(doc,
          targetPk);
      SimpleDocument copy = AttachmentServiceFactory.getAttachmentService().searchDocumentById(pk,
          doc.getLanguage());
      String content = replaceInternalImagesPath(loadContent(copy, doc.getLanguage()),
          oldComponentId, oldObjectId, componentId, objectId);

      List<SimpleDocument> images = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
      for (SimpleDocument image : images) {
        SimpleDocumentPK imageCopyPk = AttachmentServiceFactory.getAttachmentService().copyDocument(
            image, targetPk);
        content = replaceInternalImageId(content, image.getPk(), imageCopyPk);
      }
      AttachmentServiceFactory.getAttachmentService().updateAttachment(copy,
          new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)), true, true);
    }

  }

  static String replaceInternalImageId(String wysiwygContent, SimpleDocumentPK oldPK,
      SimpleDocumentPK newPK) {
    String from = "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK.getId() + "/";
    String fromOldId = "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK
        .getOldSilverpeasId() + "/";
    String to = "/componentId/" + newPK.getInstanceId() + "/attachmentId/" + newPK.getId() + "/";
    return wysiwygContent.replaceAll(from, to).replaceAll(fromOldId, to);
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
      String newComponentId, String newObjectId) {
    ForeignPK foreignKey = new ForeignPK(oldObjectId, newComponentId);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, null);
    if (documents != null && !documents.isEmpty()) {
      for (SimpleDocument document : documents) {
        for (String language : I18NHelper.getAllSupportedLanguages()) {
          String wysiwyg = load(newComponentId, newObjectId, language);
          if (StringUtil.isDefined(wysiwyg)) {
            List<SimpleDocument> images = AttachmentServiceFactory.getAttachmentService().
                listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
            for (SimpleDocument image : images) {
              wysiwyg = replaceInternalImagesPath(wysiwyg, oldComponentId, oldObjectId,
                  newComponentId, newObjectId);
              image.getPk().setComponentName(oldComponentId);
              SimpleDocumentPK imageCopyPk = new SimpleDocumentPK(image.getId(), newComponentId);
              imageCopyPk.setOldSilverpeasId(image.getOldSilverpeasId());
              wysiwyg = replaceInternalImageId(wysiwyg, image.getPk(), imageCopyPk);
            }
            AttachmentServiceFactory.getAttachmentService().updateAttachment(document,
                new ByteArrayInputStream(wysiwyg.getBytes(Charsets.UTF_8)), true, true);
          }
        }
      }
    }
  }

  public static String getWysiwygPath(String componentId, String objectId, String language) {
    List<SimpleDocument> attachements = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(new ForeignPK(objectId, componentId), DocumentType.wysiwyg,
        language);
    if (!attachements.isEmpty()) {
      return attachements.get(0).getAttachmentPath();
    }
    return "";
  }

  public static String getWysiwygPath(String componentId, String objectId) {
    return getWysiwygPath(componentId, objectId, null);
  }

  public static List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = new ArrayList<ComponentInstLight>();
    OrganisationController orgaController = OrganisationControllerFactory
        .getOrganisationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (String compoId : compoIds) {
      if (StringUtil.getBooleanValue(orgaController.getComponentParameterValue("gallery" + compoId,
          "viewInWysiwyg"))) {
        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoId);
        galleries.add(gallery);
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
    OrganisationController controller = OrganisationControllerFactory.getOrganisationController();
    // gets all kmelia components
    String[] compoIds = controller.getCompoId("kmelia");
    for (String compoId : compoIds) {
      // retain only the components considered as a file storage
      if (StringUtil.getBooleanValue(controller.getComponentParameterValue(compoId, "publicFiles"))) {
        ComponentInstLight component = controller.getComponentInstLight(compoId);
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
            searchDocumentById(new SimpleDocumentPK(attachmentId), null);
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
   * To create path. Warning: the token separing the repertories is ",".
   *
   *
   * @param componentId : the name of component
   * @param context : string made up of the repertories separated by token ","
   * @return the path.
   */
  public static String createPath(String componentId, String context) {
    String path = getLegacyWysiwygPath(context, componentId);
    try {
      File folder = new File(path);
      if (!folder.exists()) {
        FileFolderManager.createFolder(path);
      }
      return path;
    } catch (UtilException e) {
      throw new AttachmentException("Wysiwyg.createPath(spaceId, componentId, context)",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_CREATE_FILE", e);
    }
  }
}
