/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.mail;

import com.icegreen.greenmail.base.GreenMailOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.mail.engine.MailSender;
import org.silverpeas.core.mail.engine.MailSenderTask;
import org.silverpeas.core.mail.engine.SmtpMailSender;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.GreenMailExtension;
import org.silverpeas.core.test.unit.extention.LoggerExtension;
import org.silverpeas.core.test.unit.extention.LoggerLevel;
import org.silverpeas.core.test.unit.extention.SmtpConfig;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.Level;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

@EnableSilverTestEnv
@ExtendWith(LoggerExtension.class)
@ExtendWith(GreenMailExtension.class)
@LoggerLevel(Level.DEBUG)
@SmtpConfig("/org/silverpeas/notificationserver/channel/smtp/smtpSettings.properties")
@Execution(ExecutionMode.SAME_THREAD)
@TestManagedBeans({MailSenderTask.class, RequestTaskManager.class,
    SmtpMailSendingMassiveTest.StubbedSmtpMailSender.class})
class SmtpMailSendingMassiveTest {

  private final static String COMMON_FROM = "from@titi.org";
  private final static String COMMON_TO = "to@toto.org";

  private long lastLogTime = -1;

  @BeforeEach
  void setup() {
    getStubbedSmtpMailSender().setTestInstance(this);
  }

  @SuppressWarnings("JUnitMalformedDeclaration")
  @Test
  void sendingSeveralMailsSynchronouslyAndAsynchronouslyAndVerifyingSendingPerformedOneByOne(
      GreenMailOperations mail) throws Exception {

    List<Runnable> runnables = new ArrayList<>();
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      Runnable runnable =
          new SendOperation("ID_" + StringUtil.leftPad(String.valueOf(i), 3, "0"), (i % 4 != 0));
      runnables.add(runnable);
      threads.add(new Thread(runnable));
    }

    log("STARTING THREADS...");

    // Starting threads
    for (Thread thread : threads) {
      awaitUntil(50, TimeUnit.MILLISECONDS);
      thread.start();
    }

    log(runnables.size() + " THREADS STARTED");
    log("WAITING ENDING OF THREADS...");

    // Waiting for the end of all threads.
    for (Thread thread : threads) {
      thread.join(60000);
    }

    log(runnables.size() + " THREADS STOPPED");

    // Verifying that mails has been sent
    mail.waitForIncomingEmail(60000, runnables.size());
    awaitUntil(100, TimeUnit.MILLISECONDS);
    assertMailSentOneByOne(runnables);

    MimeMessage[] messages = mail.getReceivedMessages();
    assertThat(messages, arrayWithSize(runnables.size()));

    System.out.println("Received messages:");
    for (MimeMessage message : messages) {
      System.out.println("\t" + message.getSubject());
    }
  }

  private class SendOperation implements Runnable {

    private final String id;
    private final boolean asynch;

    private SendOperation(final String id, final boolean asynch) {
      this.id = id;
      this.asynch = asynch;
    }

    @Override
    public void run() {
      MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
      MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
      MailSending mailSending = MailSending.from(senderEmail)
          .to(receiverEmail)
          .withSubject(id + " subject - " + (asynch ? "A" : "S"))
          .withContent(id + " content");

      // Sending mail
      if (asynch) {
        log(id + " - send asynchronously...");
        mailSending.send();
      } else {
        log(id + " - send synchronously...");
        mailSending.sendSynchronously();
      }
    }
  }

  /*
  Tool methods
   */

  private synchronized void log(String message) {
    long currentTime = System.currentTimeMillis();
    if (lastLogTime < 0) {
      lastLogTime = currentTime;
    }
    System.out.println(
        StringUtil.leftPad(String.valueOf(currentTime - lastLogTime), 6, " ") + " ms -> " +
            message);
  }

  private void assertMailSentOneByOne(List<Runnable> runnables) {
    List<String> mailSentOneByOne = getStubbedSmtpMailSender().sentOneByOne;
    assertThat(mailSentOneByOne.size() % 2, is(0));
    assertThat(mailSentOneByOne, hasSize(runnables.size() * 2));
    for (int i = 0; i < mailSentOneByOne.size(); i = i + 2) {
      assertThat(mailSentOneByOne.get(i), is("TIC"));
      assertThat(mailSentOneByOne.get(i + 1), is("TAC"));
    }
  }

  private StubbedSmtpMailSender getStubbedSmtpMailSender() {
    return (StubbedSmtpMailSender) MailSender.get();
  }

  /**
   * Stubbed SMTP mail sender.
   */
  @Service
  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  static class StubbedSmtpMailSender extends SmtpMailSender {

    public List<String> sentOneByOne = new ArrayList<>();
    private SmtpMailSendingMassiveTest testInstance;

    @Override
    public void send(final MailToSend mail) {
      awaitUntil(5, TimeUnit.MILLISECONDS);
      tagOneByOne("TIC");
      testInstance.log(mail.getSubject() + " - " + (mail.isAsynchronous() ? "asynchronously" : "synchronously") +
          " - sending...");
      super.send(mail);
      testInstance.log(mail.getSubject() + " - sent.");
      tagOneByOne("TAC");
    }

    private synchronized void tagOneByOne(String tag) {
      sentOneByOne.add(tag);
    }

    public void setTestInstance(final SmtpMailSendingMassiveTest testInstance) {
      this.testInstance = testInstance;
    }
  }
}