package nl.armatiek.xslweb.saxon.functions.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.OneOrMore;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class Size extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_IMAGE, "size");

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
    return SequenceType.makeSequenceType(BuiltInAtomicType.POSITIVE_INTEGER, StaticProperty.ALLOWS_ONE_OR_MORE);    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new SizeCall();
  }
  
  private static class SizeCall extends ExtensionFunctionCall {

    @Override
    public OneOrMore<Int64Value> call(XPathContext context, Sequence[] arguments) throws XPathException {
      String source = ((StringValue) arguments[0].head()).getStringValue();
      try {                                            
        InputStream is;
        if (source.startsWith("http")) {
          is = new URL(source).openStream();
        } else {
          File file;
          if (source.startsWith("file:")) {
            file = new File(new URI(source));
          } else {
            file = new File(source);
          }                    
          if (!file.isFile()) {
            throw new IOException("File \"" + file.getAbsolutePath() + "\" not found or not a file");
          } 
          is = new BufferedInputStream(new FileInputStream(file));
        } 
        try {                
          BufferedImage img = ImageIO.read(is);          
          return new OneOrMore<Int64Value>(new Int64Value[]{new Int64Value(img.getWidth()), new Int64Value(img.getHeight())}); 
        } finally {
          is.close();
        }
      } catch (Exception e) {
        throw new XPathException("Error getting size of image \"" + source + "\"", e);
      }
    }
  }
}