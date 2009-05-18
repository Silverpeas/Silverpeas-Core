/*
 * Created by IntelliJ IDEA.
 * User: mikhail_nikolaenko
 * Date: Aug 15, 2002
 * Time: 3:34:07 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.stratelia.webactiv.searchEngine.model;

public class AxisFilterNode
{
    private String property;
    private String value;

    public AxisFilterNode( String property, String value)
    {
        this.property = property;
        this.value = value;
    }

    public String getPriperty()
    {
        return property;
    }

    public String getValue()
    {
        return value;
    }
}
