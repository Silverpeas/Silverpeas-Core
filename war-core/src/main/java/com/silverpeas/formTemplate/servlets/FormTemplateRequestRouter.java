package com.silverpeas.formTemplate.servlets;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileUploadException;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormTemplateSessionController;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class FormTemplateRequestRouter extends ComponentRequestRouter {

	public ComponentSessionController createComponentSessionController(
		MainSessionController mainSessionCtrl,
		ComponentContext context) {
		return (
			(ComponentSessionController) new FormTemplateSessionController(mainSessionCtrl, context));
	}

	/**
	 * This method has to be implemented in the component request rooter class.
	 * returns the session control bean name to be put in the request object
	 * ex : for almanach, returns "almanach"
	 */
	public String getSessionControlBeanName() {
		return "formTemplate";
	}

	/**
	 * This method has to be implemented by the component request Router
	 * it has to compute a destination page
	 * @param function The entering request function (ex : "Main.jsp")
	 * @param componentSC The component Session Control, build and initialised.
		 * @param request The entering request. The request Router need it to get parameters
	 * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
	 */
	public String getDestination(
		String function,
		ComponentSessionController componentSC,
		HttpServletRequest request) {
		
		SilverTrace.info("form", "FormTemplateRequestRouter.getDestination()", "root.MSG_GEN_ENTER_METHOD", "function = "+function);
			
		FormTemplateSessionController controller = (FormTemplateSessionController) componentSC;
		
		String destination = "";

		try
		{
			if (function.equals("Edit")) 
			{
				//display editing form with data
				
				String componentId 		= request.getParameter("ComponentId");
				String objectId			= request.getParameter("ObjectId");
				String objectType		= request.getParameter("ObjectType");
				String objectLanguage	= request.getParameter("ObjectLanguage");
				String xmlFormName		= request.getParameter("XMLFormName");
				
				controller.setComponentId(componentId);
				controller.setObjectId(objectId);
				controller.setObjectType(objectType);
				controller.setObjectLanguage(objectLanguage);
				controller.setXmlFormName(xmlFormName);
				
				String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/")+1, xmlFormName.indexOf("."));
					
				//register xmlForm to object
				PublicationTemplateManager.addDynamicPublicationTemplate(componentId+":"+objectType+":"+xmlFormShortName, xmlFormName);
								
				PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(componentId+":"+objectType+":"+xmlFormShortName, xmlFormName);
				Form formUpdate = pubTemplate.getUpdateForm();
				RecordSet recordSet = pubTemplate.getRecordSet();
				
				DataRecord data= recordSet.getRecord(objectId, objectLanguage);
				if (data == null) {
					data = recordSet.getEmptyRecord();
					data.setId(objectId);
				}
				
				PagesContext pageContext = new PagesContext("myForm", "2", controller.getLanguage(), false, componentId, controller.getUserId());
				pageContext.setObjectId(objectId);
				
				request.setAttribute("XMLForm", formUpdate);
				request.setAttribute("XMLData", data);
				request.setAttribute("XMLFormName", xmlFormName);
				request.setAttribute("PagesContext", pageContext);
				
				destination = "/form/jsp/edit.jsp";
			}
			else if (function.equals("Update"))
			{
				//Save changes
				List items = getRequestItems(request);
				
				String xmlFormName = controller.getXmlFormName();
				String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/")+1, xmlFormName.indexOf("."));
				
				String objectId 		= controller.getObjectId();
				String objectType 		= controller.getObjectType();
				String componentId 		= controller.getComponentId();
				String objectLanguage	= controller.getObjectLanguage();
								
				PublicationTemplate pub = PublicationTemplateManager.getPublicationTemplate(componentId+":"+objectType+":"+xmlFormShortName);
				
				RecordSet 	set 	= pub.getRecordSet();			
				Form 		form 	= pub.getUpdateForm();
							
				int callbackAction = CallBackManager.ACTION_XMLCONTENT_UPDATE;
				
   				DataRecord data = set.getRecord(objectId, objectLanguage);
   				if (data == null) {
   					data = set.getEmptyRecord();
					data.setId(objectId);
					data.setLanguage(objectLanguage);
					callbackAction = CallBackManager.ACTION_XMLCONTENT_CREATE;
   				}
   				
   				PagesContext context = new PagesContext("myForm", "3", controller.getLanguage(), false, componentId, controller.getUserId());
   				context.setObjectId(objectId);
   				context.setContentLanguage(objectLanguage);
   				
   				form.update(items, data, context);
				set.save(data);
				
				Hashtable<String, String> params = new Hashtable<String, String>();
				params.put("ObjectId", objectId);
				params.put("ObjectType", objectType);
				params.put("ObjectLanguage", objectLanguage);
				params.put("XMLFormName", xmlFormShortName);
				
				//launch event
				CallBackManager.invoke(callbackAction, Integer.parseInt(controller.getUserId()), componentId, params);
				
				destination = "/form/jsp/close.jsp";
			}
			else if (function.equals("View"))
			{
				String componentId 		= request.getParameter("ComponentId");
				String objectId			= request.getParameter("ObjectId");
				String objectType		= request.getParameter("ObjectType");
				String objectLanguage	= request.getParameter("ObjectLanguage");
				String xmlFormName		= request.getParameter("XMLFormName");
				
				if (xmlFormName.indexOf(".") != -1)
					xmlFormName = xmlFormName.substring(0, xmlFormName.indexOf("."));
				
				if (StringUtil.isDefined(xmlFormName) && StringUtil.isDefined(objectId))
				{
					PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(componentId+":"+objectType+":"+xmlFormName);
					
					Form formView = pubTemplate.getViewForm();
			
					RecordSet recordSet = pubTemplate.getRecordSet();
					DataRecord data = recordSet.getRecord(objectId, objectLanguage);
					if (data == null) {
						data = recordSet.getEmptyRecord();
						data.setId(objectId);
					}
					
					PagesContext pageContext = new PagesContext("myForm", "2", controller.getLanguage(), false, componentId, controller.getUserId());
					pageContext.setObjectId(objectId);
					
					request.setAttribute("XMLForm", formView);
					request.setAttribute("XMLData", data);
					request.setAttribute("PagesContext", pageContext);
				}
				
				destination = "/form/jsp/view.jsp"; 
			}
			else
			{
				destination = "/form/jsp/" + function;
			}
		} catch (Exception e) {
			request.setAttribute("javax.servlet.jsp.jspException", e);
			return "/admin/jsp/errorpageMain.jsp";
		}
		
		SilverTrace.info("form", "FormTemplateRequestRouter.getDestination()", "root.MSG_GEN_EXIT_METHOD", "destination = " + destination);
		return destination;
	}
	
	private List getRequestItems(HttpServletRequest request) throws FileUploadException
	{
		DiskFileUpload 	dfu 	= new DiskFileUpload();
		List 			items 	= dfu.parseRequest(request);
		return items;
	}

}