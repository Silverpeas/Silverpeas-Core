/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.content.wysiwyg.service;

import org.silverpeas.core.contribution.content.wysiwyg.WysiwygContent;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.content.wysiwyg.notification.WysiwygEventNotifier;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central service to manage Wysiwyg.
 */
@Singleton
public class WysiwygManager {

  protected WysiwygManager() {
    // hidden constructor
  }

  @Inject
  private WysiwygEventNotifier notifier;

  public final static String WYSIWYG_CONTEXT = DocumentType.wysiwyg.name();
  public final static String WYSIWYG_IMAGES = "Images";
  public final static String WYSIWYG_WEBSITES = "webSites";

  /**
   * This method loads the content of the WYSIWYG file directly from the filesystem for backward
   * compatibility.
   * @param id the unique identifier of the contribution to which the WYSIWYG is related.
   * @param language the language of he WYSIWYG content.
   * @return the content of the WYSIWYG.
   */
  private String loadFromFileSystemDirectly(ContributionIdentifier id, String language)
      throws IOException {
    File wysiwygFile = new File(getLegacyWysiwygPath(WYSIWYG_CONTEXT, id.getComponentInstanceId()),
        getWysiwygFileName(id.getLocalId(), language));
    if (!wysiwygFile.exists() || !wysiwygFile.isFile()) {
      wysiwygFile = new File(getLegacyWysiwygPath(WYSIWYG_CONTEXT, id.getComponentInstanceId()),
          getOldWysiwygFileName(id.getLocalId()));
    }
    String content = null;
    if (wysiwygFile.exists() && wysiwygFile.isFile()) {
      content = FileUtils.readFileToString(wysiwygFile);
    }
    return content;
  }

  private String getLegacyWysiwygPath(String context, String componentId) {
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

  /**
   * Turn over all the images attached according to the parameters id, componentId.
   * @param id the id of the object to which this wysiwyg is attached.
   * @param componentId the id of component.
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getImages(String id, String componentId) {
    List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKeyAndType(new ForeignPK(id, componentId), DocumentType.image, null);
    Iterator<SimpleDocument> it = attachments.iterator();
    while (it.hasNext()) {
      SimpleDocument document = it.next();
      if (!document.isContentImage()) {
        it.remove();
      }
    }
    return attachments;
  }

  public String getWebsiteRepository() {
    SettingBundle websiteSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
    return websiteSettings.getString("uploadsPath");
  }

  /**
   * Get images of the website.
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public String[][] getWebsiteImages(String path, String componentId) throws WysiwygException {
    checkPath(path);
    try {
      Collection<File> listImages = FileFolderManager.getAllImages(path);
      Iterator<File> i = listImages.iterator();
      int nbImages = listImages.size();
      String[][] images = new String[nbImages][2];
      SilverTrace
          .info("wysiwyg", "WysiwygController.getWebsiteImages()", "root.MSG_GEN_PARAM_VALUE",
              "nbImages=" + nbImages + " path=" + path);
      File image;
      for (int j = 0; j < nbImages; j++) {
        image = i.next();
        SilverTrace
            .info("wysiwyg", "WysiwygController.getWebsiteImages()", "root.MSG_GEN_PARAM_VALUE",
                "image=" + image.getAbsolutePath());
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
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N][2] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public String[][] getWebsitePages(String path, String componentId) throws WysiwygException {
    checkPath(path);
    try {
      Collection<File> listPages = FileFolderManager.getAllWebPages(getNodePath(path, componentId));
      Iterator<File> i = listPages.iterator();
      int nbPages = listPages.size();
      String[][] pages = new String[nbPages][2];

      File page;
      for (int j = 0; j < nbPages; j++) {
        page = i.next();
        SilverTrace
            .info("wysiwyg", "WysiwygController.getWebsitePages()", "root.MSG_GEN_PARAM_VALUE",
                "page=" + page.getAbsolutePath());
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
   * @param componentId the component id.
   * @param path the full path.
   * @return the path for the nodes.
   */
  String finNode(String path, String componentId) {

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
   * @param componentId the component id.
   * @param path the full path.
   * @return the path for the nodes.
   */
  String finNode2(String path, String componentId) {

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
  String getNodePath(String currentPath, String componentId) {
    String path = currentPath;
    if (path != null) {
      path = suppressFinalSlash(path);
      int indexComponent = path.lastIndexOf(componentId) + componentId.length();
      String pathEnd = suppressLeadingSlashesOrAntislashes(path.substring(indexComponent));
      int index = -1;
      if (pathEnd.contains("/")) {
        index = pathEnd.indexOf('/');
      } else if (pathEnd.contains("\\")) {
        index = pathEnd.indexOf('\\');
      }


      if (index == -1) {
        return path;
      }
      return path.substring(0, path.indexOf(pathEnd) + index);
    }
    return "";
  }

  /* supprAntiSlashFin */
  String suppressFinalSlash(String path) {
    if (path.endsWith("/")) {
      return suppressFinalSlash(path.substring(0, path.length() - 1));
    }
    return path;
  }

  String ignoreLeadingSlash(String path) {
    if (path.startsWith("/")) {
      return ignoreLeadingSlash(path.substring(1));
    }
    return path;
  }

  String supprDoubleAntiSlash(String path) {
    StringBuilder res = new StringBuilder("");
    int i = 0;
    while (i < path.length()) {
      char car = path.charAt(i);
      if (car == '\\' && path.charAt(i + 1) == '\\') {
        res.append(car);
        i++;
      } else {
        res.append(car);
      }
      i++;
    }
    return res.toString();
  }

  String suppressLeadingSlashesOrAntislashes(String path) {
    if (path.startsWith("\\") || path.startsWith("/")) {
      return suppressLeadingSlashesOrAntislashes(path.substring(1));
    }
    return path;
  }

  /* doubleAntiSlash */
  String doubleAntiSlash(String path) {
    int i = 0;
    String res = path;
    boolean ok = true;
    while (ok) {
      int j = i + 1;
      if ((i < res.length()) && (j < res.length())) {
        char car1 = res.charAt(i);
        char car2 = res.charAt(j);
        if (!((car1 == '\\' && car2 == '\\') || (car1 != '\\' && car2 != '\\'))) {
          String before = res.substring(0, j);
          String after = res.substring(j);
          if (!after.startsWith("\\\\") && !before.endsWith("\\\\")) {
            res = before + '\\' + after;
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
   * @param objectId: for example the id of the publication.
   * @return the name of the file
   */
  public String getOldWysiwygFileName(String objectId) {
    return objectId + WYSIWYG_CONTEXT + ".txt";
  }

  public String getWysiwygFileName(String objectId, String lang) {
    String language = I18NHelper.checkLanguage(lang);
    return objectId + WYSIWYG_CONTEXT + "_" + language + ".txt";
  }

  /**
   * Method declaration built the name of the images to be attached.
   * @param objectId : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public String getImagesFileName(String objectId) {
    return objectId + WYSIWYG_IMAGES;
  }

  public void deleteFileAndAttachment(String componentId, String id) {
    ForeignPK foreignKey = new ForeignPK(id, componentId);
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(foreignKey, null);
    for (SimpleDocument doc : documents) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(doc);
    }
  }

  public void deleteFile(String componentId, String objectId, String language) {
    ForeignPK foreignKey = new ForeignPK(objectId, componentId);
    List<SimpleDocument> files = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(foreignKey, null);
    for (SimpleDocument file : files) {
      if (file != null && file.getFilename().
          equalsIgnoreCase(getWysiwygFileName(objectId, language))) {
        AttachmentServiceProvider.getAttachmentService().removeContent(file, language, false);
      }
    }
  }

  /**
   * Creation of the file and its attachment.
   * @param content the WYSIWYG content to create.
   * @param context the context images/wysiwyg....
   */
  public void createFileAndAttachment(final WysiwygContent content, String context) {
    createFileAndAttachment(content, DocumentType.valueOf(context), true, true);
  }

  private void createFileAndAttachment(WysiwygContent content, DocumentType context,
      boolean indexIt, boolean notify) {
    if (!StringUtil.isDefined(content.getData())) {
      return;
    }
    String fileName =
        getWysiwygFileName(content.getContributionId().getLocalId(), content.getLanguage());
    String language = I18NHelper.checkLanguage(content.getLanguage());
    String textHtml = content.getData();
    String userId = content.getAuthorId();
    SimpleDocumentPK docPk =
        new SimpleDocumentPK(null, content.getContributionId().getComponentInstanceId());
    SimpleDocument document =
        new SimpleDocument(docPk, content.getContributionId().getLocalId(), 0, false, userId,
            new SimpleAttachment(fileName, language, fileName, null, textHtml.length(),
                MimeTypes.HTML_MIME_TYPE, userId, new Date(), null));
    document.setDocumentType(context);
    AttachmentServiceProvider.getAttachmentService()
        .createAttachment(document, new ByteArrayInputStream(textHtml.getBytes(Charsets.UTF_8)),
            indexIt, notify);
    if (notify) {
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, content);
    }
    AttachmentServiceProvider.getAttachmentService()
        .unlock(new UnlockContext(document.getId(), userId, document.getLanguage()));
  }

  /**
   * Creation of the file and its attachment.
   * @param content the WYSIWYG content to create.
   */
  public void createFileAndAttachment(final WysiwygContent content) {
    createFileAndAttachment(content, WYSIWYG_CONTEXT);
  }

  /**
   * Creation of the file and its attachment but without indexing it.
   * @param content the WYSIWYG content to create.
   */
  public void createUnindexedFileAndAttachment(final WysiwygContent content) {
    createFileAndAttachment(content, DocumentType.wysiwyg, false, false);
  }

  /**
   * Add all elements attached to object identified by the given index into the given index
   * @param indexEntry the index of the related resource.
   * @param pk the primary key of the container of the wysiwyg.
   * @param language the language.
   */
  public void addToIndex(FullIndexEntry indexEntry, ForeignPK pk, String language) {
    List<SimpleDocument> docs = AttachmentServiceProvider.getAttachmentService()
        .listDocumentsByForeignKeyAndType(pk, DocumentType.wysiwyg, language);
    if (!docs.isEmpty()) {
      for (SimpleDocument wysiwyg : docs) {
        String wysiwygPath = wysiwyg.getAttachmentPath();
        indexEntry.addFileContent(wysiwygPath, null, MimeTypes.HTML_MIME_TYPE, language);
        String wysiwygContent = loadContent(wysiwyg, language);
        // index embedded linked attachment (links presents in wysiwyg content)
        List<String> embeddedAttachmentIds = getEmbeddedAttachmentIds(wysiwygContent);
        indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
      }
    }
  }

  /**
   * This method must be synchronized. Quick wysiwyg's saving can generate problems without
   * synchronization !!!
   * @param content the WYSIWYG content to save.
   * @param indexIt should the content be indexed?
   */
  private void saveFile(final WysiwygContent content, boolean indexIt) {
    String lang = I18NHelper.checkLanguage(content.getLanguage());
    DocumentType wysiwygType = DocumentType.wysiwyg;
    String fileName = getWysiwygFileName(content.getContributionId().getLocalId(), lang);
    SilverTrace
        .info("wysiwyg", "WysiwygController.updateFileAndAttachment()", "root.MSG_GEN_PARAM_VALUE",
            "fileName=" + fileName + " context=" + wysiwygType + "objectId=" +
                content.getContributionId().getLocalId());
    SimpleDocument document =
        searchAttachmentDetail(content.getContributionId(), wysiwygType, lang);
    if (document != null) {
      // Load old content
      WysiwygContent beforeUpdateContent = load(content.getContributionId(), lang);
      if (!document.getLanguage().equals(lang)) {
        document.setFilename(fileName);
      }
      document.setLanguage(lang);
      document.setSize(content.getData().getBytes(Charsets.UTF_8).length);
      document.setDocumentType(wysiwygType);
      document.setUpdatedBy(content.getAuthorId());
      if (document.getSize() > 0) {
        AttachmentServiceProvider.getAttachmentService().updateAttachment(document,
            new ByteArrayInputStream(content.getData().getBytes(Charsets.UTF_8)), indexIt, false);
        notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdateContent, content);
      } else {
        AttachmentServiceProvider.getAttachmentService().removeContent(document, lang, true);
      }
    } else {
      createFileAndAttachment(content, wysiwygType, indexIt, true);
    }
  }

  /**
   * Removes and recreates the file attached
   * @param content the updated WYSIWYG content.
   */
  public void updateFileAndAttachment(final WysiwygContent content) {
    updateFileAndAttachment(content, true);
  }

  public void updateFileAndAttachment(final WysiwygContent content, boolean indexIt) {
    saveFile(content, indexIt);
  }

  public void save(final WysiwygContent content, boolean indexIt) {
    saveFile(content, indexIt);
  }

  /**
   * Method declaration remove the file attached.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   */
  public void deleteWysiwygAttachments(String componentId, String objectId) {
    try {
      // delete all the attachments
      ForeignPK foreignKey = new ForeignPK(objectId, componentId);
      List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService()
          .listAllDocumentsByForeignKey(foreignKey, null);
      for (SimpleDocument document : documents) {
        AttachmentServiceProvider.getAttachmentService().deleteAttachment(document);
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
   * @param spaceId
   * @param componentId
   * @param objectId
   * @throws WysiwygException
   */
  public void deleteWysiwygAttachmentsOnly(String spaceId, String componentId, String objectId)
      throws WysiwygException {
    try {
      ForeignPK foreignKey = new ForeignPK(objectId, componentId);
      List<SimpleDocument> docs = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, null);
      for (SimpleDocument wysiwygAttachment : docs) {
        AttachmentServiceProvider.getAttachmentService().deleteAttachment(wysiwygAttachment);
      }
      docs = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
      for (SimpleDocument document : docs) {
        AttachmentServiceProvider.getAttachmentService().deleteAttachment(document);
      }
    } catch (Exception exc) {
      throw new WysiwygException("WysiwygController.deleteWysiwygAttachments()",
          SilverpeasException.ERROR, "wysiwyg.DELETING_WYSIWYG_ATTACHMENTS_FAILED", exc);
    }
  }

  private String loadContent(SimpleDocument document, String lang) {
    if (isEmptyWysiwygContent(document, lang)) {
      return "";
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {
      AttachmentServiceProvider.getAttachmentService()
          .getBinaryContent(buffer, document.getPk(), lang);
      return new String(buffer.toByteArray(), Charsets.UTF_8);
    } finally {
      IOUtils.closeQuietly(buffer);
    }
  }

  /**
   * Indicates for the specified document and the specified language if the related content is
   * empty.
   * @param document the simple document to verify.
   * @param lang the language of the content to verify.
   * @return true if the specified content is empty, false otherwise.
   */
  private boolean isEmptyWysiwygContent(SimpleDocument document, String lang) {
    if (document.getDocumentType() == DocumentType.wysiwyg) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try {
        AttachmentServiceProvider.getAttachmentService()
            .getBinaryContent(buffer, document.getPk(), lang, 0, 1);
        if (buffer.size() == 0) {
          return true;
        }
      } finally {
        IOUtils.closeQuietly(buffer);
      }
    }
    return false;
  }

  /**
   * Load wysiwyg content.
   * @param id the unique identifier of the contribution to which the WYSIWYG is related.
   * @param language the language of the content.
   * @return the wysiwyg content.
   */
  public WysiwygContent load(final ContributionIdentifier id, String language) {
    String content = internalLoad(id, language);
    if (I18NHelper.isI18nContentEnabled() && content != null && StringUtil.isNotDefined(content)) {
      List<String> languages = new ArrayList<>(I18NHelper.getAllSupportedLanguages());
      languages.remove(language);
      for (String lang : languages) {
        content = internalLoad(id, lang);
        if (content == null || StringUtil.isDefined(content)) {
          break;
        }
      }
    }
    if (content == null) {
      content = "";
    }
    return new WysiwygContent(id, content);
  }

  /**
   * Load wysiwyg content.
   * @param id the unique identifier of the contribution to which the WYSIWYG is related.
   * @param language the language of the content.
   * @return not empty string if a content exists, "" if it is empty,
   * null if empty guessed on language fallback.
   */
  private String internalLoad(ContributionIdentifier id, String language) {
    String currentLanguage = I18NHelper.checkLanguage(language);
    String finalLanguage = currentLanguage;
    String content = "";
    SimpleDocument document = searchAttachmentDetail(id, DocumentType.wysiwyg, currentLanguage);
    if (document != null) {
      content = loadContent(document, currentLanguage);
      finalLanguage = document.getLanguage();
    }
    if (!StringUtil.isDefined(content)) {
      try {
        String contentFromSystem = loadFromFileSystemDirectly(id, currentLanguage);
        if (StringUtil.isDefined(contentFromSystem)) {
          content = contentFromSystem;
          finalLanguage = currentLanguage;
        }
      } catch (IOException ex) {
        SilverTrace.error("wysiwyg", "WysiwygController.load()", "Error loading content", ex);
      }
    }
    if (StringUtil.isNotDefined(content) && !finalLanguage.equals(currentLanguage)) {
      content = null;
    }
    return content;
  }

  /**
   * Get all Silverpeas Files linked by wysiwyg content
   * @param content
   * @return
   */
  public List<String> getEmbeddedAttachmentIds(String content) {
    List<String> attachmentIds = new ArrayList<>();

    if (content != null) {
      // 1 - search url with format : /silverpeas/File/####
      Pattern attachmentLinkPattern = Pattern.compile("href=\\\"\\/silverpeas\\/File\\/(.*?)\\\"");
      Matcher linkMatcher = attachmentLinkPattern.matcher(content);
      while (linkMatcher.find()) {
        String fileId = linkMatcher.group(1);
        attachmentIds.add(fileId);
      }

      // 2 - search url with format : /silverpeas/FileServer/....attachmentId=###...
      attachmentLinkPattern =
          Pattern.compile("href=\\\"\\/silverpeas\\/FileServer\\/(.*?)attachmentId=(\\d*)");
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
  public String loadFileWebsite(String path, String fileName) throws WysiwygException {
    checkPath(path);
    try {
      return FileFolderManager.getCode(path, fileName);
    } catch (UtilException e) {
      // There is no document
      throw new WysiwygException("WysiwygController.loadFileWebsite()", SilverpeasException.WARNING,
          "wysiwyg.NO_WYSIWYG_DOCUMENT_ASSOCIATED");
    }
  }

  public boolean haveGotWysiwygToDisplay(final ContributionIdentifier id, String language) {
    return haveGotWysiwyg(id, language);
  }

  public boolean haveGotWysiwyg(final ContributionIdentifier id, String language) {
    WysiwygContent wysiwygContent = load(id, language);
    return !wysiwygContent.isEmpty();
  }

  /**
   * Search all file attached by primary key of customer object and context of file attached
   * @param id the unique identifier of the contribution to which the WYSIWYG is related.
   * @param context String : for example wysiwyg.
   * @return SimpleDocument
   */
  private SimpleDocument searchAttachmentDetail(ContributionIdentifier id, DocumentType context,
      String lang) {
    String language = I18NHelper.checkLanguage(lang);
    SimpleDocumentList<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService()
        .listDocumentsByForeignKeyAndType(
            new ForeignPK(id.getLocalId(), id.getComponentInstanceId()), context, language);
    if (!documents.isEmpty()) {
      return documents.orderByLanguageAndLastUpdate(lang).get(0);
    }
    return null;
  }

  /**
   * updateWebsite : creation or update of a file of a website Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   * contenuFichier = code du fichier : "<HTML><TITLE>...."
   * @param filePath
   * @param fileContent
   * @param fileName
   */
  public void updateWebsite(String filePath, String fileName, String fileContent)
      throws WysiwygException {
    checkPath(filePath);

    createFile(filePath, fileName, fileContent);
  }

  /**
   * Creation or update of a file
   * @param filePath the path to the directory containing the file.
   * @param fileName the name of the file.
   * @param fileContent the content of the file.
   * @return the created file.
   */
  protected File createFile(String filePath, String fileName, String fileContent)
      throws WysiwygException {
    checkPath(filePath);

    FileFolderManager.createFile(filePath, fileName, fileContent);
    File directory = new File(filePath);
    return FileUtils.getFile(directory, fileName);
  }

  /**
   * Method declaration
   * @param oldComponentId
   * @param oldObjectId
   * @param componentId
   * @param objectId
   * @param userId
   * @see
   */
  public Map<String, String> copy(String oldComponentId, String oldObjectId, String componentId,
      String objectId, String userId) {

    ForeignPK foreignKey = new ForeignPK(oldObjectId, oldComponentId);
    ForeignPK targetPk = new ForeignPK(objectId, componentId);
    SimpleDocument copy = null;
    List<Pair<SimpleDocumentPK, SimpleDocumentPK>> oldNewImagePkMapping = new ArrayList<>();
    Map<String, String> fileIds = new HashMap<>();
    List<String> languagesWithEmptyContent = new ArrayList<>();
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      SimpleDocumentList<SimpleDocument> documents =
          AttachmentServiceProvider.getAttachmentService().
              listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, language)
              .removeLanguageFallbacks();
      for (SimpleDocument doc : documents) {
        if (!isEmptyWysiwygContent(doc, doc.getLanguage())) {
          doc.getAttachment().setCreatedBy(userId);
          if (copy == null) {
            SimpleDocumentPK pk =
                AttachmentServiceProvider.getAttachmentService().copyDocument(doc, targetPk);
            copy = AttachmentServiceProvider.getAttachmentService()
                .searchDocumentById(pk, doc.getLanguage());
            List<SimpleDocument> images = AttachmentServiceProvider.getAttachmentService().
                listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
            for (SimpleDocument image : images) {
              SimpleDocumentPK imageCopyPk =
                  AttachmentServiceProvider.getAttachmentService().copyDocument(image, targetPk);
              fileIds.put(image.getId(), imageCopyPk.getId());
              oldNewImagePkMapping.add(Pair.of(image.getPk(), imageCopyPk));
            }
          }
          copy.setLanguage(language);
          String content =
              replaceInternalImagesPath(loadContent(copy, doc.getLanguage()), oldComponentId,
                  oldObjectId, componentId, objectId);
          for (Pair<SimpleDocumentPK, SimpleDocumentPK> oldNewPk : oldNewImagePkMapping) {
            content = replaceInternalImageId(content, oldNewPk.getLeft(), oldNewPk.getRight());
          }
          AttachmentServiceProvider.getAttachmentService()
              .updateAttachment(copy, new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)),
                  false, false);
        } else {
          languagesWithEmptyContent.add(language);
        }
      }
    }
    if (copy != null) {
      for (String languageWithEmptyContent : languagesWithEmptyContent) {
        AttachmentServiceProvider.getAttachmentService()
            .removeContent(copy, languageWithEmptyContent, false);
      }
    }
    return fileIds;
  }

  public void move(String fromComponentId, String fromObjectId, String componentId,
      String objectId) {
    ForeignPK fromForeignPK = new ForeignPK(fromObjectId, fromComponentId);
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService()
        .listAllDocumentsByForeignKey(fromForeignPK, null);
    ForeignPK toForeignPK = new ForeignPK(objectId, componentId);
    for (SimpleDocument document : documents) {
      AttachmentServiceProvider.getAttachmentService().moveDocument(document, toForeignPK);
    }

    // change images path in wysiwyg
    wysiwygPlaceHaveChanged(fromComponentId, fromObjectId, componentId, objectId);
  }

  String replaceInternalImageId(String wysiwygContent, SimpleDocumentPK oldPK,
      SimpleDocumentPK newPK) {
    String from = "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK.getId() + "/";
    String fromOldId =
        "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK.getOldSilverpeasId() +
            "/";
    String to = "/componentId/" + newPK.getInstanceId() + "/attachmentId/" + newPK.getId() + "/";
    return wysiwygContent.replaceAll(from, to).replaceAll(fromOldId, to);
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
  private String replaceInternalImagesPath(String wysiwygContent, String oldComponentId,
      String oldObjectId, String componentId, String objectId) {
    String newStr = "";
    if (wysiwygContent.contains("FileServer")) {
      String co = "ComponentId=" + oldComponentId;
      String di = "Directory=Attachment/" + getImagesFileName(oldObjectId);
      String diBis = "Directory=Attachment%2F" + getImagesFileName(oldObjectId);
      String diTer = "Directory=Attachment\\" + getImagesFileName(oldObjectId);
      String diQua = "Directory=Attachment%5C" + getImagesFileName(oldObjectId);

      int begin = 0;
      int end;

      // search for "ComponentId=" and replace
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

  public void wysiwygPlaceHaveChanged(String oldComponentId, String oldObjectId,
      String newComponentId, String newObjectId) {
    ForeignPK foreignKey = new ForeignPK(newObjectId, newComponentId);
    List<SimpleDocument> images = null;
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, language)
          .removeLanguageFallbacks();
      for (SimpleDocument document : documents) {
        String wysiwyg = loadContent(document, language);
        if (StringUtil.isDefined(wysiwyg)) {
          wysiwyg = replaceInternalImagesPath(wysiwyg, oldComponentId, oldObjectId, newComponentId,
              newObjectId);
          if (images == null) {
            images = AttachmentServiceProvider.getAttachmentService().
                listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
          }
          for (SimpleDocument image : images) {
            image.getPk().setComponentName(oldComponentId);
            SimpleDocumentPK imageCopyPk = new SimpleDocumentPK(image.getId(), newComponentId);
            imageCopyPk.setOldSilverpeasId(image.getOldSilverpeasId());
            wysiwyg = replaceInternalImageId(wysiwyg, image.getPk(), imageCopyPk);
          }
          AttachmentServiceProvider.getAttachmentService().updateAttachment(document,
              new ByteArrayInputStream(wysiwyg.getBytes(Charsets.UTF_8)), true, true);
        }
      }
    }
  }

  public String getWysiwygPath(String componentId, String objectId, String language) {
    List<SimpleDocument> attachements = AttachmentServiceProvider.getAttachmentService()
        .listDocumentsByForeignKeyAndType(new ForeignPK(objectId, componentId),
            DocumentType.wysiwyg, language);
    if (!attachements.isEmpty()) {
      return attachements.get(0).getAttachmentPath();
    }
    return "";
  }

  public String getWysiwygPath(String componentId, String objectId) {
    return getWysiwygPath(componentId, objectId, null);
  }

  public List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = new ArrayList<>();
    OrganizationController orgaController =
        OrganizationControllerProvider.getOrganisationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (String compoId : compoIds) {
      if (StringUtil.getBooleanValue(
          orgaController.getComponentParameterValue("gallery" + compoId, "viewInWysiwyg"))) {
        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoId);
        galleries.add(gallery);
      }
    }
    return galleries;
  }

  /**
   * Gets the components dedicated to file storage
   * @return a components list
   */
  public List<ComponentInstLight> getStorageFile() {
    // instiate all needed objects
    List<ComponentInstLight> components = new ArrayList<>();
    OrganizationController controller = OrganizationControllerProvider.getOrganisationController();
    // gets all kmelia components
    String[] compoIds = controller.getCompoId("kmelia");
    for (String compoId : compoIds) {
      // retain only the components considered as a file storage
      if (StringUtil
          .getBooleanValue(controller.getComponentParameterValue(compoId, "publicFiles"))) {
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
  public void indexEmbeddedLinkedFiles(FullIndexEntry indexEntry,
      List<String> embeddedAttachmentIds) {
    for (String attachmentId : embeddedAttachmentIds) {
      try {
        SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
            searchDocumentById(new SimpleDocumentPK(attachmentId), null);
        if (attachment != null) {
          indexEntry.addLinkedFileContent(attachment.getAttachmentPath(), CharEncoding.UTF_8,
              attachment.getContentType(), attachment.getLanguage());
          indexEntry.addLinkedFileId(attachmentId);
        }
      } catch (Exception e) {
        SilverTrace.warn("wisiwyg", "WysiwygController", "root.MSG_GEN_PARAM_VALUE",
            "Erreur dans l'indexation d'un fichier joint lié au contenu wysiwyg - attachmentId:" +
                attachmentId);
      }
    }
  }

  /**
   * To create path. Warning: the token separing the repertories is ",".
   * @param componentId : the name of component
   * @param context : string made up of the repertories separated by token ","
   * @return the path.
   */
  public String createPath(String componentId, String context) {
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

  /**
   * Checks the specified path is valid according to some security rules. For example, check there
   * is no attempt to go up the path to access a forbidden resource.
   * @param path the patch to check.
   * @throws WysiwygException if the path breaks some security rules.
   */
  private void checkPath(String path) throws WysiwygException {
    if (path.contains("..")) {
      throw new WysiwygException(WysiwygManager.class.getSimpleName() + ".checkPath",
          SilverpeasException.ERROR, "peasCore.RESOURCE_ACCESS_FORBIDDEN");
    }
  }
}
