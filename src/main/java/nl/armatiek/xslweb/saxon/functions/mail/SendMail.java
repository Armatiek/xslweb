package nl.armatiek.xslweb.saxon.functions.mail;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.utils.XMLUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SendMail extends ExtensionFunctionDefinition {

  private static final long serialVersionUID = 1L;
  
  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_RESPONSE, "send-mail");

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
    return SequenceType.SINGLE_BOOLEAN;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new sendMailCall();
  }
  
  private static class sendMailCall extends ExtensionFunctionCall {
        
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("rawtypes")
    public SequenceIterator<BooleanValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {                            
      try {        
        NodeInfo nodeInfo = (NodeInfo) arguments[0].next();      
        Element mailElem = (Element) NodeOverNodeInfo.wrap(nodeInfo);        
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
          @Override
          public String getNamespaceURI(String prefix) {          
            return null;
          }
  
          @Override
          public String getPrefix(String namespace) {        
            return null;
          }
  
          @Override
          public Iterator getPrefixes(String namespace) {        
            return null;
          }        
        });
                        
        HtmlEmail email = new HtmlEmail();
        String hostName = (String) xpath.evaluate("hostname", mailElem, XPathConstants.STRING);        
        String port = (String) xpath.evaluate("port", mailElem, XPathConstants.STRING);
        String username = (String) xpath.evaluate("username", mailElem, XPathConstants.STRING);
        String password = (String) xpath.evaluate("password", mailElem, XPathConstants.STRING);
        String useSSLVal = (String) xpath.evaluate("use-ssl", mailElem, XPathConstants.STRING);
        boolean useSSL = useSSLVal.equals("true") || useSSLVal.equals("1");
        String startTLSEnabled = (String) xpath.evaluate("start-tls-enabled", mailElem, XPathConstants.STRING);
        String startTLSRequired = (String) xpath.evaluate("start-tls-required", mailElem, XPathConstants.STRING);
        
        email.setHostName(hostName);        
        if (StringUtils.isNotBlank(port)) {
          if (useSSL) {
            email.setSslSmtpPort(port);
          } else {
            email.setSmtpPort(Integer.parseInt(port));
          }         
        }
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
            (String) xpath.evaluate("from/email", mailElem, XPathConstants.STRING), 
            (String) xpath.evaluate("from/name", mailElem, XPathConstants.STRING), 
            (String) xpath.evaluate("from/charset", mailElem, XPathConstants.STRING));
        
        NodeList replyToNodes = (NodeList) xpath.evaluate("reply-to", mailElem, XPathConstants.NODESET);
        for (int i=0; i<replyToNodes.getLength(); i++) {
          Node replyToNode = replyToNodes.item(i);
          email.addReplyTo(
              (String) xpath.evaluate("email", replyToNode, XPathConstants.STRING), 
              (String) xpath.evaluate("name", replyToNode, XPathConstants.STRING), 
              (String) xpath.evaluate("charset", replyToNode, XPathConstants.STRING));          
        }
        NodeList toNodes = (NodeList) xpath.evaluate("to", mailElem, XPathConstants.NODESET);
        for (int i=0; i<toNodes.getLength(); i++) {
          Node toNode = toNodes.item(i);
          email.addTo(
              (String) xpath.evaluate("email", toNode, XPathConstants.STRING), 
              (String) xpath.evaluate("name", toNode, XPathConstants.STRING), 
              (String) xpath.evaluate("charset", toNode, XPathConstants.STRING));          
        }
        NodeList bccNodes = (NodeList) xpath.evaluate("bcc", mailElem, XPathConstants.NODESET);
        for (int i=0; i<bccNodes.getLength(); i++) {
          Node bccNode = bccNodes.item(i);
          email.addBcc(
              (String) xpath.evaluate("email", bccNode, XPathConstants.STRING), 
              (String) xpath.evaluate("name", bccNode, XPathConstants.STRING), 
              (String) xpath.evaluate("charset", bccNode, XPathConstants.STRING));          
        }
        email.setSubject((String) xpath.evaluate("subject", mailElem, XPathConstants.STRING));
        
        String textMessage = (String) xpath.evaluate("message/text", mailElem, XPathConstants.STRING);
        if (StringUtils.isNotBlank(textMessage)) {
          email.setTextMsg(textMessage);
        }
        NodeList htmlMessageNodes = (NodeList) xpath.evaluate("message/html/node()", mailElem, XPathConstants.NODESET);                
        if (htmlMessageNodes.getLength() > 0) {
          StringBuilder sb = new StringBuilder();
          for (int i=0; i<htmlMessageNodes.getLength(); i++) {
            sb.append(XMLUtils.nodeToString(htmlMessageNodes.item(i)));
          }          
          email.setHtmlMsg(sb.toString());
        }
        
        NodeList attachmentNodes = (NodeList) xpath.evaluate("attachment", mailElem, XPathConstants.NODESET);
        for (int i=0; i<attachmentNodes.getLength(); i++) {
          Element attachmentElem = (Element) attachmentNodes.item(i);           
          EmailAttachment attachment = new EmailAttachment();
          attachment.setPath(attachmentElem.getAttribute("file-path"));
          attachment.setDisposition(EmailAttachment.ATTACHMENT);
          attachment.setDescription(attachmentElem.getAttribute("description"));
          attachment.setName(attachmentElem.getAttribute("name"));          
          email.attach(attachment);
        }
        
        email.send();
        
        
        
        
        
        // <mail> <hostname/> <from/> <reply-to/> <to/> <cc/> <bcc/> <subject/> <message> <text/> <xhtml/> </message> <attachment filename="" mimetype="">xs:base64Binary</attachment>
        
        
        return SingletonIterator.makeIterator(BooleanValue.get(true));
      } catch (Exception e) {
        throw new XPathException(e);
      }
    }
  }

}