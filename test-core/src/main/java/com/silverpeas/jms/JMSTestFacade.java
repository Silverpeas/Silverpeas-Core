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

package com.silverpeas.jms;

import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.jms.MessageManager;
import com.mockrunner.mock.jms.MockMessage;
import com.mockrunner.mock.jms.MockTopic;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import java.io.IOException;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A facade object on the JMS system dedicated to tests.
 */
public class JMSTestFacade extends JMSTestCaseAdapter {

  /**
   * The name of the topic created by default for testing purpose.
   */
  public static final String DEFAULT_TOPIC = "toto";
  public static final String TOPIC_PREFIX = "java:/topic/";
  public static String JMS_FACTORY =
      "com.stratelia.silverpeas.notificationserver.jms.QueueConnectionFactory";
  private MockTopic topic;

  /**
   * Bootstraps a mocked JMS system.
   * @throws Exception if the boostrapping failed.
   */
  public void bootstrap() throws Exception {
    super.setUp();
    prepareJndi();
    Context context = getInitialContext();
    context.rebind(JMS_FACTORY, getJMSMockObjectFactory().createMockConnectionFactory());
    topic = (MockTopic) newTopic(DEFAULT_TOPIC);
  }

  /**
   * Creates a new topic with the specified name. The topic will be registered in the mocked JNDI
   * under the JNDI name "topic/"topicName.
   * @param topicName the name of the topic.
   * @return the new topic.
   * @throws Exception if an error occurs while creating the new topic.
   */
  public Topic newTopic(String topicName) throws Exception {
    Topic newTopic = getDestinationManager().createTopic(topicName);
    Context context = getInitialContext();
    context.rebind(TOPIC_PREFIX + topicName, newTopic);
    return newTopic;
  }

  /**
   * Shutdowns the mocked JMS system. Warning, shutdowning the messaging system within a runtime in
   * which several different test cases will be run can break theses tests cases as the JMS system
   * can be shared among theses test cases.
   * @throws Exception if the shutdown failed.
   */
  public void shutdown() throws Exception {
    super.tearDown();
    cleanJndi();
  }

  /**
   * Gets the default JMS topic.
   * @return
   */
  public Topic getTopic() {
    return topic;
  }

  @Override
  public MockTopic getTopic(String string) {
    return super.getTopic(string);
  }

  private void prepareJndi() throws IOException {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  private void cleanJndi() throws IOException {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  private Context getInitialContext() throws NamingException {
    return new InitialContext();
  }

  @Override
  public MessageManager getTopicMessageManager(int i) {
    return super.getTopicMessageManager(i);
  }

  @Override
  public void verifyAllDurableTopicSubscribersClosed(int i) {
    super.verifyAllDurableTopicSubscribersClosed(i);
  }

  @Override
  public void verifyAllMessageConsumersClosed(int i) {
    super.verifyAllMessageConsumersClosed(i);
  }

  @Override
  public void verifyAllMessageProducersClosed(int i) {
    super.verifyAllMessageProducersClosed(i);
  }

  @Override
  public void verifyAllQueueBrowsersClosed(int i) {
    super.verifyAllQueueBrowsersClosed(i);
  }

  @Override
  public void verifyAllQueueReceiversClosed(int i) {
    super.verifyAllQueueReceiversClosed(i);
  }

  @Override
  public void verifyAllQueueSendersClosed(int i) {
    super.verifyAllQueueSendersClosed(i);
  }

  @Override
  public void verifyAllQueueSessionsClosed() {
    super.verifyAllQueueSessionsClosed();
  }

  @Override
  public void verifyAllQueueSessionsCommitted() {
    super.verifyAllQueueSessionsCommitted();
  }

  @Override
  public void verifyAllQueueSessionsRecovered() {
    super.verifyAllQueueSessionsRecovered();
  }

  @Override
  public void verifyAllQueueSessionsRolledBack() {
    super.verifyAllQueueSessionsRolledBack();
  }

  @Override
  public void verifyAllReceivedQueueMessagesAcknowledged(String string) {
    super.verifyAllReceivedQueueMessagesAcknowledged(string);
  }

  @Override
  public void verifyAllReceivedQueueMessagesAcknowledged(int i, int i1) {
    super.verifyAllReceivedQueueMessagesAcknowledged(i, i1);
  }

  @Override
  public void verifyAllReceivedTopicMessagesAcknowledged(String string) {
    super.verifyAllReceivedTopicMessagesAcknowledged(string);
  }

  @Override
  public void verifyAllReceivedTopicMessagesAcknowledged(int i, int i1) {
    super.verifyAllReceivedTopicMessagesAcknowledged(i, i1);
  }

  @Override
  public void verifyAllSessionsClosed() {
    super.verifyAllSessionsClosed();
  }

  @Override
  public void verifyAllSessionsCommitted() {
    super.verifyAllSessionsCommitted();
  }

  @Override
  public void verifyAllSessionsRecovered() {
    super.verifyAllSessionsRecovered();
  }

  @Override
  public void verifyAllSessionsRolledBack() {
    super.verifyAllSessionsRolledBack();
  }

  @Override
  public void verifyAllTemporaryQueuesDeleted(int i) {
    super.verifyAllTemporaryQueuesDeleted(i);
  }

  @Override
  public void verifyAllTemporaryTopicsDeleted(int i) {
    super.verifyAllTemporaryTopicsDeleted(i);
  }

  @Override
  public void verifyAllTopicPublishersClosed(int i) {
    super.verifyAllTopicPublishersClosed(i);
  }

  @Override
  public void verifyAllTopicSessionsClosed() {
    super.verifyAllTopicSessionsClosed();
  }

  @Override
  public void verifyAllTopicSessionsCommitted() {
    super.verifyAllTopicSessionsCommitted();
  }

  @Override
  public void verifyAllTopicSessionsRecovered() {
    super.verifyAllTopicSessionsRecovered();
  }

  @Override
  public void verifyAllTopicSessionsRolledBack() {
    super.verifyAllTopicSessionsRolledBack();
  }

  @Override
  public void verifyAllTopicSubscribersClosed(int i) {
    super.verifyAllTopicSubscribersClosed(i);
  }

  @Override
  public void verifyConnectionClosed() {
    super.verifyConnectionClosed();
  }

  @Override
  public void verifyConnectionStarted() {
    super.verifyConnectionStarted();
  }

  @Override
  public void verifyConnectionStopped() {
    super.verifyConnectionStopped();
  }

  @Override
  public void verifyCreatedBytesMessageAcknowledged(int i, int i1) {
    super.verifyCreatedBytesMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedBytesMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedBytesMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedMapMessageAcknowledged(int i, int i1) {
    super.verifyCreatedMapMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedMapMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedMapMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedMessageAcknowledged(int i, int i1) {
    super.verifyCreatedMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedObjectMessageAcknowledged(int i, int i1) {
    super.verifyCreatedObjectMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedObjectMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedObjectMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueBytesMessageAcknowledged(int i, int i1) {
    super.verifyCreatedQueueBytesMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueBytesMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedQueueBytesMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueMapMessageAcknowledged(int i, int i1) {
    super.verifyCreatedQueueMapMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueMapMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedQueueMapMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueMessageAcknowledged(int i, int i1) {
    super.verifyCreatedQueueMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedQueueMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueObjectMessageAcknowledged(int i, int i1) {
    super.verifyCreatedQueueObjectMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueObjectMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedQueueObjectMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueStreamMessageAcknowledged(int i, int i1) {
    super.verifyCreatedQueueStreamMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueStreamMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedQueueStreamMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueTextMessageAcknowledged(int i, int i1) {
    super.verifyCreatedQueueTextMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedQueueTextMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedQueueTextMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedStreamMessageAcknowledged(int i, int i1) {
    super.verifyCreatedStreamMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedStreamMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedStreamMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTextMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTextMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTextMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTextMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicBytesMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTopicBytesMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicBytesMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTopicBytesMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicMapMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTopicMapMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicMapMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTopicMapMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTopicMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTopicMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicObjectMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTopicObjectMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicObjectMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTopicObjectMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicStreamMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTopicStreamMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicStreamMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTopicStreamMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicTextMessageAcknowledged(int i, int i1) {
    super.verifyCreatedTopicTextMessageAcknowledged(i, i1);
  }

  @Override
  public void verifyCreatedTopicTextMessageNotAcknowledged(int i, int i1) {
    super.verifyCreatedTopicTextMessageNotAcknowledged(i, i1);
  }

  @Override
  public void verifyCurrentQueueMessageEquals(String string, int i, MockMessage mm) {
    super.verifyCurrentQueueMessageEquals(string, i, mm);
  }

  @Override
  public void verifyCurrentQueueMessageEquals(int i, int i1, int i2, MockMessage mm) {
    super.verifyCurrentQueueMessageEquals(i, i1, i2, mm);
  }

  @Override
  public void verifyCurrentTopicMessageEquals(int i, int i1, int i2, MockMessage mm) {
    super.verifyCurrentTopicMessageEquals(i, i1, i2, mm);
  }

  @Override
  public void verifyCurrentTopicMessageEquals(String string, int i, MockMessage mm) {
    super.verifyCurrentTopicMessageEquals(string, i, mm);
  }

  @Override
  public void verifyDurableTopicSubscriberClosed(int i, String string) {
    super.verifyDurableTopicSubscriberClosed(i, string);
  }

  @Override
  public void verifyDurableTopicSubscriberPresent(int i, String string) {
    super.verifyDurableTopicSubscriberPresent(i, string);
  }

  @Override
  public void verifyMessageEquals(MockMessage mm, MockMessage mm1) {
    super.verifyMessageEquals(mm, mm1);
  }

  @Override
  public void verifyNumberDurableTopicSubscribers(int i, String string, int i1) {
    super.verifyNumberDurableTopicSubscribers(i, string, i1);
  }

  @Override
  public void verifyNumberDurableTopicSubscribers(int i, int i1) {
    super.verifyNumberDurableTopicSubscribers(i, i1);
  }

  @Override
  public void verifyNumberMessageConsumers(int i, int i1) {
    super.verifyNumberMessageConsumers(i, i1);
  }

  @Override
  public void verifyNumberMessageProducers(int i, int i1) {
    super.verifyNumberMessageProducers(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedBytesMessages(int i, int i1) {
    super.verifyNumberOfCreatedBytesMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedMapMessages(int i, int i1) {
    super.verifyNumberOfCreatedMapMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedMessages(int i, int i1) {
    super.verifyNumberOfCreatedMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedObjectMessages(int i, int i1) {
    super.verifyNumberOfCreatedObjectMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedQueueBytesMessages(int i, int i1) {
    super.verifyNumberOfCreatedQueueBytesMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedQueueMapMessages(int i, int i1) {
    super.verifyNumberOfCreatedQueueMapMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedQueueMessages(int i, int i1) {
    super.verifyNumberOfCreatedQueueMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedQueueObjectMessages(int i, int i1) {
    super.verifyNumberOfCreatedQueueObjectMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedQueueStreamMessages(int i, int i1) {
    super.verifyNumberOfCreatedQueueStreamMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedQueueTextMessages(int i, int i1) {
    super.verifyNumberOfCreatedQueueTextMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedStreamMessages(int i, int i1) {
    super.verifyNumberOfCreatedStreamMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTextMessages(int i, int i1) {
    super.verifyNumberOfCreatedTextMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTopicBytesMessages(int i, int i1) {
    super.verifyNumberOfCreatedTopicBytesMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTopicMapMessages(int i, int i1) {
    super.verifyNumberOfCreatedTopicMapMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTopicMessages(int i, int i1) {
    super.verifyNumberOfCreatedTopicMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTopicObjectMessages(int i, int i1) {
    super.verifyNumberOfCreatedTopicObjectMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTopicStreamMessages(int i, int i1) {
    super.verifyNumberOfCreatedTopicStreamMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCreatedTopicTextMessages(int i, int i1) {
    super.verifyNumberOfCreatedTopicTextMessages(i, i1);
  }

  @Override
  public void verifyNumberOfCurrentQueueMessages(int i, int i1, int i2) {
    super.verifyNumberOfCurrentQueueMessages(i, i1, i2);
  }

  @Override
  public void verifyNumberOfCurrentQueueMessages(String string, int i) {
    super.verifyNumberOfCurrentQueueMessages(string, i);
  }

  @Override
  public void verifyNumberOfCurrentTopicMessages(int i, int i1, int i2) {
    super.verifyNumberOfCurrentTopicMessages(i, i1, i2);
  }

  @Override
  public void verifyNumberOfCurrentTopicMessages(String string, int i) {
    super.verifyNumberOfCurrentTopicMessages(string, i);
  }

  @Override
  public void verifyNumberOfReceivedQueueMessages(String string, int i) {
    super.verifyNumberOfReceivedQueueMessages(string, i);
  }

  @Override
  public void verifyNumberOfReceivedQueueMessages(int i, int i1, int i2) {
    super.verifyNumberOfReceivedQueueMessages(i, i1, i2);
  }

  @Override
  public void verifyNumberOfReceivedTopicMessages(int i, int i1, int i2) {
    super.verifyNumberOfReceivedTopicMessages(i, i1, i2);
  }

  @Override
  public void verifyNumberOfReceivedTopicMessages(String string, int i) {
    super.verifyNumberOfReceivedTopicMessages(string, i);
  }

  @Override
  public void verifyNumberQueueBrowsers(int i, String string, int i1) {
    super.verifyNumberQueueBrowsers(i, string, i1);
  }

  @Override
  public void verifyNumberQueueBrowsers(int i, int i1) {
    super.verifyNumberQueueBrowsers(i, i1);
  }

  @Override
  public void verifyNumberQueueReceivers(int i, int i1) {
    super.verifyNumberQueueReceivers(i, i1);
  }

  @Override
  public void verifyNumberQueueReceivers(int i, String string, int i1) {
    super.verifyNumberQueueReceivers(i, string, i1);
  }

  @Override
  public void verifyNumberQueueSenders(int i, String string, int i1) {
    super.verifyNumberQueueSenders(i, string, i1);
  }

  @Override
  public void verifyNumberQueueSenders(int i, int i1) {
    super.verifyNumberQueueSenders(i, i1);
  }

  @Override
  public void verifyNumberQueueSessions(int i) {
    super.verifyNumberQueueSessions(i);
  }

  @Override
  public void verifyNumberSessions(int i) {
    super.verifyNumberSessions(i);
  }

  @Override
  public void verifyNumberTemporaryQueues(int i, int i1) {
    super.verifyNumberTemporaryQueues(i, i1);
  }

  @Override
  public void verifyNumberTemporaryTopics(int i, int i1) {
    super.verifyNumberTemporaryTopics(i, i1);
  }

  @Override
  public void verifyNumberTopicPublishers(int i, String string, int i1) {
    super.verifyNumberTopicPublishers(i, string, i1);
  }

  @Override
  public void verifyNumberTopicPublishers(int i, int i1) {
    super.verifyNumberTopicPublishers(i, i1);
  }

  @Override
  public void verifyNumberTopicSessions(int i) {
    super.verifyNumberTopicSessions(i);
  }

  @Override
  public void verifyNumberTopicSubscribers(int i, int i1) {
    super.verifyNumberTopicSubscribers(i, i1);
  }

  @Override
  public void verifyNumberTopicSubscribers(int i, String string, int i1) {
    super.verifyNumberTopicSubscribers(i, string, i1);
  }

  @Override
  public void verifyQueueBrowserClosed(int i, String string, int i1) {
    super.verifyQueueBrowserClosed(i, string, i1);
  }

  @Override
  public void verifyQueueConnectionClosed() {
    super.verifyQueueConnectionClosed();
  }

  @Override
  public void verifyQueueConnectionStarted() {
    super.verifyQueueConnectionStarted();
  }

  @Override
  public void verifyQueueConnectionStopped() {
    super.verifyQueueConnectionStopped();
  }

  @Override
  public void verifyQueueReceiverClosed(int i, String string, int i1) {
    super.verifyQueueReceiverClosed(i, string, i1);
  }

  @Override
  public void verifyQueueSenderClosed(int i, String string, int i1) {
    super.verifyQueueSenderClosed(i, string, i1);
  }

  @Override
  public void verifyQueueSessionClosed(int i) {
    super.verifyQueueSessionClosed(i);
  }

  @Override
  public void verifyQueueSessionCommitted(int i) {
    super.verifyQueueSessionCommitted(i);
  }

  @Override
  public void verifyQueueSessionNotCommitted(int i) {
    super.verifyQueueSessionNotCommitted(i);
  }

  @Override
  public void verifyQueueSessionNotRecovered(int i) {
    super.verifyQueueSessionNotRecovered(i);
  }

  @Override
  public void verifyQueueSessionNotRolledBack(int i) {
    super.verifyQueueSessionNotRolledBack(i);
  }

  @Override
  public void verifyQueueSessionNumberCommits(int i, int i1) {
    super.verifyQueueSessionNumberCommits(i, i1);
  }

  @Override
  public void verifyQueueSessionNumberRollbacks(int i, int i1) {
    super.verifyQueueSessionNumberRollbacks(i, i1);
  }

  @Override
  public void verifyQueueSessionRecovered(int i) {
    super.verifyQueueSessionRecovered(i);
  }

  @Override
  public void verifyQueueSessionRolledBack(int i) {
    super.verifyQueueSessionRolledBack(i);
  }

  @Override
  public void verifyReceivedQueueMessageAcknowledged(String string, int i) {
    super.verifyReceivedQueueMessageAcknowledged(string, i);
  }

  @Override
  public void verifyReceivedQueueMessageAcknowledged(int i, int i1, int i2) {
    super.verifyReceivedQueueMessageAcknowledged(i, i1, i2);
  }

  @Override
  public void verifyReceivedQueueMessageEquals(String string, int i, MockMessage mm) {
    super.verifyReceivedQueueMessageEquals(string, i, mm);
  }

  @Override
  public void verifyReceivedQueueMessageEquals(int i, int i1, int i2, MockMessage mm) {
    super.verifyReceivedQueueMessageEquals(i, i1, i2, mm);
  }

  @Override
  public void verifyReceivedQueueMessageNotAcknowledged(int i, int i1, int i2) {
    super.verifyReceivedQueueMessageNotAcknowledged(i, i1, i2);
  }

  @Override
  public void verifyReceivedQueueMessageNotAcknowledged(String string, int i) {
    super.verifyReceivedQueueMessageNotAcknowledged(string, i);
  }

  @Override
  public void verifyReceivedTopicMessageAcknowledged(String string, int i) {
    super.verifyReceivedTopicMessageAcknowledged(string, i);
  }

  @Override
  public void verifyReceivedTopicMessageAcknowledged(int i, int i1, int i2) {
    super.verifyReceivedTopicMessageAcknowledged(i, i1, i2);
  }

  @Override
  public void verifyReceivedTopicMessageEquals(String string, int i, MockMessage mm) {
    super.verifyReceivedTopicMessageEquals(string, i, mm);
  }

  @Override
  public void verifyReceivedTopicMessageEquals(int i, int i1, int i2, MockMessage mm) {
    super.verifyReceivedTopicMessageEquals(i, i1, i2, mm);
  }

  @Override
  public void verifyReceivedTopicMessageNotAcknowledged(int i, int i1, int i2) {
    super.verifyReceivedTopicMessageNotAcknowledged(i, i1, i2);
  }

  @Override
  public void verifyReceivedTopicMessageNotAcknowledged(String string, int i) {
    super.verifyReceivedTopicMessageNotAcknowledged(string, i);
  }

  @Override
  public void verifySessionClosed(int i) {
    super.verifySessionClosed(i);
  }

  @Override
  public void verifySessionCommitted(int i) {
    super.verifySessionCommitted(i);
  }

  @Override
  public void verifySessionNotCommitted(int i) {
    super.verifySessionNotCommitted(i);
  }

  @Override
  public void verifySessionNotRecovered(int i) {
    super.verifySessionNotRecovered(i);
  }

  @Override
  public void verifySessionNotRolledBack(int i) {
    super.verifySessionNotRolledBack(i);
  }

  @Override
  public void verifySessionNumberCommits(int i, int i1) {
    super.verifySessionNumberCommits(i, i1);
  }

  @Override
  public void verifySessionNumberRollbacks(int i, int i1) {
    super.verifySessionNumberRollbacks(i, i1);
  }

  @Override
  public void verifySessionRecovered(int i) {
    super.verifySessionRecovered(i);
  }

  @Override
  public void verifySessionRolledBack(int i) {
    super.verifySessionRolledBack(i);
  }

  @Override
  public void verifyTemporaryQueueDeleted(int i, int i1) {
    super.verifyTemporaryQueueDeleted(i, i1);
  }

  @Override
  public void verifyTemporaryTopicDeleted(int i, int i1) {
    super.verifyTemporaryTopicDeleted(i, i1);
  }

  @Override
  public void verifyTopicConnectionClosed() {
    super.verifyTopicConnectionClosed();
  }

  @Override
  public void verifyTopicConnectionStarted() {
    super.verifyTopicConnectionStarted();
  }

  @Override
  public void verifyTopicConnectionStopped() {
    super.verifyTopicConnectionStopped();
  }

  @Override
  public void verifyTopicPublisherClosed(int i, String string, int i1) {
    super.verifyTopicPublisherClosed(i, string, i1);
  }

  @Override
  public void verifyTopicSessionClosed(int i) {
    super.verifyTopicSessionClosed(i);
  }

  @Override
  public void verifyTopicSessionCommitted(int i) {
    super.verifyTopicSessionCommitted(i);
  }

  @Override
  public void verifyTopicSessionNotCommitted(int i) {
    super.verifyTopicSessionNotCommitted(i);
  }

  @Override
  public void verifyTopicSessionNotRecovered(int i) {
    super.verifyTopicSessionNotRecovered(i);
  }

  @Override
  public void verifyTopicSessionNotRolledBack(int i) {
    super.verifyTopicSessionNotRolledBack(i);
  }

  @Override
  public void verifyTopicSessionNumberCommits(int i, int i1) {
    super.verifyTopicSessionNumberCommits(i, i1);
  }

  @Override
  public void verifyTopicSessionNumberRollbacks(int i, int i1) {
    super.verifyTopicSessionNumberRollbacks(i, i1);
  }

  @Override
  public void verifyTopicSessionRecovered(int i) {
    super.verifyTopicSessionRecovered(i);
  }

  @Override
  public void verifyTopicSessionRolledBack(int i) {
    super.verifyTopicSessionRolledBack(i);
  }

  @Override
  public void verifyTopicSubscriberClosed(int i, String string, int i1) {
    super.verifyTopicSubscriberClosed(i, string, i1);
  }
}
