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

package org.silverpeas.core.contribution.attachment.webdav;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.webdav.impl.WebdavContentDescriptor;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.wbe.WbeFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.contribution.attachment.WebdavServiceProvider.getWebdavService;
import static org.silverpeas.core.util.file.FileUtil.getMimeType;

/**
 * This class represents a file content stored into WEBDAV repository from point of view of WBE
 * services.
 * <p>
 * This file representation permits to the WEBDAV content to be taken in charge by WBE services
 * and so to be edited by online editors.
 * </p>
 * @author silveryocha
 */
public class WebdavWbeFile extends WbeFile {

  public static final String DOC_CACHE_KEY_PREFIX = WebdavWbeFile.class.getSimpleName() + "DOC";
  public static final String DESC_CACHE_KEY_PREFIX = WebdavWbeFile.class.getSimpleName() + "DESC";
  private final String docId;
  private final String docLanguage;

  public WebdavWbeFile(final SimpleDocument document) {
    this(document.getId(), document.getLanguage());
  }

  protected WebdavWbeFile(final String docId, final String docLanguage) {
    this.docId = docId;
    this.docLanguage = docLanguage;
  }

  @Override
  public Optional<ResourceReference> linkedToResource() {
    final SimpleDocument document = getDocument();
    return Optional.of(new ResourceReference(document.getId(), document.getInstanceId()));
  }

  @Override
  public String silverpeasId() {
    return docId;
  }

  @Override
  public String id() {
    return getContentDescriptor().getId();
  }

  @Override
  public User owner() {
    return User.getById(getDocument().getEditedBy());
  }

  @Override
  public String name() {
    final SimpleDocument document = getDocument();
    return Optional.ofNullable(document.getTitle())
        .filter(StringUtil::isDefined)
        .map(t -> t + "." + ext())
        .orElse(document.getFilename());
  }

  @Override
  public String ext() {
    return getExtension(getDocument().getFilename());
  }

  @Override
  public String mimeType() {
    return getMimeType(getDocument().getAttachmentPath());
  }

  @Override
  public long size() {
    return getContentDescriptor().getSize();
  }

  @Override
  public OffsetDateTime lastModificationDate() {
    return getContentDescriptor().getLastModificationDate();
  }

  @Override
  public void updateFrom(final InputStream input) throws IOException {
    getWebdavService().updateContentFrom(getDocument(), input);
    clearCaches();
  }

  @Override
  public void loadInto(final OutputStream output) throws IOException {
    getWebdavService().loadContentInto(getDocument(), output);
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    final SimpleDocument doc = getDocument();
    return doc.isReadOnly() &&
        (doc.getEditedBy().equals(user.getId()) || doc.editableSimultaneously().orElse(false)) &&
        doc.canBeAccessedBy(user);
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    final SimpleDocument doc = getDocument();
    return doc.isReadOnly() &&
        (doc.getEditedBy().equals(user.getId()) || doc.editableSimultaneously().orElse(false)) &&
        doc.canBeModifiedBy(user);
  }

  private SimpleDocument getDocument() {
    final SimpleCache cache = getRequestCacheService().getCache();
    final String key = DOC_CACHE_KEY_PREFIX + docId;
    return cache.computeIfAbsent(key, SimpleDocument.class, () -> Optional.ofNullable(
        AttachmentService.get().searchDocumentById(new SimpleDocumentPK(docId), docLanguage))
        .orElseThrow(() -> new SilverpeasRuntimeException(
            format("document {0}[{1}] does not exist anymore", docId, docLanguage))));
  }

  private WebdavContentDescriptor getContentDescriptor() {
    final SimpleCache cache = getRequestCacheService().getCache();
    final String key = DESC_CACHE_KEY_PREFIX + docId;
    return cache.computeIfAbsent(key, WebdavContentDescriptor.class,
        () -> getWebdavContentDescriptor().orElseThrow(
            () -> new SilverpeasRuntimeException(
                format("no WEBDAV description for document {0}[{1}]", docId, docLanguage))));
  }

  private Optional<WebdavContentDescriptor> getWebdavContentDescriptor() {
    if (getDocument().isOpenOfficeCompatible() && getDocument().isReadOnly()) {
      return getWebdavService().getDescriptor(getDocument().getVersionMaster());
    }
    return empty();
  }

  private void clearCaches() {
    final SimpleCache cache = getRequestCacheService().getCache();
    Stream.of(DOC_CACHE_KEY_PREFIX, DESC_CACHE_KEY_PREFIX)
        .map(p -> p + docId)
        .forEach(cache::remove);
  }
}
