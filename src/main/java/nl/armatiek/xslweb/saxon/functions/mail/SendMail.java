package nl.armatiek.xslweb.saxon.functions.mail;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.xpath.XPathEvaluator;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

public class SendMail extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_EMAIL, "send-mail");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.OPTIONAL_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return true;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new sendMailCall();
  }
  
  private static class sendMailCall extends ExtensionFunctionCall {
        
    private String stripXHTML(NodeInfo node, Configuration configuration) throws Exception {
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);   
      String xsl = "<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" +
          "<xsl:template match='/|comment()|processing-instruction()'>" +
          "  <xsl:copy>" +
          "    <xsl:apply-templates/>" +
          "  </xsl:copy>" +
          "</xsl:template>" +
          "<xsl:template match='*'>" +
          "  <xsl:element name='{local-name()}'>" +
          "    <xsl:apply-templates select='@*|node()'/>" +
          "  </xsl:element>" +
          "</xsl:template>" +
          "<xsl:template match='@*'>" +
          "  <xsl:attribute name='{local-name()}'>" +
          "    <xsl:value-of select='.'/>" +
          "  </xsl:attribute>" +
          "</xsl:template>" +
          "</xsl:stylesheet>";            
      TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl(configuration);
      Templates templates = factory.newTemplates(new StreamSource(new StringReader(xsl)));
      Transformer transformer = templates.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.METHOD, "xhtml");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");        
      transformer.transform(node, result);      
      return sw.toString();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {                            
      try {                        
        NodeInfo mailElem = unwrapNodeInfo((NodeInfo) arguments[0].head());
                        
        XPathEvaluator xpath = new XPathEvaluator(context.getConfiguration());               
        xpath.setNamespaceContext(new NamespaceContext() {
          @Override
          public String getNamespaceURI(String prefix) {          
            return Definitions.NAMESPACEURI_XSLWEB_FX_EMAIL;
          }
  
          @Override
          public String getPrefix(String namespace) {        
            return "email";
          }
  
          @SuppressWarnings("rawtypes")
          @Override
          public Iterator getPrefixes(String namespace) {        
            return null;
          }        
        });
                        
        HtmlEmail email = new HtmlEmail();
        String hostName = (String) xpath.evaluate("email:hostname", mailElem, XPathConstants.STRING);        
        String port = (String) xpath.evaluate("email:port", mailElem, XPathConstants.STRING);
        String username = (String) xpath.evaluate("email:username", mailElem, XPathConstants.STRING);
        String password = (String) xpath.evaluate("email:password", mailElem, XPathConstants.STRING);
        String useSSLVal = (String) xpath.evaluate("email:use-ssl", mailElem, XPathConstants.STRING);
        boolean useSSL = useSSLVal.equals("true") || useSSLVal.equals("1");
        String startTLSEnabled = (String) xpath.evaluate("email:start-tls-enabled", mailElem, XPathConstants.STRING);
        String startTLSRequired = (String) xpath.evaluate("email:start-tls-required", mailElem, XPathConstants.STRING);
        String SSLCheckServerIdentityVal = (String) xpath.evaluate("email:ssl-check-server-identity", mailElem, XPathConstants.STRING); 
        boolean SSLCheckServerIdentity = SSLCheckServerIdentityVal.equals("true") || SSLCheckServerIdentityVal.equals("1");
      
        email.setHostName(hostName); 
        if (StringUtils.isNotBlank(port)) {
          if (useSSL) {
            email.setSslSmtpPort(port);
          } else {
            email.setSmtpPort(Integer.parseInt(port));
          }         
        }
        email.setSSLCheckServerIdentity(SSLCheckServerIdentity);
        if (StringUtils.isNotBlank(username)) {        
          email.setAuthenticator(new DefaultAuthenticator(username, password));        
        }                
        if (useSSL) {
          email.setSSLOnConnect(useSSL);                    
        }
        if (StringUtils.isNotBlank(startTLSEnabled)) {        
          email.setStartTLSEnabled(startTLSEnabled.equals("true") || startTLSEnabled.equals("1"));     
        }
        if (StringUtils.isNotBlank(startTLSRequired)) {        
          email.setStartTLSEnabled(startTLSRequired.equals("true") || startTLSRequired.equals("1"));        
        }
        
        // email.setBoolHasAttachments(b);
        // email.setSSLCheckServerIdentity(sslCheckServerIdentity);
        
        email.setFrom(
            (String) xpath.evaluate("email:from/email:email", mailElem, XPathConstants.STRING), 
            (String) xpath.evaluate("email:from/email:name", mailElem, XPathConstants.STRING), 
            (String) xpath.evaluate("email:from/email:charset", mailElem, XPathConstants.STRING));
        
        List<NodeInfo> replyToNodes = (List<NodeInfo>) xpath.evaluate("email:reply-to", mailElem, XPathConstants.NODESET);
        for (NodeInfo replyToNode : replyToNodes) {          
          email.addReplyTo(
              (String) xpath.evaluate("email:email", replyToNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:name", replyToNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:charset", replyToNode, XPathConstants.STRING));          
        }        
        List<NodeInfo> toNodes = (List<NodeInfo>) xpath.evaluate("email:to", mailElem, XPathConstants.NODESET);
        for (NodeInfo toNode : toNodes) {          
          email.addTo(
              (String) xpath.evaluate("email:email", toNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:name", toNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:charset", toNode, XPathConstants.STRING));          
        }
        List<NodeInfo> ccNodes = (List<NodeInfo>) xpath.evaluate("email:cc", mailElem, XPathConstants.NODESET);
        for (NodeInfo ccNode : ccNodes) {          
          email.addCc(
              (String) xpath.evaluate("email:email", ccNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:name", ccNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:charset", ccNode, XPathConstants.STRING));          
        }
        List<NodeInfo> bccNodes = (List<NodeInfo>) xpath.evaluate("email:bcc", mailElem, XPathConstants.NODESET);
        for (NodeInfo bccNode : bccNodes) {          
          email.addBcc(
              (String) xpath.evaluate("email:email", bccNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:name", bccNode, XPathConstants.STRING), 
              (String) xpath.evaluate("email:charset", bccNode, XPathConstants.STRING));          
        }
        email.setSubject((String) xpath.evaluate("email:subject", mailElem, XPathConstants.STRING));
        
        String textMessage = (String) xpath.evaluate("email:message/email:text", mailElem, XPathConstants.STRING);
        if (StringUtils.isNotBlank(textMessage)) {
          email.setTextMsg(textMessage);
        }
        List<NodeInfo> htmlMessageNodes = (List<NodeInfo>) xpath.evaluate("email:message/email:html/node()", mailElem, XPathConstants.NODESET);                
        if (htmlMessageNodes.size() > 0) {
          StringBuilder sb = new StringBuilder();
          for (NodeInfo htmlMessageNode : htmlMessageNodes) {
            sb.append(stripXHTML(htmlMessageNode, context.getConfiguration()));
          }          
          email.setHtmlMsg(sb.toString());
        }
        
        List<NodeInfo> attachmentNodes = (List<NodeInfo>) xpath.evaluate("email:attachment", mailElem, XPathConstants.NODESET);
        for (NodeInfo attachmentNode : attachmentNodes) {                              
          EmailAttachment attachment = new EmailAttachment();          
          attachment.setPath((String) xpath.evaluate("email:file-path", attachmentNode, XPathConstants.STRING));
          attachment.setDisposition(EmailAttachment.ATTACHMENT);
          attachment.setDescription((String) xpath.evaluate("email:description", attachmentNode, XPathConstants.STRING));
          attachment.setName((String) xpath.evaluate("email:name", attachmentNode, XPathConstants.STRING));          
          email.attach(attachment);
        }
        
        email.send();
        
        return EmptySequence.getInstance();
      } catch (Exception e) {
        throw new XPathException(e);
      }
    }
  }

}