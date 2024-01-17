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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.selection;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.i18n.ResourceTranslation;

import java.util.Date;

/**
 * @author silveruser
 */
public class MyContribution
    implements I18nContribution, LocalizedContribution, ResourceTranslation {

  private ContributionIdentifier id;
  private String title;
  private String description = "";
  private Date creationDate = new Date();
  private Date updateDate = new Date();
  private User creator = User.getCurrentRequester();
  private User updater = creator;
  private String language = "en";

  public MyContribution(final String title) {
    this.title = title;
  }

  public MyContribution(final String title, final String description) {
    this.title = title;
    this.description = description;
  }

  public MyContribution(final String title, final String description, final String language) {
    this.title = title;
    this.description = description;
    this.language = language;
  }

  private MyContribution(final MyContribution contribution) {
    this.id = contribution.id;
    this.title = contribution.title;
    this.description = contribution.description;
    this.creationDate = contribution.creationDate;
    this.updateDate = contribution.updateDate;
    this.creator = contribution.creator;
    this.updater = contribution.updater;
    this.language = contribution.language;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Date getLastUpdateDate() {
    return updateDate;
  }

  @Override
  public User getCreator() {
    return creator;
  }

  @Override
  public User getLastUpdater() {
    return updater;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getId() {
    return id.asString();
  }

  public MyContribution setId(final ContributionIdentifier id) {
    this.id = id;
    return this;
  }

  public MyContribution setTitle(final String title) {
    this.title = title;
    return this;
  }

  public MyContribution setDescription(final String description) {
    this.description = description;
    return this;
  }

  public MyContribution setUpdater(final User updater) {
    this.updater = updater;
    this.updateDate = new Date();
    return this;
  }

  @Override
  public MyContribution getTranslation(final String language) {
    if (this.language.equals(language)) {
      return this;
    }
    MyContribution copy = new MyContribution(this);
    copy.language = language;
    return copy;
  }

  @Override
  public String getLanguage() {
    return language;
  }
}
