package nl.armatiek.xslweb.saxon.errrorlistener;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMLocator;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.ComponentBody;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.Template;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ContextStackFrame;
import net.sf.saxon.trace.ContextStackIterator;
import net.sf.saxon.trace.InstructionInfo;
import net.sf.saxon.trace.Location;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.ValidationException;
import nl.armatiek.xslweb.saxon.log.Slf4JLogger;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

public class TransformationErrorListener implements ErrorListener, Serializable {

  private static final long serialVersionUID = 1L;

  private HttpServletResponse response;
  private boolean firstError;

  private int recoveryPolicy = Configuration.RECOVER_WITH_WARNINGS;
  private int warningCount = 0;
  private int maximumNumberOfWarnings = 25;
  private boolean developmentMode;
  protected transient Logger logger = new Slf4JLogger();

  public TransformationErrorListener(HttpServletResponse response, boolean developmentMode) {
    this.response = response;
    this.firstError = true;
    this.developmentMode = developmentMode;
  }

  /**
   * Make a clean copy of this ErrorListener. This is necessary because the
   * standard error listener is stateful (it remembers how many errors there
   * have been)
   * 
   * @param hostLanguage
   *          the host language (not used by this implementation)
   * @return a copy of this error listener
   */

  public TransformationErrorListener makeAnother(int hostLanguage) {
    TransformationErrorListener sel;
    try {
      sel = this.getClass().newInstance();
    } catch (InstantiationException e) {
      sel = new TransformationErrorListener(response, developmentMode);
    } catch (IllegalAccessException e) {
      sel = new TransformationErrorListener(response, developmentMode);
    }
    sel.logger = logger;
    return sel;
  }

  private void logError(String message) throws TransformerException {
    try {
      logger.error(message);
      if (developmentMode) {
        if (firstError) {
          response.setContentType("text/plain;charset=UTF-8");
          firstError = false;
        }
        IOUtils.copy(new StringReader(message), response.getOutputStream());
      }
    } catch (IOException ioe) {
      throw new TransformerException("IOException dumping track trace");
    }
  }

  /**
   * Set the recovery policy
   * 
   * @param policy
   *          the recovery policy for XSLT recoverable errors. One of
   *          {@link Configuration#RECOVER_SILENTLY},
   *          {@link Configuration#RECOVER_WITH_WARNINGS},
   *          {@link Configuration#DO_NOT_RECOVER}.
   */

  public void setRecoveryPolicy(int policy) {
    recoveryPolicy = policy;
  }

  /**
   * Get the recovery policy
   * 
   * @return the recovery policy for XSLT recoverable errors. One of
   *         {@link Configuration#RECOVER_SILENTLY},
   *         {@link Configuration#RECOVER_WITH_WARNINGS},
   *         {@link Configuration#DO_NOT_RECOVER}.
   */

  public int getRecoveryPolicy() {
    return recoveryPolicy;
  }

  /**
   * Set the maximum number of warnings that are reported; further warnings
   * after this limit are silently ignored
   * 
   * @param max
   *          the maximum number of warnings output
   */

  public void setMaximumNumberOfWarnings(int max) {
    this.maximumNumberOfWarnings = max;
  }

  /**
   * Get the maximum number of warnings that are reported; further warnings
   * after this limit are silently ignored
   * 
   * @return the maximum number of warnings output
   */

  public int getMaximumNumberOfWarnings() {
    return this.maximumNumberOfWarnings;
  }

  /**
   * Receive notification of a warning.
   * <p/>
   * <p>
   * Transformers can use this method to report conditions that are not errors
   * or fatal errors. The default behaviour is to take no action.
   * </p>
   * <p/>
   * <p>
   * After invoking this method, the Transformer must continue with the
   * transformation. It should still be possible for the application to process
   * the document through to the end.
   * </p>
   * 
   * @param exception
   *          The warning information encapsulated in a transformer exception.
   * @throws javax.xml.transform.TransformerException
   *           if the application chooses to discontinue the transformation.
   * @see javax.xml.transform.TransformerException
   */

  public void warning(TransformerException exception) throws TransformerException {

    if (recoveryPolicy == Configuration.RECOVER_SILENTLY) {
      // do nothing
      return;
    }

    String message = "";
    if (exception.getLocator() != null) {
      message = getLocationMessage(exception) + "\n  ";
    }
    message += wordWrap(getExpandedMessage(exception));

    if (exception instanceof ValidationException) {
      logError("Validation error " + message);
    } else {
      logger.warning("Warning: " + message);
      warningCount++;
      if (warningCount > getMaximumNumberOfWarnings()) {
        logger.info("No more warnings will be displayed");
        recoveryPolicy = Configuration.RECOVER_SILENTLY;
        warningCount = 0;
      }
    }
  }

  /**
   * Receive notification of a recoverable error.
   * <p/>
   * <p>
   * The transformer must continue to provide normal parsing events after
   * invoking this method. It should still be possible for the application to
   * process the document through to the end.
   * </p>
   * <p/>
   * <p>
   * The action of the standard error listener depends on the recovery policy
   * that has been set, which may be one of RECOVER_SILENTLY,
   * RECOVER_WITH_WARNING, or DO_NOT_RECOVER
   * 
   * @param exception
   *          The error information encapsulated in a transformer exception.
   * @throws TransformerException
   *           if the application chooses to discontinue the transformation.
   * @see TransformerException
   */

  public void error(TransformerException exception) throws TransformerException {
    if (recoveryPolicy == Configuration.RECOVER_SILENTLY && !(exception instanceof ValidationException)) {
      // do nothing
      return;
    }
    String message;
    if (exception instanceof ValidationException) {
      String explanation = getExpandedMessage(exception);
      String constraintReference = ((ValidationException) exception).getConstraintReferenceMessage();
      String validationLocation = ((ValidationException) exception).getValidationLocationText();
      String contextLocation = ((ValidationException) exception).getContextLocationText();
      message = "Validation error " + getLocationMessage(exception) + "\n  " + wordWrap(explanation) + wordWrap(validationLocation.length() == 0 ? "" : "\n  " + validationLocation) + wordWrap(contextLocation.length() == 0 ? "" : "\n  " + contextLocation) + wordWrap(constraintReference == null ? "" : "\n  " + constraintReference);
    } else {
      String prefix = recoveryPolicy == Configuration.RECOVER_WITH_WARNINGS ? "Recoverable error " : "Error ";
      message = prefix + getLocationMessage(exception) + "\n  " + wordWrap(getExpandedMessage(exception));
    }

    if (exception instanceof ValidationException) {
      logError(message);
    } else if (recoveryPolicy == Configuration.RECOVER_WITH_WARNINGS) {
      logger.warning(message);
      warningCount++;
      if (warningCount > 25) {
        logger.info("No more warnings will be displayed");
        recoveryPolicy = Configuration.RECOVER_SILENTLY;
        warningCount = 0;
      }
    } else {
      logError(message + "\n" + "Processing terminated because error recovery is disabled");
      throw XPathException.makeXPathException(exception);
    }
  }

  /**
   * Receive notification of a non-recoverable error.
   * <p/>
   * <p>
   * The application must assume that the transformation cannot continue after
   * the Transformer has invoked this method, and should continue (if at all)
   * only to collect addition error messages. In fact, Transformers are free to
   * stop reporting events once this method has been invoked.
   * </p>
   * 
   * @param exception
   *          The error information encapsulated in a transformer exception.
   * @throws TransformerException
   *           if the application chooses to discontinue the transformation.
   * @see TransformerException
   */

  public void fatalError(TransformerException exception) throws TransformerException {
    if (exception instanceof XPathException && ((XPathException) exception).hasBeenReported()) {
      // don't report the same error twice
      return;
    }
    String message;
    if (exception instanceof ValidationException) {
      String explanation = getExpandedMessage(exception);
      String constraintReference = ((ValidationException) exception).getConstraintReferenceMessage();
      if (constraintReference != null) {
        explanation += " (" + constraintReference + ')';
      }
      message = "Validation error " + getLocationMessage(exception) + "\n  " + wordWrap(explanation);
    } else {
      message = "Error " + getLocationMessage(exception) + "\n  " + wordWrap(getExpandedMessage(exception));
    }

    logError(message);
    if (exception instanceof XPathException) {
      ((XPathException) exception).setHasBeenReported(true);
      // probably redundant. It's the caller's job to set this flag, because
      // there might be
      // a non-standard error listener in use.
    }

    if (exception instanceof XPathException) {
      XPathContext context = ((XPathException) exception).getXPathContext();
      if (context != null && getRecoveryPolicy() != Configuration.RECOVER_SILENTLY) {
        outputStackTrace(logger, context);
      }
    }
  }

  /**
   * Generate a stack trace. This method is protected so it can be overridden in
   * a subclass.
   * 
   * @param out
   *          the destination for the stack trace
   * @param context
   *          the context (which holds the information to be output)
   */

  protected void outputStackTrace(Logger out, XPathContext context) {
    printStackTrace(out, context);
  }

  /**
   * Get a string identifying the location of an error.
   * 
   * @param err
   *          the exception containing the location information
   * @return a message string describing the location
   */

  public String getLocationMessage(TransformerException err) {
    SourceLocator loc = err.getLocator();
    while (loc == null) {
      if (err.getException() instanceof TransformerException) {
        err = (TransformerException) err.getException();
        loc = err.getLocator();
      } else if (err.getCause() instanceof TransformerException) {
        err = (TransformerException) err.getCause();
        loc = err.getLocator();
      } else {
        return "";
      }
    }
    return getLocationMessageText(loc);
  }

  private static String getLocationMessageText(SourceLocator loc) {
    String locMessage = "";
    String systemId = null;
    NodeInfo node = null;
    String path;
    String nodeMessage = null;
    int lineNumber = -1;
    if (loc instanceof DOMLocator) {
      nodeMessage = "at " + ((DOMLocator) loc).getOriginatingNode().getNodeName() + ' ';
    } else if (loc instanceof NodeInfo) {
      node = (NodeInfo) loc;
      nodeMessage = "at " + node.getDisplayName() + ' ';
    } else if (loc instanceof ValidationException && (node = ((ValidationException) loc).getNode()) != null) {
      nodeMessage = "at " + node.getDisplayName() + ' ';
    } else if (loc instanceof ValidationException && loc.getLineNumber() == -1 && (path = ((ValidationException) loc).getPath()) != null) {
      nodeMessage = "at " + path + ' ';
    } else if (loc instanceof Instruction) {
      String instructionName = getInstructionName((Instruction) loc);
      if (!"".equals(instructionName)) {
        nodeMessage = "at " + instructionName + ' ';
      }
      systemId = loc.getSystemId();
      lineNumber = loc.getLineNumber();
    } else if (loc instanceof ComponentBody) {
      String kind = "procedure";
      if (loc instanceof UserFunction) {
        kind = "function";
      } else if (loc instanceof Template) {
        kind = "template";
      } else if (loc instanceof AttributeSet) {
        kind = "attribute-set";
      } else if (loc instanceof KeyDefinition) {
        kind = "key";
      }
      systemId = loc.getSystemId();
      lineNumber = loc.getLineNumber();
      nodeMessage = "at " + kind + " ";
      StructuredQName name = ((InstructionInfo) loc).getObjectName();
      if (name != null) {
        nodeMessage += name.toString();
        nodeMessage += " ";
      }
    }
    if (lineNumber == -1) {
      lineNumber = loc.getLineNumber();
    }
    boolean containsLineNumber = lineNumber != -1;
    if (node != null && !containsLineNumber) {
      nodeMessage = "at " + Navigator.getPath(node) + ' ';
    }
    if (nodeMessage != null) {
      locMessage += nodeMessage;
    }
    if (containsLineNumber) {
      locMessage += "on line " + lineNumber + ' ';
      if (loc.getColumnNumber() != -1) {
        locMessage += "column " + loc.getColumnNumber() + ' ';
      }
    }

    if (systemId != null && systemId.length() == 0) {
      systemId = null;
    }
    if (systemId == null) {
      systemId = loc.getSystemId();
    }
    if (systemId != null && systemId.length() != 0) {
      locMessage += (containsLineNumber ? "of " : "in ") + abbreviatePath(systemId) + ':';
    }
    return locMessage;
  }

  /**
   * Abbreviate a URI (if requested)
   * 
   * @param uri
   *          the URI to be abbreviated
   * @return the abbreviated URI, unless full path names were requested, in
   *         which case the URI as supplied
   */

  /* @Nullable */
  public static String abbreviatePath(String uri) {
    if (uri == null) {
      return "*unknown*";
    }
    int slash = uri.lastIndexOf('/');
    if (slash >= 0 && slash < uri.length() - 1) {
      return uri.substring(slash + 1);
    } else {
      return uri;
    }
  }

  /**
   * Get a string containing the message for this exception and all contained
   * exceptions
   * 
   * @param err
   *          the exception containing the required information
   * @return a message that concatenates the message of this exception with its
   *         contained exceptions, also including information about the error
   *         code and location.
   */

  public String getExpandedMessage(TransformerException err) {

    StructuredQName qCode = null;
    String additionalLocationText = null;
    if (err instanceof XPathException) {
      qCode = ((XPathException) err).getErrorCodeQName();
      additionalLocationText = ((XPathException) err).getAdditionalLocationText();
    }
    if (qCode == null && err.getException() instanceof XPathException) {
      qCode = ((XPathException) err.getException()).getErrorCodeQName();
    }
    String message = "";
    if (qCode != null) {
      if (qCode.hasURI(NamespaceConstant.ERR)) {
        message = qCode.getLocalPart();
      } else {
        message = qCode.getDisplayName();
      }
    }

    if (additionalLocationText != null) {
      message += " " + additionalLocationText;
    }

    if (err instanceof XPathException) {
      Sequence errorObject = ((XPathException) err).getErrorObject();
      if (errorObject != null) {
        String errorObjectDesc = getErrorObjectString(errorObject);
        if (errorObjectDesc != null) {
          message += " " + errorObjectDesc;
        }
      }
    }

    Throwable e = err;
    while (true) {
      if (e == null) {
        break;
      }
      String next = e.getMessage();
      if (next == null) {
        next = "";
      }
      if (next.startsWith("net.sf.saxon.trans.XPathException: ")) {
        next = next.substring(next.indexOf(": ") + 2);
      }
      if (!("TRaX Transform Exception".equals(next) || message.endsWith(next))) {
        if (!"".equals(message) && !message.trim().endsWith(":")) {
          message += ": ";
        }
        message += next;
      }
      if (e instanceof TransformerException) {
        e = ((TransformerException) e).getException();
      } else if (e instanceof SAXException) {
        e = ((SAXException) e).getException();
      } else {
        // e.printStackTrace();
        break;
      }
    }

    return message;
  }

  /**
   * Get a string representation of the error object associated with the
   * exception (this represents the final argument to fn:error, in the case of
   * error triggered by calls on the fn:error function). The standard
   * implementation returns null, meaning that the error object is not
   * displayed; but the method can be overridden in a subclass to create a
   * custom display of the error object, which is then appended to the message
   * text.
   * 
   * @param errorObject
   *          the error object passed as the last argument to fn:error. Note:
   *          this method is not called if the error object is absent/null
   * @return a string representation of the error object to be appended to the
   *         message, or null if no output of the error object is required
   */
  public String getErrorObjectString(Sequence errorObject) {
    return null;
  }

  /**
   * Extract a name identifying the instruction at which an error occurred
   * 
   * @param inst
   *          the provider of information
   * @return the name of the containing instruction or expression, in
   *         user-meaningful terms
   */

  public static String getInstructionName(Instruction inst) {
    try {
      // InstructionInfo info = inst.getInstructionInfo();
      int construct = inst.getInstructionNameCode();
      if (construct < 0) {
        return "";
      }
      if (construct < 1024 && construct != StandardNames.XSL_FUNCTION && construct != StandardNames.XSL_TEMPLATE) {
        // it's a standard name
        if (inst.getContainer().getPackageData().getHostLanguage() == Configuration.XSLT) {
          return StandardNames.getDisplayName(construct);
        } else {
          String s = StandardNames.getDisplayName(construct);
          int colon = s.indexOf(':');
          if (colon > 0) {
            String local = s.substring(colon + 1);
            if (local.equals("document")) {
              return "document node constructor";
            } else if (local.equals("text") || s.equals("value-of")) {
              return "text node constructor";
            } else if (local.equals("element")) {
              return "computed element constructor";
            } else if (local.equals("attribute")) {
              return "computed attribute constructor";
            } else if (local.equals("variable")) {
              return "variable declaration";
            } else if (local.equals("param")) {
              return "external variable declaration";
            } else if (local.equals("comment")) {
              return "comment constructor";
            } else if (local.equals("processing-instruction")) {
              return "processing-instruction constructor";
            } else if (local.equals("namespace")) {
              return "namespace node constructor";
            }
          }
          return s;
        }
      }
      switch (construct) {
      case Location.LITERAL_RESULT_ELEMENT: {
        StructuredQName qName = inst.getObjectName();
        return "element constructor <" + qName.getDisplayName() + '>';
      }
      case Location.LITERAL_RESULT_ATTRIBUTE: {
        StructuredQName qName = inst.getObjectName();
        return "attribute constructor " + qName.getDisplayName() + "=\"{...}\"";
      }

      default:
        return "";
      }

    } catch (Exception err) {
      return "";
    }
  }

  /**
   * Wordwrap an error message into lines of 72 characters or less (if possible)
   * 
   * @param message
   *          the message to be word-wrapped
   * @return the message after applying word-wrapping
   */

  private static String wordWrap(String message) {
    if (message.length() > 1000) {
      message = message.substring(0, 1000);
    }
    int nl = message.indexOf('\n');
    if (nl < 0) {
      nl = message.length();
    }
    if (nl > 100) {
      int i = 90;
      while (message.charAt(i) != ' ' && i > 0) {
        i--;
      }
      if (i > 10) {
        return message.substring(0, i) + "\n  " + wordWrap(message.substring(i + 1));
      } else {
        return message;
      }
    } else if (nl < message.length()) {
      return message.substring(0, nl) + '\n' + wordWrap(message.substring(nl + 1));
    } else {
      return message;
    }
  }

  /**
   * Print a stack trace to a specified output destination
   * 
   * @param out
   *          the print stream to which the stack trace will be output
   * @param context
   *          the XPath dynamic execution context (which holds the head of a
   *          linked list of context objects, representing the execution stack)
   */

  public static void printStackTrace(Logger out, XPathContext context) {
    Iterator<ContextStackFrame> iterator = new ContextStackIterator(context);
    while (iterator.hasNext()) {
      ContextStackFrame frame = iterator.next();
      frame.print(out);
    }
  }

}