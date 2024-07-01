/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.filter;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.web.test.stub.TestHttpResponse;

import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class MassiveWebSecurityFilterIT {

    private static final String DATASET_SCRIPT =
            "/org/silverpeas/core/web/filter/massive-web-security-database.sql";

    @Rule
    public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(DATASET_SCRIPT);

    @Deployment
    public static Archive<?> createTestArchive() {
        return WarBuilder4WebCore.onWarForTestClass(MassiveWebSecurityFilterIT.class)
                .addRESTWebServiceEnvironment()
                .testFocusedOn(warBuilder -> warBuilder.addPackages(true, "org.silverpeas.core.web.filter"))
                .build();
    }

    private static final String COMMON_PARAMETER_NAME = "parameterName";
    private static final String SKIPPED_PARAMETER_NAME = "editor";

    private static final String[] PUNCTUATIONS = {",", "?", "-", ".", "!", "&", "~", "'", ";"};
    private static final String[] SQL_PRIVILEGES =
            {"seLect", "insert", "upDate", "Delete", "references", "alter", "index", "all"};

    @Test
    public void secureAgainstXss() {
        assertXSS(skippedParam("script"), false);
        assertXSS(param("script"), false);

        assertXSS(skippedParam("<script>"), false);
        assertXSS(param("<script>"), true);

        assertXSS(param("<sCRipt >"), true);
        assertXSS(param("< script >"), true);
        assertXSS(param("<  Script  >"), true);
        assertXSS(param("s</script>"), true);
        assertXSS(param("</script >"), true);
        assertXSS(param("</scripT  >"), true);
        assertXSS(param("ä< /script>"), true);
        assertXSS(param("Â</ script>"), true);
        assertXSS(param("< /scRipt"), true);
        assertXSS(param("< script"), true);
        assertXSS(param("< \n\n\n\t\nscript"), true);
        assertXSS(param("< script\t"), true);
        assertXSS(param("< script\n"), true);
        assertXSS(param("<<<< \tscript\n"), true);
        assertXSS(param("< script <br>"), true);
        assertXSS(param("< script <br>  >"), true);
        assertXSS(param("< /script <br>"), true);
        assertXSS(param("</ script <br>  >"), true);
        assertXSS(param("</sCript"), true);
        assertXSS(param("< \\ script  >"), false);
        assertXSS(param("<script " + "type=\"text/javascript\">var webContext='/silverpeas';</script>"),
                true);
        assertXSS(param("<script " +
                        "type=\"text/javascript\" src=\"/silverpeas/util/javaScript/silverpeas.js\"></script>"),
                true);
    }

    private void assertXSS(URLConfigTest urlConfigTest, boolean expected) {
        performInjectionDetectionAssert(urlConfigTest, expected, "XSS");
    }

    @Test
    public void secureAgainstSqlGRANT() {
        assertSQL(skippedParam("script"), false);
        assertSQL(param("script"), false);

        assertSQL(skippedParam("GRANT SELECT ON suppliers TO smithj"), false);
        assertSQL(param("GRANT COOK ON suppliers TO smithj"), false);
        assertSQL(param("GRANT SELECT ON suppliers TO smithj"), true);
        assertSQL(param("miGRANT SELECT ON suppliers TO smithj"), false);
        assertSQL(param("SGRANT SELECT ON suppliers TO smithj"), false);
        assertSQL(param("2GRANT SELECT ON suppliers TO smithj"), false);
        assertSQL(param("intéGRANT SELECT ON suppliers TO smithj"), false);
        assertSQL(param("ÂGRANT SELECT ON suppliers TO smithj"), false);
        assertSQL(param("s \t\f\nGRANT SELECT ON suppliers TO smithj"), true);
        assertSQL(param("s GRANT SELECT ON suppliers TO smithj"), true);
        assertSQL(param(";GRANT SELECT ON suppliers TO smithj"), true);
        assertSQL(param("GRANT/* SELECT */ON suppliers TO smithj"), true);
        assertSQL(param("GRANT/* SELECT */ON suppliers FROM smithj"), false);
        assertSQL(param("GRANT/* */SELECT/* */ON suppliers FROM smithj"), false);
        assertSQL(param("GRANT/* */SELECT /* */ON suppliers FROM smithj"), false);
        assertSQL(param("GRANT/* */SELECT/* */ ON suppliers FROM smithj"), false);
        assertSQL(param("GRANT/* */SELECT/* */ON suppliers TO smithj"), true);
        assertSQL(param("GRANT/* */SELECT/* */ON suppliers/* */TO smithj"), true);
        assertSQL(param("GRANT /* */TOTO,SELECT,UPDATE/* */ ON suppliers TO smithj"), true);
        assertSQL(param("GRANT SELECT \t\f\rON suppliers TO smithj"), true);
        for (String privilege : SQL_PRIVILEGES) {
            assertSQL(param("GRANT " + privilege + " ON suppliers TO smithj"), true);
            assertSQL(param("GRANT \t\f\n \t\f\n " + privilege + "\t\f\n , \t\f\n" + privilege +
                    " \t\f\n \t\f\n \t\f\nON suppliers TO smithj"), true);
            assertSQL(param("GRANT \t\f\n \t\f\n " + privilege + "\t\f\n \t\f\n" + privilege +
                    " \t\f\n \t\f\n \t\f\nON suppliers TO smithj"), true);
            assertSQL(param("GRANT /*" + privilege + "*/" + privilege + " ON suppliers TO smithj"),
                    true);
            assertSQL(param("GRANT " + privilege + "S ON suppliers TO smithj"), false);
        }
        assertSQL(param("GRANT SELECTON suppliers TO smithj"), false);
        assertSQL(param("GRANTSELECT ON suppliers TO smithj"), false);
        assertSQL(param("GRANTSELECTON suppliers TO smithj"), false);
        for (String ponctuation : PUNCTUATIONS) {
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "GRANT SELECT" + ponctuation + "ON suppliers TO smithj"),
                    false);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "GRANT SELECT" + ponctuation + " ON suppliers TO smithj"),
                    false);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "GRANT SELECT " + ponctuation + "ON suppliers TO smithj"),
                    true);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "GRANT" + ponctuation + "SELECT ON suppliers TO smithj"),
                    false);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "GRANT " + ponctuation + "SELECT ON suppliers TO smithj"),
                    true);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "GRANT" + ponctuation + " SELECT ON suppliers TO smithj"),
                    false);
        }
        assertSQL(param("GRANT SELECsT, INSERsT, UPDAsTE, DELsETE ON suppliers TO smithj"), false);
    }

    @Test
    public void secureAgainstSqlREVOKE() {
        assertSQL(skippedParam("REVOKE SELECT ON suppliers FROM smithj"), false);
        assertSQL(param("REVOKE COOK ON suppliers FROM smithj"), false);
        assertSQL(param("miREVOKE SELECT ON suppliers FROM smithj"), false);
        assertSQL(param("SREVOKE SELECT ON suppliers FROM smithj"), false);
        assertSQL(param("2REVOKE SELECT ON suppliers FROM smithj"), false);
        assertSQL(param("intéREVOKE SELECT ON suppliers FROM smithj"), false);
        assertSQL(param("ÂREVOKE SELECT ON suppliers FROM smithj"), false);
        assertSQL(param("s \t\f\nREVOKE SELECT ON suppliers FROM smithj"), true);
        assertSQL(param("s REVOKE SELECT ON suppliers FROM smithj"), true);
        assertSQL(param(";REVOKE SELECT ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE SELECT ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE/* SELECT */ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE/* */SELECT/* */ON suppliers TO smithj"), false);
        assertSQL(param("REVOKE/* */SELECT /* */ON suppliers TO smithj"), false);
        assertSQL(param("REVOKE/* */SELECT/* */ ON suppliers TO smithj"), false);
        assertSQL(param("REVOKE/* */ SELECT /* */ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE /* */SELECT/* */ ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE /* */SELECT/* */ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE /* */TOTO,SELECT,UPDATE/* */ ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE \t\f\nSELECT \t\f\rON suppliers FROM smithj"), true);
        for (String privilege : SQL_PRIVILEGES) {
            assertSQL(param("REVOKE " + privilege + " ON suppliers FROM smithj"), true);
            assertSQL(param("REVOKE \t\f\n \t\f\n " + privilege + "\t\f\n , \t\f\n" + privilege +
                    " \t\f\n \t\f\n \t\f\nON suppliers FROM smithj"), true);
            assertSQL(param("REVOKE \t\f\n \t\f\n " + privilege + "\t\f\n \t\f\n" + privilege +
                    " \t\f\n \t\f\n \t\f\nON suppliers FROM smithj"), true);
            assertSQL(param("REVOKE " + privilege + "S ON suppliers FROM smithj"), false);
        }
        assertSQL(param("REVOKE SELECTON suppliers FROM smithj"), false);
        assertSQL(param("REVOKESELECT ON suppliers FROM smithj"), false);
        assertSQL(param("REVOKESELECTON suppliers FROM smithj"), false);
        for (String ponctuation : PUNCTUATIONS) {
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "REVOKE SELECT" + ponctuation + "ON suppliers FROM smithj"),
                    false);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "REVOKE SELECT" + ponctuation + " ON suppliers FROM smithj"),
                    false);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "REVOKE SELECT " + ponctuation + "ON suppliers FROM smithj"),
                    true);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "REVOKE" + ponctuation + "SELECT ON suppliers FROM smithj"),
                    false);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "REVOKE " + ponctuation + "SELECT ON suppliers FROM smithj"),
                    true);
            assertSQL(
                    param(COMMON_PARAMETER_NAME, "REVOKE" + ponctuation + " SELECT ON suppliers FROM smithj"),
                    false);
        }
        assertSQL(param("\t\f\n \t\f\n REVOKE SELECT,INSERT,UPDATE,DELETE ON suppliers FROM smithj"),
                true);
        assertSQL(param("REVOKE SELECT , INSERT , UPDATE , DELETE ON suppliers FROM smithj"), true);
        assertSQL(param("REVOKE SELECT , INSERT , UPDATE , DELETE ONs suppliers FROM smithj"), false);
        assertSQL(param("REVOKE SELECsT, INSERsT, UPDAsTE, DELsETE ON suppliers FROM smithj"), false);
    }

    @Test
    public void secureAgainstSqlCREATE_DROP_ALTER() {
        String[] actions = {"cReAte", "DROP", "ALTER"};
        String[] resources = {"TAble", "DATaBase", "sChemA"};

        for (String action : actions) {
            for (String resource : resources) {
                assertSQL(skippedParam(action + " " + resource + " aResource"), false);
                assertSQL(param(action + " " + resource + " aResource"), true);
                assertSQL(param("s" + action + " " + resource + " aResource"), false);
                assertSQL(param("é" + action + " " + resource + " aResource"), false);
                assertSQL(param("Ù" + action + " " + resource + " aResource"), false);
                assertSQL(param("Ù;" + action + " " + resource + " aResource"), true);
                assertSQL(param(" \t\f\n \t\f\n \t\f\n" + action + " " + resource + " aResource"), true);
                assertSQL(param(" \t\f\n \t\f\n \t\f\n" + action + " " + resource + "s aResource"), false);
            }
        }
    }

    @Test
    public void secureAgainstSqlSELECT() {
        assertSQL(skippedParam("SELECT * FROM catalogs"), false);
        assertSQL(param("SELECT ; FRoM catalog"), false);
        assertSQL(param("SELECT ; FRoM catalogs SELECT 1 FROM supliers"), true);
        assertSQL(param("SELECT supliers; FRoM supliers SELECT 1 FROM supliers"), false);
        assertSQL(param("SELECT catalogs; FRoM supliers SELECT 1 FROM supliers"), true);
        assertSQL(param("SELECT sddcatalogs; FRoM supliers SELECT 1 FROM supliers"), false);
        assertSQL(param("SELECT sddcatalogs; FRoM supliers SELECT 1 FROM catalogs"), true);
        assertSQL(param("SELECT sddcatalogs; FRoM supliers SELECT 1 FROM scatalogs"), false);
        assertSQL(param("SELECT ; FRoM catalogs"), true);
        assertSQL(param("miSELECT * FRoM catalogs"), false);
        assertSQL(param("SSELECT * FRoM catalogs"), false);
        assertSQL(param("2SELECT * FRoM catalogs"), false);
        assertSQL(param("intéSELECT * FRoM catalogs"), false);
        assertSQL(param("ÂSELECT * FRoM catalogs"), false);
        assertSQL(param("s \t\f\nSELECT * FRoM catalogs"), true);
        assertSQL(param("SELECT * FRoM catalogs"), true);
        assertSQL(param("SELECT/* */FRoM catalogs"), true);
        assertSQL(param("SELECT/* */ FRoM catalogs"), true);
        assertSQL(param("SELECT /* */FRoM catalogs"), true);
        assertSQL(param("SELECT * FRoMcatalogs"), false);
        assertSQL(param("seLect * from st_space_space"), true);
        assertSQL(param("seLect * from zz_st_space_space"), true);
        assertSQL(param("seLect * from st_space_space_i18n"), true);
        assertSQL(param("seLect * from zz_st_space_space_i18n"), false);
        assertSQL(param("SELECT * * FRoM catalogs"), true);
        assertSQL(param("SELECT *,* FRoMs catalogs"), false);
        assertSQL(param("SELECT supl.*,* FRoM zdzefze catalogs supl"), true);
        assertSQL(param("SELECT supl.* * FRoM sdecatalogs supl"), false);
        assertSQL(param("SELECT supl.* toto FRoM catalogs supl"), true);
        assertSQL(param("SELECT supl.* toto FRoM catalogsÄ supl"), false);
        assertSQL(param("SELECT su1pl.k-l_m.* \t\f\n , \t\f\n _su1pl_z-ed_.da_t-a010 FRoM catalogs"),
                true);
        assertSQL(param("SELECT su1pl.k-l_m.* , ,  _su1pl_z-ed_.da_t-a010 FRoM catalOGs"), true);
        assertSQL(param("SELECT su1pl.k-l_m.* , ,  _su1pl_z-ed_.da_t-a010 FRoM catalogss"), false);
        assertSQL(param(
                "SELECT * FRoM (select toto from (select * from suppliers supl where supl in (select id " +
                        "from catalogs))"), true);
        assertSQL(param(
                "SELECT * FRoM (select toto from (select * from suppliers supl where supl in (select id " +
                        "from tata))"), false);
        assertSQL(param(
                "SELECT *, 'catalogs' FRoM (select toto from (select * from suppliers supl where supl in " +
                        "(select id from tata))"), true);
    }

    @Test
    public void secureAgainstSqlINSERT() {
        assertSQL(skippedParam("INSERT inTo catalogs (id) values"), false);
        assertSQL(param("INSERT inTo catalogs (id) valuess"), false);
        assertSQL(param("INSERT inTo catalogs (id) values"), true);
        assertSQL(param("INSERT inTo catalogs (id) values \t\n"), true);
        assertSQL(param("INSERT inTo/* */catalogs (id) values"), true);
        assertSQL(param("INSERT inTo /* */catalogs (id) values"), true);
        assertSQL(param("INSERT inTo/* */ catalogs (id) values"), true);
        assertSQL(param("INSERT/* */inTo catalogs (id) values"), true);
        assertSQL(param("INSERT/* */ inTo catalogs (id) values"), true);
        assertSQL(param("INSERT /* */inTo catalogs (id) values"), true);
        assertSQL(param("INSERT inTo catalogs (id)/* */values"), true);
        assertSQL(param("INSERT inTo catalogs (id) /* */values"), true);
        assertSQL(param("INSERT inTo catalogs (id)/* */ values"), true);
        assertSQL(param("INSERT inTo catalogs/* */values"), true);
        assertSQL(param("INSERT inTo catalogs /* */values"), true);
        assertSQL(param("INSERT inTo catalogs/* */ values"), true);
        assertSQL(param("INSERT inTo catalos/* */ values"), false);
        assertSQL(param("sINSERT inTo catalogs (id) values"), false);
        assertSQL(param("INSERT inTo catalogs (id) value"), false);
        assertSQL(param("INSERT inTo catalogss (id) values"), false);
        assertSQL(param("INsERT inTo catalogsô (id) values"), false);
        assertSQL(param("INSERT inTo catalogs\t\f\n (id) values"), true);
        assertSQL(param("INSERT ino catalogs\t\f\n (id) values"), false);
    }

    @Test
    public void secureAgainstSqlUPDATE() {
        assertSQL(skippedParam("uPdaTe catalogs set"), false);
        assertSQL(param("uPdaTe catalogs sets"), false);
        assertSQL(param("uPdaTe catalogs set"), true);
        assertSQL(param("uPdaTe st_space_space set"), true);
        assertSQL(param("uPdaTe zz_st_space_space set"), true);
        assertSQL(param("uPdaTe st_space_space_i18n set"), true);
        assertSQL(param("uPdaTe zz_st_space_space_i18n set"), false);
        assertSQL(param("uPdaTe/* */catalogs/* */set"), true);
        assertSQL(param("uPdaTe /* */catalogs/* */set"), true);
        assertSQL(param("uPdaTe/* */ catalogs /* */set"), true);
        assertSQL(param("uPdaTe/* */catalogs /* */set"), true);
        assertSQL(param("uPdaTe/* */catalogs/* */ set"), true);
        assertSQL(param("suPdaTe catalogs set"), false);
        assertSQL(param("uPdaTe\t\f\ncatalogs\t\f\nset"), true);
        assertSQL(param("uPdaTe\r\ncatalogs\r\nset"), true);
        assertSQL(param("uPdaTe\r\nscatalogs\r\nset"), false);
        assertSQL(param("uPdaTe\r\ncatalogss\r\nset"), false);
        assertSQL(param("uPdaTe\r\ncatalogsë\r\nset"), false);
        assertSQL(param("uPdaTe\r\ncatalogs^y\r\nset"), true);
        assertSQL(param("uPdaTe\r\nµcatalogs\r\nset"), false);
    }

    @Test
    public void secureAgainstSqlDELETE() {
        assertSQL(skippedParam("DeleTe from catalogs"), false);
        assertSQL(param("DeleTe from catalogs"), true);
    }

    private void assertSQL(URLConfigTest urlConfigTest, boolean expected) {
        performInjectionDetectionAssert(urlConfigTest, expected, "SQL");
    }

    private void performInjectionDetectionAssert(URLConfigTest urlConfigTest,
                                                 boolean injectionHasToBeDetected, String messagePart) {
        for (PARAMETER_CONFIG parameterConfig : PARAMETER_CONFIG.values()) {
            TestHttpResponse httpResponse = new TestHttpResponse();
            HttpRequest httpRequest = urlConfigTest.createHttpRequest(parameterConfig);
            try {

                new MassiveWebSecurityFilter().doFilter(httpRequest, httpResponse,
                        (servletRequest, servletResponse) -> {
                            // nothing
                        });

                Pair<Integer, String> status = httpResponse.getActualStatus();
                if (injectionHasToBeDetected) {
                    assertThat(status.getFirst(), is(HttpServletResponse.SC_FORBIDDEN));
                    assertThat(status.getSecond().contains(messagePart), is(true));
                } else {
                    assertThat(status.getFirst(), not(HttpServletResponse.SC_FORBIDDEN));
                }
            } catch (Exception e) {
                assertThat(e.getMessage(), false, is(true));
            }
        }
    }

    enum PARAMETER_CONFIG {
        FIRST_PARAMETER_FIRST_VALUE, FIRST_PARAMETER_SECOND_VALUE, SECOND_PARAMETER_FIRST_VALUE,
        SECOND_PARAMETER_SECOND_VALUE
    }

    private URLConfigTest skippedParam(String parameterValue) {
        return param(SKIPPED_PARAMETER_NAME, parameterValue);
    }

    private URLConfigTest param(String parameterValue) {
        return param(COMMON_PARAMETER_NAME, parameterValue);
    }

    private URLConfigTest param(final String parameterName, String parameterValue) {
        return new URLConfigTest(parameterName, parameterValue);
    }

    private static class URLConfigTest {
        private final String parameterName;
        private final String parameterValue;

        protected URLConfigTest(final String parameterName, String parameterValue) {
            this.parameterName = parameterName;
            this.parameterValue = parameterValue;
        }

        public HttpRequest createHttpRequest(PARAMETER_CONFIG parameterConfig) {
            org.silverpeas.web.test.stub.TestHttpRequest request = new org.silverpeas.web.test.stub.TestHttpRequest("GET", parameterName + " -> " + parameterValue);
            switch (parameterConfig) {
                case SECOND_PARAMETER_SECOND_VALUE:
                    request.addParameter("otherParameterName", "tata")
                            .addParameter("otherParameterName", "toto");
                case FIRST_PARAMETER_SECOND_VALUE:
                    request.addParameter(parameterName, "tata")
                            .addParameter(parameterName, parameterValue);
                    break;
                case SECOND_PARAMETER_FIRST_VALUE:
                    request.addParameter("otherParameterName", "tata")
                            .addParameter("otherParameterName", "toto");
                case FIRST_PARAMETER_FIRST_VALUE:
                default:
                    request.addParameter(parameterName, parameterValue)
                            .addParameter(parameterName, "tata");
                    break;
            }
            return HttpRequest.decorate(request);
        }
    }
}
