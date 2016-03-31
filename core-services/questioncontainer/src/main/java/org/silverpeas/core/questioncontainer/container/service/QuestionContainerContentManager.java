/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.questioncontainer.container.service;

import java.sql.Connection;

import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.StringUtil;

public class QuestionContainerContentManager {

  // if beginDate is null, it will be replace in database with it
  private final static String nullBeginDate = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private final static String nullEndDate = "9999/99/99";

  public static int getSilverObjectId(String id, String peasId) throws ContentManagerException {

    return getContentManager().getSilverContentId(id, peasId);
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param qC the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public static int createSilverContent(Connection con, QuestionContainerHeader qC, String userId,
      boolean isVisible) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible);
    setDateAttributes(scv, qC.getBeginDate(), qC.getEndDate());

    return getContentManager()
        .addSilverContent(con, qC.getPK().getId(), qC.getPK().getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param qC
   * @param isVisible
   * @throws ContentManagerException
   */
  public static void updateSilverContentVisibility(QuestionContainerHeader qC, boolean isVisible)
      throws ContentManagerException {
    int silverContentId =
        getContentManager().getSilverContentId(qC.getPK().getId(), qC.getPK().getComponentName());
    if (silverContentId != -1) {
      SilverContentVisibility scv = new SilverContentVisibility(isVisible);
      setDateAttributes(scv, qC.getBeginDate(), qC.getEndDate());
      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, qC.getPK().getComponentName(),
              silverContentId);
      ClassifyEngine.clearCache();
    } else {
      createSilverContent(null, qC, qC.getCreatorId(), isVisible);
    }
  }

  private static void setDateAttributes(SilverContentVisibility visibility, String startDate,
      String endDate) {
    String updatableStart = (StringUtil.isDefined(startDate)) ? startDate : nullBeginDate;
    String updatableEnd = (StringUtil.isDefined(endDate)) ? endDate : nullEndDate;
    visibility.setVisibilityAttributes(updatableStart, updatableEnd);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con the database connection
   * @param pk the pk identifier
   * @throws ContentManagerException
   */
  public static void deleteSilverContent(Connection con, QuestionContainerPK pk)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pk.getId(), pk.getComponentName());

    getContentManager().removeSilverContent(con, contentId, pk.getComponentName());
  }

  private static ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace
            .fatal("questionContainer", "QuestionContainerContentManager.getContentManager()",
                "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private static ContentManager contentManager = null;
}