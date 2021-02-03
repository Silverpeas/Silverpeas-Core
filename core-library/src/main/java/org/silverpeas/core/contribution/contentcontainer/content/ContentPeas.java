/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.util.ServiceProvider;

/**
 * This class represents one content descriptor in memory (read from the xml)
 */
public class ContentPeas {

  // The content type (unique among all contents)
  String type = null;
  // The class to call that implements the ContentInterface
  String contentInterfaceClassName = null;
  // The object (class.forName(m_sContentInterface))
  ContentInterface contentInterface = null;

  public ContentPeas(String sContentDescriptorPath) {
    // -------------------------------------------------
    // We don't have enough time to do the parsing !!!
    // We hard coded for this time !!!!
    // -------------------------------------------------
    if ("whitePages".equals(sContentDescriptorPath)) {
      this.init("whitePages",
          "org.silverpeas.components.whitepages.control.WhitePagesContentManager");
    } else if ("questionReply".equals(sContentDescriptorPath)) {
      this.init("questionReply",
          "org.silverpeas.components.questionreply.service.QuestionReplyContentManager");
    } else if ("kmelia".equals(sContentDescriptorPath)) {
      this.init("kmelia", "org.silverpeas.components.kmelia.KmeliaContentManager");
    } else if ("survey".equals(sContentDescriptorPath)) {
      this.init("survey", "org.silverpeas.components.survey.SurveyContentManager");
    } else if ("toolbox".equals(sContentDescriptorPath)) {
      this.init("toolbox", "org.silverpeas.components.kmelia.KmeliaContentManager");
    } else if ("quickinfo".equals(sContentDescriptorPath)) {
      this.init("quickinfo", "org.silverpeas.components.quickinfo.service.QuickInfoContentManager");
    } else if ("almanach".equals(sContentDescriptorPath)) {
      this.init("almanach", "org.silverpeas.components.almanach.AlmanachContentManager");
    } else if ("quizz".equals(sContentDescriptorPath)) {
      this.init("quizz", "org.silverpeas.components.quizz.QuizzContentManager");
    } else if ("forums".equals(sContentDescriptorPath)) {
      this.init("forums", "org.silverpeas.components.forums.ForumsContentManager");
    } else if ("pollingStation".equals(sContentDescriptorPath)) {
      this.init("pollingStation", "org.silverpeas.components.survey.SurveyContentManager");
    } else if ("bookmark".equals(sContentDescriptorPath)) {
      this.init("bookmark", "org.silverpeas.components.websites.WebSitesContentManager");
    } else if ("infoLetter".equals(sContentDescriptorPath)) {
      this.init("infoLetter", "org.silverpeas.components.infoletter.InfoLetterContentManager");
    } else if ("webSites".equals(sContentDescriptorPath)) {
      this.init("webSites", "org.silverpeas.components.websites.WebSitesContentManager");
    } else if ("gallery".equals(sContentDescriptorPath)) {
      this.init("gallery", "org.silverpeas.components.gallery.GalleryContentManager");
    } else if ("blog".equals(sContentDescriptorPath)) {
      this.init("blog", "org.silverpeas.components.blog.BlogContentManager");
    }
  }

  private void init(String type, String contentInterfaceClassName) {
    this.type = type;
    this.contentInterfaceClassName = contentInterfaceClassName;
  }

  public String getType() {
    return type;
  }

  private String getContentInterfaceClass() {
    return contentInterfaceClassName;
  }

  @SuppressWarnings("unchecked")
  public ContentInterface getContentInterface() throws SilverpeasException {
    if (contentInterface == null) {
      try {
        Class<ContentInterface> contentInterfaceClass =
            (Class<ContentInterface>) Class.forName(this.getContentInterfaceClass());
        this.contentInterface = ServiceProvider.getSingleton(contentInterfaceClass);
      } catch (ClassNotFoundException e) {
        throw new SilverpeasException(e);
      }
    }

    return contentInterface;
  }

}