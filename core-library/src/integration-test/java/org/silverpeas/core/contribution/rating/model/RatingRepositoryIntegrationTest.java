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

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.rating.service.RatingRepository;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class RatingRepositoryIntegrationTest {

  private static final Operation NOTATION_SETUP = Operations.insertInto("SB_Notation_Notation")
      .columns("id", "instanceId", "externalId", "externalType", "author", "note")
      .values(0, "kmelia12", "365", "Publication", "42", 8)
      .values(1, "kmelia12", "365", "Publication", "8", 5)
      .values(2, "SuggestionBox2", "12", "Suggestion", "42", 10)
      .values(3, "kmelia12", "122", "Publication", "8", 8)
      .build();
  private static final Operation UNIQUE_ID_SETUP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(3, "SB_Notation_Notation")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(
      "/org/silverpeas/core/contribution/rating/model/create_table.sql")
          .loadInitialDataSetFrom(NOTATION_SETUP, UNIQUE_ID_SETUP);

  @Inject
  private RatingRepository repository;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(RatingRepositoryIntegrationTest.class)
        .addJpaPersistenceFeatures()
        .testFocusedOn(
            war -> war.addClasses(ContributionRating.class, Rating.class, RatingRepository.class,
                RaterRatingPK.class, ContributionRatingPK.class))
        .build();
  }

  @Test
  @Ignore
  public void emptyTest() {

  }

  @Test
  public void deleteAllRatingsOfAContribution() {
    Transaction.performInOne(() -> {
      ContributionRatingPK pk = new ContributionRatingPK("365", "kmelia12", "Publication");
      repository.deleteAllRatingsOfAContribution(pk);
      return null;
    });

    List<Rating> ratings = RatingFinder.getSomeByQuery(
        "from Rating where contributionId = '365' and instanceId = 'kmelia12' and " +
            "contributionType = 'Publication'");
    assertThat(ratings.isEmpty(), is(true));
  }

  @Test
  public void deleteAllRatingsOfANonExistingContribution() {
    Transaction.performInOne(() -> {
      ContributionRatingPK pk = new ContributionRatingPK("1000", "kmelia12", "Publication");
      repository.deleteAllRatingsOfAContribution(pk);
      return null;
    });

    long count = RatingFinder.count();
    assertThat(count, is(4L));
  }

  @Test
  public void deleteAllRatingsInComponentInstance() {
    Transaction.performInOne(() -> {
      repository.deleteAllRatingsInComponentInstance("kmelia12");
      return null;
    });

    List<Rating> ratings = RatingFinder.getSomeByQuery("from Rating where instanceId = 'kmelia12'");
    assertThat(ratings.isEmpty(), is(true));
  }

  @Test
  public void deleteAllRatingsInANonExistingComponentInstance() {
    Transaction.performInOne(() -> {
      repository.deleteAllRatingsInComponentInstance("Todo12");
      return null;
    });

    long count = RatingFinder.count();
    assertThat(count, is(4L));
  }

  @Test
  public void getRating() {
    UserDetail user = new UserDetail();
    user.setId("42");
    RaterRatingPK pk = new RaterRatingPK("365", "kmelia12", "Publication", user);
    Rating rating = repository.getRating(pk);
    assertThat(rating.getId(), is("0"));
    assertThat(rating.getAuthorId(), is("42"));
    assertThat(rating.getContributionId(), is("365"));
    assertThat(rating.getInstanceId(), is("kmelia12"));
    assertThat(rating.getContributionType(), is("Publication"));
    assertThat(rating.getNote(), is(8));
  }

  @Test
  public void getRatingFromANonExistingContribution() {
    UserDetail user = new UserDetail();
    user.setId("42");
    RaterRatingPK pk = new RaterRatingPK("1000", "kmelia12", "Publication", user);
    Rating rating = repository.getRating(pk);
    assertThat(rating, nullValue());
  }

  @Test
  public void getRatingFromANonExistingUser() {
    UserDetail user = new UserDetail();
    user.setId("100");
    RaterRatingPK pk = new RaterRatingPK("365", "kmelia12", "Publication", user);
    Rating rating = repository.getRating(pk);
    assertThat(rating, nullValue());
  }

  @Test
  public void getAllRatingByContributions() {
    Map<String, ContributionRating> ratingsByContribution =
        repository.getAllRatingByContributions("kmelia12", "Publication", "365", "122");
    assertThat(ratingsByContribution.size(), is(2));
    assertThat(ratingsByContribution.containsKey("365"), is(true));
    assertThat(ratingsByContribution.containsKey("122"), is(true));

    ContributionRating rating = ratingsByContribution.get("365");
    assertThat(rating.getContributionType(), is("Publication"));
    assertThat(rating.getInstanceId(), is("kmelia12"));
    assertThat(rating.getContributionId(), is("365"));
    assertThat(rating.getRaterRatings().size(), is(2));

    rating = ratingsByContribution.get("122");
    assertThat(rating.getContributionType(), is("Publication"));
    assertThat(rating.getInstanceId(), is("kmelia12"));
    assertThat(rating.getContributionId(), is("122"));
    assertThat(rating.getRaterRatings().size(), is(1));
  }

  @Test
  public void getAllRatingByNonExistingContributions() {
    Map<String, ContributionRating> ratingsByContribution =
        repository.getAllRatingByContributions("Todo10", "Publication", "111");
    assertThat(ratingsByContribution.isEmpty(), is(true));
  }

  @Test
  public void moveAllRatingsOfAContribution() {
    Transaction.performInOne(() -> {
      ContributionRatingPK pk = new ContributionRatingPK("365", "kmelia12", "Publication");
      repository.moveAllRatingsOfAContribution(pk, "kmelia100");
      return null;
    });

    List<Rating> ratings = RatingFinder.getSomeByQuery("from Rating where instanceId = 'kmelia100'");
    assertThat(ratings.size(), is(2));
    assertThat(ratings.get(0).getId(), is("0"));
    assertThat(ratings.get(0).getAuthorId(), is("42"));
    assertThat(ratings.get(0).getContributionId(), is("365"));
    assertThat(ratings.get(0).getInstanceId(), is("kmelia100"));
    assertThat(ratings.get(0).getContributionType(), is("Publication"));
    assertThat(ratings.get(0).getNote(), is(8));

    assertThat(ratings.get(1).getId(), is("1"));
    assertThat(ratings.get(1).getAuthorId(), is("8"));
    assertThat(ratings.get(1).getContributionId(), is("365"));
    assertThat(ratings.get(1).getInstanceId(), is("kmelia100"));
    assertThat(ratings.get(1).getContributionType(), is("Publication"));
    assertThat(ratings.get(1).getNote(), is(5));
  }

  @Test
  public void moveAllRatingsOfANonExistingContribution() {
    Transaction.performInOne(() -> {
      ContributionRatingPK pk = new ContributionRatingPK("365", "Todo12", "Publication");
      repository.moveAllRatingsOfAContribution(pk, "Todo100");
      return null;
    });

    List<Rating> ratings = RatingFinder.getSomeByQuery("from Rating where instanceId = 'Todo100'");
    assertThat(ratings.size(), is(0));
  }

}