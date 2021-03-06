<%@ page buffer="100kb" pageEncoding="utf-8" contentType="text/html; charset=utf-8" language="java" %>
<%@ taglib uri="/WEB-INF/tlds/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/eurodyn.tld" prefix="ed" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ed:breadcrumbs-push label="XML Services" url="/index.jsp" level="0"/>

<%
pageContext.setAttribute("org.apache.struts.globals.XHTML", "true", 1);

String a=request.getContextPath();
session.setAttribute("webRoot",a==null?"":a);
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache");
response.setHeader("Cache-Control","no-store");
response.setDateHeader("Expires", 0);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <title>
            <tiles:importAttribute name="title" scope="request"/>
            <bean:message name="title"/>
        </title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <!--  EEA central styles -->
        <link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/print.css" media="print" />
        <link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/handheld.css" media="handheld" />
        <link rel="stylesheet" type="text/css" href="http://www.eionet.europa.eu/styles/eionet2007/screen.css" media="screen" />
        <!--  local style -->
        <link rel="stylesheet" type="text/css" href="<c:url value="/css/main.css"/>" media="screen" />
        <link rel="shortcut icon" href="<c:url value="/images/favicon.ico"/>" type="image/x-icon" />
        <!-- jQuery javascripts and css-->
        <link href="<c:url value="/css/smoothness/jquery-ui-1.10.3.min.css" />" rel="stylesheet" type="text/css"/>
        <script type="text/javascript" src="<c:url value="/scripts/jquery-1.9.1.min.js" />"></script>
        <script type="text/javascript" src="<c:url value="/scripts/jquery-ui-1.10.3.min.js" />"></script>
        <script type="text/javascript" src="<c:url value="/scripts/jquery-validate-1.11.1.min.js" />"></script>
        <!-- local javascripts -->
        <script type="text/javascript" src="<c:url value="/scripts/admin.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/scripts/pageops.js"/>"></script>

        <tiles:insert attribute="specific_header" ignore="true"/>
    </head>

    <body>
        <%-- include header --%>
        <tiles:insert attribute="header"/>

        <%-- include Error display --%>
        <%-- move system-msg and error-msg below the  <h1> heading --%>
        <%-- tiles:insert definition="Error" /> --%>

        <%-- include body --%>
        <tiles:insert attribute="body"/>

        <tiles:useAttribute id="showFooter" name="showFooter"/>
        <%-- include footer --%>
        <tiles:insert attribute="footer">
            <tiles:put name="showFooter" beanName="showFooter" />
        </tiles:insert>


    </body>

</html>
