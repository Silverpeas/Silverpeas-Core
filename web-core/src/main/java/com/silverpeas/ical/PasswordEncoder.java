package com.silverpeas.ical;

/**
 * Simple password encoder. Note, this is not meant to keep your password
 * secure. 
 * 
 * Created: Jan 03, 2007 12:50:56 PM
 * 
 * @author Andras Berkes
 */
public final class PasswordEncoder {

	public static final String encodePassword(String password) throws Exception {
		byte[] bytes = StringUtils.encodeString(password, StringUtils.UTF_8);
		String base64 = StringUtils.encodeBASE64(bytes);
		StringBuffer buffer = new StringBuffer(base64);
		String seed = Long.toString(System.currentTimeMillis());
		seed = seed.substring(seed.length() - 3);
		String encoded = seed + buffer.reverse().toString();
		return encoded.replace('=', '$');
	}

}
