package com.silverpeas.util.cryptage;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.KeyTransRecipientInformation;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SilverCryptFactoryAsymetric
{
	// Singleton pour gèrer une seule Map de trousseaux de clés
	private static SilverCryptFactoryAsymetric factory = null;
	
	private SilverCryptFactoryAsymetric()
	{
	}
	
	private Map keyMap = new HashMap();
	
	public static SilverCryptFactoryAsymetric getInstance()
	{
		if (factory == null)
			factory = new SilverCryptFactoryAsymetric();
		
		return factory;
	}	
	
	public byte[] goCrypting(String stringUnCrypted, String fileName) throws CryptageException
	{
		try {
		        // Chargement de la chaine à crypter
				byte[] buffer = stringToByteArray(stringUnCrypted);
				
		        // Chiffrement du document
				CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
		        // La variable cert correspond au certificat du destinataire
		        // La clé publique de ce certificat servira à chiffrer la clé symétrique
		        gen.addKeyTransRecipient((java.security.cert.X509Certificate)this.getKeys(fileName).getCert());
		        
		        // Choix de l'algorithme à clé symétrique pour chiffrer le document.
		        // AES est un standard. Vous pouvez donc l'utiliser sans crainte.
		        // Il faut savoir qu'en france la taille maximum autorisée est de 128
		        // bits pour les clés symétriques (ou clés secrètes)
		        String algorithm = CMSEnvelopedDataGenerator.AES128_CBC;
		        CMSEnvelopedData envData = gen.generate(
		                                        new CMSProcessableByteArray(buffer),
		                                        algorithm, "BC");
	
		        byte[] pkcs7envelopedData = envData.getEncoded();
		        
		        return  pkcs7envelopedData;
		        
		        //return String.valueOf(pkcs7envelopedData);
		        
		} catch (CryptageException e) {
	        throw e;
	    } catch (Exception e) {
				
	    	throw new CryptageException(
					"SilverCryptFactory.goCrypting",
					SilverpeasException.ERROR,
					"util.CRYPT_FAILED", e);
		}
	}
	
	public String goUnCrypting(byte[] stringCrypted, String fileName) throws CryptageException
	{
		try {
			// Chargement de la chaine à déchiffrer
	        byte[] pkcs7envelopedData = stringCrypted;
			
	        // Déchiffrement de la chaine
	        CMSEnvelopedData ced = new CMSEnvelopedData(pkcs7envelopedData);
	        Collection recip = ced.getRecipientInfos().getRecipients();

	        KeyTransRecipientInformation rinfo = (KeyTransRecipientInformation)
	            recip.iterator().next();
	        // privatekey est la clé privée permettant de déchiffrer la clé secrète
	        // (symétrique)
	        byte[] contents = rinfo.getContent(this.getKeys(fileName).getPrivatekey(), "BC");
	        
	        return byteArrayToString(contents);
	        
		} catch (CryptageException e) {
		        throw e;
		} catch (Exception e) {
			throw new CryptageException(
					"SilverCryptFactory.goUnCrypting",
					SilverpeasException.ERROR,
					"util.UNCRYPT_FAILED", e);
		}
	}
	
	public void addKeys(String filename, String password) throws CryptageException
	{// ajout d'une trousseau de clé à partir d'un chemin d'un fichier p12 + password
		synchronized (keyMap)
		{
			if(this.keyMap.containsKey(filename))
			{
				throw new CryptageException(
						"SilverCryptFactory.addKeys",
						SilverpeasException.ERROR,
						"util.KEY_ALREADY_IN");
			}else
			{
				try
				{
					FileInputStream file = new FileInputStream(filename);
					SilverCryptKeysAsymetric silverkeys = new SilverCryptKeysAsymetric(file, password);
					
					this.keyMap.put(filename,silverkeys);					
				}catch(Exception e)
				{
					throw new CryptageException(
							"SilverCryptFactory.addKeys",
							SilverpeasException.ERROR,
							"util.KEYS_CREATION_FAILED");
				}
			}
		}
	}
	
	private SilverCryptKeysAsymetric getKeys(String filename) throws CryptageException
	{// récupération du trousseau de clé!
		if(this.keyMap.containsKey(filename))
		{
			return (SilverCryptKeysAsymetric)this.keyMap.get(filename);
		}else
		{
			throw new CryptageException(
					"SilverCryptFactory.addKeys",
					SilverpeasException.ERROR,
					"util.KEY_NOT_FOUND");
		}
	}
	
	private String byteArrayToString(byte[] bArray)
	{// A n'utiliser qu'avec des Strings décryptés!!!
		return new String(bArray);
	}
	
	private byte[] stringToByteArray( String theString)
	{
		return theString.getBytes(); 
	}
	
}
