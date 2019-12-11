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
package org.silverpeas.core.web.util.viewgenerator.html.layout;

import org.apache.ecs.xhtml.head;
import org.apache.ecs.xhtml.title;
import org.silverpeas.core.web.util.viewgenerator.html.LookAndStyleTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

/**
 * This tag MUST be the first included into a {@link HtmlLayoutTag}.
 * <p>
 * By default, the look and feel stuffs are automatically included into the HEAD part.<br/>
 * If there is no need to include the look and feel, please fill {@code true} value to attribute
 * {@link #noLookAndFeel}.
 * </p>
 * <p>
 * Attributes {@link #withCheckFormScript} and {@link #withFieldsetStyle} are directly
 * transmitted to the look and feel TAG ({@link LookAndStyleTag}).
 * </p>
 */
public class HeadLayoutPartTag extends SilverpeasLayout {
  private static final long serialVersionUID = 6334425081267420038L;

  private boolean noLookAndFeel;
  private boolean withFieldsetStyle;
  private boolean withCheckFormScript;

  public void setNoLookAndFeel(final boolean noLookAndFeel) {
    this.noLookAndFeel = noLookAndFeel;
  }

  public void setWithCheckFormScript(final boolean withCheckFormScript) {
    this.withCheckFormScript = withCheckFormScript;
  }

  public void setWithFieldsetStyle(final boolean withFieldsetStyle) {
    this.withFieldsetStyle = withFieldsetStyle;
  }

  @Override
  public int doEndTag() throws JspException {
    final head head = new head();
    head.addElement(new title(getBundle().getString("GML.popupTitle")));
    renderLookAndFeel(head);
    final BodyContent bodyContent = getBodyContent();
    if (bodyContent != null) {
      head.addElement(bodyContent.getString());
    }
    head.output(pageContext.getOut());
    return EVAL_PAGE;
  }

  private void renderLookAndFeel(final head head) {
    if (!noLookAndFeel) {
      final LookAndStyleTag lookAndFeel = new LookAndStyleTag();
      lookAndFeel.setPageContext(pageContext);
      lookAndFeel.setParent(this);
      lookAndFeel.setWithCheckFormScript(withCheckFormScript);
      lookAndFeel.setWithFieldsetStyle(withFieldsetStyle);
      head.addElement(lookAndFeel.getContent());
    }
  }

  @Override
  public HtmlLayoutTag getParent() {
    return (HtmlLayoutTag) super.getParent();
  }
}
