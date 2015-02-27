/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.myLinks.ejb;

import com.silverpeas.myLinks.MyLinksRuntimeException;
import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.myLinks.model.LinkDetailComparator;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.sql.Connection;
import java.util.List;

import static com.silverpeas.myLinks.dao.LinkDAO.getLinkDao;

/**
 * @author
 */
@Stateless(name = "MyLinks", description =
    "Stateless session bean to manage personal links to content")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class MyLinksBmEJB implements MyLinksBm {

  @Override
  public List<LinkDetail> getAllLinks(String userId) {
    return getAllLinksByUser(userId);
  }

  @Override
  public List<LinkDetail> getAllLinksByUser(String userId) {
    Connection con = initCon();
    try {
      List<LinkDetail> links = getLinkDao().getAllLinksByUser(con, userId);
      return LinkDetailComparator.sort(links);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByUser()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<LinkDetail> getAllLinksByInstance(String instanceId) {
    Connection con = initCon();
    try {
      return getLinkDao().getAllLinksByInstance(con, instanceId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByInstance()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<LinkDetail> getAllLinksByObject(String instanceId, String objectId) {
    Connection con = initCon();
    try {
      return getLinkDao().getAllLinksByObject(con, instanceId, objectId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByObject()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void createLink(LinkDetail link) {
    Connection con = initCon();
    try {
      int id = getLinkDao().createLink(con, link);
      link.setLinkId(id);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.createLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_CREATE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteLinks(String[] links) {
    Connection con = initCon();
    try {
      for (String linkId : links) {
        getLinkDao().deleteLink(con, linkId);
      }
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.deleteLinks()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_DELETE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateLink(LinkDetail link) {
    Connection con = initCon();
    try {
      getLinkDao().updateLink(con, link);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.updateLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_UPDATE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public LinkDetail getLink(String linkId) {
    Connection con = initCon();
    try {
      return getLinkDao().getLink(con, linkId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  protected Connection initCon() {
    Connection con;
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }
}
