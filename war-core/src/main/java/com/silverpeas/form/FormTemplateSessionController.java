package com.silverpeas.form;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;

/**
 *
 * @author  neysseri
 * @version
 */
public class FormTemplateSessionController extends AbstractComponentSessionController
{
	private String componentId;
	private String objectId;
	private String objectType;
	private String xmlFormName;
	private String objectLanguage;
	
	public FormTemplateSessionController(MainSessionController mainSessionCtrl, ComponentContext context)
	{
		super(mainSessionCtrl, context, "com.silverpeas.form.multilang.formBundle", null, null);
	}
	
	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getXmlFormName() {
		return xmlFormName;
	}

	public void setXmlFormName(String xmlFormName) {
		this.xmlFormName = xmlFormName;
	}
	
	public String getObjectLanguage() {
		return objectLanguage;
	}

	public void setObjectLanguage(String objectLanguage) {
		this.objectLanguage = objectLanguage;
	}
	
}