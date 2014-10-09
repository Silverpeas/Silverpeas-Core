/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.notification;

import org.silverpeas.util.exception.DecodingException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mmoquillon
 */
public abstract class JMSResourceEventListener<T extends ResourceEvent> implements MessageListener {

  protected final Logger logger = Logger.getLogger(getClass().getSimpleName());

  /**
   * Decodes the event embbeded into the text of the specified message.
   * @param message the text message into which is encoded the event.
   * @return the event.
   * @throws JMSException if an error occurs while reading the text of the message.
   * @throws org.silverpeas.util.exception.DecodingException if an error occurs while decoding the
   * event from the text of the message.
   */
  protected abstract T decodeResourceEventFrom(final TextMessage message) throws JMSException,
      DecodingException;

  /**
   * Should JMS retry to replay the message to the listener at failure? By default returns false.
   * <p>
   * If true, a RuntimeException exception will be thrown if an exception is catched from the
   * message consummation, provoking JMS to replay the message to this listener. If false, only
   * an error level log will output.
   * </p>
   * @return true if the message should be replay by JMS to this listener, false otherwise.
   */
  protected boolean retryAtFailure() {
    return false;
  }

  /**
   * An event on the deletion of a resource has be listened. By default, this method does nothing.
   * @param event the event on the deletion of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public void onDeletion(final T event) throws Exception {
  }

  /**
   * An event on the update of a resource has be listened. By default, this method does nothing.
   * @param event the event on the update of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public void onUpdate(final T event) throws Exception {
  }

  /**
   * An event on the creation of a resource has be listened. By default, this method does nothing.
   * @param event the event on the creation of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public void onCreation(final T event) throws Exception{
  }

  /**
   * Listens for events related to a resource managed in Silverpeas.
   * <p>
   *   The event is decoded from the specified message and according to the type of the event,
   *   the adequate method is then invoked (
   *   {@code org.silverpeas.notification.JMSResourceEventListener#onCreation},
   *   {@code org.silverpeas.notification.JMSResourceEventListener#onUpdate},
   *   {@code org.silverpeas.notification.JMSResourceEventListener#onDeletion}).
   * </p>
   * @param message the notification message in which is encoded the event. The message must be
   * a {@code javax.jms.TextMessage} instance.
   * @throws java.lang.RuntimeException if an error occurs while treating the event.
   */
  @Override
  public void onMessage(final Message message) {
    TextMessage notification;
    try {
      if (message instanceof TextMessage) {
        notification = (TextMessage) message;
        T event = decodeResourceEventFrom(notification);
        switch (event.getType()) {
          case CREATION:
            onCreation(event);
            break;
          case UPDATE:
            onUpdate(event);
            break;
          case DELETION:
            onDeletion(event);
            break;
          default:
            logger.log(Level.WARNING, "Event type {0} not yet supported", event.getType());
            break;
        }
      } else {
        logger.log(Level.WARNING, "Invalid event notification received");
      }
    } catch (Exception e) {
      if (retryAtFailure()) {
        throw new RuntimeException(e);
      } else {
        logger.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
