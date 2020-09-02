<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:sec="http://www.armatiek.com/xslweb/functions/security"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all"
  version="3.0">
  
  <xsl:import href="../common/example-page.xsl"/>
  
  <xsl:template name="title" as="xs:string">Example 33: Security - Login</xsl:template>
  
  <xsl:template name="tab-contents-1">
    
    <xsl:if test="sec:is-guest()">
      <p>Here are a few sample accounts to play with in the default text-based Realm (used for this
        demo and test installs only).</p>
      <table border="1">
        <thead>
          <tr>
            <th>Username</th>
            <th>Password</th>
            <th>Roles</th>
            <th>Permissions</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>root</td>
            <td>secret</td>
            <td>admin</td>
            <td>*</td>
          </tr>
          <tr>
            <td>jdoe</td>
            <td>test123</td>
            <td>user</td>
            <td>portal:read , portal:write</td>
          </tr>
        </tbody>
      </table>
      <br/><br/>
    </xsl:if>
    
    <form name="loginform"  action="" method="post">
      <table border="0" cellspacing="0" cellpadding="3">
        <tr>
          <td>Username:</td>
          <td>
            <input type="text" name="username" maxlength="30"/>
          </td>
        </tr>
        <tr>
          <td>Password:</td>
          <td>
            <input type="password" name="password" maxlength="30"/>
          </td>
        </tr>
        <tr>
          <td colspan="2" align="left">
            <input type="checkbox" name="rememberMe"/>
            <font size="2">Remember Me</font>
          </td>
        </tr>
        <tr>
          <td colspan="2" align="right">
            <input type="submit" name="submit" value="Login"/>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>
  
  <!-- These variables can be ignored: -->
  <xsl:variable name="pipeline-xsl" select="document('')" as="document-node()"/>
  
  <xsl:variable name="template-name" as="xs:string">security-login</xsl:variable>
  
</xsl:stylesheet>