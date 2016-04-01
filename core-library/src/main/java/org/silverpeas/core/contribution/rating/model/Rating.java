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

package org.silverpeas.core.contribution.rating.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author mmoquillon
 */
@Entity
@Table(name = "SB_Notation_Notation")
@NamedQueries({
    @NamedQuery(name = "deleteAllByContribution",
        query = "delete from Rating where contributionId = :contributionId and instanceId = " +
            ":instanceId and contributionType = :contributionType"),
    @NamedQuery(name = "deleteByInstanceId",
        query = "delete from Rating where instanceId = :instanceId"),
    @NamedQuery(name = "findByAuthorRating",
        query = "from Rating where contributionId = :contributionId and instanceId = " +
            ":instanceId and contributionType = :contributionType and authorId = :authorId"),
    @NamedQuery(name = "findByContributions",
        query = "from Rating where instanceId = :instanceId and contributionType = " +
            ":contributionType and contributionId in :contributionIds"),
    @NamedQuery(name = "updateInstanceId",
        query = "update Rating set instanceId = :newInstanceId where instanceId = :instanceId " +
            "and contributionId = :contributionId and contributionType = :contributionType")})
public class Rating extends AbstractJpaCustomEntity<Rating, UniqueIntegerIdentifier> {

  @Column(name = "author", nullable = false)
  @NotNull
  private String authorId;
  @Column(nullable = false)
  @NotNull
  private Integer note;
  @Column(nullable = false)
  @NotNull
  private String instanceId;
  @NotNull
  @Column(name = "externalId", nullable = false)
  private String contributionId;
  @NotNull
  @Column(name = "externalType", nullable = false)
  private String contributionType;

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(final String authorId) {
    this.authorId = authorId;
  }

  public Integer getNote() {
    return note;
  }

  public void setNote(final Integer note) {
    this.note = note;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(final String instanceId) {
    this.instanceId = instanceId;
  }

  public String getContributionId() {
    return contributionId;
  }

  public void setContributionId(final String contributionId) {
    this.contributionId = contributionId;
  }

  public String getContributionType() {
    return contributionType;
  }

  public void setContributionType(final String contributionType) {
    this.contributionType = contributionType;
  }
}
