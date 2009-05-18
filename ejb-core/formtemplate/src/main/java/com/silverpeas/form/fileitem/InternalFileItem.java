package com.silverpeas.form.fileitem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;

/**
 * File item created manually, without being retrieved from an HTTP request.
 * Used to update an imported publication's form.
 * 
 * @author Antoine HEDIN
 */
public class InternalFileItem implements FileItem {
	
	private String fieldName;
	private String value;
	
	public InternalFileItem(String fieldName, String value) {
		setFieldName(fieldName);
		setValue(value);
	}
	
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	public String getFieldName()
	{
		return fieldName;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String getString()
	{
		return value;
	}
	
	public void setFormField(boolean formField)
	{
		
	}
	
	public boolean isFormField()
	{
		return true;
	}
	
	public void delete()
	{
	}

	public byte[] get()
	{
		return null;
	}

	public String getContentType()
	{
		return null;
	}

	public InputStream getInputStream()
		throws IOException
	{
		return null;
	}

	public String getName()
	{
		return null;
	}

	public OutputStream getOutputStream()
		throws IOException
	{
		return null;
	}

	public long getSize()
	{
		return 0;
	}

	public String getString(String arg0)
		throws UnsupportedEncodingException
	{
		return null;
	}

	public boolean isInMemory()
	{
		return false;
	}

	public void write(File arg0)
		throws Exception
	{	
	}

}