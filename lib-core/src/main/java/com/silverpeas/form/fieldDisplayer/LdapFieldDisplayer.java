	package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.novell.ldap.LDAPConnection;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.LdapField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * A LDAPFieldDisplayer is an object which can display a listbox in HTML
 * the content of a listbox to a end user
 * and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class LdapFieldDisplayer extends AbstractFieldDisplayer
{
	
	/**
	 * Constructeur
	 */  
	public LdapFieldDisplayer() 
	{
	}
   
  
	/**
	 * Returns the name of the managed types.
	 */
	public String[] getManagedTypes() 
	{
		String [] s = new String[0];
		s[0] = LdapField.TYPE;
		return s;
	}

	/**
	 * Prints the javascripts which will be used to control
	 * the new value given to the named field.
	 *
	 * The error messages may be adapted to a local language.
	 * The FieldTemplate gives the field type and constraints.
	 * The FieldTemplate gives the local labeld too.
	 *
	 * Never throws an Exception
	 * but log a silvertrace and writes an empty string when :
	 * <UL>
	 * <LI> the fieldName is unknown by the template.
	 * <LI> the field type is not a managed type.
	 * </UL>
	 */
	public void displayScripts(PrintWriter out,
								FieldTemplate template,
								PagesContext PagesContext) throws java.io.IOException 
	{ 
 		
		String language = PagesContext.getLanguage();
		
		if (! template.getTypeName().equals(LdapField.TYPE))
			SilverTrace.info("form", "LdapFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", LdapField.TYPE);
		
	 	if (template.isMandatory() && PagesContext.useMandatory()) 
		{
			out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
			out.println("		errorMsg+=\"  - '"+template.getLabel(language)+"' "+Util.getString("GML.MustBeFilled", language)+"\\n \";");
			out.println("		errorNb++;");
			out.println("	}");
    	}
	 	
	 	Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
	}

	/**
	 * Prints the HTML value of the field.
	 * The displayed value must be updatable by the end user.
	 *
	 * The value format may be adapted to a local language.
	 * The fieldName must be used to name the html form input.
	 *
	 * Never throws an Exception
	 * but log a silvertrace and writes an empty string when :
	 * <UL>
	 * <LI> the field type is not a managed type.
	 * </UL>
	 */
	public void display(PrintWriter out,
                      Field field,
                      FieldTemplate template,
                      PagesContext PagesContext) throws FormException
	{
		String value = "";	
		String html = "";
		String currentUserId = PagesContext.getUserId();
		String language = PagesContext.getLanguage();

		String mandatoryImg = Util.getIcon("mandatoryField");

		String fieldName = template.getFieldName();
		Map parameters = template.getParameters(language);
		LdapField ldapField = null;
		
		if (! field.getTypeName().equals(LdapField.TYPE)) {
			SilverTrace.info("form", "LdapFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", LdapField.TYPE);
		} else {
			ldapField = (LdapField) field;
		}
			
		if (! field.isNull()) {
			value = field.getValue(language);
		}
		Collection listRes = null; //liste de valeurs String
		
		//Parameters
		String host = null;
		String port = null;
		String version = null;
		String baseDN = null;
		String password = null;
		String searchBase = null;
		String searchScope = null;
		String searchFilter = null;
		String searchAttribute = null;
		String searchTypeOnly = null;
		String maxResultDisplayed = null;
		String valueFieldType = "1"; //valeurs possibles 1 = choix restreint à la liste ou 2 = saisie libre, par défaut 1
		if (parameters.containsKey("host")) {
			host = (String) parameters.get("host");
		}
		if (parameters.containsKey("port")) {
			port = (String) parameters.get("port");
		}
		if (parameters.containsKey("version")) {
			version = (String) parameters.get("version");
		}
		if (parameters.containsKey("baseDN")) {
			baseDN = (String) parameters.get("baseDN");
		}
		if (parameters.containsKey("password")) {
			password = (String) parameters.get("password");
		}
		if (parameters.containsKey("searchBase")) {
			searchBase = (String) parameters.get("searchBase");
		}
		if (parameters.containsKey("searchScope")) {
			searchScope = (String) parameters.get("searchScope");
		}
		if (parameters.containsKey("searchFilter")) {
			searchFilter = (String) parameters.get("searchFilter");
		}
		if (parameters.containsKey("searchAttribute")) {
			searchAttribute = (String) parameters.get("searchAttribute");
		}
		if (parameters.containsKey("searchTypeOnly")) {
			searchTypeOnly = (String) parameters.get("searchTypeOnly");
		}
		if (parameters.containsKey("maxResultDisplayed")) {
			maxResultDisplayed = (String) parameters.get("maxResultDisplayed");
		}
		if (parameters.containsKey("valueFieldType")) {
			valueFieldType = (String) parameters.get("valueFieldType"); //valeurs possibles 1 = choix restreint à la liste ou 2 = saisie libre, par défaut 1
		}
		
		if(ldapField != null) {
			LDAPConnection ldapConnection = null;
			try {
				ldapConnection = ldapField.connectLdap(host, port);
				
				//Bind LDAP
				byte[] tabPassword = null;
				if(password != null) {
					tabPassword = password.getBytes();
				}
				ldapField.bindLdap(ldapConnection, version, baseDN, tabPassword);
				
				//Set max result displayed
				if(maxResultDisplayed != null) {
					ldapField.setConstraintLdap(ldapConnection, maxResultDisplayed);
				}
				
				//Requête LDAP
				boolean boolSearchTypeOnly = false;
				if(searchTypeOnly != null) {
					if("true".equals(searchTypeOnly)) {
						boolSearchTypeOnly = true;
					}
				}
				listRes = ldapField.searchLdap(ldapConnection, searchBase, searchScope, searchFilter, searchAttribute, boolSearchTypeOnly, currentUserId);
			} finally {
				ldapField.disconnectLdap(ldapConnection);
			}
		}
		
		if(listRes != null && listRes.size()>0) {
			String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
			int zindex = (PagesContext.getLastFieldIndex() - new Integer(PagesContext.getCurrentFieldIndex()).intValue()) * 9000;
			html += "<link rel=\"stylesheet\" type=\"text/css\" href=\""+m_context+"/util/yui/fonts/fonts-min.css\" />\n";
			html += "<link rel=\"stylesheet\" type=\"text/css\" href=\""+m_context+"/util/yui/autocomplete/assets/skins/sam/autocomplete.css\" />\n";
			html += "<script type=\"text/javascript\" src=\""+m_context+"/util/yui/yahoo-dom-event/yahoo-dom-event.js\"></script>\n";
			html += "<script type=\"text/javascript\" src=\""+m_context+"/util/yui/animation/animation-min.js\"></script>\n";
			html += "<script type=\"text/javascript\" src=\""+m_context+"/util/yui/autocomplete/autocomplete-min.js\"></script>\n";
			html += "<style type=\"text/css\">\n";
			html += "	#listAutocomplete"+fieldName+" {\n";
			html += "		width:15em;\n";
			html += "		padding-bottom:2em;\n";
			html += "	}\n";
			html += "	#listAutocomplete"+fieldName+" {\n";
			html += "		z-index:"+zindex+"; /* z-index needed on top instance for ie & sf absolute inside relative issue */\n";
			html += "	}\n";
			html += "	#"+fieldName+" {\n";
			html += "		_position:absolute; /* abs pos needed for ie quirks */\n";
			html += "	}\n";
			html += "</style>\n";
			
			html += "<div id=\"listAutocomplete"+fieldName+"\">\n";
			html += "<input id=\""+fieldName+"\" name=\""+fieldName+"\" type=\"text\"";
			if(value != null) {
				html += " value=\""+value+"\"";
			}
			if (template.isDisabled() || template.isReadOnly()) { 
				html += " disabled";
			}
			html += "/>\n";
			html += "<div id=\"container"+fieldName+"\"/>\n";
			html += "</div>\n";
			
			if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() && !template.isHidden() && PagesContext.useMandatory()) 
			{
				html += "<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\" style=\"position:absolute;left:16em;top:5px\"/>\n";
			} 
			
			html += "<script type=\"text/javascript\">\n";
			html += "listArray"+fieldName+" = [\n";
		
			Iterator itRes = listRes.iterator();
			String val;
			while(itRes.hasNext()) {
				val = (String) itRes.next();
				
				html += "\""+EncodeHelper.javaStringToJsString(val)+"\",\n";
				
			}
			
			//supprime dernière virgule inutile
			html = html.substring(0, html.length() - 1);
		
			html += "];\n";
			html += "</script>\n";
			
			html += "<script type=\"text/javascript\">\n";
			html += "	this.oACDS"+fieldName+" = new YAHOO.widget.DS_JSArray(listArray"+fieldName+");\n";
			html += "	this.oAutoComp"+fieldName+" = new YAHOO.widget.AutoComplete('"+fieldName+"','container"+fieldName+"', this.oACDS"+fieldName+");\n";
			html += "	this.oAutoComp"+fieldName+".prehighlightClassName = \"yui-ac-prehighlight\";\n";
			html += "	this.oAutoComp"+fieldName+".typeAhead = true;\n";
			html += "	this.oAutoComp"+fieldName+".useShadow = true;\n";
			html += "	this.oAutoComp"+fieldName+".minQueryLength = 0;\n";
			
			if("1".equals(valueFieldType)) {//valeurs possibles 1 = choix restreint à la liste ou 2 = saisie libre, par défaut 1
				html += "	this.oAutoComp"+fieldName+".forceSelection = true;\n";
			}
			
			html += "	this.oAutoComp"+fieldName+".textboxFocusEvent.subscribe(function(){\n";
			html += "		var sInputValue = YAHOO.util.Dom.get('"+fieldName+"').value;\n";
			html += "		if(sInputValue.length == 0) {\n";
			html += "			var oSelf = this;\n";
			html += "			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);\n";
			html += "		}\n";
			html += "	});\n";
			html += "</script>\n";

		} else {
			
			if("1".equals(valueFieldType)) {//valeurs possibles 1 = choix restreint à la liste ou 2 = saisie libre, par défaut 1
			
				html += "<SELECT name=\""+fieldName+"\"";
				
				if (template.isDisabled() || template.isReadOnly()) { 
					html += " disabled";
				}
				html += " >\n";
				html += "</SELECT>\n";
				
				if ((template.isMandatory()) && (!template.isDisabled()) && (!template.isReadOnly()) && (!template.isHidden())) 
				{
					html += "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">&nbsp;\n";
				}
			} else {
				html += "<input type=\"text\" name=\""+fieldName+"\"";
				
				if (template.isDisabled() || template.isReadOnly()) { 
					html += " disabled";
				}
				html += " >\n";
				
				if ((template.isMandatory()) && (!template.isDisabled()) && (!template.isReadOnly()) && (!template.isHidden())) 
				{
					html += "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">&nbsp;\n";
				}
			}
		}
				
		out.println(html);
  }
	
  /**
   * Updates the value of the field.
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
   public void update(String newValue,
						Field field,
						FieldTemplate template,
						PagesContext PagesContext)
		throws FormException {
     	
		   if (! field.getTypeName().equals(LdapField.TYPE))
				   throw new FormException("LdapFieldDisplayer.update","form.EX_NOT_CORRECT_TYPE",LdapField.TYPE);
		
		   if (field.acceptValue(newValue, PagesContext.getLanguage()))
			   field.setValue(newValue, PagesContext.getLanguage());
		   else throw new FormException("LdapFieldDisplayer.update","form.EX_NOT_CORRECT_VALUE",LdapField.TYPE);
		
	  }
   
   public boolean isDisplayedMandatory() {
		return true;
   }   

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext)
  {
		return 1;
  }

}
