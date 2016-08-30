package nl.armatiek.xslweb.saxon.functions.zip;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;
import nl.armatiek.xslweb.utils.ZipUtils;

/**
 * XPath extension function class for
 * 
 * @author Maarten Kroon
 */
public class Unzip extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_ZIP, "unzip");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;    
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new UnzipCall();
  }
  
  private static class UnzipCall extends ExtensionFunctionCall {

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
      String source = ((StringValue) arguments[0].head()).getStringValue();
      String target = ((StringValue) arguments[1].head()).getStringValue();
      try {              
        File targetDir = new File(target);                
        if (targetDir.isFile()) {          
          throw new IOException("Output directory \"" + targetDir.getAbsolutePath() + "\" already exists as file");          
        } else if (!targetDir.isDirectory() && !targetDir.mkdirs()) {
          throw new IOException("Could not create output directory \"" + targetDir.getAbsolutePath() + "\"");
        }        
        InputStream is;
        if (source.startsWith("http")) {          
          /*
          CloseableHttpClient client = getWebApp(context).getHttpClient();          
          CloseableHttpResponse response = client.execute(new HttpGet(source));
          is = response.getEntity().getContent();
          */                                        
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
          ZipUtils.unzipStream(is, targetDir);
        } finally {
          is.close();
        }       
        return EmptySequence.getInstance();
      } catch (Exception e) {
        throw new XPathException("Error unzipping \"" + source + "\" to \"" + target + "\"", e);
      }
    }
  }
}