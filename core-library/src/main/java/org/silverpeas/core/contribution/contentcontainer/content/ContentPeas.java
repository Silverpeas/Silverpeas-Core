/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  // User roles of the content
  List<String> userRoles = null;
  // Name of the bean in the session
  String sessionControlBeanName = null;

  public ContentPeas(String sContentDescriptorPath) {
    userRoles = new ArrayList<>();
    // -------------------------------------------------
    // We don't have enough time to do the parsing !!!
    // We hard coded for this time !!!!
    // -------------------------------------------------
    if ("fileBoxPlus".equals(sContentDescriptorPath)) {
      this.init("fileBoxPlus", "com.stratelia.silverpeas.fileBoxPlus.FileBoxPlusContentManager",
          "fileBoxPlus", "admin", "publisher", "user");
    } else if ("whitePages".equals(sContentDescriptorPath)) {
      this.init("whitePages", "org.silverpeas.components.whitepages.control.WhitePagesContentManager",
          "whitePagesPDC", "admin", "user");
    } else if ("questionReply".equals(sContentDescriptorPath)) {
      this.init("questionReply",
          "org.silverpeas.components.questionreply.service.QuestionReplyContentManager",
          "questionReplyPDC", "admin", "publisher", "writer", "user");
    } else if ("kmelia".equals(sContentDescriptorPath)) {
      this.init("kmelia", "org.silverpeas.components.kmelia.KmeliaContentManager", "kmelia", "admin",
          "publisher", "writer", "user");
    } else if ("survey".equals(sContentDescriptorPath)) {
      this.init("survey", "com.stratelia.webactiv.survey.SurveyContentManager", "survey", "admin",
          "user");
    } else if ("toolbox".equals(sContentDescriptorPath)) {
      this.init("toolbox", "org.silverpeas.components.kmelia.KmeliaContentManager", "kmelia", "admin",
          "publisher", "user");
    } else if ("quickinfo".equals(sContentDescriptorPath)) {
      this.init("quickinfo", "org.silverpeas.components.quickinfo.service.QuickInfoContentManager",
          "quickinfo",
          "admin", "publisher", "user");
    } else if ("almanach".equals(sContentDescriptorPath)) {
      this.init("almanach", "org.silverpeas.components.almanach.AlmanachContentManager", "almanach",
          "admin", "publisher", "user");
    } else if ("quizz".equals(sContentDescriptorPath)) {
      this.init("quizz", "org.silverpeas.components.quizz.QuizzContentManager", "quizz", "admin",
          "publisher", "user");
    } else if (sContentDescriptorPath.equals("forums")) {
      this.init("forums", "org.silverpeas.components.forums.ForumsContentManager", "forums", "admin",
          "user");
    } else if ("pollingStation".equals(sContentDescriptorPath)) {
      this.init("pollingStation", "com.stratelia.webactiv.survey.SurveyContentManager", "survey",
          "admin", "user");
    } else if ("bookmark".equals(sContentDescriptorPath)) {
      this.init("bookmark", "org.silverpeas.components.websites.WebSitesContentManager", "webSites",
          "Publisher", "Reader");
    } else if ("chat".equals(sContentDescriptorPath)) {
      this.init("chat", "com.stratelia.silverpeas.chat.ChatContentManager", "chat", "admin",
          "publisher", "user");
    } else if ("infoLetter".equals(sContentDescriptorPath)) {
      this.init("infoLetter", "org.silverpeas.components.infoLetter.InfoLetterContentManager",
          "infoLetter", "admin", "publisher", "user");
    } else if ("expertLocator".equals(sContentDescriptorPath)) {
      this.init("expertLocator",
          "com.silverpeas.expertLocator.control.ExpertLocatorContentManager",
          "expertLocator", "admin", "user");
    } else if ("webSites".equals(sContentDescriptorPath)) {
      this.init("webSites", "org.silverpeas.components.websites.WebSitesContentManager", "webSites",
          "Publisher", "Reader");
    } else if ("gallery".equals(sContentDescriptorPath)) {
      this.init("gallery", "org.silverpeas.components.gallery.GalleryContentManager", "gallery", "admin",
          "publisher", "writer", "user");
    } else if (sContentDescriptorPath.equals("blog")) {
      this.init("blog", "org.silverpeas.components.blog.BlogContentManager", "blog", "admin", "user");
    }
  }

  private void init(String type, String contentInterfaceClassName, String sessionControlBeanName,
      String... roles) {
    this.type = type;
    this.contentInterfaceClassName = contentInterfaceClassName;
    this.sessionControlBeanName = sessionControlBeanName;
    setRoles(roles);
  }

  private void setRoles(String... roles) {
    userRoles.addAll(Arrays.asList(roles));
  }

  public void setType(String sType) {
    type = sType;
  }

  public String getType() {
    return type;
  }

  public void setContentInterfaceClass(String sContentInterfaceClass) {
    contentInterfaceClassName = sContentInterfaceClass;
  }

  public String getContentInterfaceClass() {
    return contentInterfaceClassName;
  }

  @SuppressWarnings("unchecked")
  public ContentInterface getContentInterface() throws Exception {
    if (contentInterface == null) {
      Class<ContentInterface> contentInterfaceClass =
          (Class<ContentInterface>) Class.forName(this.getContentInterfaceClass());
      this.contentInterface = ServiceProvider.getService(contentInterfaceClass);
    }

    return contentInterface;
  }

  public void setUserRoles(List<String> asUserRoles) {
    userRoles = asUserRoles;
  }

  public List<String> getUserRoles() {
    return userRoles;
  }

  public void setSessionControlBeanName(String sSessionControlBeanName) {
    sessionControlBeanName = sSessionControlBeanName;
  }

  public String getSessionControlBeanName() {
    return sessionControlBeanName;
  }
}