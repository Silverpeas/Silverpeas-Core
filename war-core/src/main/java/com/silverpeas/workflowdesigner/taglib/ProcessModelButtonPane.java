package com.silverpeas.workflowdesigner.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane;
import com.stratelia.webactiv.util.viewGenerator.html.buttons.Button;

/**
 * Class implementing the tag &lt;buttonPane&gt; from workflowEditor.tld
 *
 */
public class ProcessModelButtonPane extends TagSupport
{
    private String                strCancelAction;
    
    /**
     * @return the current tab name
     */
    public String getCancelAction()
    {
        return strCancelAction;
    }

    /**
     * @param cancelAction the current Tab name to set
     */
    public void setCancelAction(String cancelAction)
    {
        strCancelAction = cancelAction;
    }

    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException
    {
        GraphicElementFactory gef;
        ResourcesWrapper      resource;
        ButtonPane            buttonPane;
        Button                validateButton;
        Button                cancelButton;
        
        gef = (GraphicElementFactory)pageContext.getSession().getAttribute("SessionGraphicElementFactory");
        buttonPane = gef.getButtonPane();
        resource = (ResourcesWrapper)pageContext.getRequest().getAttribute("resources");
        validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), 
                "javascript:sendData();", false);
        cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), strCancelAction, false);
        
        buttonPane.addButton(validateButton);
        buttonPane.addButton(cancelButton);

        try
        {
            pageContext.getOut().println("<BR><center>" + buttonPane.print() + "</center><BR>");
        } 
        catch (IOException e)
        {
            throw new JspException("Error when printing the Workflow Designer tabs", e);
        }
        return super.doStartTag();
    }

}
