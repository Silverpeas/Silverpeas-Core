/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.tagcloud.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudDAO;
import com.silverpeas.tagcloud.model.TagCloudPK;
import com.silverpeas.tagcloud.model.TagCloudUtil;
import com.silverpeas.tagcloud.model.comparator.TagCloudByCountComparator;
import com.silverpeas.tagcloud.model.comparator.TagCloudByNameComparator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class TagCloudBmEJB implements SessionBean {

  private Connection openConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("TagCloudBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("tagCloud", "TagCloudBmEJB.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * @param tagCloud The tagcloud to create in database.
   * @throws RemoteException
   */
  public void createTagCloud(TagCloud tagCloud) throws RemoteException {
    Connection con = openConnection();
    try {
      TagCloudDAO.createTagCloud(con, tagCloud);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("TagCloudBmEJB.createTagCloud()",
          SilverpeasRuntimeException.ERROR,
          "tagCloud.CREATING_NEW_TAGCLOUD_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param pk The primary key of the tagcloud to delete from database.
   * @throws RemoteException
   */
  public void deleteTagCloud(TagCloudPK pk, int type) throws RemoteException {
    Connection con = openConnection();
    try {
      TagCloudDAO.deleteTagCloud(con, pk, type);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("TagCloudBmEJB.deleteTagCloud()",
          SilverpeasRuntimeException.ERROR, "tagCloud.DELETE_TAGCLOUD_FAILED",
          e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param instanceId The id of the instance which tagclouds are searched for.
   * @return The list of tagclouds corresponding to the instance.
   * @throws RemoteException
   */
  public Collection getInstanceTagClouds(String instanceId)
      throws RemoteException {
    return getInstanceTagClouds(instanceId, -1);
  }

  /**
   * @param instanceId The id of the instance which tagclouds are searched for.
   * @param maxCount The maximum number of required tagclouds (all are returned it is lower than 0).
   * @return The list of tagclouds corresponding to the instance.
   * @throws RemoteException
   */
  public Collection getInstanceTagClouds(String instanceId, int maxCount)
      throws RemoteException {
    Connection con = openConnection();
    try {
      Collection tagClouds = TagCloudDAO.getInstanceTagClouds(con, instanceId);
      List tagList = new ArrayList();
      if (tagClouds.size() > 0) {
        Iterator iter = tagClouds.iterator();
        tagList.add((TagCloud) iter.next());
        TagCloud iterTagCloud;
        String iterTag;
        TagCloud currentTagCloud;
        int i;
        boolean tagExists;
        while (iter.hasNext()) {
          iterTagCloud = (TagCloud) iter.next();
          iterTag = iterTagCloud.getTag();
          i = 0;
          tagExists = false;
          while (i < tagList.size() && !tagExists) {
            currentTagCloud = (TagCloud) tagList.get(i);
            if (currentTagCloud.getTag().equals(iterTag)) {
              tagExists = true;
              currentTagCloud.incrementCount();
            }
            i++;
          }
          if (!tagExists) {
            tagList.add(iterTagCloud);
          }
        }

        if (maxCount > 0 && tagList.size() > maxCount) {
          Collections.sort(tagList, new TagCloudByCountComparator());
          tagList = tagList.subList(0, maxCount);
          Collections.sort(tagList, new TagCloudByNameComparator());
        }
      }
      return tagList;
    } catch (Exception e) {
      throw new TagCloudRuntimeException(
          "TagCloudBmEJB.getInstanceTagClouds()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param externalId The id of the element which tagclouds are searched for.
   * @return The list of tagclouds corresponding to the element.
   * @throws RemoteException
   */
  public Collection getElementTagClouds(TagCloudPK pk) throws RemoteException {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getElementTagClouds(con, pk);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("TagCloudBmEJB.getElementTagClouds()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param tags The searched tags.
   * @param instanceId The id of the instance.
   * @param type The type of elements referenced by the tagclouds (publications or forums).
   * @return The list of tagclouds which correspond to the tag and the id of the instance given as
   * parameters.
   * @throws RemoteException
   */
  public Collection getTagCloudsByTags(String tags, String instanceId, int type)
      throws RemoteException {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getTagCloudsByTags(con, TagCloudUtil.getTag(tags),
          instanceId, type);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("TagCloudBmEJB.getTagCloudsByTags()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param instanceId The id of the instance.
   * @param externalId The id of the element.
   * @return The list of tagclouds corresponding to the ids given as parameters.
   * @throws RemoteException
   */
  public Collection getTagCloudsByElement(String instanceId, String externalId,
      int type) throws RemoteException {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getTagCloudsByElement(con, instanceId, externalId,
          type);
    } catch (Exception e) {
      throw new TagCloudRuntimeException(
          "TagCloudBmEJB.getTagCloudsByElement()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public String getTagsByElement(TagCloudPK pk) throws RemoteException {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getTagsByElement(con, pk);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("TagCloudBmEJB.getTagsByElement()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public void ejbCreate() throws CreateException {
  }

  public void ejbRemove() {
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  public void setSessionContext(SessionContext sc) {
  }

}