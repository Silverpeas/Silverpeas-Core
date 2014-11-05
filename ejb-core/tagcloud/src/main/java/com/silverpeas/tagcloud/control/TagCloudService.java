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
package com.silverpeas.tagcloud.control;

import java.util.Collection;

import javax.ejb.Local;

import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;

public interface TagCloudService {

  /**
   *
   * @param tagCloud the tagCloud to create
   */
  public void createTagCloud(TagCloud tagCloud);

  /**
   * @param pk the tag cloud identifier
   * @param type the external type (Publication or Forum or Message)
   */
  public void deleteTagCloud(TagCloudPK pk, int type);

  /**
   * @param instanceId the instance identifier
   * @return The list of tagclouds corresponding to the instance
   */
  public Collection<TagCloud> getInstanceTagClouds(String instanceId);

  public Collection<TagCloud> getInstanceTagClouds(String instanceId, int maxCount);

  public Collection<TagCloud> getElementTagClouds(TagCloudPK pk);

  public Collection<TagCloud> getTagCloudsByTags(String tags, String instanceId, int type);

  /**
   * @param instanceId The id of the instance.
   * @param externalId The id of the element.
   * @param type The type of elements referenced by the tagclouds (publications or forums).
   * @return The list of tagclouds corresponding to the ids given as parameters.
   */
  public Collection<TagCloud> getTagCloudsByElement(String instanceId, String externalId, int type);

  public String getTagsByElement(TagCloudPK pk);
}