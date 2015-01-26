package com.silverpeas.myLinks.dao;

import java.sql.Connection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.myLinks.model.LinkDetail;
import com.stratelia.webactiv.util.DBUtil;

public class LinkDAOTest extends AbstractTestDao {

  @Override
  protected String getDatasetFileName() {
    return "test-mylinks-dataset.xml";
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }

  @Test
  public void testGetAllLinks() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      List<LinkDetail> result = LinkDAO.getAllLinksByUser(con, "user");

      Assert.assertEquals(4, result.get(0).getLinkId());
      Assert.assertEquals(1, result.get(1).getLinkId());
      Assert.assertEquals(5, result.get(2).getLinkId());
      Assert.assertEquals(3, result.get(3).getLinkId());
      Assert.assertEquals(2, result.get(4).getLinkId());
    } finally {
      if (con != null) {
        DBUtil.close(con);
      }
    }
  }
  
  @Test
  public void testUpdateLinks() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      LinkDetail link = LinkDAO.getLink(con, "4");
      link.setPosition(4);
      link.setHasPosition(true);
      LinkDAO.updateLink(con, link);


      List<LinkDetail> result = LinkDAO.getAllLinksByUser(con, "user");
      
      Assert.assertEquals(1, result.get(0).getLinkId());
      Assert.assertEquals(5, result.get(1).getLinkId());
      Assert.assertEquals(3, result.get(2).getLinkId());
      Assert.assertEquals(2, result.get(3).getLinkId());
      Assert.assertEquals(4, result.get(4).getLinkId());
    } finally {
      if (con != null) {
        DBUtil.close(con);
      }
    }
  }
}
