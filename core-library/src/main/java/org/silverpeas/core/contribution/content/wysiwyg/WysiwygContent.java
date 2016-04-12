/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.contribution.content.wysiwyg;

import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A WYSIWYG content. A WYSIWYG content is always related to a contribution from a user (an event,
 * a publication, ...) that belongs to a given Silverpeas application instance.
 * <p>
 *   This object can be serialized in JSON in order to be transmitted either by JMS or by HTTP.
 * </p>
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WysiwygContent implements ContributionContent<String> {

  @XmlElement
  private String text;
  @XmlElement
  private ContributionIdentifier contributionId;
  @XmlElement
  private String authorId;
  @XmlElement
  private String language;

  protected WysiwygContent() {

  }

  /**
   * Constructs a new WYSIWYG content for the specified contribution and with the given text.
   * @param id the unique identifier of the contribution to which this content is related.
   * @param text the rich-text of the content.
   */
  public WysiwygContent(final ContributionIdentifier id, final String text) {
    this.text = text;
    this.contributionId = id;
  }

  /**
   * Sets the unique identifier of the author of this WYSIWYG content. The author is either the
   * creator of this content or the updater of the WYSIWYG in this content is a new version of
   * an existing WYSIWYG content.
   * @param authorId the unique identifier of the author.
   * @return itself.
   */
  public WysiwygContent writtenBy(String authorId) {
    this.authorId = authorId;
    return this;
  }

  /**
   * Sets the ISO 639 code of the language in which is written the text.
   * @param language the ISO 639 code of a language.
   * @return itself.
   */
  public WysiwygContent inLanguage(String language) {
    this.language = language;
    return this;
  }

  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  /**
   * Gets the unique identifier of the user that has edited this WYSIWYG content. An author can
   * be either the creator (if this content is a new one) or the updater if this content is a
   * modified version of an existing WYSIWYG text.
   * @return the unique identifier of the user, author of this content.
   */
  public String getAuthorId() {
    return authorId;
  }

  /**
   * Sets the rich-text of this WYSIWYG.
   * @param text the text to set.
   */
  public void setData(final String text) {
    this.text = text;
  }

  /**
   * Gets the rich-text of this WYSIWYG content.
   * @return the rich-text of this WYSIWYG.
   */
  @Override
  public String getData() {
    return text;
  }

  /**
   * Gets the language in which is written this content.
   * @return the ISO 639 code of a language.
   */
  public String getLanguage() {
    return language;
  }

  @Override
  public boolean isEmpty() {
    return !StringUtil.isDefined(getData());
  }
}
