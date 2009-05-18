package com.silverpeas.util.i18n;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.StringUtil;

public abstract class AbstractI18NBean implements I18NBean, Serializable {

	private String 		language			= null;
	private String 		translationId 		= null;
	private Hashtable<String, Translation> 	translations 		= new Hashtable<String, Translation>();
	private boolean 	removeTranslation 	= false;
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public boolean isRemoveTranslation() {
		return removeTranslation;
	}

	public void setRemoveTranslation(boolean removeTranslation) {
		this.removeTranslation = removeTranslation;
	}

	public String getTranslationId() {
		return translationId;
	}

	public void setTranslationId(String translationId) {
		this.translationId = translationId;
	}
	
	public Iterator<String> getLanguages()
	{
		return translations.keySet().iterator();
	}

	public Hashtable<String, Translation> getTranslations() {
		return translations;
	}

	public void setTranslations(Hashtable<String, Translation> translations) {
		this.translations = translations;
	}
	
	public void setTranslations(Collection<Translation> translations) {
	  if(translations != null && ! translations.isEmpty()) {
	    for (Translation translation  : translations) {
        addTranslation(translation);
      }
	  }	 
	}
	
	public void setTranslations(List<Translation> translations) {
		if(translations != null && ! translations.isEmpty()) {
			for (Translation translation  : translations) {
				addTranslation(translation);
			}
		}
	}
	 
	public Translation getTranslation(String language) {
		return (Translation) translations.get(language);
	}
	
	public void addTranslation(Translation translation) {
		String language = translation.getLanguage();
		if (!StringUtil.isDefined(language))
		{
			language = I18NHelper.defaultLanguage;
			translation.setLanguage(language);
		}
		translations.put(language, translation);
	}
	
	public Translation getNextTranslation()
	{
		Iterator<String> languages = I18NHelper.getLanguages();
		Translation translation = null;
		while (translation == null && languages.hasNext())
		{
			translation = (Translation) getTranslations().get(languages.next());
		}
		return translation;
	}
	
	public String getLanguageToDisplay(String language)
	{
		String languageToDisplay = language;
		
		Translation translation = getTranslation(language);
		if (translation == null)
		{
			translation = getNextTranslation();
			if (translation != null)
				languageToDisplay = translation.getLanguage();
		}
		
		return languageToDisplay;
	}
}
