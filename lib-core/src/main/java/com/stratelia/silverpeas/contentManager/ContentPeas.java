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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.contentManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents one content descriptor in memory (read from the xml)
 */
public class ContentPeas extends Object {
  String m_sType = null; // The content type (unique among all contents)
  String m_sContentInterfaceClass = null; // The class to call that implements
  // the ContentInterface
  ContentInterface m_contentInterface = null; // The object
  // (class.forName(m_sContentInterface))
  List<String> m_asUserRoles = null; // User roles of the content
  String m_sSessionControlBeanName = null; // Name of the bean in the session

  public ContentPeas(String sContentDescriptorPath) {
    // -------------------------------------------------
    // We don't have enough time to do the parsing !!!
    // We hard coded for this time !!!!
    // -------------------------------------------------
    if (sContentDescriptorPath.equals("fileBoxPlus")) {
      this.setType("fileBoxPlus");
      this
          .setContentInterfaceClass("com.stratelia.silverpeas.fileBoxPlus.FileBoxPlusContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("fileBoxPlus");
    } else if (sContentDescriptorPath.equals("whitePages")) {
      this.setType("whitePages");
      this
          .setContentInterfaceClass("com.silverpeas.whitePages.control.WhitePagesContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("whitePagesPDC");
    } else if (sContentDescriptorPath.equals("questionReply")) {
      this.setType("questionReply");
      this
          .setContentInterfaceClass("com.silverpeas.questionReply.control.QuestionReplyContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("writer");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("questionReplyPDC");
    } else if (sContentDescriptorPath.equals("kmelia")) {
      this.setType("kmelia");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.kmelia.KmeliaContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("writer");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("kmelia");
    } else if (sContentDescriptorPath.equals("survey")) {
      this.setType("survey");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.survey.SurveyContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("survey");
    } else if (sContentDescriptorPath.equals("toolbox")) {
      this.setType("toolbox");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.kmelia.KmeliaContentManager");
      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("kmelia");
    } else if (sContentDescriptorPath.equals("quickinfo")) {
      this.setType("quickinfo");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.quickinfo.QuickInfoContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("quickinfo");
    } else if (sContentDescriptorPath.equals("almanach")) {
      this.setType("almanach");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.almanach.AlmanachContentManager");
      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("almanach");
    } else if (sContentDescriptorPath.equals("quizz")) {
      this.setType("quizz");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.quizz.QuizzContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("quizz");
    } else if (sContentDescriptorPath.equals("forums")) {
      this.setType("forums");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.forums.ForumsContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("forums");
    } else if (sContentDescriptorPath.equals("pollingStation")) {
      this.setType("pollingStation");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.survey.SurveyContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("survey");
    } else if (sContentDescriptorPath.equals("bookmark")) {
      this.setType("bookmark");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.webSites.WebSitesContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("Publisher");
      asUserRoles.add("Reader");

      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("webSites");
    } else if (sContentDescriptorPath.equals("chat")) {
      this.setType("chat");
      this
          .setContentInterfaceClass("com.stratelia.silverpeas.chat.ChatContentManager");
      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("chat");
    } else if (sContentDescriptorPath.equals("infoLetter")) {
      this.setType("infoLetter");
      this
          .setContentInterfaceClass("com.stratelia.silverpeas.infoLetter.InfoLetterContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("infoLetter");
    } else if (sContentDescriptorPath.equals("expertLocator")) {
      this.setType("expertLocator");
      this
          .setContentInterfaceClass("com.silverpeas.expertLocator.control.ExpertLocatorContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("expertLocator");
    } else if (sContentDescriptorPath.equals("webSites")) {
      this.setType("webSites");
      this
          .setContentInterfaceClass("com.stratelia.webactiv.webSites.WebSitesContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("Publisher");
      asUserRoles.add("Reader");

      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("webSites");
    } else if (sContentDescriptorPath.equals("gallery")) {
      this.setType("gallery");
      this
          .setContentInterfaceClass("com.silverpeas.gallery.GalleryContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("publisher");
      asUserRoles.add("writer");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("gallery");
    } else if (sContentDescriptorPath.equals("blog")) {
      this.setType("blog");
      this.setContentInterfaceClass("com.silverpeas.blog.BlogContentManager");

      ArrayList<String> asUserRoles = new ArrayList<String>();
      asUserRoles.add("admin");
      asUserRoles.add("user");
      this.setUserRoles(asUserRoles);
      this.setSessionControlBeanName("blog");
    }
  }

  public void setType(String sType) {
    m_sType = sType;
  }

  public String getType() {
    return m_sType;
  }

  public void setContentInterfaceClass(String sContentInterfaceClass) {
    m_sContentInterfaceClass = sContentInterfaceClass;
  }

  public String getContentInterfaceClass() {
    return m_sContentInterfaceClass;
  }

  public ContentInterface getContentInterface() throws Exception {
    if (m_contentInterface == null) {
      Class contentInterface = Class.forName(this.getContentInterfaceClass());
      m_contentInterface = (ContentInterface) contentInterface.newInstance();
    }

    return m_contentInterface;
  }

  public void setUserRoles(List<String> asUserRoles) {
    m_asUserRoles = asUserRoles;
  }

  public List<String> getUserRoles() {
    return m_asUserRoles;
  }

  public void setSessionControlBeanName(String sSessionControlBeanName) {
    m_sSessionControlBeanName = sSessionControlBeanName;
  }

  public String getSessionControlBeanName() {
    return m_sSessionControlBeanName;
  }
}