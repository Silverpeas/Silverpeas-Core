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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.kernel.util.Mutable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests on some common functionality on the
 * {@link org.silverpeas.core.contribution.model.ContributionModel} objects.
 * @author mmoquillon
 */
class ContributionModelTest {

  private final User bart = new MyUser("1", "Bart", "Simpson").inDomainById("0");

  @Test
  void testOnAccessor() {
    OffsetDateTime expectedDate = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(expectedDate);

    boolean published = myContribution.getModel().getProperty("isPublished");
    assertThat(published, is(true));

    Optional<OffsetDateTime> actualDate = myContribution.getModel().getProperty("publicationDate");
    assertThat(actualDate.isPresent(), is(true));
    assertThat(actualDate.get(), is(expectedDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  void testOnAccessorWithParameters() {
    final String language = "en";
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(OffsetDateTime.now().minusMonths(1));

    LocalizedContribution translation =
        myContribution.getModel().getProperty("inLanguage", language);
    assertThat(translation.getLanguage(), is(language));
    assertThat(translation.getIdentifier(), is(myContribution.getIdentifier()));
  }

  @Test
  void testOnMethod() {
    MyContribution myContribution = new MyContribution("42").authoredBy(bart);
    assertThat(myContribution.isPublished(), is(false));

    myContribution.getModel().getProperty("publish");
    assertThat(myContribution.isPublished(), is(true));
  }

  @Test
  void testOnMethodWithParameters() {
    OffsetDateTime actualDate = OffsetDateTime.now().minusDays(2);
    MyContribution myContribution = new MyContribution("42").authoredBy(bart);
    assertThat(myContribution.isPublished(), is(false));

    myContribution.getModel().getProperty("publishAt", actualDate);
    assertThat(myContribution.isPublished(), is(true));
    assertThat(myContribution.getPublicationDate().isPresent(), is(true));
    assertThat(myContribution.getPublicationDate().get(),
        is(actualDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  void testOnFilterByTypeWithMatchFirst() {
    OffsetDateTime expectedDate = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(expectedDate);

    Optional<OffsetDateTime> actualDate = myContribution.getModel()
        .filterByType("publicationDate")
        .matchFirst(LocalDateTime.class::isAssignableFrom,
            d -> ((LocalDateTime) d).atOffset(ZoneOffset.UTC))
        .matchFirst(OffsetDateTime.class::isAssignableFrom, d -> (OffsetDateTime) d)
        .result();
    assertThat(actualDate.isPresent(), is(true));
    assertThat(actualDate.get(), is(expectedDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  void testOnFilterByTypeWithMatch() {
    OffsetDateTime expectedDate = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(expectedDate);

    Mutable<OffsetDateTime> actualDate = Mutable.empty();
    myContribution.getModel()
        .filterByType("publicationDate")
        .match(LocalDateTime.class::isAssignableFrom,
            d -> actualDate.set(((LocalDateTime) d).atOffset(ZoneOffset.UTC)))
        .match(OffsetDateTime.class::isAssignableFrom,
            d -> actualDate.set((OffsetDateTime) d));

    assertThat(actualDate.isPresent(), is(true));
    assertThat(actualDate.get(), is(expectedDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  void testOnFilterByTypeWithParameters() {
    OffsetDateTime dateTime = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution = new MyContribution("42").authoredBy(bart);

    Optional<LocalizedContribution> publishedContribution = myContribution.getModel()
        .filterByType("publishAt", dateTime)
        .matchFirst(LocalizedContribution.class::isAssignableFrom,
            d -> (LocalizedContribution) d)
        .matchFirst(Contribution.class::isAssignableFrom,
            d -> LocalizedContribution.from((Contribution) d, "en"))
        .result();
    assertThat(publishedContribution.isPresent(), is(true));
    assertThat(myContribution.isPublished(), is(true));
    assertThat(myContribution.getPublicationDate().isPresent(), is(true));
    assertThat(myContribution.getPublicationDate().get(),
        is(dateTime.withOffsetSameInstant(ZoneOffset.UTC)));
  }
}
  