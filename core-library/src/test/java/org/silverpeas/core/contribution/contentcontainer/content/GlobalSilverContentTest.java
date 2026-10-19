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
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the building of a content classified on the PdC from a contribution managed by a
 * content manager. Some contributions don't know their creator (the publications of an infoLetter
 * for example) or have a creator that doesn't exist anymore: they have to be taken into account
 * anyway in the results of a search on the PdC.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class GlobalSilverContentTest {

  private static final ContributionIdentifier CONTRIBUTION =
      ContributionIdentifier.from("infoLetter3", "12", "publication");

  private final Contribution contribution = mock(Contribution.class);

  @BeforeEach
  void setUpTheContribution(@TestManagedMock I18n i18n) {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
    when(contribution.getIdentifier()).thenReturn(CONTRIBUTION);
    when(contribution.getTitle()).thenReturn("A contribution");
    when(contribution.getCreationDate()).thenReturn(new Date());
  }

  @Test
  void theUserOfTheContentIsTheCreatorOfTheContribution() {
    final User creator = mock(User.class);
    when(creator.getId()).thenReturn("2");
    when(contribution.getCreator()).thenReturn(creator);

    final GlobalSilverContent content =
        new GlobalSilverContent(new ManagedContribution(contribution, "icon.gif"));

    assertThat(content.getUserId(), is("2"));
  }

  @Test
  void aContributionWithoutCreatorGivesAContentWithoutUser() {
    final GlobalSilverContent content =
        new GlobalSilverContent(new ManagedContribution(contribution, "icon.gif"));

    assertThat(content.getId(), is("12"));
    assertThat(content.getUserId(), is(nullValue()));
  }
}
