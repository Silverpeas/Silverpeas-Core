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

package com.silverpeas.comment.service;

import com.silverpeas.jms.JMSTestFacade;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.silverpeas.usernotification.RegisteredTopics.COMMENT_TOPIC;

/**
 * An abstract test case on the comments. It sets up the test context within which a test case on
 * the comments run. All more concrete test cases should extends this abstract class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-comment.xml")
public abstract class AbstractCommentTest {

  private static JMSTestFacade jmsTestFacade;

  @BeforeClass
  public static void boostrapMessagingSystem() throws Exception {
    if (jmsTestFacade == null) {
      jmsTestFacade = new JMSTestFacade();
      jmsTestFacade.bootstrap();
      jmsTestFacade.newTopic(COMMENT_TOPIC.getTopicName());
    }
  }
}
