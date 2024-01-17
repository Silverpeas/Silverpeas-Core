/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.service.WysiwygContentRepository;
import org.silverpeas.core.util.ServiceProvider;

/**
 * A WYSIWYG content is a rich text (text with styles with graphics and images) that is produced by
 * an editor in a form closely resembling its appearance when printed or displayed.
 * <p>
 * Usually a WYSIWYG content doesn't require to be compiled or transpiled into another form
 * dedicated to the rendering. The WYSIWYG content is enough by itself as it contains all the
 * information to be displayed.
 * @author mmoquillon
 */
public class WysiwygContent implements ContributionContent<String> {
  private static final long serialVersionUID = 356629338911833531L;

  private final LocalizedContribution contribution;
  private String text;
  private User author;
  private boolean modified;

  /**
   * Constructs a new WYSIWYG content for the specified contribution with the given rich text.
   * @param contribution the contribution related by the content.
   * @param richText the data of the content. If null then an empty text is set.
   */
  public WysiwygContent(final LocalizedContribution contribution, final String richText) {
    this.contribution = contribution;
    this.text = richText == null ? "" : richText;
  }

  /**
   * Constructs an empty WYSIWYG content for the specified contribution.
   * @param contribution the contribution related by the content.
   */
  public WysiwygContent(final LocalizedContribution contribution) {
    this(contribution, "");
  }

  /**
   * Constructs a copy of the specified other content.
   * @param content the WYSIWYG content to copy.
   */
  public WysiwygContent(final WysiwygContent content) {
    this.contribution = content.contribution;
    this.text = content.text;
    this.author = content.author;
    this.modified = content.modified;
  }

  /**
   * Gets the WYSIWYG content of the specified contribution.
   * @param contribution a localized contribution.
   * @return the WYSIWYG content of the contribution or null if no such content exists.
   */
  public static WysiwygContent getContent(final LocalizedContribution contribution) {
    WysiwygContentRepository repository =
        ServiceProvider.getSingleton(WysiwygContentRepository.class);
    return repository.getByContribution(contribution);
  }

  /**
   * Deletes all the WYSIWYG contents of the specified contribution. If the given contribution is
   * an I18n one (a multi-localized contribution) then all of its WYSIWYG contents, one per
   * localization, will be deleted. In the case of a single one localized contribution, only this
   * single WYSIWYG content will be deleted. If the contribution has no WYSIWYG contents, nothing is
   * done.
   * @param contribution the contribution for which the WYSIWYG contents have to be deleted.
   */
  public static void deleteAllContents(final Contribution contribution) {
    WysiwygContentRepository repository =
        ServiceProvider.getService(WysiwygContentRepository.class);
    repository.deleteByContribution(contribution);
  }

  /**
   * Sets a user as the current author of this content.
   * @param author the user to set.
   * @return itself.
   */
  public WysiwygContent authoredBy(final User author) {
    this.author = author;
    return this;
  }

  @Override
  public LocalizedContribution getContribution() {
    return contribution;
  }

  /**
   * The rich text of this content.
   * @return the rich text as a non null string.
   */
  @Override
  public String getData() {
    return this.text;
  }

  /**
   * Modifies the text of this content. The content is effectively modified only if the specified
   * text is different to the actual one; in such a case, the content is marked as modified and
   * is then elective for the next storage update.
   * @param richTest the new text to set. If null then an empty text is set.
   */
  public void setData(final String richTest) {
    String newText = richTest == null ? "" : richTest;
    if (!newText.contentEquals(this.text)) {
      this.text = newText;
      this.modified = true;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.text.isEmpty();
  }

  /**
   * Is this content was modified since its getting.
   * @return true if this content is modified, false otherwise.
   */
  public boolean isModified() {
    return this.modified;
  }

  /**
   * Gets the user that has authored this content. If no specific user was set as author then
   * returns the user behind the current request if any.
   * @return the author of this content. He can be either the creator of this content or a modifier.
   */
  public User getAuthor() {
    return author == null ? User.getCurrentRequester() : author;
  }

  /**
   * Saves this content.
   */
  public void save() {
    WysiwygContentRepository repository =
        ServiceProvider.getService(WysiwygContentRepository.class);
    repository.save(this);
  }

  /**
   * Deletes this content.
   */
  public void delete() {
    WysiwygContentRepository repository =
        ServiceProvider.getService(WysiwygContentRepository.class);
    repository.delete(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WysiwygContent)) {
      return false;
    }

    final WysiwygContent content = (WysiwygContent) o;

    if (!contribution.getIdentifier().equals(content.contribution.getIdentifier())) {
      return false;
    }
    return text != null ? text.equals(content.text) : content.text == null;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(contribution.getIdentifier())
        .append(text)
        .toHashCode();
  }
}
  