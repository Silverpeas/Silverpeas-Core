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
package org.silverpeas.core.tagcloud.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.tagcloud.model.TagCloud;
import org.silverpeas.core.tagcloud.dao.TagCloudDAO;
import org.silverpeas.core.tagcloud.dao.TagCloudPK;
import org.silverpeas.core.tagcloud.model.TagCloudUtil;
import org.silverpeas.core.tagcloud.model.comparator.TagCloudByCountComparator;
import org.silverpeas.core.tagcloud.model.comparator.TagCloudByNameComparator;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultTagCloudService implements TagCloudService, ComponentInstanceDeletion {

  private Connection openConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * @param tagCloud The tagcloud to create in database.
   *
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void createTagCloud(TagCloud tagCloud) {
    Connection con = openConnection();
    try {
      TagCloudDAO.createTagCloud(con, tagCloud);
    } catch (SQLException e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.createTagCloud()",
          SilverpeasRuntimeException.ERROR, "tagCloud.CREATING_NEW_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param pk The primary key of the tagcloud to delete from database.
   *
   */
  @Override
  public void deleteTagCloud(TagCloudPK pk, int type) {
    Connection con = openConnection();
    try {
      TagCloudDAO.deleteTagCloud(con, pk, type);
    } catch (SQLException e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.deleteTagCloud()",
          SilverpeasRuntimeException.ERROR, "tagCloud.DELETE_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param instanceId The id of the instance which tagclouds are searched for.
   * @return The list of tagclouds corresponding to the instance.
   *
   */
  @Override
  public Collection<TagCloud> getInstanceTagClouds(String instanceId) {
    return getInstanceTagClouds(instanceId, -1);
  }

  /**
   * @param instanceId The id of the instance which tagclouds are searched for.
   * @param maxCount The maximum number of required tagclouds (all are returned it is lower than 0).
   * @return The list of tagclouds corresponding to the instance.
   *
   */
  @Override
  public Collection<TagCloud> getInstanceTagClouds(String instanceId, int maxCount) {
    Connection con = openConnection();
    try {
      Collection<TagCloud> tagClouds = TagCloudDAO.getInstanceTagClouds(con, instanceId);
      List<TagCloud> tagList = new ArrayList<TagCloud>(tagClouds.size());
      if (!tagClouds.isEmpty()) {
        Iterator<TagCloud> iter = tagClouds.iterator();
        tagList.add(iter.next());
        boolean tagExists;
        while (iter.hasNext()) {
          TagCloud iterTagCloud = iter.next();
          String iterTag = iterTagCloud.getTag();
          int i = 0;
          tagExists = false;
          while (i < tagList.size() && !tagExists) {
            TagCloud currentTagCloud = tagList.get(i);
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
    } catch (SQLException e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.getInstanceTagClouds()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param pk The id of the element which tagclouds are searched for.
   * @return The list of tagclouds corresponding to the element.
   *
   */
  @Override
  public Collection<TagCloud> getElementTagClouds(TagCloudPK pk) {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getElementTagClouds(con, pk);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.getElementTagClouds()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param tags The searched tags.
   * @param instanceId The id of the instance.
   * @param type The type of elements referenced by the tagclouds (publications or forums).
   * @return The list of tagclouds which correspond to the tag and the id of the instance given as
   * parameters.
   *
   */
  @Override
  public Collection<TagCloud> getTagCloudsByTags(String tags, String instanceId, int type) {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getTagCloudsByTags(con, TagCloudUtil.getTag(tags), instanceId, type);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.getTagCloudsByTags()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param instanceId The id of the instance.
   * @param externalId The id of the element.
   * @param type The type of elements referenced by the tagclouds (publications or forums).
   * @return The list of tagclouds corresponding to the ids given as parameters.
   *
   */
  @Override
  public Collection<TagCloud> getTagCloudsByElement(String instanceId, String externalId, int type) {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getTagCloudsByElement(con, instanceId, externalId,
          type);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.getTagCloudsByElement()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public String getTagsByElement(TagCloudPK pk) {
    Connection con = openConnection();
    try {
      return TagCloudDAO.getTagsByElement(con, pk);
    } catch (Exception e) {
      throw new TagCloudRuntimeException("DefaultTagCloudService.getTagsByElement()",
          SilverpeasRuntimeException.ERROR, "tagCloud.GET_TAGCLOUD_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      TagCloudDAO.deleteAllTagClouds(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}