/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contribution.model;

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

  private final LocalizedContribution contribution;
  private String text;
  private User author;
  private boolean modified;

  /**
   * Gets the WYSIWYG content of the specified contribution.
   * @param contribution a localized contribution.
   * @return the WYSIWYG content of the contribution or null if no such content exists.
   */
  public static WysiwygContent getContent(final LocalizedContribution contribution) {
    WysiwygContentRepository repository =
        ServiceProvider.getService(WysiwygContentRepository.class);
    return repository.getByContribution(contribution);
  }

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
   * Modifies the text of this content. The content will then be marked as modified.
   * @param richTest the new text to set. If null then an empty text is set.
   */
  public void setData(final String richTest) {
    this.text = richTest == null ? "" : richTest;
    this.modified = true;
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

}
  