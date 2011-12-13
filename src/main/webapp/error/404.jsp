<%@ page pageEncoding="utf-8" contentType="text/html; charset=utf-8" language="java" %>
<%@ taglib uri="/WEB-INF/tlds/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>

<%-- include header --%>
<tiles:insert page="/WEB-INF/tiles/TmpHeader.jsp" />

    <div class="error-msg">
        <bean:message key="label.error.404" arg0="<%=(String)request.getAttribute("javax.servlet.error.request_uri")%>" />
    </div>

<%-- include footer --%>
<tiles:insert page="/WEB-INF/tiles/TmpFooter.jsp">
    <tiles:put name="showFooter" type="string" value="true"/>
</tiles:insert>
