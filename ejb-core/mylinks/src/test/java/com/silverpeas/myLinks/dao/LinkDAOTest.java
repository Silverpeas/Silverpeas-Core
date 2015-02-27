package com.silverpeas.myLinks.dao;

import com.silverpeas.myLinks.model.LinkDetail;
import com.stratelia.webactiv.util.DBUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.persistence.dao.DAOBasedTest;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static com.silverpeas.myLinks.dao.LinkDAO.getLinkDao;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class LinkDAOTest extends DAOBasedTest {

  private static final String USER_ID_WITH_POSITIONS = "user_1";
  private static final String USER_ID_WITHOUT_POSITION = "user_2";
  private static final Integer UNIQUE_ID = 25;

  Connection con = null;

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"spring-mylinks.xml"};
  }

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/mylinks/dao/test-mylinks-dataset.xml";
  }

  @Before
  public void beforeTest() throws Exception {
    con = getConnection();
    DBUtil.getInstanceForTest(con);
  }

  @After
  public void afterTest() {
    DBUtil.clearTestInstance();
  }

  @Test
  public void getAllUserLinks() throws Exception {
    List<LinkDetail> result = getLinkDao().getAllLinksByUser(con, USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5));

    result = getLinkDao().getAllLinksByUser(con, USER_ID_WITHOUT_POSITION);
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
  }

  @Test
  public void insertUserLinkIntoLinksWithoutPositionSet() throws Exception {
    LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
    link.setUserId(USER_ID_WITHOUT_POSITION);
    link.setPosition(56);
    link.setHasPosition(true);

    getLinkDao().createLink(con, link);

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

  @Test
  public void insertUserLinkIntoLinksWithPositionSet() throws Exception {
    LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
    link.setUserId(USER_ID_WITH_POSITIONS);
    link.setPosition(56);
    link.setHasPosition(true);

    getLinkDao().createLink(con, link);

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

  @Test
  public void updateUserLinkThatHadAlreadyPosition() throws Exception {
    LinkDetail linkToUpdate = getLinkDao().getLink(con, "4");
    assertThat(linkToUpdate.getLinkId(), is(4));
    assertThat(linkToUpdate.getUserId(), is(USER_ID_WITH_POSITIONS));
    assertThat(linkToUpdate.hasPosition(), is(true));
    assertThat(linkToUpdate.getPosition(), is(2));

    linkToUpdate.setPosition(26);
    getLinkDao().updateLink(con, linkToUpdate);
    
    LinkDetail updatedLink = getLinkDao().getLink(con, "4");
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
    assertThat(updatedLink.hasPosition(), is(linkToUpdate.hasPosition()));
    assertThat(updatedLink.getPosition(), is(26));
  }

  @Test
  public void updateUserLinkThatHadNoPosition() throws Exception {
    LinkDetail linkToUpdate = getLinkDao().getLink(con, "14");
    assertThat(linkToUpdate.getLinkId(), is(14));
    assertThat(linkToUpdate.getUserId(), is(USER_ID_WITHOUT_POSITION));
    assertThat(linkToUpdate.hasPosition(), is(false));
    assertThat(linkToUpdate.getPosition(), is(0));

    linkToUpdate.setHasPosition(true);
    linkToUpdate.setPosition(26);
    getLinkDao().updateLink(con, linkToUpdate);

    LinkDetail updatedLink = getLinkDao().getLink(con, "14");
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
    assertThat(updatedLink.hasPosition(), is(true));
    assertThat(updatedLink.getPosition(), is(26));
  }

  @Test
  public void deleteUserLink() throws Exception {
    getAllUserLinks();

    getLinkDao().deleteLink(con, "4");

    List<LinkDetail> result = getLinkDao().getAllLinksByUser(con, USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 5));

    result = getLinkDao().getAllLinksByUser(con, USER_ID_WITHOUT_POSITION);
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
  }

  /*
  TOOL METHODS
   */

  private List<Integer> extractLinkIds(List<LinkDetail> links) {
    List<Integer> linkIds = new ArrayList<Integer>();
    for (LinkDetail link : links) {
      linkIds.add(link.getLinkId());
    }
    return linkIds;
  }
}
