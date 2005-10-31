<%@ page contentType="text/html; charset=UTF-8" 
%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/eurodyn.tld" prefix="ed" %>

<html:xhtml/>
	<div style="width:100%;">
		<div id="tabbedmenu">
			<ul>
				<li><a onclick="return submitTab(this);" style="color: black; text-decoration: none;" title="<bean:message key="label.config.ldap"/>" href="<bean:write name="webRoot" />/do/ldapForm"><bean:message key="label.config.ldap"/></a></li>
				<li class="currenttab"><span style="color: black; text-decoration: none;" title="<bean:message key="label.config.db"/>"><bean:message key="label.config.db"/></span></li>
			</ul>
		</div>
		<p>&nbsp;</p>
				
<div id="stylesheet" class="box"> 
	<div class="boxleft"> 
		<div class="boxtop"><div>
		</div>
	</div> 

			<ed:breadcrumbs-push label="DB configuration" level="1" />
			<h4><bean:message key="label.config.db.admin"/></h4> 

		<div class="boxcontent">

		<html:form action="/db" method="post" >
		  <table cellpadding="0" cellspacing="0" border="0" align="center">
		    <tr>
		      <td align="right"> 
		       <h6> <bean:message key="label.config.db.url"/>: </h6>
		      </td>
		      <td>&nbsp;</td>
		      <td>
		        <html:text property="dbUrl"  maxlength="255" style="width: 30em;" />		        	        
		      </td>
		    </tr>
		    <tr>
		      <td>&nbsp;</td>
		    </tr>
		    <tr>
		      <td align="right"> 
		       <h6> <bean:message key="label.config.db.user"/>: </h6>
		      </td>
		      <td>&nbsp;</td>
		      <td>
		        <html:text property="user"  maxlength="255" style="width: 30em;" />		        	        
		      </td>
		    </tr>
		    <tr>
		      <td>&nbsp;</td>
		    </tr>
		    <tr>
		      <td align="right"> 
		       <h6> <bean:message key="label.config.db.password"/>: </h6>
		      </td>
		      <td>&nbsp;</td>
		      <td>
		        <html:password property="password"  maxlength="255" style="width: 30em;" />		        	        
		      </td>
		    </tr>
		    <tr>
		      <td>&nbsp;</td>
		    </tr>
		    <tr>
		      <td colspan="3" align="center">
		        <html:submit styleClass="button">
		        	<bean:message key="label.config.db.update"/>
		        </html:submit>		        
		      </td>
		    </tr>
		  </table>
		</html:form>
		
		</div>
		<div class="boxbottom"><div></div></div> 
	</div>
</div>

</div>