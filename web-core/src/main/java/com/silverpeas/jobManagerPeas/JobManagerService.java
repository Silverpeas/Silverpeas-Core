package com.silverpeas.jobManagerPeas;

public class JobManagerService 
{

	private String id = null;
	private String label = null;
	private int level = 0;
	private String url = null;
	private String[] idSubServices = null;
	private boolean isActif = false;

	public static int LEVEL_SERVICE = 0;
	public static int LEVEL_OPERATION = 1;
	//public static int LEVEL_ACTION = 2;
	//public static int LEVEL_ACTEUR = 3;



	/**
	 * constructors
	 */
	 public JobManagerService(String id, String label, int level, String url, String[] idSubServices, boolean isActif){
			this.id = id;
			this.label = label;
			this.level = level;
			this.url = url;
			this.idSubServices = idSubServices;
			this.isActif =isActif;
	 }


	 public void setActif(boolean a){
		 isActif = a;
	 }
	 public String getLabel(){
		return label;
	 }
	 public boolean isActif(){
		return isActif;
	}
	public int getLevel(){
		return level;
	}
	public String[] getIdSubServices(){
		return idSubServices;
	}
	public String getId(){
		return id;
	}
	public String getUrl(){
		return url;
	}
	public String getDefautIdSubService(){
		return idSubServices[0];
	}
}
