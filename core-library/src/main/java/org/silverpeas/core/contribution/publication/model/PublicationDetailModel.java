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

package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.DefaultContributionModel;
import org.silverpeas.core.util.DateUtil;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;

import static org.silverpeas.core.contribution.publication.model.PublicationDetail.DELAYED_VISIBILITY_AT_MODEL_PROPERTY;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * The default implementation of the {@link PublicationDetail} entity which is extending the default
 * one.
 * @author silveryocha
 * @see ContributionModel
 * @see DefaultContributionModel
 */
public class PublicationDetailModel extends DefaultContributionModel<PublicationDetail> {

  PublicationDetailModel(final PublicationDetail contribution) {
    super(contribution);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getProperty(final String property, final Object... parameters) {
    if (DELAYED_VISIBILITY_AT_MODEL_PROPERTY.equals(property)) {
      return (T) getDelayedVisibilityTemporalIfAny();
    }
    return super.getProperty(property, parameters);
  }

  /**
   * Gets the delayed visibility temporal.
   * <p>
   * A visibility is computed if the publication:
   * <ul>
   *   <li>has a visibility in the future against the day date</li>
   *   <li>is VALID</li>
   *   <li>is not a clone</li>
   *   <li>has no clone</li>
   * </ul>
   * </p>
   * @return a {@link Temporal} if any, null otherwise.
   */
  private Temporal getDelayedVisibilityTemporalIfAny() {
    final PublicationDetail contribution = getContribution();
    if (contribution.isValid() && !contribution.isClone() && !contribution.haveGotClone()) {
      final Date beginVisibilityDay = contribution.getBeginDate();
      if (beginVisibilityDay != null) {
        final String beginVisibilityHour = contribution.getBeginHour();
        final Date beginVisibilityMoment = isDefined(beginVisibilityHour)
            ? DateUtil.getDate(beginVisibilityDay, beginVisibilityHour)
            : beginVisibilityDay;
        if (beginVisibilityMoment.after(DateUtil.getNow())) {
          return OffsetDateTime.ofInstant(beginVisibilityMoment.toInstant(), ZoneId.systemDefault());
        }
      }
    }
    return null;
  }
}
