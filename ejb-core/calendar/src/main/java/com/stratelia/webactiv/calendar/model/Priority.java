package com.stratelia.webactiv.calendar.model;


import java.io.Serializable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.calendar.control.CalendarException;

public class Priority implements Serializable, Comparable
{
    public static int[] getAllPriorities() {
      int[] result = {0, 1, 2, 3};
      return result;
    }
    /** The minimum allowable priority value. Please note that this refers
     * to the case where no priority is set. This case happens to be
     * represented by the value 0 which is the lowest integer value allowable.
     */
    public static final int MINIMUM_PRIORITY = 0;

    /** The maximum alloable priority value. Please note that this refers to
     * the highest possible integer value for priority. When interpreting
     * this value it is seen as the lowest priority because the value 1 is
     * the highest priority value.
     */
    public static final int MAXIMUM_PRIORITY = 9;
    
    private int priority = 2;

    /**
     * This is the default constructor. It is used by Castor. You should 
     * probably use the constructor that takes an integer argument in your
     * application code.
     */
    public Priority(){} 

    /**
     * The purpose of this method is to create a new priority property with
     * the given initial value.
     * @param newval The initial value of the priority property
     */
    public Priority(int newval)throws CalendarException
    {
        setValue(newval);
    }

    /**
     * The purpose of this method is to set the value of the priority
     * property.
     * @param newval The new value for the priority property
     */
    
     public void setValue(int newval) throws CalendarException
    {
        if(newval > MAXIMUM_PRIORITY)
        {
           SilverTrace.warn("calendar", "Priority.setValue(int newval)", "calendar_MSG_GREATER_MAXIMUM_PRIORITY","priority = MAXIMUM_PRIORITY =" + MAXIMUM_PRIORITY);
           newval = MAXIMUM_PRIORITY;
        }
        else if(newval < MINIMUM_PRIORITY)
        {
           SilverTrace.warn("calendar", "Priority.setValue(int newval)", "calendar_MSG_LOWER_MINIMUM_PRIORITY","priority = MINIMUM_PRIORITY =" + MINIMUM_PRIORITY);
           newval = MINIMUM_PRIORITY;
        }

       priority = newval;

    }
   /*
    public void setValue(int newval)
    {
        if(newval > MAXIMUM_PRIORITY)
        {
            throw new EJBException("The priority has to be greater than or equals to 0.");
        }
        else if(newval < MINIMUM_PRIORITY)
        {
            throw new EJBException("The priority has to be lower than or equals to 9");
        }
        else
        {
            priority = newval;
        }
    }
    */
    public int getValue() {
      return priority;
    }
           
    public int compareTo(final java.lang.Object other) {
      if (other == null) return 1;
      if (! (other instanceof Priority)) return 0;
      Priority tmp = (Priority) other;
      if ((getValue() == 0) && (tmp.getValue() != 0)) return -1;
      if ((getValue() != 0) && (tmp.getValue() == 0)) return 1;
      return tmp.getValue() - getValue();
    }
}     
