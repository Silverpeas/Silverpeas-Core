package com.stratelia.silverpeas.authentication;

public interface EncryptionInterface
 {
    public String encode(String str);
    public String decode(String str);
    public String decode(String str, String key, boolean extraCrypt);
    
 }
