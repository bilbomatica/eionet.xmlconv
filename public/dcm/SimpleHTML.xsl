<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output  method="xml"/>
    
    <xsl:template match="/">
        <xsl:apply-templates select="table"/>
    </xsl:template>
    
    <xsl:template match="table">
        <xsl:text  xml:space="default" disable-output-escaping="yes">&#xd;&#xa;&lt;xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"&#xd;&#xa;    xmlns:dd</xsl:text>
        <xsl:value-of select="parentNS"/>
        <xsl:text disable-output-escaping="yes">="http://rubi:8080/dd/namespace.jsp?ns_id=</xsl:text>
        <xsl:value-of select="parentNS"/>
        <xsl:text disable-output-escaping="yes">" xmlns:dd</xsl:text>
        <xsl:value-of select="correspondingNS"/>
        <xsl:text disable-output-escaping="yes">="http://rubi:8080/dd/namespace.jsp?ns_id=</xsl:text>
        <xsl:value-of select="correspondingNS"/>
        <xsl:text disable-output-escaping="yes">"&gt;&#xd;&#xa;    &lt;xsl:output method="html"/&gt;&#xd;&#xa;</xsl:text>
 
  <!--   TEMPLATE match ="/"  -->
        <xsl:text disable-output-escaping="yes">&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">    &lt;xsl:template match="/"&gt;&#xd;&#xa;</xsl:text>
        
       
        <xsl:text disable-output-escaping="yes">&lt;html&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;head&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;title&gt;</xsl:text>
        <xsl:value-of select="identifier"/>
        <xsl:text disable-output-escaping="yes">&lt;/title&gt;</xsl:text>        
        <xsl:text disable-output-escaping="yes">&lt;/head&gt;</xsl:text>        
        <xsl:text disable-output-escaping="yes">&lt;body&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;h1&gt;</xsl:text>
        <xsl:value-of select="identifier"/>        
        <xsl:text disable-output-escaping="yes">&lt;/h1&gt;</xsl:text> 
        <xsl:text disable-output-escaping="yes">&lt;table border="1"&gt;</xsl:text>
       
        
        <xsl:text disable-output-escaping="yes">        &lt;xsl:apply-templates select="dd</xsl:text>
        <xsl:value-of select="parentNS"/>
        <xsl:text disable-output-escaping="yes">:</xsl:text>
        <xsl:value-of select="identifier"/>
        <xsl:text disable-output-escaping="yes">/dd</xsl:text>
        <xsl:value-of select="parentNS"/>
        <xsl:text disable-output-escaping="yes">:Row"/&gt;&#xd;&#xa;</xsl:text>
       
        <xsl:text disable-output-escaping="yes">&lt;/table&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/body&gt;</xsl:text>                
        <xsl:text disable-output-escaping="yes">&lt;/html&gt;</xsl:text> 
        
        
        
        <xsl:text disable-output-escaping="yes">    &lt;/xsl:template&gt;&#xd;&#xa;</xsl:text>
        
<!--   TEMPLATE match ="Row"  -->
        <xsl:text disable-output-escaping="yes">&#xd;&#xa;</xsl:text>        
        <xsl:text disable-output-escaping="yes">    &lt;xsl:template match="dd</xsl:text>
        <xsl:value-of select="parentNS"/>
        <xsl:text disable-output-escaping="yes">:</xsl:text>
        <xsl:value-of select="identifier"/>
        <xsl:text disable-output-escaping="yes">/dd</xsl:text>
        <xsl:value-of select="parentNS"/>
        <xsl:text disable-output-escaping="yes">:Row"&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">        &lt;xsl:if test="position()=1"&gt;&#xd;&#xa;</xsl:text>



        <xsl:text disable-output-escaping="yes">            &lt;xsl:call-template name="create-table"/&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">        &lt;/xsl:if&gt;&#xd;&#xa;</xsl:text> 
        <xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
        
        <xsl:text disable-output-escaping="yes">&lt;xsl:for-each select="*"&gt; &lt;td&gt; &lt;xsl:if test=".=''"&gt; &#160; &lt;/xsl:if&gt;  &lt;xsl:value-of select="." /&gt; &lt;/td&gt; &lt;/xsl:for-each&gt;&#xd;&#xa;</xsl:text>        
        <xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>



        <xsl:text disable-output-escaping="yes">&lt;/xsl:template&gt;&#xd;&#xa;</xsl:text>
        
   <!--   TEMPLATE name="create-table"  -->
        <xsl:text disable-output-escaping="yes">&#xd;&#xa;</xsl:text>     
        <xsl:text disable-output-escaping="yes">&lt;xsl:template name="create-table"&gt;&#xd;&#xa;</xsl:text>

        <xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
        <xsl:value-of select="identifier"/>
        
        <xsl:text disable-output-escaping="yes">    &lt;xsl:for-each select="*"&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;th bgcolor="#87CEFA"&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;xsl:value-of select="local-name()" /&gt; &#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/th&gt; &#xd;&#xa;</xsl:text> 
        <xsl:text disable-output-escaping="yes">&lt;/xsl:for-each&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>
        
               
        <xsl:text disable-output-escaping="yes">&lt;/xsl:template&gt;&#xd;&#xa;</xsl:text>
        
 <!--   TEMPLATE name ="getFieldType"  -->
        <xsl:text disable-output-escaping="yes">&#xd;&#xa;</xsl:text>       
        <xsl:text disable-output-escaping="yes">&lt;xsl:template name="getFieldType"&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;xsl:param name="name" select="''"/&gt;&#xd;&#xa;</xsl:text>        
        <xsl:text disable-output-escaping="yes">&lt;xsl:choose&gt;&#xd;&#xa;</xsl:text>
        
        <xsl:for-each select="//elements/element">            
            <xsl:text disable-output-escaping="yes">&lt;xsl:when test="$name = '</xsl:text>
            <xsl:value-of select="identifier"/>
            <xsl:text disable-output-escaping="yes">'"&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;xsl:variable name="type"&gt;</xsl:text><xsl:value-of select="type"/><xsl:text disable-output-escaping="yes">&lt;/xsl:variable&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;xsl:choose&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;xsl:when test="$type = 'integer'"&gt;int&lt;/xsl:when&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;xsl:when test="$type = 'string'"&gt;varchar(255)&lt;/xsl:when&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;xsl:when test="$type = 'float'"&gt;float&lt;/xsl:when&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;xsl:when test="$type = 'double'"&gt;float&lt;/xsl:when&gt;&#xd;&#xa;</xsl:text> 
            <xsl:text disable-output-escaping="yes">&lt;xsl:otherwise&gt;varchar(255)&lt;/xsl:otherwise&gt;&#xd;&#xa;</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;/xsl:choose&gt;&#xd;&#xa;</xsl:text> 
            <xsl:text disable-output-escaping="yes">&lt;/xsl:when&gt;&#xd;&#xa;</xsl:text>            
        </xsl:for-each>
        
        <xsl:text disable-output-escaping="yes">&lt;xsl:otherwise&gt;varchar(255)&lt;/xsl:otherwise&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/xsl:choose&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/xsl:template&gt;&#xd;&#xa;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/xsl:stylesheet&gt;&#xd;&#xa;</xsl:text>       
        
    </xsl:template> 
    
</xsl:stylesheet>

