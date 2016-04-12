/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.mylinks.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.mylinks.MyLinksRuntimeException;
import org.silverpeas.core.mylinks.dao.LinkDAO;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.model.LinkDetailComparator;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.silverpeas.core.mylinks.dao.LinkDAO.getLinkDao;
import static org.silverpeas.core.persistence.jdbc.DBUtil.openConnection;

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultMyLinksService implements MyLinksService, ComponentInstanceDeletion {

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      LinkDAO.deleteComponentInstanceData(componentInstanceId);
    } catch (SQLException e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.delete()", SilverpeasRuntimeException.ERROR,
          "node.DELETING_COMPONENT_INSTANCE_MYLINKS_FAILED", "instanceId = " + componentInstanceId,
          e);
    }
  }

  @Override
  public List<LinkDetail> getAllLinks(String userId) {
    return getAllLinksByUser(userId);
  }

  @Override
  public List<LinkDetail> getAllLinksByUser(String userId) {
    try (Connection con = openConnection()) {
      List<LinkDetail> links = getLinkDao().getAllLinksByUser(con, userId);
      return LinkDetailComparator.sort(links);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByUser()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    }
  }

  @Override
  public List<LinkDetail> getAllLinksByInstance(String instanceId) {
    try (Connection con = openConnection()) {
      return getLinkDao().getAllLinksByInstance(con, instanceId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByInstance()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    }
  }

  @Override
  public List<LinkDetail> getAllLinksByObject(String instanceId, String objectId) {
    try (Connection con = openConnection()) {
      return getLinkDao().getAllLinksByObject(con, instanceId, objectId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByObject()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    }
  }

  @Override
  public void createLink(LinkDetail link) {
    try (Connection con = openConnection()) {
      int id = getLinkDao().createLink(con, link);
      link.setLinkId(id);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.createLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_CREATE", e);
    }
  }

  @Override
  public void deleteLinks(String[] links) {
    try (Connection con = openConnection()) {
      for (String linkId : links) {
        getLinkDao().deleteLink(con, linkId);
      }
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.deleteLinks()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_DELETE", e);
    }
  }

  @Override
  public void updateLink(LinkDetail link) {
    try (Connection con = openConnection()) {
      getLinkDao().updateLink(con, link);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.updateLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_UPDATE", e);
    }
  }

  @Override
  public LinkDetail getLink(String linkId) {
    try (Connection con = openConnection()) {
      return getLinkDao().getLink(con, linkId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getLink()", SilverpeasRuntimeException.ERROR,
          "myLinks.MSG_LINKS_NOT_EXIST", e);
    }
  }
}
