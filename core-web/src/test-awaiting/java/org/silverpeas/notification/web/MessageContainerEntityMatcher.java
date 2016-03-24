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
package org.silverpeas.notification.web;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.silverpeas.core.notification.message.Message;
import org.silverpeas.core.notification.message.MessageContainer;

import java.util.Iterator;

/**
 * @author Yohann Chastagnier
 */
public class MessageContainerEntityMatcher extends BaseMatcher<MessageContainerEntity> {

  private final MessageContainer expected;

  protected MessageContainerEntityMatcher(final MessageContainer expected) {
    this.expected = expected;
  }

  @Override
  public void describeTo(final Description description) {
    description.appendValue(expected);
  }

  /*
   * (non-Javadoc)
   * @see org.hamcrest.Matcher#matches(java.lang.Object)
   */
  @Override
  public boolean matches(final Object item) {
    EqualsBuilder match = new EqualsBuilder();
    if (item instanceof MessageContainerEntity) {
      final MessageContainerEntity actual = (MessageContainerEntity) item;
      match.append(actual.getMessages().size(), expected.getMessages().size());
      Iterator<Message> expectedMessageIt = expected.getMessages().iterator();
      for (MessageEntity actualMessage : actual.getMessages()) {
        Message expectedMessage = expectedMessageIt.next();
        match.append(actualMessage.getType(), expectedMessage.getType());
        match.append(actualMessage.getContent(), expectedMessage.getContent());
        match.append(actualMessage.getDisplayLiveTime(), expectedMessage.getDisplayLiveTime());
      }
    }
    return match.isEquals();
  }

  public static MessageContainerEntityMatcher matches(final MessageContainer expected) {
    return new MessageContainerEntityMatcher(expected);
  }
}
