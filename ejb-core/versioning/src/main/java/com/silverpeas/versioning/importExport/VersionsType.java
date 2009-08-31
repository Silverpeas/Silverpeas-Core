package com.silverpeas.versioning.importExport;

import java.util.List;

/**
 * Classe utilisée pour le (un)marshalling Castor
 * @author neysseri
 */
public class VersionsType {
	
	private List listVersions; //DocumentVersion

	/**
	 * @return
	 */
	public List getListVersions() {
		return listVersions;
	}

	/**
	 * @param list
	 */
	public void setListVersions(List list) {
		listVersions = list;
	}

}
