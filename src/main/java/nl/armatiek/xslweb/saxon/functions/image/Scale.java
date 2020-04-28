package nl.armatiek.xslweb.saxon.functions.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class Scale extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_IMAGE, "scale");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 4;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 4;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, 
        SequenceType.SINGLE_STRING, SequenceType.SINGLE_INTEGER };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ScaleCall();
  }
  
  private static class ScaleCall extends ExtensionFunctionCall {

    @Override
    public Sequence<?> call(XPathContext context, Sequence[] arguments) throws XPathException {
      String source = ((StringValue) arguments[0].head()).getStringValue();
      String target = ((StringValue) arguments[1].head()).getStringValue();      
      String formatName = ((StringValue) arguments[2].head()).getStringValue();
      int targetSize = (int) ((Int64Value) arguments[3].head()).longValue();
      try {                             
        File targetFile = new File(target);                
        if (targetFile.isDirectory()) {          
          throw new IOException("Output file \"" + targetFile.getAbsolutePath() + "\" already exists as directory");          
        } else if (!targetFile.getParentFile().isDirectory() && !targetFile.getParentFile().mkdirs()) {
          throw new IOException("Could not create output directory \"" + targetFile.getParentFile().getAbsolutePath() + "\"");
        } else if (targetFile.isFile() && !targetFile.delete()) {
          throw new IOException("Error deleting existing target file \"" + targetFile.getAbsolutePath() + "\"");
        }               
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
          BufferedImage scaledImg = Scalr.resize(img, Method.AUTOMATIC, Mode.AUTOMATIC, targetSize, targetSize);
          BufferedImage imageToSave = new BufferedImage(scaledImg.getWidth(), scaledImg.getHeight(), BufferedImage.TYPE_INT_RGB);
          Graphics g = imageToSave.getGraphics();
          g.drawImage(scaledImg, 0, 0, null);          
          ImageIO.write(imageToSave, formatName, targetFile);
        } finally {
          is.close();
        }
        return EmptySequence.getInstance();
      } catch (Exception e) {
        throw new XPathException("Error scaling image \"" + source + "\" to \"" + target + "\"", e);
      }
    }
  }
}