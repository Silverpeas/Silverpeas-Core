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

import org.junit.Test;
import org.silverpeas.core.util.Charsets;

import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class MailContentTest {

  private static final String HTML_TYPE = "text/html; charset=\"UTF-8\"";
  private static final String OTHER_TYPE = "other type";

  @Test
  public void emptyContent() throws Exception {
    MailContent mailContent = MailContent.of((String) null);
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "");

    mailContent = MailContent.of((Multipart) null);
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "");

    mailContent = MailContent.of("");
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "");

    mailContent = MailContent.of("   ");
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "");

    mailContent = MailContent.of("null");
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "");
  }

  @Test
  public void stringContentHtmlIndicated() throws Exception {
    MailContent mailContent = MailContent.of("mail content");
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "mail content");
  }

  @Test
  public void stringOtherContentTypeIndicated() throws Exception {
    MailContent mailContent = MailContent.of("mail content").withContentType(OTHER_TYPE);
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsOtherType(mailContent, "mail content");
  }

  @Test
  public void stringContentHtmlExplicitlyNotIndicated() throws Exception {
    MailContent mailContent = MailContent.of("mail content").notHtml();
    assertThat(mailContent.isHtml(), is(false));
    assertContentAsText(mailContent, "mail content");
  }

  @Test
  public void stringContentHtmlExplicitlyNotIndicatedButHtmlTagExists() throws Exception {
    MailContent mailContent = MailContent.of("mail content <html>").notHtml();
    assertThat(mailContent.isHtml(), is(false));
    assertContentAsHtml(mailContent, "mail content <html>");
  }

  @Test
  public void multipartContentHtmlIndicated() throws Exception {
    Multipart multipart = new MimeMultipart();
    MailContent mailContent = MailContent.of(multipart);
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsMultipart(mailContent, multipart);
  }

  @Test
  public void multipartContentHtmlExplicitlyNotIndicated() throws Exception {
    Multipart multipart = new MimeMultipart();
    MailContent mailContent = MailContent.of(multipart).notHtml();
    assertThat(mailContent.isHtml(), is(false));
    assertContentAsMultipart(mailContent, multipart);
  }

  /*
  ASSERTION METHODS
   */

  private void assertContentAsOtherType(final MailContent mailContent, String expectedContent)
      throws Exception {
    MimeMessage mimeMessageMock = mock(MimeMessage.class);
    mailContent.applyOn(mimeMessageMock);
    verify(mimeMessageMock, times(1)).setContent(expectedContent, OTHER_TYPE);
    verify(mimeMessageMock, times(0)).setContent(any(Multipart.class));
    verify(mimeMessageMock, times(0)).setText(anyString(), anyString());
  }

  private void assertContentAsHtml(final MailContent mailContent, String expectedContent)
      throws Exception {
    MimeMessage mimeMessageMock = mock(MimeMessage.class);
    mailContent.applyOn(mimeMessageMock);
    verify(mimeMessageMock, times(1)).setContent(expectedContent, HTML_TYPE);
    verify(mimeMessageMock, times(0)).setContent(any(Multipart.class));
    verify(mimeMessageMock, times(0)).setText(anyString(), anyString());
  }

  private void assertContentAsText(final MailContent mailContent, String expectedContent)
      throws Exception {
    MimeMessage mimeMessageMock = mock(MimeMessage.class);
    mailContent.applyOn(mimeMessageMock);
    verify(mimeMessageMock, times(0)).setContent(anyObject(), anyString());
    verify(mimeMessageMock, times(0)).setContent(any(Multipart.class));
    verify(mimeMessageMock, times(1)).setText(expectedContent, Charsets.UTF_8.name());
  }

  private void assertContentAsMultipart(final MailContent mailContent, Multipart expectedMultipart)
      throws Exception {
    MimeMessage mimeMessageMock = mock(MimeMessage.class);
    mailContent.applyOn(mimeMessageMock);
    verify(mimeMessageMock, times(0)).setContent(anyObject(), anyString());
    verify(mimeMessageMock, times(1)).setContent(expectedMultipart);
    verify(mimeMessageMock, times(0)).setText(anyString(), anyString());
  }
}