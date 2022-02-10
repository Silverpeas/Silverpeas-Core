/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.cmis.model;

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.StringUtil;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A document in Silverpeas. A document is a user contribution whose content is stored into
 * a single document file in the Silverpeas filesystem. The document, in the current context of the
 * Silverpeas CMIS implementation, is always attached to another contribution that can be either a
 * folder or a publication.
 * @author mmoquillon
 */
public class DocumentFile extends CmisFile implements Fileable {

  /**
   * The identifier of its CMIS type.
   */
  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_DOCUMENT;

  private final ContributionIdentifier id;
  private long size;
  private String mimeType;
  private String title;
  private String lastComment = "";
  private boolean readOnly = false;

  /**
   * Constructs a new document with the specified identifier, filename and language.
   * @param id the {@link ContributionIdentifier} instance identifying the attachment in Silverpeas.
   * @param fileName the name of the file referred by this document.
   * @param language the language in which the content of the document is written.
   */
  public DocumentFile(final ContributionIdentifier id, final String fileName, final String language) {
    super(id, fileName, language);
    this.id = id;
  }

  @Override
  public String getSymbol() {
    return "";
  }

  /**
   * Gets the name of the document in the filesystem of Silverpeas. It is the name of the file in
   * which is stored the document.
   * @return the filename.
   */
  @Override
  public String getName() {
    return super.getName();
  }

  /**
   * Gets the title of this document. If no title is set, then it should returns the document name.
   * @return the title of the document.
   */
  public String getTitle() {
    return this.title;
  }

  public String getApplicationId() {
    return id.getComponentInstanceId();
  }

  @Override
  public BaseTypeId getBaseTypeId() {
    return BaseTypeId.CMIS_DOCUMENT;
  }

  @Override
  public TypeId getTypeId() {
    return CMIS_TYPE;
  }

  /**
   * Gets the comment set at the last update of this document.
   * @return the last comment set.
   */
  public String getLastComment() {
    return lastComment;
  }

  /**
   * Gets the size in bytes of this document.
   * @return the document size.
   */
  public long getSize() {
    return this.size;
  }

  /**
   * Gets the MIME type of the document. It defines how the document content is stored.
   * @return the document MIME type.
   */
  public String getMimeType() {
    return this.mimeType;
  }

  /**
   * Is this document read-only? If read-only, its content cannot be modified. This case occurs for
   * example when the document is locked by a user in Silverpeas other that the current requester.
   * @return true if the content of this document cannot be modified. False otherwise.
   */
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Sets the size of this document.
   * @param size the size in bytes.
   * @return itself.
   */
  public DocumentFile setSize(final long size) {
    this.size = size;
    return this;
  }

  /**
   * Sets the MIME type of the document.
   * @param mimeType a MIME type.
   * @return itself.
   */
  public DocumentFile setMimeType(final String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  /**
   * Sets the title of the document.
   * @param title the title of the document.
   * @return itself.
   */
  public DocumentFile setTitle(final String title) {
    this.title = title;
    return this;
  }

  /**
   * Sets the last comment that was written when saving lastly this document.
   * @param comment a comment about the last change.
   * @return itself.
   */
  public DocumentFile setLastComment(final String comment) {
    if (StringUtil.isDefined(comment)) {
      this.lastComment = comment;
    }
    return this;
  }


  public DocumentFile setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }

  @Override
  protected Supplier<Set<Action>> getAllowableActionsSupplier() {
    return () -> completeWithDocumentActions(completeWithFileActions(theCommonActions()));
  }

  private Set<Action> completeWithDocumentActions(final Set<Action> actions) {
    actions.add(Action.CAN_GET_ALL_VERSIONS);
    actions.add(Action.CAN_GET_CONTENT_STREAM);
    if (!isReadOnly()) {
      actions.add(Action.CAN_SET_CONTENT_STREAM);
    }
    return actions;
  }
}
