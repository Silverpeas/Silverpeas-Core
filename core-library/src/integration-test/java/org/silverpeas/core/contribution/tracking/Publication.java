/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import java.util.Date;

import static org.silverpeas.kernel.util.StringUtil.isNotDefined;

@ModificationTracked
public class Publication implements Contribution {

  private PublicationID pubId;
  private String title;
  private String description;
  private Date creationDate;
  private String creatorId;
  private Date updateDate;
  private String updaterId;
  private String version = "0.0";
  private String keywords = "";
  private String content = "";

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public PublicationID getIdentifier() {
    return pubId;
  }

  void setIdentifier(PublicationID pubId) {
    this.pubId = pubId;
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
  public String getContributionType() {
    return "Publication";
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
    return User.getById(creatorId);
  }

  @Override
  public User getLastUpdater() {
    return User.getById(updaterId);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  String getCreatorId() {
    return creatorId;
  }

  String getLastUpdaterId() {
    return updaterId;
  }

  public void setLastUpdate(Date updateDate, String updaterId) {
    this.updateDate = updateDate;
    this.updaterId = updaterId;
  }

  @SuppressWarnings("SameParameterValue")
  void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public Publication copy() {
    return Publication.builder()
        .setId(pubId.getLocalId(), pubId.getComponentInstanceId())
        .created(creationDate, creatorId)
        .updated(updateDate, updaterId)
        .setTitleAndDescription(title, description)
        .setKeywords(keywords)
        .setContent(content)
        .setVersion(version)
        .build();
  }

  public static class PublicationID extends ContributionIdentifier {

    private static final String TYPE = "Publication";

    public PublicationID(String id, String instanceId) {
      super(instanceId, id, TYPE);
    }

    int localIdAsInt() {
      return Integer.parseInt(getLocalId());
    }
  }

  public static class Builder {

    private final Publication publication;

    Builder() {
      publication = new Publication();
    }

    public Publication build() {
      if (isNotDefined(publication.creatorId) || publication.creationDate == null) {
        publication.creationDate = new Date();
        publication.creatorId = User.getCurrentRequester().getId();
      }
      if (publication.updateDate == null || isNotDefined(publication.updaterId)) {
        publication.updateDate = publication.creationDate;
        publication.updaterId = publication.creatorId;
      }
      return publication;
    }

    Builder setId(String id, String instanceId) {
      publication.setIdentifier(new PublicationID(id, instanceId));
      return this;
    }

    public Builder created(final Date creationDate, final String creatorId) {
      publication.creationDate = creationDate;
      publication.creatorId = creatorId;
      return this;
    }

    public Builder updated(final Date updateDate, final String updaterId) {
      publication.updateDate = updateDate;
      publication.updaterId = updaterId;
      return this;
    }

    public Builder setKeywords(final String keywords) {
      publication.setKeywords(keywords);
      return this;
    }

    public Builder setContent(final String content) {
      publication.setContent(content);
      return this;
    }

    public Builder setVersion(final String version) {
      publication.setVersion(version);
      return this;
    }

    public Builder setTitleAndDescription(final String title, final String description) {
      publication.setTitle(title);
      publication.setDescription(description);
      return this;
    }
  }
}
  