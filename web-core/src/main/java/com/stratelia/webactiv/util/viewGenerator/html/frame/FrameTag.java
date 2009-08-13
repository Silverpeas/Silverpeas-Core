package com.stratelia.webactiv.util.viewGenerator.html.frame;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class FrameTag extends TagSupport {
	private static final String FRAME_PAGE_ATT = "pageContextFrame";
	private String title;

	@Override
	public int doEndTag() throws JspException {
		final Frame frame = (Frame) pageContext.getAttribute(FRAME_PAGE_ATT);
		try {
			pageContext.getOut().println(frame.printAfter());
		} catch (final IOException e) {
			throw new JspException("Frame Tag", e);
		}
		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException {
		final GraphicElementFactory gef = (GraphicElementFactory) pageContext
				.getSession().getAttribute(
						GraphicElementFactory.GE_FACTORY_SESSION_ATT);
		final Frame frame = gef.getFrame();
		if (title != null) {
			frame.addTitle("&nbsp;&nbsp;"+title);
		}
		pageContext.setAttribute(FRAME_PAGE_ATT, frame);
		try {
			pageContext.getOut().println(frame.printBefore());
		} catch (final IOException e) {
			throw new JspException("Frame Tag", e);
		}
		return EVAL_BODY_INCLUDE;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

}
