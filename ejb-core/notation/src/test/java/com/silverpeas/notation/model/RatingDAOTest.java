/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notation.model;

import com.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.silverpeas.persistence.dao.DAOBasedTest;
import org.silverpeas.rating.RaterRatingPK;
import org.silverpeas.rating.RaterRating;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

/**
 * @author: Yohann Chastagnier
 */
public class RatingDAOTest extends DAOBasedTest {

  private static int RATING_ROW_COUNT = 10;

  private static final String INSTANCE_ID = "instanceId_1";
  private static final String CONTRIBUTION_ID = "contribution_1";
  private static final String CONTRIBUTION_TYPE = "type_1";

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/notation/model/notation-dataset.xml";
  }

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"spring-notation.xml"};
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DBUtil.getInstanceForTest(getDataSource().getConnection());
    verifyDataBeforeTest();
  }

  @Override
  public void tearDown() throws Exception {
    try {
      super.tearDown();
    } finally {
      DBUtil.clearTestInstance();
    }
  }

  @Test
  public void createRating() throws Exception {
    UserDetail rater = aUser("26");

    RatingDAO.createRaterRating(getConnection(),
        new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE, rater), 15);

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT + 1));

    int index = getTableIndexForId(table, 101);
    assertThat((String) table.getValue(index, "instanceId"), is(INSTANCE_ID));
    assertThat((String) table.getValue(index, "externalId"), is(CONTRIBUTION_ID));
    assertThat((String) table.getValue(index, "externalType"), is(CONTRIBUTION_TYPE));
    assertThat((String) table.getValue(index, "author"), is(rater.getId()));
    assertThat((Integer) table.getValue(index, "note"), is(15));
  }

  @Test
  public void updateRating() throws Exception {
    UserDetail rater = aUser("3");

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    int index = getTableIndexForId(table, 3);
    assertThat((String) table.getValue(index, "instanceId"), is(INSTANCE_ID));
    assertThat((String) table.getValue(index, "externalId"), is(CONTRIBUTION_ID));
    assertThat((String) table.getValue(index, "externalType"), is(CONTRIBUTION_TYPE));
    assertThat((String) table.getValue(index, "author"), is(rater.getId()));
    assertThat((Integer) table.getValue(index, "note"), is(2));

    RatingDAO.updateRaterRating(getConnection(),
        new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE, rater), -100);

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT));

    assertThat((String) table.getValue(index, "instanceId"), is(INSTANCE_ID));
    assertThat((String) table.getValue(index, "externalId"), is(CONTRIBUTION_ID));
    assertThat((String) table.getValue(index, "externalType"), is(CONTRIBUTION_TYPE));
    assertThat((String) table.getValue(index, "author"), is(rater.getId()));
    assertThat((Integer) table.getValue(index, "note"), is(-100));
  }

  @Test
  public void moveRating() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    int[] aimedIds = new int[]{1, 2, 3, 7};
    Map<Integer, Pair<String, Integer>> raterRatings =
        new HashMap<Integer, Pair<String, Integer>>();
    for (int id : aimedIds) {
      int index = getTableIndexForId(table, id);
      assertThat((Integer) table.getValue(index, "id"), is(id));
      assertThat((String) table.getValue(index, "instanceId"), is(INSTANCE_ID));
      assertThat((String) table.getValue(index, "externalId"), is(CONTRIBUTION_ID));
      assertThat((String) table.getValue(index, "externalType"), is(CONTRIBUTION_TYPE));
      raterRatings.put(id, Pair.of((String) table.getValue(index, "author"),
          (Integer) table.getValue(index, "note")));
    }

    long nbMoved = RatingDAO
        .moveRatings(getConnection(), new ContributionRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE),
            "otherInstanceId");

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT));

    assertThat(nbMoved, is((long) aimedIds.length));
    for (int id : aimedIds) {
      int index = getTableIndexForId(table, id);
      assertThat((Integer) table.getValue(index, "id"), is(id));
      assertThat((String) table.getValue(index, "instanceId"), is("otherInstanceId"));
      assertThat((String) table.getValue(index, "externalId"), is(CONTRIBUTION_ID));
      assertThat((String) table.getValue(index, "externalType"), is(CONTRIBUTION_TYPE));
      Pair<String, Integer> raterRating = raterRatings.get(id);
      assertThat((String) table.getValue(index, "author"), is(raterRating.getLeft()));
      assertThat((Integer) table.getValue(index, "note"), is(raterRating.getRight()));
    }
  }

  @Test
  public void deleteRatings() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    int[] aimedIds = new int[]{1, 2, 3, 7};

    long nbDeleted = RatingDAO.deleteRatings(getConnection(),
        new ContributionRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT - aimedIds.length));

    assertThat(nbDeleted, is((long) aimedIds.length));
    for (int id : aimedIds) {
      int index = getTableIndexForId(table, id);
      assertThat(index, lessThan(0));
    }
  }

  @Test
  public void deleteRaterRating() throws Exception {
    UserDetail rater = aUser("3");

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    int index = getTableIndexForId(table, 3);
    assertThat(index, greaterThanOrEqualTo(0));

    long nbDeleted = RatingDAO.deleteRaterRating(getConnection(),
        new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE, rater));

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT - 1));

    assertThat(nbDeleted, is(1L));
    index = getTableIndexForId(table, 3);
    assertThat(index, lessThan(0));
  }

  @Test
  public void deleteComponentRatings() throws Exception {
    UserDetail rater = aUser("3");

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    int index = getTableIndexForId(table, 3);
    assertThat(index, greaterThanOrEqualTo(0));

    long nbDeleted = RatingDAO.deleteComponentRatings(getConnection(), INSTANCE_ID);

    actualDataSet = getActualDataSet();
    table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT - 9));

    assertThat(nbDeleted, is(9L));
    for (int i = 0; i < table.getRowCount(); i++) {
      assertThat((String) table.getValue(i, "instanceId"), not(is(INSTANCE_ID)));
    }
  }

  @Test
  public void existRaterRating() throws Exception {
    UserDetail rater = aUser("3");
    UserDetail dummyRater = aUser("26");

    assertThat(RatingDAO.existRaterRating(getConnection(),
        new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE, rater)), is(true));
    assertThat(RatingDAO.existRaterRating(getConnection(),
            new RaterRatingPK(CONTRIBUTION_ID + "dummy", INSTANCE_ID, CONTRIBUTION_TYPE, rater)),
        is(false)
    );
    assertThat(RatingDAO.existRaterRating(getConnection(),
            new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID + "dummy", CONTRIBUTION_TYPE, rater)),
        is(false)
    );
    assertThat(RatingDAO.existRaterRating(getConnection(),
            new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE + "dummy", rater)),
        is(false)
    );
    assertThat(RatingDAO.existRaterRating(getConnection(),
        new RaterRatingPK(CONTRIBUTION_ID, INSTANCE_ID, CONTRIBUTION_TYPE, dummyRater)), is(false));
  }

  @Test
  public void getRatingsOnDummyContribution() throws Exception {
    Map<String, ContributionRating> indexedRating = RatingDAO
        .getRatings(getConnection(), INSTANCE_ID, CONTRIBUTION_TYPE,
            Collections.singleton(CONTRIBUTION_ID + "dummy"));
    assertThat(indexedRating.size(), is(1));
    assertThat(indexedRating, hasKey(CONTRIBUTION_ID + "dummy"));
    ContributionRating contributionRating = indexedRating.values().iterator().next();
    assertRatingOnDummyContribution(contributionRating);
  }

  private void assertRatingOnDummyContribution(ContributionRating contributionRating) {
    assertThat(contributionRating.getInstanceId(), is(INSTANCE_ID));
    assertThat(contributionRating.getContributionType(), is(CONTRIBUTION_TYPE));
    assertThat(contributionRating.getContributionId(), is(CONTRIBUTION_ID + "dummy"));
    assertThat(contributionRating.getRaterRatings().size(), is(0));
    assertThat(contributionRating.getRatingAverage(), is(0F));
    UserDetail dummyUser = aUser("dummy");
    RaterRating rater1Rating = contributionRating.getRaterRating(dummyUser);
    assertThat(rater1Rating.getRating(), is(contributionRating));
    assertThat(rater1Rating.getRater(), is(dummyUser));
    assertThat(rater1Rating.getValue(), is(0));
    assertThat(rater1Rating.isRatingDone(), is(false));
  }

  @Test
  public void getRatingsOnExistingContribution() throws Exception {
    Map<String, ContributionRating> indexedRating = RatingDAO
        .getRatings(getConnection(), INSTANCE_ID, CONTRIBUTION_TYPE,
            Collections.singleton(CONTRIBUTION_ID));
    assertThat(indexedRating.size(), is(1));
    assertThat(indexedRating, hasKey(CONTRIBUTION_ID));
    ContributionRating contributionRating = indexedRating.values().iterator().next();
    assertRatingOnContribution1(contributionRating);
  }

  private void assertRatingOnContribution1(ContributionRating contributionRating) {
    assertThat(contributionRating.getInstanceId(), is(INSTANCE_ID));
    assertThat(contributionRating.getContributionType(), is(CONTRIBUTION_TYPE));
    assertThat(contributionRating.getContributionId(), is(CONTRIBUTION_ID));
    assertThat(contributionRating.getRaterRatings().size(), is(4));
    assertThat(contributionRating.getRatingAverage(), is(2.25F));
    UserDetail user1 = aUser("1");
    RaterRating rater1Rating = contributionRating.getRaterRating(user1);
    assertThat(rater1Rating.getRating(), is(contributionRating));
    assertThat(rater1Rating.getRater(), is(user1));
    assertThat(rater1Rating.getValue(), is(1));
    assertThat(rater1Rating.isRatingDone(), is(true));
    UserDetail user2 = aUser("2");
    RaterRating rater2Rating = contributionRating.getRaterRating(user2);
    assertThat(rater2Rating.getRating(), is(contributionRating));
    assertThat(rater2Rating.getRater(), is(user2));
    assertThat(rater2Rating.getValue(), is(5));
    assertThat(rater1Rating.isRatingDone(), is(true));
    UserDetail user3 = aUser("3");
    RaterRating rater3Rating = contributionRating.getRaterRating(user3);
    assertThat(rater3Rating.getRating(), is(contributionRating));
    assertThat(rater3Rating.getRater(), is(user3));
    assertThat(rater3Rating.getValue(), is(2));
    assertThat(rater1Rating.isRatingDone(), is(true));
    UserDetail user4 = aUser("4");
    RaterRating rater4Rating = contributionRating.getRaterRating(user4);
    assertThat(rater4Rating.getRating(), is(contributionRating));
    assertThat(rater4Rating.getRater(), is(user4));
    assertThat(rater4Rating.getValue(), is(1));
    assertThat(rater1Rating.isRatingDone(), is(true));
  }

  @Test
  public void getRatingsOnSeveralContributions() throws Exception {
    Map<String, ContributionRating> indexedRating = RatingDAO
        .getRatings(getConnection(), INSTANCE_ID, CONTRIBUTION_TYPE,
            CollectionUtil.asList(CONTRIBUTION_ID, CONTRIBUTION_ID + "dummy", "contribution_2"));
    assertThat(indexedRating.size(), is(3));
    assertRatingOnContribution1(indexedRating.get(CONTRIBUTION_ID));
    assertRatingOnDummyContribution(indexedRating.get(CONTRIBUTION_ID + "dummy"));
    assertRatingOnContribution2(indexedRating.get("contribution_2"));
  }

  private void assertRatingOnContribution2(ContributionRating contributionRating) {
    assertThat(contributionRating.getInstanceId(), is(INSTANCE_ID));
    assertThat(contributionRating.getContributionType(), is(CONTRIBUTION_TYPE));
    assertThat(contributionRating.getContributionId(), is("contribution_2"));
    assertThat(contributionRating.getRaterRatings().size(), is(3));
    assertThat(
        new BigDecimal("" + contributionRating.getRatingAverage()).setScale(2, BigDecimal.ROUND_HALF_DOWN)
            .floatValue(), is(3.33F));
    UserDetail user1 = aUser("1");
    RaterRating rater1Rating = contributionRating.getRaterRating(user1);
    assertThat(rater1Rating.getRating(), is(contributionRating));
    assertThat(rater1Rating.getRater(), is(user1));
    assertThat(rater1Rating.getValue(), is(1));
    assertThat(rater1Rating.isRatingDone(), is(true));
    UserDetail user2 = aUser("2");
    RaterRating rater2Rating = contributionRating.getRaterRating(user2);
    assertThat(rater2Rating.getRating(), is(contributionRating));
    assertThat(rater2Rating.getRater(), is(user2));
    assertThat(rater2Rating.getValue(), is(5));
    assertThat(rater1Rating.isRatingDone(), is(true));
    UserDetail user3 = aUser("3");
    RaterRating rater3Rating = contributionRating.getRaterRating(user3);
    assertThat(rater3Rating.getRating(), is(contributionRating));
    assertThat(rater3Rating.getRater(), is(user3));
    assertThat(rater3Rating.getValue(), is(4));
    assertThat(rater1Rating.isRatingDone(), is(true));
  }

  /**
   * Verifying the data before a test.
   */
  private void verifyDataBeforeTest() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sb_notation_notation");
    assertThat(table.getRowCount(), is(RATING_ROW_COUNT));
  }

  private UserDetail aUser(String userId) {
    UserDetail user = new UserDetail();
    user.setId(userId);
    return user;
  }
}
