package nl.armatiek.xslweb.saxon.functions.expath.file;

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

import java.io.File;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Maarten Kroon
 * @see <a href="http://expath.org/spec/file">EXPath File Module</a>
 */
public class WriteTextLinesCall extends FileExtensionFunctionCall {
  
  private boolean append;
  
  public WriteTextLinesCall(boolean append) {
    this.append = append;
  }
  
  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {      
    try {                      
      File file = getFile(((StringValue) arguments[0].head()).getStringValue());
      File parentFile = file.getParentFile();
      if (!parentFile.exists()) {
        throw new FileException(String.format("Parent directory \"%s\" does not exist", 
            parentFile.getAbsolutePath()), FileException.ERROR_PATH_NOT_DIRECTORY);
      }     
      if (file.isDirectory()) {
        throw new FileException(String.format("Path \"%s\" points to a directory", 
            file.getAbsolutePath()), FileException.ERROR_PATH_IS_DIRECTORY);
      }               
      String encoding = "UTF-8";
      if (arguments.length > 2) {
        encoding = ((StringValue) arguments[2].head()).getStringValue();                   
      }        
      try {
        ArrayList<String> lines = new ArrayList<String>();
        Sequence seq = arguments[1];
        SequenceIterator iter = seq.iterate();
        Item item = null;
        while ((item = iter.next()) != null) {
          lines.add(item.getStringValue());            
        }                              
        FileUtils.writeLines(file, encoding, lines, System.getProperty("line.separator"), append);                    
      } catch (UnsupportedCharsetException uce) {
        throw new FileException(String.format("Encoding \"%s\" is invalid or not supported", 
            encoding), FileException.ERROR_UNKNOWN_ENCODING);
      }
      return EmptySequence.getInstance();
    } catch (FileException fe) {
      throw fe;
    } catch (Exception e) {
      throw new FileException("Other file error", e, FileException.ERROR_IO);
    }
  } 
}
