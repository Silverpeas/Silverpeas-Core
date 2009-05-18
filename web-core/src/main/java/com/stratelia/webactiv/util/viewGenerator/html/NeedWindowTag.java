package com.stratelia.webactiv.util.viewGenerator.html;

import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.window.Window;
import com.stratelia.webactiv.util.viewGenerator.html.window.WindowTag;

public class NeedWindowTag extends TagSupport {
  protected Window getWindow() {
    Window window = (Window) pageContext
        .getAttribute(WindowTag.WINDOW_PAGE_ATT);
    if (window == null) {
      GraphicElementFactory gef = (GraphicElementFactory) pageContext
          .getSession().getAttribute(
              GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      window = gef.getWindow();
      pageContext.setAttribute(WindowTag.WINDOW_PAGE_ATT, window);
    }
    return window;
  }
}
