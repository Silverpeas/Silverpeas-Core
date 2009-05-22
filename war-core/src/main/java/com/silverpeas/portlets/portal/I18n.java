/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */

package com.silverpeas.portlets.portal;

import java.io.UnsupportedEncodingException;

/**
 * I18n class provides methods to decode the value based on the character sets.
 */

public class I18n {
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String ASCII_CHARSET = "ISO-8859-1";
    
    public static String decodeCharset(String s, String charset) {
        if (s == null) {
            return null;
        }
        
        try {
            byte buf[] = s.getBytes(ASCII_CHARSET);
            return new String(buf, 0, buf.length, charset);
        } catch (UnsupportedEncodingException uee) {
            return s;
        }
    }
    
    public static String encodeCharset(String s, String charset) {
        if (s == null) {
            return null;
        }
        
        try {
            byte buf[] = s.getBytes(charset);
            return new String(buf, 0, buf.length, ASCII_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            return s;
        }
    }
}

