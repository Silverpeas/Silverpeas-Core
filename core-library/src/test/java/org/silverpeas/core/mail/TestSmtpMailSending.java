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
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.mail.engine.MailSender;
import org.silverpeas.core.mail.engine.MailSenderProvider;
import org.silverpeas.core.mail.engine.SmtpMailSender;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestSmtpMailSending {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public GreenMailRule greenMailRule = new GreenMailRule(new SmtpConfigTest());

  private final static String COMMON_FROM = "from@titi.org";
  private final static String MALFORMED_FROM = "fromATtiti.org";
  private final static String COMMON_TO = "to@toto.org";
  private final static String MALFORMED_TO = "toATtoto.org";

  private MailSender oldMailSender;

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
  public void sendingMailSynchronouslyWithDefaultValues() throws Exception {
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
    sendSynchronouslyAndAssertThatNoSendingDone(mailSending);
  }

  @Test
  public void sendingMailSynchronouslyWithFromNoToAndDefaultValues() throws Exception {
    MailAddress email = MailAddress.eMail(COMMON_FROM);
    MailSending mailSending = MailSending.from(email);

    // Verifying data
    assertThat(mailSending.getMailToSend().getFrom(), is(email));
    assertThat(mailSending.getMailToSend().getTo(), nullValue());
    assertThat(mailSending.getMailToSend().getSubject(), isEmptyString());
    assertThat(mailSending.getMailToSend().getContent().isHtml(), is(true));
    assertThat(mailSending.getMailToSend().getContent().toString(), isEmptyString());
    assertThat(mailSending.getMailToSend().isReplyToRequired(), is(false));
    assertThat(mailSending.getMailToSend().isAsynchronous(), is(true));

    // Sending mail and verifying the result
    sendSynchronouslyAndAssertThatNoSendingDone(mailSending);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithToAndDefaultValues() throws Exception {
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
    assertThat(mailToSend.getContent().toString(), isEmptyString());
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend);
  }

  @Test
  public void sendingMailSynchronouslyWithMalformedFromWithToAndDefaultValues() throws Exception {
    MailAddress senderEmail = MailAddress.eMail(MALFORMED_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    MailSending mailSending = MailSending.from(senderEmail).to(receiverEmail);

    // Verifying data
    assertThat(mailSending.getMailToSend().getFrom(), is(senderEmail));
    assertThat(mailSending.getMailToSend().getTo(), hasItem(receiverEmail));
    assertThat(mailSending.getMailToSend().getSubject(), isEmptyString());
    assertThat(mailSending.getMailToSend().getContent().isHtml(), is(true));
    assertThat(mailSending.getMailToSend().getContent().toString(), isEmptyString());
    assertThat(mailSending.getMailToSend().isReplyToRequired(), is(false));
    assertThat(mailSending.getMailToSend().isAsynchronous(), is(true));

    // Sending mail and verifying the result
    sendSynchronouslyAndAssertThatNoSendingDone(mailSending);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithMalformedToAndDefaultValues() throws Exception {
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
    assertThat(mailToSend.getContent().toString(), isEmptyString());
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithToWithSubjectAndDefaultValues() throws Exception {
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
    assertThat(mailToSend.getContent().toString(), isEmptyString());
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend);
  }

  @Test
  public void sendingMailSynchronouslyWithFromWithToWithStringContentAndDefaultValues()
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
    assertThat(mailToSend.getContent().toString(), is(content));
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend);
  }

  @Test
  public void
  sendingMailSynchronouslyWithFromPersonalWithToWithSubjectWithMailContentContentAndDefaultValues()
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
    assertThat(mailToSend.getContent().toString(), isEmptyString());
    assertThat(mailToSend.isReplyToRequired(), is(false));
    assertThat(mailToSend.isAsynchronous(), is(false));
    assertMailSent(mailToSend);
  }

  @Test
  public void
  sendingMailSynchronouslyWithFromWithToPersonalWithSubjectWithMultipartContentAndDefaultValues()
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
    assertMailSent(mailToSend);
  }

  @Test
  public void sendingMailSynchronouslyValidMailWithReplyTo() throws Exception {
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
    assertMailSent(mailToSend);
  }

  @Test
  public void sendingMailAsynchronously() throws Exception {
    MailAddress senderEmail = MailAddress.eMail(COMMON_FROM);
    MailAddress receiverEmail = MailAddress.eMail(COMMON_TO);
    MailSending mailSending =
        MailSending.from(senderEmail).to(receiverEmail).withSubject("A subject")
            .withContent("A super content !");

    // Sending mail
    mailSending.send();

    // Verifying immediately that the mail is not yet processed and waiting a moment that it will be
    assertThat(greenMailRule.getReceivedMessages(), emptyArray());
    greenMailRule.waitForIncomingEmail(60000, 1);

    // Verifying that the mail has been sent
    assertThat(greenMailRule.getReceivedMessages(), arrayWithSize(1));
  }

  /*
  Tool methods
   */

  private void sendSynchronouslyAndAssertThatNoSendingDone(MailSending mailSending)
      throws Exception {
    mailSending.sendSynchronously();
    // Verifying sent data
    assertThat(greenMailRule.getReceivedMessages(), emptyArray());
  }

  private void assertMailSent(MailToSend verifiedMailToSend)
      throws Exception {
    assertThat("assertMailSent is compatible with one receiver only...", verifiedMailToSend.getTo(),
        hasSize(1));

    assertThat(verifiedMailToSend.getTo().getRecipientType().getTechnicalType(),
        is(Message.RecipientType.TO));

    MimeMessage[] messages = greenMailRule.getReceivedMessages();
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
  class StubbedSmtpMailSender extends SmtpMailSender {

    public MailToSend currentMailToSend;

    @Override
    public void send(final MailToSend mail) {
      try {
        Thread.sleep(2);
      } catch (InterruptedException ignored) {
      }
      currentMailToSend = mail;
      super.send(mail);
    }
  }
}