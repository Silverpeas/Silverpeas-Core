/** 
 *
 * @author  akhadrou
 * @version 
 */
package com.stratelia.webactiv.util.publication.info.model;
 
import java.io.Serializable;

public class ModelDetail implements Serializable {
 
  private String id = null;
  private String name = null;
  private String description = null;
  private String imageName = null;
  private String htmlDisplayer = null;
  private String htmlEditor = null;
  
  public ModelDetail(String id, String name, String description, String imageName, String htmlDisplayer, String htmlEditor) {
	    setId(id);
            setName(name);
            setDescription(description);
            setImageName(imageName);
            setHtmlDisplayer(htmlDisplayer);
            setHtmlEditor(htmlEditor);
  }

  // set get on attributes        
  //id
  public String getId() {
        return id;
  }
		
  public void setId(String val) {
        id = val;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getImageName() {
    return imageName;
  }
  
  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public String getHtmlDisplayer() {
    return htmlDisplayer;
  }
  
  public void setHtmlDisplayer(String htmlDisplayer) {
    this.htmlDisplayer = htmlDisplayer;
  }
  
  public String getHtmlEditor() {
    return htmlEditor;
  }
  
  public void setHtmlEditor(String htmlEditor) {
    this.htmlEditor = htmlEditor;
  }
  
  public String toString() {
	return "(id = " + getId() + ", name = " + getName() +
                ", description = " + getDescription() + 
                ", imageName = " + getImageName() +
                ", hmlDisplayer = " + getHtmlDisplayer() +
                ", htmlEditor = " + getHtmlEditor() + ")";
  }
  
}