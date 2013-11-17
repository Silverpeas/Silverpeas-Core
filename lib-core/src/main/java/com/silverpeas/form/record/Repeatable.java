package com.silverpeas.form.record;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "multivaluable")
@XmlAccessorType(XmlAccessType.FIELD)
public class Repeatable {
  
  private int max = 1;
  private int mandatory;
  
  public Repeatable() {
    
  }
  
  public Repeatable(int max, int mandatory) {
    super();
    this.max = max;
    this.mandatory = mandatory;
  }
  
  public void setMax(int max) {
    this.max = max;
  }
  public int getMax() {
    return max;
  }
  
  public void setMandatory(int mandatory) {
    this.mandatory = mandatory;
  }
  public int getMandatory() {
    return mandatory;
  }
  
  public boolean isRepeatable() {
   return max > 1;
  }

}
