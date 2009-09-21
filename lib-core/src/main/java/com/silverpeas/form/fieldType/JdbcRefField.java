package com.silverpeas.form.fieldType;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;

public class JdbcRefField implements Field
{
	
	static public final String TYPE = "jdbcRef";
	
	private String value = "";
	
	public JdbcRefField()
	{
		
	}
	
	public String getTypeName()
	{
		return TYPE;
	}

	public boolean acceptObjectValue(Object value)
	{
		return false;
	}

	public boolean acceptStringValue(String value)
	{
		return false;
	}

	public boolean acceptValue(String value)
	{
		return false;
	}

	public boolean acceptValue(String value, String lang)
	{
		return false;
	}

	public Object getObjectValue()
	{
		return getStringValue();
	}

	public String getStringValue()
	{
		return value;
	}
	
	public void setStringValue(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return getStringValue();
	}

	public String getValue(String lang)
	{
		return getStringValue();
	}

	public boolean isNull()
	{
		return (getStringValue() == null || getStringValue().trim().equals(""));
	}

	public void setNull()
		throws FormException
	{
		setStringValue(null);
	}

	public void setObjectValue(Object value)
		throws FormException
	{
	    if (value instanceof String)
	    {
			setStringValue((String) value);
		}
	    else
	    {
			if (value != null)
			{
				throw new FormException("JdbcRefField.setObjectValue", "form.EXP_NOT_A_STRING");
			}
			else
			{
				setNull();
			}
		}		
	}

	public void setValue(String value) throws FormException
	{
		setStringValue(value);
	}

	public void setValue(String value, String lang) throws FormException
	{
		setStringValue(value);
	}
	
	public int compareTo(Object o)
	{
		String s = getStringValue();
		if (s == null)
		{
			s = "";
		}
		if (o instanceof JdbcRefField)
		{
			String t = ((JdbcRefField) o).getStringValue();
			if (t == null)
			{
				t = "";
			}
			return s.compareTo(t);
		}
		else if (o instanceof Field)
		{
			String t = ((Field) o).getValue("");
			if (t == null)
			{
				t = "";
			}
			return s.compareTo(t);
		}
		else
		{
			return -1;
		}
	}

}