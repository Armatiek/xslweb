package nl.armatiek.xslweb.saxon.functions.cache;

import net.sf.ehcache.Cache;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class Remove extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_CACHE, "remove");

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
  public ExtensionFunctionCall makeCallExpression() {
    return new ClearCall();
  }
  
  private static class ClearCall extends ExtensionFunctionCall {

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String key = ((StringValue) arguments[0].head()).getStringValue(); 
        Cache cache = Context.getInstance().getCacheManager().getCache(Definitions.CACHENAME_RESPONSECACHINGFILTER);
        cache.remove(key);
        return EmptySequence.getInstance();        
      } catch (Exception e) {
        throw new XPathException("Could not remove element from cache", e);
      }
    }
  }
}