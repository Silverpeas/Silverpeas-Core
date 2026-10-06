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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.contribution.model.WithThumbnail;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests on the thumbnail of a contribution managed by a content manager. The transverse
 * services working on such contributions, like the notification of the subscribers on the PdC,
 * have to be able to get the thumbnail of the contribution when it has one.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ManagedContributionThumbnailTest {

  private static final ContributionIdentifier CONTRIBUTION =
      ContributionIdentifier.from("kmelia42", "23", "Publication");

  @BeforeEach
  void setUpTheLanguages(@TestManagedMock I18n i18n) {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
  }

  @Test
  void theThumbnailIsTheOneOfTheManagedContribution() {
    final Thumbnail thumbnail = mock(Thumbnail.class);
    final Contribution contribution =
        mock(Contribution.class, withSettings().extraInterfaces(WithThumbnail.class));
    when(contribution.getIdentifier()).thenReturn(CONTRIBUTION);
    when(((WithThumbnail) contribution).getThumbnail()).thenReturn(thumbnail);

    final ManagedContribution managed = new ManagedContribution(contribution, "icon.gif");

    assertThat(managed.getThumbnail(), is(thumbnail));
  }

  @Test
  void aContributionWithoutAnySetThumbnailHasNoThumbnail() {
    final Contribution contribution =
        mock(Contribution.class, withSettings().extraInterfaces(WithThumbnail.class));
    when(contribution.getIdentifier()).thenReturn(CONTRIBUTION);

    final ManagedContribution managed = new ManagedContribution(contribution, "icon.gif");

    assertThat(managed.getThumbnail(), is(nullValue()));
  }

  @Test
  void aContributionNotSupportingThumbnailsHasNoThumbnail() {
    final Contribution contribution = mock(Contribution.class);
    when(contribution.getIdentifier()).thenReturn(CONTRIBUTION);

    final ManagedContribution managed = new ManagedContribution(contribution, "icon.gif");

    assertThat(managed.getThumbnail(), is(nullValue()));
  }
}
