package nl.armatiek.xslweb.servlet;

import java.io.StringWriter;

public class LogWriter extends StringWriter {
  
  private String message;
  
  public LogWriter(String message) {
    this.message = message;
  }
  
  public String getMessage() {
    return message;
  }
  
}
