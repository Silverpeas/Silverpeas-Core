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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.CoreContributionType;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.model.LocalizedAttachment;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.util.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A document as an attachment to a given contribution and gathering for a same document all of its
 * translations, each of them represented by a different ({@link SimpleDocument} instances. These
 * different translations can be get with the {@link Document#getTranslation(String)} method.
 * The properties of a {@link Document} instance are those of the document master that is either the
 * single document file (in the case there is only one translation) or the document file written in
 * the default language of Silverpeas (see {@link I18n#getDefaultLanguage()} or the first
 * translation found (if no translation exists for the default language).
 * <p>
 * The {@link Document} class is a way to get and use attachments of a contribution without any
 * knowledge about the language in which it is written.
 * </p>
 * @author mmoquillon
 */
public class Document implements I18nContribution, LocalizedAttachment {

  private final ContributionIdentifier id;
  private final SimpleDocument master;

  /**
   * Constructs a new document with the specified identifier.
   * @param id a unique identifier of a document.
   */
  public Document(final ContributionIdentifier id) {
    this.id = id;
    this.master = selectTranslation(null);
  }

  /**
   * Constructs a new document from the specified document file to be used as the master of the
   * document.
   * @param master document file to use as master.
   */
  public Document(final SimpleDocument master) {
    this.id = master.getIdentifier();
    this.master = master;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return id;
  }

  /**
   * Gets the contribution to which this document is attached.
   * @return a {@link ContributionIdentifier} instance about an unknown contribution type.
   */
  public ContributionIdentifier getSourceContribution() {
    SimpleDocument document = getMasterDocument();
    return ContributionIdentifier.from(document.getInstanceId(), document.getForeignId(),
        CoreContributionType.UNKNOWN);
  }

  @Override
  public String getTitle() {
    return getMasterDocument().getTitle();
  }

  @Override
  public String getContentType() {
    return getMasterDocument().getContentType();
  }

  @Override
  public String getDisplayIcon() {
    return getMasterDocument().getDisplayIcon();
  }

  @Override
  public String getFilename() {
    return getMasterDocument().getFilename();
  }

  @Override
  public long getSize() {
    return getMasterDocument().getSize();
  }

  @Override
  public String getAttachmentPath() {
    return getMasterDocument().getAttachmentPath();
  }

  @Override
  public boolean isVersioned() {
    return getMasterDocument().isVersioned();
  }

  @Override
  public int getMinorVersion() {
    return getMasterDocument().getMinorVersion();
  }

  @Override
  public int getMajorVersion() {
    return getMasterDocument().getMajorVersion();
  }

  @Override
  public SimpleDocument getTranslation(final String language) {
    SimpleDocument translation;
    if (getMasterDocument().getLanguage().equals(language)) {
      translation = master;
    } else {
      translation = selectTranslation(language);
    }
    return translation == null ? master : translation;
  }

  /**
   * Gets all the translations available for this document.
   * @return a list of {@link SimpleDocument} instances, each of them being a document file of this
   * document written in a given language. If no translations exist, otherwise if this document
   * doesn't exist yet, then an empty list is returned.
   */
  public List<SimpleDocument> getAllTranslations() {
    AttachmentService service = AttachmentService.get();
    SimpleDocumentPK pk = new SimpleDocumentPK(id.getLocalId(), id.getComponentInstanceId());
    return I18NHelper.getLanguages()
        .stream()
        .map(l -> service.searchDocumentById(pk, l))
        .collect(Collectors.toList());
  }

  @Override
  public Date getCreationDate() {
    return getMasterDocument().getCreationDate();
  }

  @Override
  public Date getLastUpdateDate() {
    return getMasterDocument().getLastUpdateDate();
  }

  @Override
  public User getCreator() {
    return getMasterDocument().getCreator();
  }

  @Override
  public User getLastUpdater() {
    return getMasterDocument().getLastUpdater();
  }

  private SimpleDocument getMasterDocument() {
    return this.master;
  }

  private SimpleDocument selectTranslation(final String language) {
    String lang = StringUtil.isDefined(language) ? language : I18NHelper.DEFAULT_LANGUAGE;
    AttachmentService service = AttachmentService.get();
    SimpleDocumentPK pk = new SimpleDocumentPK(id.getLocalId(), id.getComponentInstanceId());
    SimpleDocument document = service.searchDocumentById(pk, lang);
    if (document == null) {
      return I18NHelper.getLanguages()
          .stream()
          .map(l -> service.searchDocumentById(pk, l))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new NotFoundException(
              "No such document " + id.asString() + " in whatever language"));
    }
    return document;
  }

  public boolean isReadOnly() {
    return getMasterDocument().isReadOnly();
  }

  public boolean isEdited() {
    return getMasterDocument().isEdited();
  }

  public boolean isEditedBy(final User user) {
    return getMasterDocument().isEditedBy(user);
  }

  public boolean isSharingAllowedForRolesFrom(final User user) {
    return getMasterDocument().isSharingAllowedForRolesFrom(user);
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return getMasterDocument().canBeAccessedBy(user);
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return getMasterDocument().canBeModifiedBy(user);
  }

  public boolean isDownloadAllowedForRolesFrom(final User user) {
    return getMasterDocument().isDownloadAllowedForRolesFrom(user);
  }

  public boolean isDownloadAllowedForRoles(final Set<SilverpeasRole> roles) {
    return getMasterDocument().isDownloadAllowedForRoles(roles);
  }

  public boolean isDownloadAllowedForReaders() {
    return getMasterDocument().isDownloadAllowedForReaders();
  }
}
  