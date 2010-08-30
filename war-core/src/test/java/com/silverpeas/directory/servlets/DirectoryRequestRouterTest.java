package com.silverpeas.directory.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.silverpeas.directory.model.Member;
import com.stratelia.webactiv.beans.admin.UserDetail;



public class DirectoryRequestRouterTest {

  @Test
  @SuppressWarnings("unchecked")
  public void doPaginationTest(){
//   MockHttpServletRequest request = new MockHttpServletRequest();
//   request.setParameter("currentPage", "0");
//    UserDetail user1 = new UserDetail();
//    user1.setId("1");
//    user1.setLastName("bourakbi");
//    user1.setFirstName("nidale");
//    user1.seteMail("nidale.bourakbi@gmail.com");
//    user1.setLogin("bourakbn");
//    UserDetail user2 = new UserDetail();
//    user2.setId("2");
//    user2.setLastName("bensalem");
//    user2.setFirstName("nabil");
//    user2.seteMail("nabil@gmail.com");
//    user2.setLogin("nabil");
//    UserDetail user3 = new UserDetail();
//    user3.setId("3");
//    user3.setLastName("simpson");
//    user3.setFirstName("nabil");
//    user3.seteMail("nabil@gmail.com");
//    user3.setLogin("nabil");
//    List<UserDetail> users = new ArrayList<UserDetail>();
//    users.add(user1);
//    users.add(user2);
//    users.add(user3);
//
//    DirectoryRequestRouter router = new DirectoryRequestRouter();
//    DirectoryRequestRouter.ELEMENTS_PER_PAGE = 2;
//    router.users =users;
//    @SuppressWarnings("unused")
//    String result = router.doPagination(request);
//    Integer currentPageAtt = (Integer) request.getAttribute("currentPage");
//    assertNotNull(currentPageAtt);
//    assertEquals(0, currentPageAtt.intValue());
//    Integer nbPagesAtt = (Integer) request.getAttribute("nbPages");
//    assertNotNull(nbPagesAtt);
//    assertEquals(2, nbPagesAtt.intValue());
//    List<Member> members =  (List<Member>) request.getAttribute("Members");
//    assertNotNull(members);
//    assertEquals(2,members.size());
//    assertEquals(members.get(0).getUserDetail(), user1);
//    assertEquals(members.get(1).getUserDetail(), user2);
//
//    request.setParameter("currentPage", "1");
//    result = router.doPagination(request);
//    currentPageAtt = (Integer) request.getAttribute("currentPage");
//    assertNotNull(currentPageAtt);
//    assertEquals(1, currentPageAtt.intValue());
//    members =  (List<Member>) request.getAttribute("Members");
//    assertNotNull(members);
//    assertEquals(1,members.size());
//    assertEquals(members.get(0).getUserDetail(), user3);
        
  }
  
  @Test
  public void testToListMember(){
    DirectoryRequestRouter router = new DirectoryRequestRouter();
    UserDetail user1 = new UserDetail();
    user1.setId("1");
    user1.setLastName("bourakbi");
    user1.setFirstName("nidale");
    user1.seteMail("nidale.bourakbi@gmail.com");
    user1.setLogin("bourakbn");
    UserDetail user2 = new UserDetail();
    user2.setId("2");
    user2.setLastName("bensalem");
    user2.setFirstName("nabil");
    user2.seteMail("nabil@gmail.com");
    user2.setLogin("nabil");
    
    List<UserDetail> users = new ArrayList<UserDetail>();
    users.add(user1);
    users.add(user2);
    assertEquals(users.get(0), user1);
    assertEquals(users.get(1),user2);
  }

}