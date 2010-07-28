<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<c:set var="browseContext" value="${requestScope.browseContext}" />

<html>
    <head>
    <view:looknfeel />



    <style type="text/css">
        * {
            margin: 0;
            padding: 0;
        }

        /* message display page */
        .message_list {
            list-style: none;
            margin: 0;
            padding: 0;

            width: 100%;
        }
        .message_list li {
            padding: 0px;
            margin: 3px;


        }
        .message_table rd {
            padding: 0px;
            margin: 3px;


        }
        #recherche {
            background-image:
                url(/silverpeas/admin/jsp/icons/silverpeasV5/recherche.jpg);
            background-repeat: no-repeat;

        }
        .index {
            text-decoration: underline;
            color: black;
        }
        .active {

            color:blue
        }
        .mail {

            color: blue;
        }
        .accordion {
            width: 480px;
            border-bottom: solid 1px #c4c4c4;
        }
        .accordion h3 {
            background: #e9e7e7 url(/silverpeas/directory/jsp/icons/arrow-square.gif) no-repeat right -51px;
            padding: 7px 15px;
            margin: 0;
            font: bold 120%/100% Arial, Helvetica, sans-serif;
            border: solid 1px #c4c4c4;
            border-bottom: none;
            cursor: pointer;
        }
        .accordion h3:hover {
            background-color: #e3e2e2;
        }
        .accordion h3.active {
            background-position: right 5px;
        }
        .accordion p {
            background: #f7f7f7;
            margin: 0;
            padding: 10px 15px 20px;
            border-left: solid 1px #c4c4c4;
            border-right: solid 1px #c4c4c4;
        }

    </style>
</head>




<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<c:url value="/Rdirectory/Directory" var="Directory" />
 <c:set var="user" value="${requestScope.user}" />
<view:window>

    <view:frame >

        <li >
                               <view:board>

                                    <table border="0" >

                                        <tbody >
                                            <tr >
                                                <td>
                                                 <img src="<c:url value="${vignette_url}" />" width="110" height="110"/>
                                                </td>
                                                <td style="vertical-align: top">
                                                    <div >
                                                        <b style="top: 10px">${user.lastName} ${user.firstName}</b> <br /><br />

                                                        <a class="mail" href="mailto:${user.eMail}">${user.eMail}</a>
                                                    </div>
                                                </td>
                                                <td style="vertical-align: top" >

                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>



                                    <br />
                                </view:board>
                            </li>
    </view:frame>

</view:window>

</body>
</html>
