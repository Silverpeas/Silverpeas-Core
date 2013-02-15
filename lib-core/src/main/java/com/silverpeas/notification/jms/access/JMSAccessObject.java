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

package com.silverpeas.notification.jms.access;

import com.stratelia.webactiv.util.JNDINames;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.silverpeas.notification.jms.access.SilverpeasTopicPublisher
    .decorateTopicPublisher;
import static com.silverpeas.notification.jms.access.SilverpeasTopicSubscriber.decorateTopicSubscriber;

/**
 * An object providing an access to the services of a JMS system and managing the life-cycle of the
 * connections and of the sessions.
 * <p/>
 * This object is managed by the IoC container so that it can be injected as dependency into the
 * JMS
 * implementation of the Notification API.
 * <p/>
 * This object acts as a facade to the underlying JMS system and provides operations to access the
 * JMS objects. It manages the life-cycle of connections and sessions with the JMS system and wraps
 * some technical details from the JMS consumer/producer operations.
 */
@Named
public final class JMSAccessObject {

  private TopicConnection topicConnection;
  // for now, we use two sessions (and thus two threads of control): one for managing the
  // subscriptions, another for managing the publishing.
  private TopicSession subscriptionSession;
  private TopicSession publishingSession;
  // to catch errors and transparently reallocate all pf the lost resources.
  private ExceptionListener exceptionListener = new ConnectionExceptionListener();
  private Set<ConnectionFailureListener> listeners = new HashSet<ConnectionFailureListener>();
  /**
   * The prefix of the JNDI name under which JMS topic will be registered.
   */
  static final String PREFIX_TOPIC_JNDI = "java:/topic/";

  /**
   * Gets the topic corresponding to the specified name. The topic should exist, otherwise an
   * exception is thrown.
   * @param name the topic name.
   * @return the Topic instance matching the name.
   * @throws NamingException if no such topic exists with the specified name.
   */
  public Topic getExistingTopic(final String name) throws NamingException {
    try {
      return InitialContext.doLookup(PREFIX_TOPIC_JNDI + name);
    } catch(NamingException ex) {
      NamingException namingException = new NamingException("No such topic '" + name +
          "' or its name doesn't match its JNDI name according to the pattern '" +
          PREFIX_TOPIC_JNDI + "TOPIC_NAME' with TOPIC_NAME the topic name");
      namingException.initCause(ex);
      throw namingException;
    }
  }

  /**
   * Creates a subscription to the specified topic with the specified listener for receiving the
   * messages published in the topic. The subscription will be uniquely identified by the specified
   * identifier.
   * <p/>
   * A subscription in JMS is represented by a TopicSubscriber instance.
   * <p/>
   * To unsubscribe from the topic, just call the
   * <code>disposeTopicSubscriber</code> method with the TopicSubscriber instance as parameter.
   * @param topicName the name of topic.
   * @param subscriberId the unique identifier of the subscription.
   * @param listener the listener that will receive the messages published in the topic.
   * @return a TopicSubscriber instance resulting of the topic subscription. For each topic
   *         subscription matchs a specific and single TopicSubscriber instance.
   * @throws JMSException if an error occurs while subscribing to the specified topic.
   * @throws NamingException if no such topic exists with the specified name.
   */
  public TopicSubscriber createTopicSubscriber(String topicName, String subscriberId,
      final MessageListener listener) throws JMSException, NamingException {
    Topic topic = getExistingTopic(topicName);
    TopicSession session = getTopicSessionForSubscription();
    SilverpeasTopicSubscriber topicSubscriber =
        decorateTopicSubscriber(session.createSubscriber(topic));
    topicSubscriber.setMessageListener(listener);
    topicSubscriber.setSession(session);
    topicSubscriber.setId(subscriberId);
    return topicSubscriber;
  }

  /**
   * Disposes the subscription to a topic represented by the specified TopicSubscriver instance. It
   * frees all the resources used by the subscriber in JMS.
   * @param subscriber the subscriber matching the subscription to a given topic.
   * @throws JMSException if an error occurs while unsubscribing the subscription.
   */
  public void disposeTopicSubscriber(final TopicSubscriber subscriber) throws JMSException {
    SilverpeasTopicSubscriber silverpeasSubscriber = (SilverpeasTopicSubscriber) subscriber;
    Session session = silverpeasSubscriber.getSession();
    subscriber.close();
    releaseSession(session);
  }

  /**
   * Creates a publisher to the specified topic. The method allocates the required resources for
   * the
   * publisher can send messages to the topic. When the publisher isn't more required for
   * publishing
   * messages, call the disposeTopicPublisher method to frees the publisher. For each created
   * publisher, one entry to the topic is opened, so that is recommended to frees it after message
   * publishing.
   * @param topicName the name of the topic.
   * @return a TopicSubscriber instance.
   * @throws NamingException if no such topic exists with the specified name.
   * @throws JMSException if an error occurs while creating a message publisher.
   */
  public TopicPublisher createTopicPublisher(String topicName)
      throws NamingException, JMSException {
    Topic topic = getExistingTopic(topicName);
    TopicSession session = getTopicSessionForPublishing();
    SilverpeasTopicPublisher topicPublisher =
        decorateTopicPublisher(session.createPublisher(topic));
    topicPublisher.setSession(session);
    return topicPublisher;
  }

  /**
   * Disposes the specified publisher. It cannot then be anymore used. The method frees the
   * resources allocated to the publisher and the entry to the topic is closed.
   * @param publisher the publisher to dispose.
   * @throws JMSException if an error occurs while disposing the publisher.
   */
  public void disposeTopicPublisher(final TopicPublisher publisher) throws JMSException {
    Session session = ((SilverpeasTopicPublisher) publisher).getSession();
    publisher.close();
    releaseSession(session);
  }

  /**
   * Creates a message ready to be sent by the specified publisher. The message is created within
   * the session to which the specified publisher stands.
   * @param publisher the publisher from which the session with the JMS system can be retreived.
   * @return an ObjectMessage instance.
   * @throws JMSException if an error occurs while creating a JMS message.
   */
  public ObjectMessage createObjectMessageFor(final TopicPublisher publisher) throws JMSException {
    SilverpeasTopicPublisher topicPublisher = (SilverpeasTopicPublisher) publisher;
    Session session = topicPublisher.getSession();
    return session.createObjectMessage();
  }

  /**
   * Adds a listener of JMS exceptions that will be occur between the client and the server.
   * The specified listener will be informed about such exceptions so that it will be do specific
   * actions.
   * @param listener a listener of exceptions on a JMS connection.
   */
  public void addConnectionFailureListener(final ConnectionFailureListener listener) {
    listeners.add(listener);
  }

  /**
   * Gets a JMS session for publishing operations. Once the publishing operations ends to be done,
   * the session should be released with the releaseSession() method.
   * @return a TopicSession instance.
   * @throws JMSException if an error occurs while creating or fetching a JMS session.
   */
  protected TopicSession getTopicSessionForPublishing() throws JMSException {
    return publishingSession;
  }

  /**
   * Gets a JMS session for subscription operations. Once the subscription operations ends to be
   * done, the session should be released with the releaseSession() method.
   * @return a TopicSession instance.
   * @throws JMSException if an error occurs while creating or fetching a JMS session.
   */
  protected TopicSession getTopicSessionForSubscription() throws JMSException {
    return subscriptionSession;
  }

  /**
   * Releases an opened sessions. The way the session is released depends on the life-cycle policy.
   * @param session the session to release.
   * @throws JMSException if an error occurs while released the JMS session.
   */
  protected void releaseSession(final Session session) throws JMSException {
    if (subscriptionSession != session && publishingSession != session) {
      session.close();
    }
  }

  @PostConstruct
  protected void openConnection() throws NamingException, JMSException {
    TopicConnectionFactory connectionFactory = InitialContext.doLookup(JNDINames.NOTIF_API_JMS);
    topicConnection = connectionFactory.createTopicConnection();
    topicConnection.setClientID("Silverpeas");
    topicConnection.setExceptionListener(exceptionListener);
    topicConnection.start();
    publishingSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    subscriptionSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  @PreDestroy
  protected void closeConnection() throws JMSException {
    try {
      subscriptionSession.close();
      publishingSession.close();
      topicConnection.stop();
      topicConnection.close();
    } finally {
      subscriptionSession = null;
      publishingSession = null;
    }
  }

  protected Set<ConnectionFailureListener> getConnectionFailureListeners() {
    return listeners;
  }

  public class ConnectionExceptionListener implements ExceptionListener {

    @Override
    public void onException(JMSException jmse) {
      Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING,
          "The connection with the remote JMS server was unexpectedly closed! I'm going to reopen it.");
      try {
        closeConnection();
      } catch (Exception ex) {
        Logger.getLogger(JMSAccessObject.class.getName()).log(Level.SEVERE, ex.getMessage());
      } finally {
        try {
          openConnection();
          informListeners();
        } catch (Exception ex) {
          Logger.getLogger(JMSAccessObject.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
      }
    }

    private void informListeners() {
      for (ConnectionFailureListener listener: getConnectionFailureListeners()) {
        listener.onConnectionFailure();
      }
    }
  }
}
