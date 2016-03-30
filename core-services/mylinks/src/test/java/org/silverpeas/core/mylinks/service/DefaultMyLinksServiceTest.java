package org.silverpeas.core.mylinks.service;

import org.silverpeas.core.mylinks.dao.LinkDAO;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.persistence.jdbc.ConnectionPool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DefaultMyLinksServiceTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private DefaultMyLinksService service;
  private LinkDAO dao;

  @Before
  public void setup() throws Exception {
    service = spy(new DefaultMyLinksService());
    commonAPI4Test.injectIntoMockedBeanContainer(mock(ConnectionPool.class));
    dao = reflectionRule.mockField(LinkDAO.class, LinkDAO.class, "linkDAO");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void createLinkAndVerifyIdIsSet() throws Exception {
    when(dao.createLink(any(Connection.class), any(LinkDetail.class)))
        .thenAnswer(new Answer<Integer>() {
          @Override
          public Integer answer(final InvocationOnMock invocation) throws Throwable {
            return 38;
          }
        });

    LinkDetail linkToAdd = new LinkDetail();

    service.createLink(linkToAdd);

    assertThat(linkToAdd.getLinkId(), is(38));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getAllLinksByUserWithoutPositions() throws Exception {
    List<LinkDetail> links = initLinkPositions(null, null, null, null, null);
    when(dao.getAllLinksByUser(any(Connection.class), anyString())).thenReturn(links);

    assertThat(extractLinkIdPositions(links),
        contains(of(10, 0), of(11, 0), of(12, 0), of(13, 0), of(14, 0)));

    links = service.getAllLinksByUser("");

    assertThat(extractLinkIdPositions(links),
        contains(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getAllLinksByUserWithPositions() throws Exception {
    List<LinkDetail> links = initLinkPositions(5, 0, 2, 1, 3);
    doReturn(links).when(dao).getAllLinksByUser(any(Connection.class), anyString());

    assertThat(extractLinkIdPositions(links),
        contains(of(10, 5), of(11, 0), of(12, 2), of(13, 1), of(14, 3)));

    links = service.getAllLinksByUser("");

    assertThat(extractLinkIdPositions(links),
        contains(of(11, 0), of(13, 1), of(12, 2), of(14, 3), of(10, 5)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getAllLinksByUserWithAndWithoutPositions() throws Exception {
    List<LinkDetail> links = initLinkPositions(null, 0, null, 1, 3);
    doReturn(links).when(dao).getAllLinksByUser(any(Connection.class), anyString());

    assertThat(extractLinkIdPositions(links),
        contains(of(10, 0), of(11, 0), of(12, 0), of(13, 1), of(14, 3)));

    links = service.getAllLinksByUser("");

    assertThat(extractLinkIdPositions(links),
        contains(of(12, 0), of(10, 0), of(11, 0), of(13, 1), of(14, 3)));
  }

  /*
  METHOD TOOLS
   */

  private List<LinkDetail> initLinkPositions(Integer... positions) {
    List<LinkDetail> links = new ArrayList<LinkDetail>();
    for (Integer position : positions) {
      LinkDetail link = new LinkDetail();
      link.setLinkId(links.size() + 10);
      if (position != null) {
        link.setPosition(position);
        link.setHasPosition(true);
      }
      links.add(link);
    }
    return links;
  }

  private List<Pair<Integer, Integer>> extractLinkIdPositions(List<LinkDetail> links) {
    List<Pair<Integer, Integer>> linkIdPositions = new ArrayList<Pair<Integer, Integer>>();
    for (LinkDetail link : links) {
      linkIdPositions.add(of(link.getLinkId(), link.getPosition()));
    }
    return linkIdPositions;
  }
}
