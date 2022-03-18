package nl.armatiek.xslweb.saxon.functions.request;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.exquery.http.AcceptHeader;
import org.exquery.http.AcceptHeader.Accept;
import org.exquery.http.InternetMediaType;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class MatchAcceptHeader extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_REQUEST, "match-accept-header");

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
    return new SequenceType[] { 
        SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ONE_OR_MORE) 
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_STRING;
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new MatchAcceptHeaderCall();
  }
  
  private static class MatchAcceptHeaderCall extends ExtensionFunctionCall {
    
    private final static Pattern ptnMediaType = Pattern.compile(InternetMediaType.mediaType_regExp);
    
    protected String encodeAsRegExp(String mediaType) {
      // escape chars in an Internet Media Type that have significance in a regexp:
      mediaType = mediaType.replace("$", "\\$");
      mediaType = mediaType.replace(".", "\\.");
      mediaType = mediaType.replace("+", "\\+");
      mediaType = mediaType.replace("-", "\\-");
      mediaType = mediaType.replace("^", "\\^");
      mediaType = mediaType.replace("/", "\\/");
      // expand subtype wildcard to valid regexp:
      mediaType = mediaType.replace("*", InternetMediaType.subtypeName_regExp);
      return mediaType;
    }

    protected void validateMediaTypes(final ArrayList<String> mediaTypes) throws XPathException {
      Matcher mtcMediaType = null;  
      for (final String mediaType : mediaTypes) {
        if (mediaType.isEmpty()) {
          throw new XPathException("Invalid MediaType (empty)");
        }
        if (mtcMediaType == null) {
          mtcMediaType = ptnMediaType.matcher(mediaType);
        } else {
          mtcMediaType = mtcMediaType.reset(mediaType);
        }
        if (!mtcMediaType.matches()) {
          throw new XPathException(String.format("Invalid mediatype %s", mediaType));
        }
      }
    }
    
    @Override
    public ZeroOrOne<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {                                                                                                  
      SequenceIterator iter = arguments[0].iterate(); 
      ArrayList<String> mediaTypes = new ArrayList<String>();
      Item item;
      while ((item = iter.next()) != null) {
        mediaTypes.add(((StringValue) item).getStringValue());
      }
      
      HttpServletRequest request = getRequest(context);
      String acceptHeaderValue = request.getHeader("Accept");
      if (acceptHeaderValue == null) {
        // TODO: base best matching mediatype on Content-Type header (json in -> json out)?
        return new ZeroOrOne<StringValue>(new StringValue(mediaTypes.get(0)));
      }
      
      validateMediaTypes(mediaTypes);
      final AcceptHeader acceptHeader = new AcceptHeader(acceptHeaderValue);
      for (final Accept accept : acceptHeader.getAccepts()) {
        final Pattern pEncodedMediaType = Pattern.compile(encodeAsRegExp(accept.getMediaRange()));
        Matcher mtcMediaType = null;
        for (final String internetMediaType : mediaTypes) {
          if (mtcMediaType == null) {
            mtcMediaType = pEncodedMediaType.matcher(internetMediaType);
          } else {
            mtcMediaType = mtcMediaType.reset(internetMediaType);
          }
          if (mtcMediaType.matches()) {
            return new ZeroOrOne<StringValue>(new StringValue(internetMediaType));
          }
        }
      }
      
      return ZeroOrOne.empty();
    } 
  }
  
}