package com.stratelia.webactiv.util.viewGenerator.html.buttons;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class ButtonTag extends TagSupport {

	private String label = "";
	private String action = "#";
	private boolean disabled = false;

	public int doEndTag() throws JspException {
		try {
			GraphicElementFactory gef = (GraphicElementFactory) pageContext
					.getSession().getAttribute(
							GraphicElementFactory.GE_FACTORY_SESSION_ATT);
			Button button = gef.getFormButton(label, action, disabled);
			pageContext.getOut().println(button.print());
		} catch (IOException e) {
			throw new JspException("ButtonTag Tag", e);
		}
		return EVAL_PAGE;
	}

	public int doStartTag() throws JspException {
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * @param disabled
	 *            the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
