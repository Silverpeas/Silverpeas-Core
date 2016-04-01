/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.mail.engine.MailSender;
import org.silverpeas.core.mail.engine.MailSenderProvider;
import org.silverpeas.core.mail.engine.SmtpMailSender;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.StringUtil;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestSmtpMailSendingMassive {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public GreenMailRule greenMailRule = new GreenMailRule(new SmtpConfigTest());

  private final static String COMMON_FROM = "from@titi.org";
  private final static String COMMON_TO = "to@toto.org";

  private MailSender oldMailSender;
  private long lastLogTime = -1;

  @Before
  public void setup() throws Exception {
    // Injecting by reflection the mock instance
    oldMailSender = MailSenderProvider.get();
    FieldUtils.writeDeclaredStaticField(MailSenderProvider.class, "mailSender",
        new StubbedSmtpMailSender(), true);
  }

  @After
  public void destroy() throws Exception {
    // Replacing by reflection the mock instances by the previous extracted one.
    FieldUtils
        .writeDeclaredStaticField(MailSenderProvider.class, "mailSender", oldMailSender, true);
  }

  @Test
  public void
  sendingSeveralMailsSynchronouslyAndAsynchronouslyAndVerifyingSendingPerformedOneByOne()
      throws Exception {

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
      Thread.sleep(50);
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
    greenMailRule.waitForIncomingEmail(60000, runnables.size());
    Thread.sleep(100);
    assertMailSentOneByOne(runnables);

    MimeMessage[] messages = greenMailRule.getReceivedMessages();
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
      MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail)
          .withSubject(id + " subject - " + (asynch ? "A" : "S")).withContent(id + " content");

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
    System.out
        .println(StringUtil.leftPad(String.valueOf(currentTime - lastLogTime), 6, " ") + " ms -> " +
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
    return (StubbedSmtpMailSender) MailSenderProvider.get();
  }

  /**
   * Stubbed SMTP mail sender.
   */
  class StubbedSmtpMailSender extends SmtpMailSender {

    public List<String> sentOneByOne = new ArrayList<>();

    @Override
    public void send(final MailToSend mail) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException ignored) {
      }
      tagOneByOne("TIC");
      log(mail.getSubject() + " - " + (mail.isAsynchronous() ? "asynchronously" : "synchronously") +
          " - sending...");
      super.send(mail);
      log(mail.getSubject() + " - sent.");
      tagOneByOne("TAC");
    }

    private synchronized void tagOneByOne(String tag) {
      sentOneByOne.add(tag);
    }
  }
}