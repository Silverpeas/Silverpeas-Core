/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.contribution.model.CoreContributionType.WYSIWYG;

/**
 * Central service to manage Wysiwyg.
 */
public class WysiwygController {

  public static final String WYSIWYG_CONTEXT = WysiwygManager.WYSIWYG_CONTEXT;
  public static final String WYSIWYG_WEBSITES = WysiwygManager.WYSIWYG_WEBSITES;

  /**
   * Hidden constructor
   */
  private WysiwygController() {
  }

  private static LocalizedContribution contributionFrom(final String componentId,
      final String objectId, final String language) {
    return contributionFrom(componentId, objectId, language, true);
  }

  private static LocalizedContribution contributionFrom(final String componentId,
      final String objectId, final String language, boolean indexable) {
    return new WysiwygLocalizedContribution(componentId, objectId, language, indexable);
  }

  /**
   * Gets the manager.
   * @return
   */
  protected static WysiwygManager getManager() {
    return ServiceProvider.getSingleton(WysiwygManager.class);
  }

  /**
   * Turn over all the images attached according to the parameters id, componentId.
   * @param id the id of the object to which this wysiwyg is attached.
   * @param componentId the id of component.
   * @return List<SimpleDocument>
   */
  public static List<SimpleDocument> getImages(String id, String componentId) {
    return getManager().getImages(id, componentId);
  }

  public static String getWebsiteRepository() {
    return getManager().getWebsiteRepository();
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
    return getManager().getWebsiteImages(path, componentId);
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
    return getManager().getWebsitePages(path, componentId);
  }

  public static String getWysiwygFileName(String objectId, String currentLanguage) {
    return getManager().getWysiwygFileName(objectId, currentLanguage);
  }

  /**
   * Method declaration built the name of the images to be attached.
   * @param objectId : for example the id of the publication.
   * @return fileName String : name of the file
   */
  public static String getImagesFileName(String objectId) {
    return getManager().getImagesFileName(objectId);
  }

  public static void deleteFileAndAttachment(String componentId, String id) {
    getManager().deleteFileAndAttachment(componentId, id);
  }

  public static void deleteFile(String componentId, String objectId, String language) {
    getManager().deleteFile(componentId, objectId, language);
  }

  /**
   * Creation of the file and its attachment.
   * @param textHtml a {@link String} containing the text published by the wysiwyg.
   * @param resource a reference to the resource to which is attached the wysiwyg.
   * @param context the context images/wysiwyg....
   * @param userId the user creating the wysiwyg.
   * @param contentLanguage the language of the content of the wysiwyg.
   */
  public static void createFileAndAttachment(String textHtml, ResourceReference resource,
      String context, String userId, String contentLanguage) {
    WysiwygContent content = new WysiwygContent(
        contributionFrom(resource.getInstanceId(), resource.getId(), contentLanguage), textHtml)
        .authoredBy(User.getById(userId));
    getManager().createFileAndAttachment(content, context);
  }

  /**
   * Method declaration creation of the file and its attachment.
   * @param textHtml a {@link String} containing the text published by the wysiwyg
   * @param resource a reference to the resource to which is attached the wysiwyg.
   * @param userId the author of the content.
   * @param contentLanguage the language of the content.
   */
  public static void createFileAndAttachment(String textHtml, ResourceReference resource,
      String userId, String contentLanguage) {
    WysiwygContent content = new WysiwygContent(
        contributionFrom(resource.getInstanceId(), resource.getId(), contentLanguage), textHtml)
        .authoredBy(User.getById(userId));
    getManager().createFileAndAttachment(content);
  }

  /**
   * Method declaration creation of the file and its attachment.
   * @param textHtml a {@link String} containing the text published by the wysiwyg
   * @param resource a reference to the resource to which is attached the wysiwyg.
   * @param userId the author of the content.
   * @param contentLanguage the language of the content.
   */
  public static void createUnindexedFileAndAttachment(String textHtml, ResourceReference resource,
      String userId, String contentLanguage) {
    WysiwygContent content = new WysiwygContent(
        contributionFrom(resource.getInstanceId(), resource.getId(), contentLanguage), textHtml)
        .authoredBy(User.getById(userId));
    getManager().createUnindexedFileAndAttachment(content);
  }

  /**
   * Add all elements attached to object identified by the given index into the given index
   * @param indexEntry the index of the related resource.
   * @param pk the primary key of the container of the wysiwyg.
   * @param language the language.
   */
  public static void addToIndex(FullIndexEntry indexEntry, ResourceReference pk, String language) {
    getManager().addToIndex(indexEntry, pk, language);
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
    WysiwygContent content =
        new WysiwygContent(contributionFrom(componentId, objectId, language), textHtml)
            .authoredBy(User.getById(userId));
    getManager().updateFileAndAttachment(content);
  }

  public static void updateFileAndAttachment(String textHtml, String componentId, String objectId,
      String userId, String language, boolean indexIt) {
    WysiwygContent content =
        new WysiwygContent(contributionFrom(componentId, objectId, language, indexIt), textHtml)
            .authoredBy(User.getById(userId));
    getManager().updateFileAndAttachment(content);
  }

  public static void save(String textHtml, String componentId, String objectId, String userId,
      String language, boolean indexIt) {
    WysiwygContent content =
        new WysiwygContent(contributionFrom(componentId, objectId, language, indexIt), textHtml)
            .authoredBy(User.getById(userId));
    getManager().save(content);
  }

  /**
   * Method declaration remove the file attached.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   */
  public static void deleteWysiwygAttachments(String componentId, String objectId) {
    getManager().deleteWysiwygAttachments(componentId, objectId);
  }

  /**
   * La méthode deleteWysiwygAttachments efface tous les attachments de la publication donc pour
   * éviter une éventuelle régression, je crée une nouvelle méthode
   * @param componentId
   * @param objectId
   * @throws WysiwygException
   */
  public static void deleteWysiwygAttachmentsOnly(String componentId, String objectId)
      throws WysiwygException {
    getManager().deleteWysiwygAttachmentsOnly(componentId, objectId);
  }

  /**
   * Gets representation of a wysiwyg content.
   * @param contribution the localized contribution for which the WYSIWYG content has to be get.
   * @return {@link WysiwygContent} instance.
   */
  public static WysiwygContent get(final LocalizedContribution contribution) {
    return getManager().getByContribution(contribution);
  }

  /**
   * Gets representation of a wysiwyg content.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param language the language of the content.
   * @return {@link WysiwygContent} instance.
   */
  public static WysiwygContent get(String componentId, String objectId, String language) {
    return get(contributionFrom(componentId, objectId, language));
  }

  /**
   * Loads wysiwyg content rendered for edition context.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param language the language of the content.
   * @return text : the contents of the file attached.
   */
  public static String load(String componentId, String objectId, String language) {
    return get(componentId, objectId, language).getRenderer().renderEdition();
  }

  /**
   * Loads wysiwyg content that will only be read and never be updated.<br>
   * Indeed, this method will call standard WYSIWYG transformations that are necessary only in
   * readOnly mode. The resizing of image attachments for example.
   * @param componentId String : the id of component.
   * @param objectId String : for example the id of the publication.
   * @param language the language of the content.
   * @return text : the contents of the file attached.
   */
  public static String loadForReadOnly(String componentId, String objectId, String language) {
    return get(componentId, objectId, language).getRenderer().renderView();
  }

  /**
   * Get all Silverpeas Files linked by wysiwyg content
   * @param content
   * @return
   */
  public static List<String> getEmbeddedAttachmentIds(String content) {
    return getManager().getEmbeddedAttachmentIds(content);
  }

  /**
   * Method declaration return the contents of the file.
   * @param fileName String : name of the file
   * @param path String : the path of the file
   * @return text : the contents of the file attached.
   * @throws WysiwygException
   */
  public static String loadFileWebsite(String path, String fileName) throws WysiwygException {
    return getManager().loadFileWebsite(path, fileName);
  }

  public static boolean haveGotWysiwygToDisplay(String componentId, String objectId,
      String language) {
    return getManager().haveGotWysiwygToDisplay(contributionFrom(componentId, objectId, language));
  }

  public static boolean haveGotWysiwyg(String componentId, String objectId, String language) {
    return getManager().haveGotWysiwyg(contributionFrom(componentId, objectId, language));
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
    getManager().updateWebsite(cheminFichier, nomFichier, contenuFichier);
  }

  /**
   * Method declaration
   * @param oldComponentId
   * @param oldObjectId
   * @param componentId
   * @param objectId
   * @param userId
   *
   */
  public static Map<String, String> copy(String oldComponentId, String oldObjectId,
      String componentId, String objectId, String userId) {
    return getManager().copy(oldComponentId, oldObjectId, componentId, objectId, userId);
  }

  public static void move(String fromComponentId, String fromObjectId, String componentId,
      String objectId) {
    getManager().move(fromComponentId, fromObjectId, componentId, objectId);
  }

  public static void wysiwygPlaceHaveChanged(String oldComponentId, String oldObjectId,
      String newComponentId, String newObjectId) {
    getManager().wysiwygPlaceHaveChanged(oldComponentId, oldObjectId, newComponentId, newObjectId);
  }

  public static String getWysiwygPath(String componentId, String objectId, String language) {
    return getManager().getWysiwygPath(componentId, objectId, language);
  }

  public static String getWysiwygPath(String componentId, String objectId) {
    return getManager().getWysiwygPath(componentId, objectId);
  }

  public static List<ComponentInstLight> getGalleries() {
    return getManager().getGalleries();
  }

  /**
   * Index given embedded linked files
   * @param indexEntry index entry to update
   * @param embeddedAttachmentIds embedded linked files ids
   */
  public static void indexEmbeddedLinkedFiles(FullIndexEntry indexEntry,
      List<String> embeddedAttachmentIds) {
    getManager().indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
  }

  private static class WysiwygLocalizedContribution implements LocalizedContribution {

    private final String componentId;
    private final String objectId;
    private final String language;
    private final boolean indexable;

    public WysiwygLocalizedContribution(final String componentId, final String objectId,
        final String language, final boolean indexable) {
      this.componentId = componentId;
      this.objectId = objectId;
      this.language = language;
      this.indexable = indexable;
    }

    @Override
    public ContributionIdentifier getContributionId() {
      return ContributionIdentifier.from(componentId, objectId, WYSIWYG);
    }

    @Override
    public User getCreator() {
      return null;
    }

    @Override
    public Date getCreationDate() {
      return null;
    }

    @Override
    public User getLastModifier() {
      return null;
    }

    @Override
    public Date getLastModificationDate() {
      return null;
    }

    @Override
    public boolean canBeAccessedBy(final User user) {
      return true;
    }

    @Override
    public String getLanguage() {
      return language;
    }

    @Override
    public boolean isIndexable() {
      return indexable;
    }
  }
}
