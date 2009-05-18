package com.stratelia.webactiv.util;

import junit.framework.TestCase;

import com.mockrunner.mock.web.MockHttpServletRequest;

public class TestClientBrowserUtil extends TestCase {

  public void testInternetExplorer() {
    // 1.0 sous Windows 95 — Microsoft Internet Explorer/4.0b1 (Windows 95)
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Microsoft Internet Explorer/4.0b1 (Windows 95)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 1.5 sous Windows NT — Mozilla/1.22 (compatible; MSIE 1.5; Windows NT)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/1.22 (compatible; MSIE 1.5; Windows NT)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 2.0 sous Windows 95 — Mozilla/1.22 (compatible; MSIE 2.0; Windows 95)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/1.22 (compatible; MSIE 1.5; Windows NT)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 3.01 sous Windows 98 — Mozilla/2.0 (compatible; MSIE 3.01; Windows 98)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/2.0 (compatible; MSIE 3.01; Windows 98)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 5.0 sous SunOS — Mozilla/4.0 (compatible; MSIE 5.0; SunOS 5.9 sun4u; X11)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/1.22 (compatible; MSIE 1.5; Windows NT)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 5.2 sous Mac OS X — Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertFalse(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertTrue(ClientBrowserUtil.isMacintosh(request));
    // 5.5 sous Windows 2000 — Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 6.0 MSN Explorer sous Windows 98 — Mozilla/4.0 (compatible; MSIE 6.0; MSN 2.5; Windows 98)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 6.0; MSN 2.5; Windows 98)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 6.0 sous Windows XP avec le framework .Net installé — Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 6.0 sous Windows Server 2003 — Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 7.0 tournant sous Windows XP — Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 7.0 tournant sous Windows Vista — Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 7.0 sous Windows Server 2003 — Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 7.0 sous Windows Vista — Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)");
    assertTrue(ClientBrowserUtil.isInternetExplorer(request));
    assertFalse(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // Mozilla Firebird (avant le renommage en Firefox) —
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.5a) Gecko/20030728 Mozilla Firebird/0.6.1");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertFalse(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertTrue(ClientBrowserUtil.isMacintosh(request));
    // 1.0 (Hollandais) sous Windows XP —
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; nl-NL; rv:1.7.5) Gecko/20041202 Firefox/1.0");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 1.0.4 sous Ubuntu Linux, avec AMD64 —
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.7.6) Gecko/20050512 Firefox");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertFalse(ClientBrowserUtil.isWindows(request));
    assertTrue(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 1.0.4 sous FreeBSD 5.4 avec i386 —
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (X11; U; FreeBSD i386; en-US; rv:1.7.8) Gecko/20050609 Firefox/1.0.4");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertFalse(ClientBrowserUtil.isWindows(request));
    assertTrue(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 2.0 sous Windows XP -
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.8.1) Gecko/20061010 Firefox/2.0");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertTrue(ClientBrowserUtil.isWindows(request));
    assertFalse(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 2.0 sous Ubuntu Linux -
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (X11; U; Linux i686; fr; rv:1.8.1.1) Gecko/20060601 Firefox/2.0.0.1 (Ubuntu-edgy)");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertFalse(ClientBrowserUtil.isWindows(request));
    assertTrue(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
    // 2.0.0.6 sous Ubuntu Linux, avec AMD64 -
    request = new MockHttpServletRequest();
    request.setHeader("User-Agent",
        "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.6) Gecko/20071008 Ubuntu/7.10 (gutsy) Firefox/2.0.0.6");
    assertFalse(ClientBrowserUtil.isInternetExplorer(request));
    assertTrue(ClientBrowserUtil.isFirefox(request));
    assertFalse(ClientBrowserUtil.isOpera(request));
    assertFalse(ClientBrowserUtil.isSafari(request));
    assertFalse(ClientBrowserUtil.isWindows(request));
    assertTrue(ClientBrowserUtil.isUnix(request));
    assertFalse(ClientBrowserUtil.isMacintosh(request));
  }
}
