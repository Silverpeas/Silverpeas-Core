package com.silverpeas.util.cryptage;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SilverCryptFactorySymetric
{
	// Singleton pour gèrer une seule Map de trousseaux de clés
	private static SilverCryptFactorySymetric factory = null;
	private static Cipher cipherEncrypt = null;
	private static Cipher cipherDecrypt = null;
	private SilverCryptKeysSymetric silverCryptKeysSymetric = null;
	
	private SilverCryptFactorySymetric()
	{
	}
	
	static
	{
		try {
			cipherEncrypt = Cipher.getInstance("Blowfish");
			cipherDecrypt = Cipher.getInstance("Blowfish");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public static SilverCryptFactorySymetric getInstance()
	{
		if (factory == null)
			factory = new SilverCryptFactorySymetric();
		
		return factory;
	}	
	
	public synchronized byte[] goCrypting(String stringUnCrypted) throws CryptageException
	{
		SilverTrace.info("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_ENTER_METHOD", "stringUnCrypted = "+stringUnCrypted);
		//String crypted = "";
		byte[] cipherText = null;
		try {
			byte[] cipherBytes = stringUnCrypted.getBytes();
			
			//SilverTrace.debug("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_PARAM_VALUE", "Before getCipher");
			// get a DES cipher object and print the provider
			//Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			//Cipher cipher = Cipher.getInstance("Blowfish");
			//SilverTrace.debug("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_PARAM_VALUE", "After getCipher");
			
			// encrypt using the key and the plaintext
			cipherEncrypt.init(Cipher.ENCRYPT_MODE, this.getSymetricKeys().getKey());
			SilverTrace.debug("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_PARAM_VALUE", "After init");
			cipherText = cipherEncrypt.doFinal(cipherBytes);
			
			//crypted = new String(cipherText, "UTF8");
		} catch (Exception e) {
				
	    	throw new CryptageException(
					"SilverCryptFactory.goCrypting",
					SilverpeasException.ERROR,
					"util.CRYPT_FAILED", e);
		}
		SilverTrace.info("util", "SilverCryptFactorySymetric.goCrypting", "root.MSG_GEN_EXIT_METHOD", "cipherText = "+cipherText);
		//return crypted;
		return cipherText;
	}
	
	public synchronized String goUnCrypting(byte[] cipherBytes) throws CryptageException
	{
		SilverTrace.info("util", "SilverCryptFactorySymetric.goUnCrypting", "root.MSG_GEN_ENTER_METHOD");
		String uncrypted = "";
		try {
			//byte[] cipherBytes = stringCrypted.getBytes();
			
			//SilverTrace.debug("util", "SilverCryptFactorySymetric.goUnCrypting", "root.MSG_GEN_PARAM_VALUE", "Before getCipher");
			// get a DES cipher object and print the provider
			//Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			//Cipher cipher = Cipher.getInstance("Blowfish");
			//SilverTrace.debug("util", "SilverCryptFactorySymetric.goUnCrypting", "root.MSG_GEN_PARAM_VALUE", "After getCipher");
			
			cipherDecrypt.init(Cipher.DECRYPT_MODE, this.getSymetricKeys().getKey());
			
			SilverTrace.debug("util", "SilverCryptFactorySymetric.goUnCrypting", "root.MSG_GEN_PARAM_VALUE", "After init");

			byte[] newPlainText = cipherDecrypt.doFinal(cipherBytes);
			uncrypted = new String(newPlainText, "UTF8");

		} catch (Exception e) {
			throw new CryptageException(
					"SilverCryptFactory.goUnCrypting",
					SilverpeasException.ERROR,
					"util.UNCRYPT_FAILED", e);
		}
		SilverTrace.info("util", "SilverCryptFactorySymetric.goUnCrypting", "root.MSG_GEN_EXIT_METHOD", "uncrypted = "+uncrypted);
		return uncrypted;
	}
	
	public SilverCryptKeysSymetric getSymetricKeys() throws CryptageException
	{// récupération du trousseau de clé!
		if(silverCryptKeysSymetric == null)
		{
			silverCryptKeysSymetric = new SilverCryptKeysSymetric();
		}
		return silverCryptKeysSymetric;
	}
}
