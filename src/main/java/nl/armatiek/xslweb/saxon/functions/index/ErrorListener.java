package nl.armatiek.xslweb.saxon.functions.index;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorListener implements javax.xml.transform.ErrorListener {
  
  private static final Logger logger = LoggerFactory.getLogger(ErrorListener.class);
  
  private String reference;
  
  private List<TransformerException> errors;
  
  public ErrorListener(String reference) {
    this.reference = reference;
  }

  @Override
  public void warning(TransformerException exception) throws TransformerException {
    logger.warn("Warning processing " + reference, exception);
  }

  @Override
  public void error(TransformerException exception) throws TransformerException {
    logger.warn("Error processing " + reference, exception);
    addException(exception);
  }

  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    logger.warn("Fatal error processing " + reference, exception);
    addException(exception);
  }
  
  public TransformerException getFirstError() {
    if (errors.isEmpty())
      return null;
    return errors.get(0);
  }
  
  private void addException(TransformerException exception) {
    if (errors == null)
      errors = new ArrayList<TransformerException>();
    errors.add(exception);
  }

}
