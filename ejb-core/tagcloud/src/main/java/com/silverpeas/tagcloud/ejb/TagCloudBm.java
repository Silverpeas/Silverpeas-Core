/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.tagcloud.ejb;

import java.util.Collection;

import javax.ejb.Local;

import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;

@Local
public interface TagCloudBm {

  public void createTagCloud(TagCloud tagCloud);

  public void deleteTagCloud(TagCloudPK pk, int type);

  public Collection<TagCloud> getInstanceTagClouds(String instanceId);

  public Collection<TagCloud> getInstanceTagClouds(String instanceId, int maxCount);

  public Collection<TagCloud> getElementTagClouds(TagCloudPK pk);

  public Collection<TagCloud> getTagCloudsByTags(String tags, String instanceId, int type);

  public Collection<TagCloud> getTagCloudsByElement(String instanceId, String externalId,
      int type);

  public String getTagsByElement(TagCloudPK pk);
}