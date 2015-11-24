package nl.armatiek.xslweb.saxon.functions.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.utils.XSLWebUtils;

/**
 * XPath extension function that logs to Slf4J logging framework.
 * 
 * @author Maarten Kroon
 */
public class RegisterTempFile extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_IO, "register-temp-file");

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
    return new SequenceType[] { SequenceType.SINGLE_STRING };
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
    return new RegisterTempFileCall();
  }
  
  private static class RegisterTempFileCall extends ExtensionFunctionCall {

    @SuppressWarnings("unchecked")
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        String path = ((StringValue) arguments[0].head()).getStringValue();
        File file;
        if ((file = XSLWebUtils.getSafeTempFile(path)) == null) {
          throw new XPathException("Could not register temporary file. Path is not a valid temporary path.");
        }                      
        HttpServletRequest request = getRequest(context);                
        List<File> tempFiles = (List<File>) request.getAttribute(Definitions.ATTRNAME_TEMPFILES);
        if (tempFiles == null) {
          tempFiles = new ArrayList<File>();
          request.setAttribute(Definitions.ATTRNAME_TEMPFILES, tempFiles);
        }
        tempFiles.add(file);
        return EmptySequence.getInstance();
      } catch (Exception e) {
        throw new XPathException("Could not register temporary file", e);
      }
    }
  }
}