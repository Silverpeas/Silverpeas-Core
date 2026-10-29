/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.web.util.viewgenerator.html.layout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.Tag;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.IOException;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the HTML document rendered by the {@code <view:sp-page>} tag.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class HtmlLayoutTagTest {

  @TestManagedMock
  private SilverpeasWebUtil webUtil;

  @Test
  void theDocumentIsAnHtml5OneInTheUserLanguage() throws Exception {
    final StringJspWriter out = new StringJspWriter();
    final HtmlLayoutTag tag = aTagRendering("<head></head><body>Hello</body>", "fr", out);

    assertThat(tag.doEndTag(), is(Tag.EVAL_PAGE));

    final String document = out.toString();
    assertThat(document, startsWith("<!DOCTYPE html>"));
    assertThat(document, containsString("<html lang='fr'>"));
    assertThat(document, not(containsString("xmlns")));
    assertThat(document, not(containsString("xml:lang")));
    assertThat(document, containsString("<head></head><body>Hello</body>"));
  }

  private HtmlLayoutTag aTagRendering(final String body, final String language,
      final JspWriter out) {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(webUtil.getUserLanguage(request)).thenReturn(language);
    final PageContext pageContext = mock(PageContext.class);
    when(pageContext.getRequest()).thenReturn(request);
    when(pageContext.getOut()).thenReturn(out);
    final BodyContent bodyContent = mock(BodyContent.class);
    when(bodyContent.getString()).thenReturn(body);
    final HtmlLayoutTag tag = new HtmlLayoutTag();
    tag.setPageContext(pageContext);
    tag.setBodyContent(bodyContent);
    return tag;
  }

  /**
   * A JSP writer keeping in memory what is written through it.
   */
  private static class StringJspWriter extends JspWriter {
    private final StringWriter buffer = new StringWriter();

    StringJspWriter() {
      super(0, false);
    }

    @Override
    public String toString() {
      return buffer.toString();
    }

    @Override
    public void write(final char[] chars, final int off, final int len) {
      buffer.write(chars, off, len);
    }

    @Override
    public void newLine() {
      buffer.write('\n');
    }

    @Override
    public void print(final boolean b) {
      buffer.write(String.valueOf(b));
    }

    @Override
    public void print(final char c) {
      buffer.write(c);
    }

    @Override
    public void print(final int i) {
      buffer.write(String.valueOf(i));
    }

    @Override
    public void print(final long l) {
      buffer.write(String.valueOf(l));
    }

    @Override
    public void print(final float f) {
      buffer.write(String.valueOf(f));
    }

    @Override
    public void print(final double d) {
      buffer.write(String.valueOf(d));
    }

    @Override
    public void print(final char[] s) {
      buffer.write(s, 0, s.length);
    }

    @Override
    public void print(final String s) {
      buffer.write(s);
    }

    @Override
    public void print(final Object obj) {
      buffer.write(String.valueOf(obj));
    }

    @Override
    public void println() {
      newLine();
    }

    @Override
    public void println(final boolean x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final char x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final int x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final long x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final float x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final double x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final char[] x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final String x) {
      print(x);
      newLine();
    }

    @Override
    public void println(final Object x) {
      print(x);
      newLine();
    }

    @Override
    public void clear() {
      buffer.getBuffer().setLength(0);
    }

    @Override
    public void clearBuffer() {
      clear();
    }

    @Override
    public void flush() {
      // nothing to flush
    }

    @Override
    public void close() throws IOException {
      buffer.close();
    }

    @Override
    public int getRemaining() {
      return 0;
    }
  }
}
