package com.silverpeas.util.i18n;

import java.util.Hashtable;

public interface I18NBean {

	public Hashtable<String, Translation> getTranslations();
	
	public Translation getTranslation(String language);
	
	public Translation getNextTranslation();
	
	public void setLanguage(String language);
	
	public void setTranslationId(String translationId);
	
	public void setRemoveTranslation(boolean remove);
		
}
