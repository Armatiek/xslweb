package nl.armatiek.xslweb.web.servlet;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    IOUtils.write(s, os, "UTF-8");    
  }

  @Override
  public void print(boolean b) throws IOException {
    IOUtils.write(String.valueOf(b), os, "UTF-8");
  }

  @Override
  public void print(char c) throws IOException {
    IOUtils.write(String.valueOf(c), os, "UTF-8");
  }

  @Override
  public void print(int i) throws IOException {
    IOUtils.write(String.valueOf(i), os, "UTF-8");    
  }

  @Override
  public void print(long l) throws IOException {
    IOUtils.write(String.valueOf(l), os, "UTF-8");
  }

  @Override
  public void print(float f) throws IOException {
    IOUtils.write(String.valueOf(f), os, "UTF-8");
  }

  @Override
  public void print(double d) throws IOException {
    IOUtils.write(String.valueOf(d), os, "UTF-8");
  }

  @Override
  public void println() throws IOException {
    IOUtils.write(lineSeparator, os, "UTF-8");    
  }

  @Override
  public void println(String s) throws IOException {    
    IOUtils.write(s + lineSeparator, os, "UTF-8");
  }

  @Override
  public void println(boolean b) throws IOException {
    IOUtils.write(String.valueOf(b) + lineSeparator, os, "UTF-8");
  }

  @Override
  public void println(char c) throws IOException {
    IOUtils.write(String.valueOf(c) + lineSeparator, os, "UTF-8");
  }

  @Override
  public void println(int i) throws IOException {
    IOUtils.write(String.valueOf(i) + lineSeparator, os, "UTF-8");
  }

  @Override
  public void println(long l) throws IOException {
    IOUtils.write(String.valueOf(l) + lineSeparator, os, "UTF-8");
  }

  @Override
  public void println(float f) throws IOException {
    IOUtils.write(String.valueOf(f) + lineSeparator, os, "UTF-8");
  }

  @Override
  public void println(double d) throws IOException {
    IOUtils.write(String.valueOf(d) + lineSeparator, os, "UTF-8");
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