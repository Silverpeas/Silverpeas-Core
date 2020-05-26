/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.util.Charsets;

import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@UnitTest
public class MailContentTest {

  private static final String HTML_TYPE = "text/html; charset=\"UTF-8\"";
  private static final String TEXT_TYPE = "text/plain; charset=\"UTF-8\"";
  private static final String OTHER_TYPE = "other type";

  @Test
  public void emptyContent() throws Exception {
    MailContent mailContent = MailContent.of((String) null);
    assertThat(mailContent.isHtml(), is(true));
    assertEmptyContentAsHtml(mailContent);

    mailContent = MailContent.of((Multipart) null);
    assertThat(mailContent.isHtml(), is(true));
    assertEmptyContentAsHtml(mailContent);

    mailContent = MailContent.of("");
    assertThat(mailContent.isHtml(), is(true));
    assertEmptyContentAsHtml(mailContent);

    mailContent = MailContent.of("   ");
    assertThat(mailContent.isHtml(), is(true));
    assertEmptyContentAsHtml(mailContent);

    mailContent = MailContent.of("null");
    assertThat(mailContent.isHtml(), is(true));
    assertEmptyContentAsHtml(mailContent);
  }

  @Test
  public void stringContentHtmlIndicated() throws Exception {
    MailContent mailContent = MailContent.of("mail content");
    assertThat(mailContent.isHtml(), is(true));
    assertContentAsHtml(mailContent, "<html><body>mail content</body></html>");
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
    verify(mimeMessageMock, times(0)).setContent(Matchers.any(Multipart.class));
    verify(mimeMessageMock, times(0)).setText(anyString(), anyString());
  }

  private void assertEmptyContentAsHtml(final MailContent mailContent) throws Exception {
    assertContentAsHtml(mailContent, "<html><body></body></html>");
  }

  private void assertContentAsHtml(final MailContent mailContent, String expectedContent)
      throws Exception {
    MimeMessage mimeMessageMock = mock(MimeMessage.class);
    mailContent.applyOn(mimeMessageMock);
    verify(mimeMessageMock, times(0)).setContent(expectedContent, HTML_TYPE);
    verify(mimeMessageMock, times(1)).setContent(Matchers.any(Multipart.class));
    verify(mimeMessageMock, times(0)).setText(anyString(), anyString());
    ArgumentCaptor<Multipart> multiPart = ArgumentCaptor.forClass(Multipart.class);
    verify(mimeMessageMock).setContent(multiPart.capture());
    Multipart actualMultiPart = multiPart.getValue();
    assertThat(actualMultiPart.getContentType(), containsString("multipart/alternative"));
    final List<DataHandler> dataHandlers = new ArrayList<>();
    try {
      for (int i = 0; i < 10; i++) {
        dataHandlers.add(actualMultiPart.getBodyPart(i).getDataHandler());
      }
    } catch (Exception ignore) {
    }
    assertThat(dataHandlers, hasSize(2));
    final DataHandler textHandler = dataHandlers.get(0);
    assertThat(textHandler.getContentType(), is(TEXT_TYPE));
    assertThat(textHandler.getContent().toString(), not(is(expectedContent)));
    final DataHandler htmlHandler = dataHandlers.get(1);
    assertThat(htmlHandler.getContentType(), is(HTML_TYPE));
    assertThat(htmlHandler.getContent().toString(), is(expectedContent));
  }

  private void assertContentAsText(final MailContent mailContent, String expectedContent)
      throws Exception {
    MimeMessage mimeMessageMock = mock(MimeMessage.class);
    mailContent.applyOn(mimeMessageMock);
    verify(mimeMessageMock, times(0)).setContent(anyObject(), anyString());
    verify(mimeMessageMock, times(0)).setContent(Matchers.any(Multipart.class));
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