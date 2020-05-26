/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.mail;

import com.icegreen.greenmail.base.GreenMailOperations;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.silverpeas.core.mail.engine.MailSender;
import org.silverpeas.core.mail.engine.MailSenderProvider;
import org.silverpeas.core.mail.engine.MailSenderTask;
import org.silverpeas.core.mail.engine.SmtpMailSender;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.GreenMailExtension;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.SmtpConfig;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@EnableSilverTestEnv
@ExtendWith(LoggerExtension.class)
@ExtendWith(GreenMailExtension.class)
@LoggerLevel(Level.DEBUG)
@SmtpConfig("/org/silverpeas/notificationserver/channel/smtp/smtpSettings.properties")
@Execution(ExecutionMode.SAME_THREAD)
@TestManagedBeans(MailSenderTask.class)
public class SmtpMailSendingTest {

  private final static String COMMON_FROM = "from@titi.org";
  private final static String MALFORMED_FROM = "fromATtiti.org";
  private final static String COMMON_TO = "to@toto.org";
  private final static String MALFORMED_TO = "toATtoto.org";
  private static final String EMPTY_HTML_CONTENT = "<html><body></body></html>";

  private MailSender oldMailSender;

  @BeforeEach
  public void setup() throws Exception {
    // Injecting by reflection the mock instance
    oldMailSender = MailSenderProvider.get();
    FieldUtils.writeDeclaredStaticField(MailSenderProvider.class, "mailSender",
        new StubbedSmtpMailSender(), true);
  }

  @AfterEach
  public void destroy() throws Exception {
    // Replacing by reflection the mock instances by the previous extracted one.
    FieldUtils
        .writeDeclaredStaticField(MailSenderProvider.class, "mailSender", oldMailSender, true);
  }

  @Test
  public void sendingMailSynchronouslyWithDefaultValues(GreenMailOperations mail) throws Exception {
    MailSending mailSending = MailSending.from(null);

    // Verifying data
    assertThat(mailSending.getMailToSend().getFrom(), nullValue());
    assertThat(mailSending.getMailToSend().getTo(), nullValue());
    assertThat(mailSending.getMailToSend().getSubject(), isEmptyString());
    assertThat(mailSending.getMailToSend().getContent().isHtml(), is(true));
    assertThat(mailSending.getMailToSend().getContent().toString(), isEmptyString());
    assertThat(mailSending.getMailToSend().isReplyToRequired(), is(false));
    assertThat(mailSending.getMailToSend().isAsynchronous(), is(true));

    // Sending mail and verifying the result
    sendSynchronouslyAndAssertThatNoSendingDone(mailSending, mail);
  }

  @Test
  public void sendingMailSynchronouslyWithFromNoToAndDefaultValues(GreenMailOperations mail) throws Exception {
    MailAddress email = MailAddress.eMail(COMMON_FROM);
    MailSending mailSending = MailSending.from(email);

    // Verifying data
    assertThat(mailSending.getMailToSend().getFrom(), is(email));
    assertThat(mailSending.getMailToSend().getTo(), nullValue());
    assertThat(mailSending.getMailToSend().getSubject(), isEmptyString());
    assertThat(mailSending.getMailToSend().getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailSending.getMailToSend(), EMPTY_HTML_CONTENT);
    assertThat(mailSending.getMailToSend().isReplyToRequired(), is(false));
    assertThat(mailSending.getMailToSend().isAsynchronous(), is(true));

    // Sending mail and verifying the result
    sendSynchronouslyAndAssertThatNoSendingDone(mailSending, mail);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithToAndDefaultValues(GreenMailOperations mail) throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail);

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), isEmptyString());
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailToSend, EMPTY_HTML_CONTENT);
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void sendingMailSynchronouslyWithMalformedFromWithToAndDefaultValues(GreenMailOperations mail) throws Exception {
    MailAddress senderEmail = MailAddress.eMail(MALFORMED_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail);

    // Verifying data
    assertThat(mailSending.getMailToSend().getFrom(), is(senderEmail));
    assertThat(mailSending.getMailToSend().getTo(), hasItem(receiverEmail));
    assertThat(mailSending.getMailToSend().getSubject(), isEmptyString());
    assertThat(mailSending.getMailToSend().getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailSending.getMailToSend(), EMPTY_HTML_CONTENT);
    assertThat(mailSending.getMailToSend().isReplyToRequired(), is(false));
    assertThat(mailSending.getMailToSend().isAsynchronous(), is(true));

    // Sending mail and verifying the result
    sendSynchronouslyAndAssertThatNoSendingDone(mailSending, mail);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithMalformedToAndDefaultValues(GreenMailOperations mail) throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(MALFORMED_TO);
    MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail);

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), isEmptyString());
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailToSend, EMPTY_HTML_CONTENT);
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithToWithSubjectAndDefaultValues(GreenMailOperations mail) throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    String subject = "A subject";
    MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail).withSubject(subject);

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), is(subject));
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailToSend, EMPTY_HTML_CONTENT);
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithToWithStringContentAndDefaultValues(GreenMailOperations mail)
      throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    String content = "Super content !";
    MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail).withContent(content);

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), isEmptyString());
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailToSend, "<html><body>" + content + "</body></html>");
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void
  sendingMailSynchronouslyWithFromPersonalWithToWithSubjectWithMailContentContentAndDefaultValues(GreenMailOperations mail)
      throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM).withName("From Personal Name");
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    String subject = "A subject";
    MailContent content = MailContent.EMPTY;
    MailSending mailSending =
        MailSending.from(senderEmail).to(receiverEmail).withSubject(subject).withContent(content);

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), is(subject));
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThatContentIsHtmlComputed(mailToSend, EMPTY_HTML_CONTENT);
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void
  sendingMailSynchronouslyWithFromWithToPersonalWithSubjectWithMultipartContentAndDefaultValues(GreenMailOperations mail)
      throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO).withName("To Personal Name");
    String subject = "A subject";
    Multipart content = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent("A content", MimeTypes.HTML_MIME_TYPE);
    content.addBodyPart(mimeBodyPart);
    MailSending mailSending =
        MailSending.from(senderEmail).to(receiverEmail).withSubject(subject).withContent(content);

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), is(subject));
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThat(mailToSend.getContent().toString(), is(content.toString()));
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void sendingMailSynchronouslyValidMailWithReplyTo(GreenMailOperations mail) throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM).withName("From Personal Name");
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO).withName("To Personal Name");
    String subject = "A subject";
    Multipart content = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent("A content", MimeTypes.HTML_MIME_TYPE);
    content.addBodyPart(mimeBodyPart);
    MailSending mailSending =
        MailSending.from(senderEmail).to(receiverEmail).withSubject(subject).withContent(content)
            .setReplyToRequired();

    // Sending mail
    mailSending.sendSynchronously();

    // Verifying sent data
    MailToSend mailToSend = getStubbedSmtpMailSender().currentMailToSend;
    assertThat(mailToSend.getFrom(), is(senderEmail));
    assertThat(mailToSend.getTo(), hasItem(receiverEmail));
    assertThat(mailToSend.getSubject(), is(subject));
    assertThat(mailToSend.getContent().isHtml(), is(true));
    assertThat(mailToSend.getContent().toString(), is(content.toString()));
    assertThat(mailToSend.isReplyToRequired(), is(true));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend, mail);
  }

  @Test
  public void sendingMailAsynchronously(GreenMailOperations mail) {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    MailSending mailSending =
        MailSending.from(senderEmail).to(receiverEmail).withSubject("A subject")
            .withContent("A super content !");

    // Sending mail
    mailSending.send();

    // Verifying immediately that the mail is not yet processed and waiting a moment that it will be
    assertThat(mail.getReceivedMessages(), emptyArray());
    mail.waitForIncomingEmail(60000, 1);

    // Verifying that the mail has been sent
    assertThat(mail.getReceivedMessages(), arrayWithSize(1));
  }

  /*
  Tool methods
   */

  private void assertThatContentIsHtmlComputed(final MailToSend mailToSend,
      final String expectedContent)
      throws IOException, MessagingException {
    assertThat(mailToSend.getContent().getValue(), instanceOf(Multipart.class));
    assertThat(
        ((Multipart) mailToSend.getContent().getValue()).getBodyPart(1).getContent().toString(),
        is(expectedContent));
  }

  private void sendSynchronouslyAndAssertThatNoSendingDone(MailSending mailSending, GreenMailOperations mail) {
    mailSending.sendSynchronously();
    // Verifying sent data
    assertThat(mail.getReceivedMessages(), emptyArray());
  }

  private void assertMailSent(MailToSend verifiedMailToSend, GreenMailOperations mail)
      throws Exception {
    assertThat("assertMailSent is compatible with one receiver only...", verifiedMailToSend.getTo(),
        hasSize(1));

    assertThat(verifiedMailToSend.getTo().getRecipientType().getTechnicalType(),
        is(Message.RecipientType.TO));

    MimeMessage[] messages = mail.getReceivedMessages();
    assertThat(messages, arrayWithSize(1));

    MimeMessage sentMessage = messages[0];
    MailAddress originalReceiverMailAddress = verifiedMailToSend.getTo().iterator().next();

    assertThat(sentMessage.getFrom().length, is(1));
    assertThat(sentMessage.getFrom()[0], instanceOf(InternetAddress.class));
    InternetAddress internetAddressFrom = (InternetAddress) sentMessage.getFrom()[0];
    assertThat(internetAddressFrom.getAddress(), is(verifiedMailToSend.getFrom().getEmail()));
    assertThat(internetAddressFrom.getPersonal(),
        is(StringUtil.defaultStringIfNotDefined(verifiedMailToSend.getFrom().getName(), null)));


    assertThat(sentMessage.getRecipients(Message.RecipientType.TO).length, is(1));
    assertThat(sentMessage.getRecipients(Message.RecipientType.CC), nullValue());
    assertThat(sentMessage.getRecipients(Message.RecipientType.BCC), nullValue());
    assertThat(sentMessage.getRecipients(Message.RecipientType.TO)[0],
        instanceOf(InternetAddress.class));
    InternetAddress internetAddressTo =
        (InternetAddress) sentMessage.getRecipients(Message.RecipientType.TO)[0];
    assertThat(internetAddressTo.getAddress(), is(originalReceiverMailAddress.getEmail()));
    assertThat(internetAddressTo.getPersonal(), nullValue());

    assertThat(sentMessage.getSubject(), is(verifiedMailToSend.getSubject()));
    if (verifiedMailToSend.getContent().getValue() instanceof Multipart) {
      assertThat(sentMessage.getContent(),
          instanceOf(verifiedMailToSend.getContent().getValue().getClass()));
    } else {
      assertThat(sentMessage.getContent().toString().replaceAll("[\n\r]*$", ""),
          is(verifiedMailToSend.getContent().getValue()));
    }

    assertThat(DateUtils.addSeconds(sentMessage.getSentDate(), 10),
        greaterThanOrEqualTo(new Date()));

    assertThat(sentMessage.getReplyTo().length, is(1));
    if (verifiedMailToSend.isReplyToRequired()) {
      assertThat(sentMessage.getHeader("Reply-To"), notNullValue());
      assertThat(sentMessage.getReplyTo()[0], instanceOf(InternetAddress.class));
      InternetAddress internetAddressReplyTo = (InternetAddress) sentMessage.getReplyTo()[0];
      assertThat(internetAddressReplyTo.getAddress(), is(verifiedMailToSend.getFrom().getEmail()));
      assertThat(internetAddressReplyTo.getPersonal(),
          is(StringUtil.defaultStringIfNotDefined(verifiedMailToSend.getFrom().getName(), null)));
    } else {
      assertThat(sentMessage.getHeader("Reply-To"), nullValue());
      assertThat(sentMessage.getReplyTo()[0].toString(), is(internetAddressFrom.toString()));
    }
  }

  private StubbedSmtpMailSender getStubbedSmtpMailSender() {
    return (StubbedSmtpMailSender) MailSenderProvider.get();
  }

  /**
   * Stubbed SMTP mail sender.
   */
  static class StubbedSmtpMailSender extends SmtpMailSender {

    public MailToSend currentMailToSend;

    @Override
    public void send(final MailToSend mail) {
      await().atLeast(2, TimeUnit.MILLISECONDS).untilTrue(new AtomicBoolean(true));
      currentMailToSend = mail;
      super.send(mail);
    }
  }
}