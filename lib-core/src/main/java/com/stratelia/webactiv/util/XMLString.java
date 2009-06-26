/*
 * XMLString.java
 *
 * Created on 12 avril 2001
 * By Franck Rageade
 */
 
package com.stratelia.webactiv.util;

public class XMLString {

	private final static String encoding = "UTF8";

	public XMLString() {
	}

	public static String toXMLString(String s) {
		String retour = null;
		try {
			byte[] readbytes = s.getBytes(encoding);
			// return new String(readbytes, encoding);
			retour = new String(readbytes);
		} catch (java.io.UnsupportedEncodingException e) {
			// e.printStackTrace();
		}
		return retour;
	}

	public static String fromXMLString(String s) {
		String retour = null;
		try {
			byte[] readbytes = s.getBytes();
			retour = new String(readbytes, encoding);
		} catch (java.io.UnsupportedEncodingException e) {
			// e.printStackTrace();
		}
		return retour;
	}

	/*public final static void main(String[] args) {
		String s1 = "Bonjour";
		String s2 = "Pépé";
		String s3 = "oh ! oui alors ?";
		System.out.println(s1);
		System.out.println(toXMLString(s1));
		System.out.println(fromXMLString(toXMLString(s1)));
		System.out.println(s2);
		System.out.println(toXMLString(s2));
		System.out.println(fromXMLString(toXMLString(s2)));
		System.out.println(s3);
		System.out.println(toXMLString(s3));
		System.out.println(fromXMLString(toXMLString(s3)));
	}*/

}