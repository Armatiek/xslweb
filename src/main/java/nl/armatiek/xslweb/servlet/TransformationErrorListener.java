package nl.armatiek.xslweb.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import nl.armatiek.xslweb.configuration.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformationErrorListener implements ErrorListener {
  
  private static final Logger logger = LoggerFactory.getLogger(TransformationErrorListener.class);
  
  protected HttpServletResponse response;
  protected boolean firstError;
  
  public TransformationErrorListener(HttpServletResponse response) {
    this.response = response;
    this.firstError = true;
  }
  
  protected void handleError(TransformerException exception) throws TransformerException {
    try {
      logger.error(exception.getMessage(), exception);
      if (Context.getInstance().isDevelopmentMode()) {
        if (firstError) {
          response.setContentType("text/plain;charset=UTF-8");
          firstError = false;
        }
        IOUtils.write(ExceptionUtils.getStackTrace(exception), response.getOutputStream(), "UTF-8");
      }
    } catch (IOException ioe) {
      throw new TransformerException("IOException dumping track trace");
    }
    throw exception;
  }

  @Override
  public void error(TransformerException exception) throws TransformerException {
    handleError(exception);
  }

  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    handleError(exception);
  }

  @Override
  public void warning(TransformerException exception) throws TransformerException {
    logger.warn(exception.getMessage(), exception);
  }

}