package nl.armatiek.xslweb.saxon.functions.queue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.utils.ClosedStatusOutputStream;
import nl.armatiek.xslweb.web.servlet.InternalRequest;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class AddRequest extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_QUEUE, "add-request");

  private static final Logger logger = LoggerFactory.getLogger(AddRequest.class);
  
  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 3;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new AddRequestToQueueCall();
  }

  private static class AddRequestToQueueCall extends ExtensionFunctionCall {
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String queueName = ((StringValue) arguments[0].head()).getStringValue();
        String path = ((StringValue) arguments[1].head()).getStringValue();
        WebApp webApp = getWebApp(context);
        HttpServletRequest request = getRequest(context);
        if (!StringUtils.equalsIgnoreCase("GET", request.getMethod())) {
          throw new XPathException("A queued request must have method \"GET\"");
        }
        String extraInfo = arguments.length > 2 ? serialize((NodeInfo) arguments[2].head(), webApp.getProcessor()) : null;
        ExecutorService service = webApp.getExecutorService(queueName);
        String ticket = UUID.randomUUID().toString();
        File queueDir = Context.getInstance().getQueueDir();
        if (!queueDir.isDirectory() && !queueDir.mkdirs())
          throw new IOException("Could not create queue directory \"" + queueDir.getAbsolutePath() + "\"");
        File lockFile = new File(queueDir, ticket + ".lck");
        FileUtils.touch(lockFile);
        try {
          service.execute(new QueuedRequest(ticket, webApp.getPath() + "/" + path, extraInfo, new QueuedHttpServletRequestClone(request)));
        } catch (RejectedExecutionException ree) {
          FileUtils.deleteQuietly(lockFile);
          return StringValue.makeStringValue("rejected");
        }
        return StringValue.makeStringValue(ticket);
      } catch (Exception e) {
        throw new XPathException("Error adding asynchronous request", e);
      } 
    }
  }
  
  private static class QueuedRequest implements Runnable {

    private String ticket;
    private String path;
    private String extraInfo;
    private HttpServletRequest request;
    
    public QueuedRequest(String ticket, String path, String extraInfo, HttpServletRequest request) {
      this.ticket = ticket;
      this.path = path;
      this.extraInfo = extraInfo;
      this.request = request;
    }
    
    @Override
    public void run() {
      try {
        File queueDir = Context.getInstance().getQueueDir();
        File lockFile = new File(queueDir, ticket + ".lck");
        File outputFile = new File(queueDir, ticket + ".bin");  
        boolean exceptionThrown = false;
        try {
          if (extraInfo != null)
            FileUtils.write(new File(queueDir, ticket + ".xml"), extraInfo, StandardCharsets.UTF_8);
          ClosedStatusOutputStream os = new ClosedStatusOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
          try {
            int status = new InternalRequest().execute(path, os, false, request);
            if (status != HttpServletResponse.SC_OK) {
              exceptionThrown = true;
              FileUtils.write(new File(queueDir, ticket + ".err"), "HTTP status " + status, StandardCharsets.UTF_8);
            }
          } catch (Exception e) {
            exceptionThrown = true;
            FileUtils.write(new File(queueDir, ticket + ".err"), ExceptionUtils.getStackTrace(e), StandardCharsets.UTF_8);
          } finally {
            if (!os.isClosed())
              os.close();
          }
        } finally {
          if (exceptionThrown)
            FileUtils.deleteQuietly(outputFile);
          FileUtils.deleteQuietly(lockFile);
        }
      } catch (Exception e) {
        logger.error("Error executing queued/asynchronous request \"" + path + "\"", e);
      }
    }
   
  }
  
  /**
   * Lightweight GET only clone of a HttpServletRequest that copies only the essential data
   * necessary for a queued/asynchronous request. 
   */
  private static class QueuedHttpServletRequestClone implements HttpServletRequest {
      
    private final HashMap<String, Object> attributes = new HashMap<>();
    private final HashMap<String, Enumeration<String>> headers = new HashMap<>();
    private final HashMap<String, String[]> parameters = new HashMap<>();
    private final String authType;
    private final String characterEncoding;
    private final int contentLength;
    private final String contentType;
    private final String method;
    private final String pathInfo;
    private final String protocol;
    private final String pathTranslated;
    private final String scheme;
    private final String serverName;
    private final String contextPath;
    private final int serverPort; 
    private final String queryString;
    private final String remoteAddr;
    private final String remoteUser;
    private final String remoteHost;
    private final String requestedSessionId;
    private final String requestURI;
    private final StringBuffer requestURL;
    private final boolean isSecure;
    private final String servletPath;
    private final int remotePort;
    private final String localName;
    private final String localAddr;
    private final int localPort;
    private final boolean isRequestedSessionIdValid;
    private final boolean isRequestedSessionIdFromCookie;
    private final boolean isRequestedSessionIdFromURL;
    private final boolean isRequestedSessionIdFromUrl;
    
    @SuppressWarnings("deprecation")
    public QueuedHttpServletRequestClone(HttpServletRequest request) {
      Enumeration<String> attrs = request.getAttributeNames();
      while (attrs.hasMoreElements()) {
        String name = attrs.nextElement();
        attributes.put(name, request.getAttribute(name));
      }
      Enumeration<String> hdrs = request.getHeaderNames();
      while (hdrs.hasMoreElements()) {
        String name = hdrs.nextElement();
        headers.put(name, request.getHeaders(name));
      }
      Enumeration<String> params = request.getParameterNames();
      while (params.hasMoreElements()) {
        String name = params.nextElement();
        parameters.put(name, request.getParameterValues(name));
      }
      authType = request.getAuthType();
      characterEncoding = request.getCharacterEncoding();
      contentLength = request.getContentLength();
      contentType = request.getContentType();
      method = request.getMethod();
      pathInfo = request.getPathInfo();
      protocol = request.getProtocol();
      pathTranslated = request.getPathTranslated();
      scheme = request.getScheme();
      serverName = request.getServerName();
      contextPath = request.getContextPath();
      serverPort = request.getServerPort();
      queryString = request.getQueryString();
      remoteAddr = request.getRemoteAddr();
      remoteUser = request.getRemoteUser();
      remoteHost = request.getRemoteHost();
      requestedSessionId = request.getRequestedSessionId();
      requestURI = request.getRequestURI();
      requestURL = new StringBuffer(request.getRequestURL());
      isSecure = request.isSecure();
      servletPath = request.getServletPath();
      remotePort = request.getRemotePort();
      localName = request.getLocalName();
      localAddr = request.getLocalAddr();
      localPort = request.getLocalPort();
      isRequestedSessionIdValid = request.isRequestedSessionIdValid();
      isRequestedSessionIdFromCookie = request.isRequestedSessionIdFromCookie();
      isRequestedSessionIdFromURL = request.isRequestedSessionIdFromURL();
      isRequestedSessionIdFromUrl = request.isRequestedSessionIdFromUrl();
    }

    public Object getAttribute(String name) {
      return attributes.get(name);
    }

    public String getAuthType() {
      return authType;
    }

    public Cookie[] getCookies() {
      throw new NotImplementedException();
    }

    public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(attributes.keySet());
    }

    public long getDateHeader(String name) {
      throw new NotImplementedException();
    }

    public String getCharacterEncoding() {
      return characterEncoding;
    }

    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
      throw new NotImplementedException();
    }

    public String getHeader(String name) {
      Enumeration<String> values = headers.get(name);
      if (values.hasMoreElements()) {
        return values.nextElement();
      }
      return null;
    }

    public int getContentLength() {
      return contentLength;
    }

    public String getContentType() {
      return contentType;
    }

    public Enumeration<String> getHeaders(String name) {
      return headers.get(name);
    }

    public ServletInputStream getInputStream() throws IOException {
      throw new NotImplementedException();
    }

    public String getParameter(String name) {
      String[] params = parameters.get(name);
      if (params != null && params.length > 0) {
        return params[0];
      }
      return null;
    }

    public Enumeration<String> getHeaderNames() {
      return Collections.enumeration(headers.keySet());
    }

    public int getIntHeader(String name) {
      throw new NotImplementedException();
    }

    public Enumeration<String> getParameterNames() {
      return Collections.enumeration(parameters.keySet());
    }

    public String[] getParameterValues(String name) {
      return parameters.get(name);
    }

    public String getMethod() {
      return method;
    }

    public String getPathInfo() {
      return pathInfo;
    }

    public Map<String, String[]> getParameterMap() {
      return parameters;
    }

    public String getProtocol() {
      return protocol;
    }

    public String getPathTranslated() {
      return pathTranslated;
    }

    public String getScheme() {
      return scheme;
    }

    public String getServerName() {
      return serverName;
    }

    public String getContextPath() {
      return contextPath;
    }

    public int getServerPort() {
      return serverPort;
    }

    public BufferedReader getReader() throws IOException {
      throw new NotImplementedException();
    }

    public String getQueryString() {
      return queryString;
    }

    public String getRemoteAddr() {
      return remoteAddr;
    }

    public String getRemoteUser() {
      return this.remoteUser;
    }

    public String getRemoteHost() {
      return remoteHost;
    }

    public boolean isUserInRole(String role) {
      throw new NotImplementedException();
    }

    public void setAttribute(String name, Object o) {
      attributes.put(name, o);
    }

    public Principal getUserPrincipal() {
      throw new NotImplementedException();
    }

    public String getRequestedSessionId() {
      return requestedSessionId;
    }

    public void removeAttribute(String name) {
      attributes.remove(name);
    }

    public String getRequestURI() {
      return requestURI;
    }

    public Locale getLocale() {
      throw new NotImplementedException();
    }

    public Enumeration<Locale> getLocales() {
      throw new NotImplementedException();
    }

    public StringBuffer getRequestURL() {
      return requestURL;
    }

    public boolean isSecure() {
      return isSecure;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
      throw new NotImplementedException();
    }

    public String getServletPath() {
      return servletPath;
    }

    public HttpSession getSession(boolean create) {
      return null;
    }

    public String getRealPath(String path) {
      throw new NotImplementedException();
    }

    public int getRemotePort() {
      return remotePort;
    }

    public String getLocalName() {
      return localName;
    }

    public String getLocalAddr() {
      return localAddr;
    }

    public HttpSession getSession() {
      return null;
    }

    public int getLocalPort() {
      return localPort;
    }

    public ServletContext getServletContext() {
      throw new NotImplementedException();
    }

    public boolean isRequestedSessionIdValid() {
      return isRequestedSessionIdValid;
    }

    public AsyncContext startAsync() throws IllegalStateException {
      throw new NotImplementedException();
    }

    public boolean isRequestedSessionIdFromCookie() {
      return isRequestedSessionIdFromCookie;
    }

    public boolean isRequestedSessionIdFromURL() {
      return isRequestedSessionIdFromURL;
    }

    public boolean isRequestedSessionIdFromUrl() {
      return isRequestedSessionIdFromUrl;
    }

    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
      throw new NotImplementedException();
    }

    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
      throw new NotImplementedException();
    }

    public void login(String username, String password) throws ServletException {
      throw new NotImplementedException();
    }

    public void logout() throws ServletException {
      throw new NotImplementedException();
    }

    public Collection<Part> getParts() throws IOException, ServletException {
      throw new NotImplementedException();
    }

    public boolean isAsyncStarted() {
      throw new NotImplementedException();
    }

    public Part getPart(String name) throws IOException, ServletException {
      throw new NotImplementedException();
    }

    public boolean isAsyncSupported() {
      return false;
    }

    public AsyncContext getAsyncContext() {
      throw new NotImplementedException();
    }

    public DispatcherType getDispatcherType() {
      throw new NotImplementedException();
    }
    
  }
  
}