//Source file: C:\\Silverpeas\\Beaujolais\\Bus\\admin\\JavaLib\\com\\stratelia\\silverpeas\\beans\\admin\\Domain.java

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;

public class Domain implements Serializable
{
   private String id;
   private String name;
   private String description;
   private String driverClassName;
   private String propFileName;
   private String authenticationServer;
   private String theTimeStamp = "0";
   private String silverpeasServerURL = "";
   
   public Domain() 
   {
    
   }
   
   /**
    * @return String
    */
   public String getId() 
   {
    return id;    
   }
   
   /**
    * @param id
    */
   public void setId(String id) 
   {
   	this.id = id;    
   }
   
   /**
    * @return String
    */
   public String getTheTimeStamp() 
   {
    return this.theTimeStamp;    
   }
   
   /**
    * @param id
    */
   public void setTheTimeStamp(String tt) 
   {
   	this.theTimeStamp = tt;    
   }
   
   /**
    * @return String
    */
   public String getName() 
   {
    return name;    
   }
   
   /**
    * @param name
    */
   public void setName(String name) 
   {
   	this.name = name;    
   }
   
   /**
    * @return String
    */
   public String getDescription() 
   {
    return description;    
   }
   
   /**
    * @param descriptionId
    */
   public void setDescription(String description) 
   {
   	this.description = description;    
   }
   
   /**
    * @return String
    */
   public String getDriverClassName() 
   {
    return driverClassName;    
   }
   
   /**
    * @param className
    */
   public void setDriverClassName(String className) 
   {
   	this.driverClassName = className;    
   }

   /**
    * @return String
    */
   public String getPropFileName() 
   {
    return propFileName;    
   }
   
   /**
    * @param className
    */
   public void setPropFileName(String propFileName) 
   {
   	this.propFileName = propFileName;    
   }

   /**
    * @return String
    */
   public String getAuthenticationServer() 
   {
    return authenticationServer;    
   }
   
   /**
    * @param className
    */
   public void setAuthenticationServer(String authenticationServer) 
   {
   	this.authenticationServer = authenticationServer;
   }

   /**
    * @return String
    */
   public String getSilverpeasServerURL() 
   {
    return silverpeasServerURL;    
   }
   
   /**
    * @param className
    */
   public void setSilverpeasServerURL(String silverpeasServerURL) 
   {
   	this.silverpeasServerURL = silverpeasServerURL;
   }
}
