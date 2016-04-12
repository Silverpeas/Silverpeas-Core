/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.notification.message;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 08/11/13
 */
public class AbstractMessageTest {

  @Test
  public void aDefaultMessage() {
    Message test = new MessageTest("the content of the default message");
    assertThat(test.getType(), is(MessageType.info));
    assertThat(test.getContent(), is("the content of the default message"));
    assertThat(test.getDisplayLiveTime(), is(0L));
  }

  @Test
  public void aSpecificMessage() {
    Message test = new MessageTest("the content of the specific message").setDisplayLiveTime(100);
    assertThat(test.getType(), is(MessageType.info));
    assertThat(test.getContent(), is("the content of the specific message"));
    assertThat(test.getDisplayLiveTime(), is(100L));
  }

  private class MessageTest extends AbstractMessage {

    protected MessageTest(final String content) {
      super(content);
    }

    @Override
    public MessageType getType() {
      return MessageType.info;
    }
  }
}
