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
package org.silverpeas.wysiwyg.control;

import org.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.wysiwyg.WysiwygException;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Central service to manage Wysiwyg.
 */
public class WysiwygController {

  public final static String WYSIWYG_CONTEXT = WysiwygManager.WYSIWYG_CONTEXT;
  public final static String WYSIWYG_IMAGES = WysiwygManager.WYSIWYG_IMAGES;
  public final static String WYSIWYG_WEBSITES = WysiwygManager.WYSIWYG_WEBSITES;

  private static WysiwygController instance = new WysiwygController();

  @Inject
  private WysiwygManager wysiwygManager;

  /**
   * Gets the manager.
   * @return
   */
  protected WysiwygManager getManager() {
    if (wysiwygManager == null) {
      wysiwygManager = new WysiwygManager();
    }
    return wysiwygManager;
  }

  /**
   * @return a WysiwygController instance.
   */
  public static WysiwygController getInstance() {
    return instance;
  }

  protected WysiwygController() {
  }

  /**
   * Turn over all the images attached according to the parameters id, componentId.
   * @param id the id of the object to which this wysiwyg is attached.
   * @param componentId the id of component.
   * @return List<SimpleDocument>
   */
  public static List<SimpleDocument> getImages(String id, String componentId) {
    return getInstance().getManager().getImages(id, componentId);
  }

  public static String getWebsiteRepository() {
    return getInstance().getManager().getWebsiteRepository();
  }

  /**
   * Get images of the website.
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public static String[][] getWebsiteImages(String path, String componentId)
      throws WysiwygException {
    return getInstance().getManager().getWebsiteImages(path, componentId);
  }

  /**
   * Method declaration Get html pages of the website
   * @param path type String: for example of the directory
   * @param componentId
   * @return imagesList a table of string[N][2] with in logical index [N][0] = path name [N][1] =
   * logical name of the file.
   * @throws WysiwygException
   */
  public static String[][] getWebsitePages(String path, String componentId)
      throws WysiwygException {
    return getInstance().getManager().getWebsitePages(path, componentId);
  }

  /**
   * Build the name of the file to be attached.
   * @param objectId: for example the id of the publication.
   * @return the name of the file
   */
  public static String getOldWysiwygFileName(String objectId) {
    return getInstance().getManager().getOldWysiwygFileName(objectId);
  }

  public static String getWysiwygFileName(String objectId, String currentLanguage) {
    return getInstance().getManager().getWysiwygFileName(objectId, currentLanguage);
  }

  /**
   * Method declaration built the name of the images to be attached.
   * @param objectId : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public static String getImagesFileName(String objectId) {
    return getInstance().getManager().getImagesFileName(objectId);
  }

  public static void deleteFileAndAttachment(String componentId, String id) {
    getInstance().getManager().deleteFileAndAttachment(componentId, id);
  }

  public static void deleteFile(String componentId, String objectId, String language) {
    getInstance().getManager().deleteFile(componentId, objectId, language);
  }

  /**
   * Creation of the file and its attachment.
   * @param textHtml String : contains the text published by the wysiwyg.
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param context the context images/wysiwyg....
   * @param userId the user creating the wysiwyg.
   * @param contentLanguage the language of the content of the wysiwyg.
   */
  public static void createFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      String context, String userId, String contentLanguage) {
    getInstance().getManager()
        .createFileAndAttachment(textHtml, foreignKey, context, userId, contentLanguage);
  }

  /**
   * Method declaration creation of the file and its attachment.
   * @param textHtml String : contains the text published by the wysiwyg
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param userId the author of the content.
   * @param contentLanguage the language of the content.
   */
  public static void createFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      String userId, String contentLanguage) {
    getInstance().getManager()
        .createFileAndAttachment(textHtml, foreignKey, userId, contentLanguage);
  }

  /**
   * Method declaration creation of the file and its attachment.
   * @param textHtml String : contains the text published by the wysiwyg
   * @param foreignKey the id of object to which is attached the wysiwyg.
   * @param userId the author of the content.
   * @param contentLanguage the language of the content.
   */
  public static void createUnindexedFileAndAttachment(String textHtml, WAPrimaryKey foreignKey,
      String userId, String contentLanguage) {
    getInstance().getManager()
        .createUnindexedFileAndAttachment(textHtml, foreignKey, userId, contentLanguage);
  }

  /**
   * Add all elements attached to object identified by the given index into the given index
   * @param indexEntry the index of the related resource.
   * @param pk the primary key of the container of the wysiwyg.
   * @param language the language.
   */
  public static void addToIndex(FullIndexEntry indexEntry, ForeignPK pk, String language) {
    getInstance().getManager().addToIndex(indexEntry, pk, language);
  }

  /**
   * Method declaration remove and recreates the file attached
   * @param textHtml String : contains the text published by the wysiwyg
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param userId
   * @param language the language of the content.
   */
  public static void updateFileAndAttachment(String textHtml, String componentId, String objectId,
      String userId, String language) {
    getInstance().getManager()
        .updateFileAndAttachment(textHtml, componentId, objectId, userId, language);
  }

  public static void updateFileAndAttachment(String textHtml, String componentId, String objectId,
      String userId, String language, boolean indexIt) {
    getInstance().getManager()
        .updateFileAndAttachment(textHtml, componentId, objectId, userId, language, indexIt);
  }

  public static void save(String textHtml, String componentId, String objectId, String userId,
      String language, boolean indexIt) {
    getInstance().getManager().save(textHtml, componentId, objectId, userId, language, indexIt);
  }

  /**
   * Method declaration remove the file attached.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   */
  public static void deleteWysiwygAttachments(String componentId, String objectId) {
    getInstance().getManager().deleteWysiwygAttachments(componentId, objectId);
  }

  /**
   * La méthode deleteWysiwygAttachments efface tous les attachments de la publication donc pour
   * éviter une éventuelle régression, je crée une nouvelle méthode
   * @param spaceId
   * @param componentId
   * @param objectId
   * @throws org.silverpeas.wysiwyg.WysiwygException
   */
  public static void deleteWysiwygAttachmentsOnly(String spaceId, String componentId,
      String objectId) throws WysiwygException {
    getInstance().getManager().deleteWysiwygAttachmentsOnly(spaceId, componentId, objectId);
  }

  /**
   * Load wysiwyg content.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param language the language of the content.
   * @return text : the contents of the file attached.
   */
  public static String load(String componentId, String objectId, String language) {
    return getInstance().getManager().load(componentId, objectId, language);
  }

  /**
   * Get all Silverpeas Files linked by wysiwyg content
   * @param content
   * @return
   */
  public static List<String> getEmbeddedAttachmentIds(String content) {
    return getInstance().getManager().getEmbeddedAttachmentIds(content);
  }

  /**
   * Method declaration return the contents of the file.
   * @param fileName String : name of the file
   * @param path String : the path of the file
   * @return text : the contents of the file attached.
   * @throws WysiwygException
   */
  public static String loadFileWebsite(String path, String fileName) throws WysiwygException {
    return getInstance().getManager().loadFileWebsite(path, fileName);
  }

  public static boolean haveGotWysiwygToDisplay(String componentId, String objectId,
      String language) {
    return getInstance().getManager().haveGotWysiwygToDisplay(componentId, objectId, language);
  }

  public static boolean haveGotWysiwyg(String componentId, String objectId, String language) {
    return getInstance().getManager().haveGotWysiwyg(componentId, objectId, language);
  }

  /**
   * updateWebsite : creation or update of a file of a website Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   * contenuFichier = code du fichier : "<HTML><TITLE>...."
   * @param cheminFichier
   * @param contenuFichier
   * @param nomFichier
   */
  public static void updateWebsite(String cheminFichier, String nomFichier, String contenuFichier)
      throws WysiwygException {
    getInstance().getManager().updateWebsite(cheminFichier, nomFichier, contenuFichier);
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
  public static Map<String, String> copy(String oldComponentId, String oldObjectId,
      String componentId, String objectId, String userId) {
    return getInstance().getManager()
        .copy(oldComponentId, oldObjectId, componentId, objectId, userId);
  }

  public static void move(String fromComponentId, String fromObjectId, String componentId,
      String objectId) {
    getInstance().getManager().move(fromComponentId, fromObjectId, componentId, objectId);
  }

  public static void wysiwygPlaceHaveChanged(String oldComponentId, String oldObjectId,
      String newComponentId, String newObjectId) {
    getInstance().getManager()
        .wysiwygPlaceHaveChanged(oldComponentId, oldObjectId, newComponentId, newObjectId);
  }

  public static String getWysiwygPath(String componentId, String objectId, String language) {
    return getInstance().getManager().getWysiwygPath(componentId, objectId, language);
  }

  public static String getWysiwygPath(String componentId, String objectId) {
    return getInstance().getManager().getWysiwygPath(componentId, objectId);
  }

  public static List<ComponentInstLight> getGalleries() {
    return getInstance().getManager().getGalleries();
  }

  /**
   * Gets the components dedicated to file storage
   *
   * @return a components list
   */
  public static List<ComponentInstLight> getStorageFile() {
    return getInstance().getManager().getStorageFile();
  }

  /**
   * Index given embedded linked files
   * @param indexEntry index entry to update
   * @param embeddedAttachmentIds embedded linked files ids
   */
  public static void indexEmbeddedLinkedFiles(FullIndexEntry indexEntry,
      List<String> embeddedAttachmentIds) {
    getInstance().getManager().indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
  }

  /**
   * To create path. Warning: the token separing the repertories is ",".
   * @param componentId : the name of component
   * @param context : string made up of the repertories separated by token ","
   * @return the path.
   */
  public static String createPath(String componentId, String context) {
    return getInstance().getManager().createPath(componentId, context);
  }
}
