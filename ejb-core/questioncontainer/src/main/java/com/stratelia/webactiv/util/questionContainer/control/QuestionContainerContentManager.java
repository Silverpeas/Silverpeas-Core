/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.questionContainer.control;

import java.sql.Connection;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

public class QuestionContainerContentManager {

  // if beginDate is null, it will be replace in database with it
  private final static String nullBeginDate = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private final static String nullEndDate = "9999/99/99";

  public static int getSilverObjectId(String id, String peasId)
      throws ContentManagerException {
    SilverTrace.info("questionContainer",
        "QuestionContainerContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id);
    return getContentManager().getSilverContentId(id, peasId);
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param qC the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public static int createSilverContent(Connection con,
      QuestionContainerHeader qC, String userId, boolean isVisible)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible);
    setDateAttributes(scv, qC.getBeginDate(), qC.getEndDate());
    SilverTrace.info("questionContainer",
        "QuestionContainerContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    return getContentManager().addSilverContent(con, qC.getPK().getId(),
        qC.getPK().getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param pubDetail the content
   * @param silverObjectId the unique identifier of the content
   */
  public static void updateSilverContentVisibility(QuestionContainerHeader qC,
      boolean isVisible) throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        qC.getPK().getId(), qC.getPK().getComponentName());
    if (silverContentId != -1) {
      SilverContentVisibility scv = new SilverContentVisibility(isVisible);
      setDateAttributes(scv, qC.getBeginDate(), qC.getEndDate());
      SilverTrace.info("questionContainer",
          "QuestionContainerContentManager.updateSilverContentVisibility()",
          "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
          + scv.toString());
      getContentManager().updateSilverContentVisibilityAttributes(scv,
          qC.getPK().getComponentName(), silverContentId);
      ClassifyEngine.clearCache();
    } else {
      createSilverContent(null, qC, qC.getCreatorId(), isVisible);
    }
  }

  private static void setDateAttributes(SilverContentVisibility visibility,
      String startDate, String endDate) {
    String updatableStart = (startDate != null && !startDate.equals("")) ? startDate
        : nullBeginDate;
    String updatableEnd = (endDate != null && !endDate.equals("")) ? endDate
        : nullEndDate;
    visibility.setVisibilityAttributes(updatableStart, updatableEnd);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public static void deleteSilverContent(Connection con, QuestionContainerPK pk)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pk.getId(),
        pk.getComponentName());
    SilverTrace.info("questionContainer",
        "QuestionContainerContentManager.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + pk.getId() + ", contentId = "
        + contentId);
    getContentManager().removeSilverContent(con, contentId,
        pk.getComponentName());
  }

  private static ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("questionContainer",
            "QuestionContainerContentManager.getContentManager()",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private static ContentManager contentManager = null;
}