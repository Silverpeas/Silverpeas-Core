package org.silverpeas.core.mylinks.dao;

import org.silverpeas.core.mylinks.model.LinkDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.mylinks.test.WarBuilder4MyLinks;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.mylinks.dao.LinkDAO.getLinkDao;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class LinkDAOIntegrationTest {

  private static final String USER_ID_WITH_POSITIONS = "user_1";
  private static final String USER_ID_WITHOUT_POSITION = "user_2";
  private static final Integer UNIQUE_ID = 25;

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String DATASET_XML_SCRIPT = "test-mylinks-dataset.xml";

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4MyLinks.onWarForTestClass(LinkDAOIntegrationTest.class).build();
  }

  @Test
  public void getAllUserLinks() throws Exception {
    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      List<LinkDetail> result = getLinkDao().getAllLinksByUser(con, USER_ID_WITH_POSITIONS);
      assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5));

      result = getLinkDao().getAllLinksByUser(con, USER_ID_WITHOUT_POSITION);
      assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
    }
  }

  @Test
  public void insertUserLinkIntoLinksWithoutPositionSet() throws Exception {
    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITHOUT_POSITION);
      link.setPosition(56);
      link.setHasPosition(true);

      getLinkDao().createLink(con, link);
    }

    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      List<LinkDetail> result = getLinkDao().getAllLinksByUser(con, USER_ID_WITHOUT_POSITION);
      assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15, (UNIQUE_ID + 1)));

      LinkDetail createdLink = getLinkDao().getLink(con, String.valueOf(UNIQUE_ID + 1));
      assertThat(createdLink.getLinkId(), is((UNIQUE_ID + 1)));
      assertThat(createdLink.getUserId(), is(USER_ID_WITHOUT_POSITION));
      assertThat(createdLink.getName(), is("new name"));
      assertThat(createdLink.getDescription(), is("new description"));
      assertThat(createdLink.getUrl(), is("new url"));
      assertThat(createdLink.isVisible(), is(true));
      assertThat(createdLink.isPopup(), is(true));
      assertThat(createdLink.hasPosition(), is(false));
      assertThat(createdLink.getPosition(), is(0));
    }
  }

  @Test
  public void insertUserLinkIntoLinksWithPositionSet() throws Exception {
    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITH_POSITIONS);
      link.setPosition(56);
      link.setHasPosition(true);

      getLinkDao().createLink(con, link);
    }

    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      List<LinkDetail> result = getLinkDao().getAllLinksByUser(con, USER_ID_WITH_POSITIONS);
      assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5, (UNIQUE_ID + 1)));

      LinkDetail createdLink = getLinkDao().getLink(con, String.valueOf(UNIQUE_ID + 1));
      assertThat(createdLink.getLinkId(), is((UNIQUE_ID + 1)));
      assertThat(createdLink.getName(), is("new name"));
      assertThat(createdLink.getDescription(), is("new description"));
      assertThat(createdLink.getUrl(), is("new url"));
      assertThat(createdLink.isVisible(), is(true));
      assertThat(createdLink.isPopup(), is(true));
      assertThat(createdLink.getUserId(), is(USER_ID_WITH_POSITIONS));
      assertThat(createdLink.hasPosition(), is(false));
      assertThat(createdLink.getPosition(), is(0));
    }
  }

  @Test
  public void updateUserLinkThatHadAlreadyPosition() throws Exception {
    final LinkDetail linkToUpdate;
    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      linkToUpdate = getLinkDao().getLink(con, "4");
      assertThat(linkToUpdate.getLinkId(), is(4));
      assertThat(linkToUpdate.getUserId(), is(USER_ID_WITH_POSITIONS));
      assertThat(linkToUpdate.hasPosition(), is(true));
      assertThat(linkToUpdate.getPosition(), is(2));

      linkToUpdate.setPosition(26);
      getLinkDao().updateLink(con, linkToUpdate);
    }

    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {

      LinkDetail updatedLink = getLinkDao().getLink(con, "4");
      assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
      assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
      assertThat(updatedLink.hasPosition(), is(linkToUpdate.hasPosition()));
      assertThat(updatedLink.getPosition(), is(26));
    }
  }

  @Test
  public void updateUserLinkThatHadNoPosition() throws Exception {
    final LinkDetail linkToUpdate;
    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      linkToUpdate = getLinkDao().getLink(con, "14");
      assertThat(linkToUpdate.getLinkId(), is(14));
      assertThat(linkToUpdate.getUserId(), is(USER_ID_WITHOUT_POSITION));
      assertThat(linkToUpdate.hasPosition(), is(false));
      assertThat(linkToUpdate.getPosition(), is(0));

      linkToUpdate.setHasPosition(true);
      linkToUpdate.setPosition(26);
      getLinkDao().updateLink(con, linkToUpdate);
    }

    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      LinkDetail updatedLink = getLinkDao().getLink(con, "14");
      assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
      assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
      assertThat(updatedLink.hasPosition(), is(true));
      assertThat(updatedLink.getPosition(), is(26));
    }
  }

  @Test
  public void deleteUserLink() throws Exception {
    getAllUserLinks();

    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      getLinkDao().deleteLink(con, "4");
    }

    try (Connection con = DbUnitLoadingRule.getSafeConnection()) {
      List<LinkDetail> result = getLinkDao().getAllLinksByUser(con, USER_ID_WITH_POSITIONS);
      assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 5));

      result = getLinkDao().getAllLinksByUser(con, USER_ID_WITHOUT_POSITION);
      assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
    }
  }

  /*
  TOOL METHODS
   */

  private List<Integer> extractLinkIds(List<LinkDetail> links) {
    List<Integer> linkIds = new ArrayList<>();
    for (LinkDetail link : links) {
      linkIds.add(link.getLinkId());
    }
    return linkIds;
  }
}
