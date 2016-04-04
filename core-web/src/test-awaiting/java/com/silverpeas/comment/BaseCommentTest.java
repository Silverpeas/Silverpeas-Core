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

package com.silverpeas.comment;

import com.silverpeas.jms.JMSTestFacade;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.silverpeas.usernotification.RegisteredTopics.COMMENT_TOPIC;

/**
 * The base class of all of the test cases in which are involved the comment service.
 * It bootstraps the default messaging service used by the notification API and prepares the topic
 * used in the notification of the actions on comments.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseCommentTest {

  private static JMSTestFacade jmsTestFacade;

  @BeforeClass
  public static void boostrapMessagingSystem() throws Exception {
    if (jmsTestFacade == null) {
      jmsTestFacade = new JMSTestFacade();
    }
    jmsTestFacade.bootstrap();
    jmsTestFacade.newTopic(COMMENT_TOPIC.getTopicName());
  }

  public static void shutdownMessagingSystem() throws Exception {
    if (jmsTestFacade != null) {
      jmsTestFacade.shutdown();
    }
  }
}
