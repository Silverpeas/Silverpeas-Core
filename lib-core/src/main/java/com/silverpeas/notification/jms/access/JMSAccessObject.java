/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static com.silverpeas.notification.jms.access.SilverpeasTopicSubscriber.*;
import static com.silverpeas.notification.jms.access.SilverpeasTopicPublisher.*;

/**
 * An object providing an access to the services of a JMS system and managing the life-cycle of
 * the connections and of the sessions.
 *
 * This object is managed by the IoC container so that it can be injected as dependency into the
 * JMS implementation of the Notification API.
 *
 * This object acts as a facade to the underlying JMS system and provides operations to access the
 * JMS objects. It manages the life-cycle of connections and sessions with the JMS system and wraps
 * some technical details from the JMS consumer/producer operations.
 */
@Named
public final class JMSAccessObject {

  private TopicConnection topicConnection;

  /**
   * The prefix of the JNDI name under which JMS topic will be registered.
   */
  static final String PREFIX_TOPIC_JNDI = "topic/";

  /**
   * Gets the topic corresponding to the specified name.
   * The topic should exist, otherwise an exception is thrown.
   * @param name the topic name.
   * @return the Topic instance matching the name.
   * @throws NamingException if no such topic exists with the specified name.
   */
  public Topic getExistingTopic(final String name) throws NamingException {
    return InitialContext.doLookup(PREFIX_TOPIC_JNDI + name);
  }

  /**
   * Creates a subscription to the specified topic and returns the subscriber resulting of the
   * subscription. The method allocates the required resources for the subscriber receive incoming
   * messages.
   * Once created, the subscriber can be used to set a message listener or to perform additional
   * settings. To unsubscribe from the topic, just call the disposeTopicSubscriber method with
   * the subscriber as parameter.
   * @param topicName the name of topic.
   * @return a TopicSubscriber instance resulting of the topic subscription. For each topic
   * subscription matchs a specific and single TopicSubscriber instance.
   * @throws JMSException if an error occurs while subscribing to the specified topic.
   * @throws NamingException if no such topic exists with the specified name.
   */
  public TopicSubscriber createTopicSubscriber(String topicName) throws JMSException, NamingException {
    Topic topic = getExistingTopic(topicName);
    TopicSession session = getTopicSession();
    SilverpeasTopicSubscriber topicSubscriber = decorateTopicSubscriber(session.createSubscriber(topic));
    topicSubscriber.setSession(session);
    return topicSubscriber;
  }

  /**
   * Disposes the subscription to a topic backed by the specified subscriber. It frees all the
   * resources used by the subscriber.
   * @param subscriber the subscriber matching the subscription to a given topic.
   * @throws JMSException if an error occurs while unsubscribing the subscription.
   */
  public void disposeTopicSubscriber(final TopicSubscriber subscriber) throws JMSException {
    Session session = ((SilverpeasTopicSubscriber) subscriber).getSession();
    releaseSession(session);
  }

  /**
   * Creates a publisher to the speficied topic. The method allocates the required resources for the
   * publisher can send messages to the topic. When the publisher isn't more required for publishing
   * messafges, call the disposeTopicPublisher method to frees the publisher. For each created
   * publisher, one entry to the topic is opened, so that is recommended to frees it after
   * message publishings.
   * @param topicName the name of the topic.
   * @return a TopicSubscriber instance.
   * @throws NamingException if no such topic exists with the specified name.
   * @throws JMSException if an error occurs while creating a message publisher.
   */
  public TopicPublisher createTopicPublisher(String topicName) throws NamingException, JMSException {
    Topic topic = getExistingTopic(topicName);
    TopicSession session = getTopicSession();
    SilverpeasTopicPublisher topicPublisher = decorateTopicPublisher(session.createPublisher(topic));
    topicPublisher.setSession(session);
    return topicPublisher;
  }

  /**
   * Disposes the specified publisher. It cannot then be anymore used. The method frees the resources
   * allocated to the publisher and the entry to the topic is closed.
   * @param publisher the publisher to dispose.
   * @throws JMSException if an error occurs while disposing the publisher.
   */
  public void disposeTopicPublisher(final TopicPublisher publisher) throws JMSException {
    Session session = ((SilverpeasTopicPublisher) publisher).getSession();
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
   * Gets a JMS session for pub/sub operations. Once the pub/sub operations ends to be done, the
   * session should be released with the releaseSession() method.
   * @return a TopicSession instance.
   * @throws JMSException if an error occurs while creating or fetching a JMS session.
   */
  protected TopicSession getTopicSession() throws JMSException {
    return topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  /**
   * Releases an opened sessions. The way the session is released depends on the life-cycle policy.
   * @param session the session to release.
   * @throws JMSException if an error occurs while released the JMS session.
   */
  protected void releaseSession(final Session session) throws JMSException {
    session.close();
  }

  @PostConstruct
  protected void openConnection() throws NamingException, JMSException {
    TopicConnectionFactory connectionFactory = InitialContext.doLookup(JNDINames.JMS_FACTORY);
    topicConnection = connectionFactory.createTopicConnection();
    topicConnection.start();
  }

  @PreDestroy
  protected void closeConnection() throws JMSException {
    topicConnection.stop();
    topicConnection.close();
  }
}
