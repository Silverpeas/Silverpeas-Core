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
package org.silverpeas.core.contribution;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.util.Mutable;

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
public class ContributionModelTest {

  private User bart = new MyUser("1", "Bart", "Simpson").inDomainById("0");

  @Test
  public void testOnAccessor() {
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
  public void testOnAccessorWithParameters() {
    final String language = "en";
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(OffsetDateTime.now().minusMonths(1));

    LocalizedContribution translation =
        myContribution.getModel().getProperty("inLanguage", language);
    assertThat(translation.getLanguage(), is(language));
    assertThat(translation.getIdentifier(), is(myContribution.getIdentifier()));
  }

  @Test
  public void testOnMethod() {
    MyContribution myContribution = new MyContribution("42").authoredBy(bart);
    assertThat(myContribution.isPublished(), is(false));

    myContribution.getModel().getProperty("publish");
    assertThat(myContribution.isPublished(), is(true));
  }

  @Test
  public void testOnMethodWithParameters() {
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
  public void testOnFilterByTypeWithMatchFirst() {
    OffsetDateTime expectedDate = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(expectedDate);

    Optional<OffsetDateTime> actualDate = myContribution.getModel()
        .filterByType("publicationDate")
        .matchFirst(d -> LocalDateTime.class.isAssignableFrom(d),
            d -> ((LocalDateTime) d).atOffset(ZoneOffset.UTC))
        .matchFirst(d -> OffsetDateTime.class.isAssignableFrom(d), d -> (OffsetDateTime) d)
        .result();
    assertThat(actualDate.isPresent(), is(true));
    assertThat(actualDate.get(), is(expectedDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  public void testOnFilterByTypeWithMatch() {
    OffsetDateTime expectedDate = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution =
        new MyContribution("42").authoredBy(bart).publishAt(expectedDate);

    Mutable<OffsetDateTime> actualDate = Mutable.empty();
    myContribution.getModel()
        .filterByType("publicationDate")
        .match(d -> LocalDateTime.class.isAssignableFrom(d),
            d -> actualDate.set(((LocalDateTime) d).atOffset(ZoneOffset.UTC)))
        .match(d -> OffsetDateTime.class.isAssignableFrom(d),
            d -> actualDate.set((OffsetDateTime) d));

    assertThat(actualDate.isPresent(), is(true));
    assertThat(actualDate.get(), is(expectedDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  public void testOnFilterByTypeWithParameters() {
    OffsetDateTime dateTime = OffsetDateTime.now().minusMonths(1);
    MyContribution myContribution = new MyContribution("42").authoredBy(bart);

    Optional<LocalizedContribution> publishedContribution = myContribution.getModel()
        .filterByType("publishAt", dateTime)
        .matchFirst(d -> LocalizedContribution.class.isAssignableFrom(d),
            d -> (LocalizedContribution) d)
        .matchFirst(d -> Contribution.class.isAssignableFrom(d),
            d -> LocalizedContribution.from((Contribution) d, "en"))
        .result();
    assertThat(publishedContribution.isPresent(), is(true));
    assertThat(myContribution.isPublished(), is(true));
    assertThat(myContribution.getPublicationDate().isPresent(), is(true));
    assertThat(myContribution.getPublicationDate().get(),
        is(dateTime.withOffsetSameInstant(ZoneOffset.UTC)));
  }
}
  