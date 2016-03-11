/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.tagcloud.ejb;

import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;
import com.silverpeas.tagcloud.model.TagCloudUtil;
import com.stratelia.webactiv.publication.control.DefaultPublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.i18n.I18NHelper;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * This service extends the default implementation of the
 * {@©ode com.stratelia.webactiv.publication.control.PublicationService} interface by adding
 * tag cloud capabilities.
 * @author mmoquillon
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class TagCloudPublicationService extends DefaultPublicationService {

  private boolean useTagCloud;

  @Inject
  private TagCloudBm tagCloudBm;

  @PostConstruct
  protected void init() {
    SettingBundle publicationSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");
    useTagCloud = publicationSettings.getBoolean("useTagCloud", false);
  }

  @Override
  @Transactional
  public PublicationPK createPublication(final PublicationDetail detail) {
    PublicationPK pk = super.createPublication(detail);
    if (useTagCloud) {
      createTagCloud(detail);
    }
    return pk;
  }

  @Override
  @Transactional
  public void setDetail(final PublicationDetail detail) {
    super.setDetail(detail);
    if (useTagCloud) {
      updateTagCloud(detail);
    }
  }

  /**
   * Called on : - deletePublication()
   * @param pubPK
   */
  @Override
  public void deleteIndex(final PublicationPK pubPK) {
    super.deleteIndex(pubPK);
    // remove the tag cloud at index deletion and not when the publication is only moved into the
    // bin.
    if (useTagCloud) {
      deleteTagCloud(pubPK);
    }
  }

  /**
   * Create the tagclouds corresponding to the publication detail.
   * @param pubDetail The detail of the publication.
   * @
   */
  private void createTagCloud(PublicationDetail pubDetail) {
    String keywords = pubDetail.getKeywords();
    if (keywords != null) {
      TagCloud tagCloud =
          new TagCloud(pubDetail.getInstanceId(), pubDetail.getId(), TagCloud.TYPE_PUBLICATION);
      StringTokenizer st = new StringTokenizer(keywords, " ");
      List<String> tagList = new ArrayList<>();
      while (st.hasMoreElements()) {
        String tag = (String) st.nextElement();
        String tagKey = TagCloudUtil.getTag(tag);
        if (!tagList.contains(tagKey)) {
          tagCloud.setTag(tagKey);
          tagCloud.setLabel(tag.toLowerCase(I18NHelper.defaultLocale));
          tagCloudBm.createTagCloud(tagCloud);
          tagList.add(tagKey);
        }
      }
    }
  }

  /**
   * Delete the tagclouds corresponding to the publication key.
   * @param pubPK The primary key of the publication.
   * @
   */
  private void deleteTagCloud(PublicationPK pubPK) {
    tagCloudBm.deleteTagCloud(new TagCloudPK(pubPK.getId(), pubPK.getInstanceId()),
        TagCloud.TYPE_PUBLICATION);
  }

  /**
   * Update the tagclouds corresponding to the publication detail.
   * @param pubDetail The detail of the publication.
   * @
   */
  private void updateTagCloud(PublicationDetail pubDetail) {
    deleteTagCloud(pubDetail.getPK());
    createTagCloud(pubDetail);
  }
}
