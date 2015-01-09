package nl.armatiek.xslweb.web.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.apache.commons.io.IOUtils;

public class DelegatingServletOutputStream extends ServletOutputStream {

  private final OutputStream os;
  private String lineSeparator;

  public DelegatingServletOutputStream(OutputStream os) {
    this.os = os;
    this.lineSeparator = System.lineSeparator();
  }

  @Override
  public void print(String s) throws IOException {
    IOUtils.write(s, os);    
  }

  @Override
  public void print(boolean b) throws IOException {
    IOUtils.write(String.valueOf(b), os);
  }

  @Override
  public void print(char c) throws IOException {
    IOUtils.write(String.valueOf(c), os);
  }

  @Override
  public void print(int i) throws IOException {
    IOUtils.write(String.valueOf(i), os);    
  }

  @Override
  public void print(long l) throws IOException {
    IOUtils.write(String.valueOf(l), os);
  }

  @Override
  public void print(float f) throws IOException {
    IOUtils.write(String.valueOf(f), os);
  }

  @Override
  public void print(double d) throws IOException {
    IOUtils.write(String.valueOf(d), os);
  }

  @Override
  public void println() throws IOException {
    IOUtils.write(lineSeparator, os);    
  }

  @Override
  public void println(String s) throws IOException {    
    IOUtils.write(s + lineSeparator, os);
  }

  @Override
  public void println(boolean b) throws IOException {
    IOUtils.write(String.valueOf(b) + lineSeparator, os);
  }

  @Override
  public void println(char c) throws IOException {
    IOUtils.write(String.valueOf(c) + lineSeparator, os);
  }

  @Override
  public void println(int i) throws IOException {
    IOUtils.write(String.valueOf(i) + lineSeparator, os);
  }

  @Override
  public void println(long l) throws IOException {
    IOUtils.write(String.valueOf(l) + lineSeparator, os);
  }

  @Override
  public void println(float f) throws IOException {
    IOUtils.write(String.valueOf(f) + lineSeparator, os);
  }

  @Override
  public void println(double d) throws IOException {
    IOUtils.write(String.valueOf(d) + lineSeparator, os);
  }

  @Override
  public void write(int b) throws IOException {
    os.write(b);    
  }

  @Override
  public void write(byte[] b) throws IOException {
    os.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    os.write(b, off, len);    
  }

  @Override
  public void flush() throws IOException {
    os.flush();
  }

  @Override
  public void close() throws IOException {
    os.close();
    super.close();
  }
  
}