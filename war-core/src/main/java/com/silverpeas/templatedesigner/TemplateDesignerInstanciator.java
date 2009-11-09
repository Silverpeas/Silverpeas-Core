package com.silverpeas.templatedesigner;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class TemplateDesignerInstanciator implements ComponentsInstanciatorIntf {

  public TemplateDesignerInstanciator() {
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {
	SilverTrace.info("templateDesigner","TemplateDesignerInstanciator.create()","root.MSG_GEN_ENTER_METHOD", "space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//insert your code here !
	
	SilverTrace.info("templateDesigner","TemplateDesignerInstanciator.create()","root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("templateDesigner","TemplateDesignerInstanciator.delete()","root.MSG_GEN_ENTER_METHOD","space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//insert your code here !

	SilverTrace.info("templateDesigner","TemplateDesignerInstanciator.delete()","root.MSG_GEN_EXIT_METHOD");
  }
}