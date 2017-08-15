package nl.armatiek.xslweb.saxon.configuration;

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

import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.expath.httpclient.saxon.SendRequestFunction;
import org.expath.pkg.saxon.EXPathFunctionDefinition;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.Initializer;
import nl.armatiek.xslweb.saxon.functions.base64.Base64Decode;
import nl.armatiek.xslweb.saxon.functions.base64.Base64Encode;
import nl.armatiek.xslweb.saxon.functions.diff.DiffText;
import nl.armatiek.xslweb.saxon.functions.diff.DiffXML;
import nl.armatiek.xslweb.saxon.functions.exec.ExecExternal;
import nl.armatiek.xslweb.saxon.functions.expath.file.Append;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendText;
import nl.armatiek.xslweb.saxon.functions.expath.file.AppendTextLines;
import nl.armatiek.xslweb.saxon.functions.expath.file.Children;
import nl.armatiek.xslweb.saxon.functions.expath.file.Copy;
import nl.armatiek.xslweb.saxon.functions.expath.file.CreateDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.CreateTempDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.CreateTempFile;
import nl.armatiek.xslweb.saxon.functions.expath.file.Delete;
import nl.armatiek.xslweb.saxon.functions.expath.file.DirName;
import nl.armatiek.xslweb.saxon.functions.expath.file.DirSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.Exists;
import nl.armatiek.xslweb.saxon.functions.expath.file.IsDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.IsFile;
import nl.armatiek.xslweb.saxon.functions.expath.file.LastModified;
import nl.armatiek.xslweb.saxon.functions.expath.file.LineSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.Move;
import nl.armatiek.xslweb.saxon.functions.expath.file.Name;
import nl.armatiek.xslweb.saxon.functions.expath.file.Parent;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathSeparator;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathToNative;
import nl.armatiek.xslweb.saxon.functions.expath.file.PathToURI;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadText;
import nl.armatiek.xslweb.saxon.functions.expath.file.ReadTextLines;
import nl.armatiek.xslweb.saxon.functions.expath.file.ResolvePath;
import nl.armatiek.xslweb.saxon.functions.expath.file.Size;
import nl.armatiek.xslweb.saxon.functions.expath.file.TempDir;
import nl.armatiek.xslweb.saxon.functions.expath.file.Write;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteBinary;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteText;
import nl.armatiek.xslweb.saxon.functions.expath.file.WriteTextLines;
import nl.armatiek.xslweb.saxon.functions.image.Scale;
import nl.armatiek.xslweb.saxon.functions.io.RegisterTempFile;
import nl.armatiek.xslweb.saxon.functions.json.EscapeJSON;
import nl.armatiek.xslweb.saxon.functions.json.ParseJSON;
import nl.armatiek.xslweb.saxon.functions.json.SerializeJSON;
import nl.armatiek.xslweb.saxon.functions.json.UnescapeJSON;
import nl.armatiek.xslweb.saxon.functions.log.Log;
import nl.armatiek.xslweb.saxon.functions.mail.SendMail;
import nl.armatiek.xslweb.saxon.functions.response.AddCookie;
import nl.armatiek.xslweb.saxon.functions.response.AddDateHeader;
import nl.armatiek.xslweb.saxon.functions.response.AddHeader;
import nl.armatiek.xslweb.saxon.functions.response.AddIntHeader;
import nl.armatiek.xslweb.saxon.functions.response.EncodeRedirectURL;
import nl.armatiek.xslweb.saxon.functions.response.EncodeURL;
import nl.armatiek.xslweb.saxon.functions.response.IsCommitted;
import nl.armatiek.xslweb.saxon.functions.response.SetBufferSize;
import nl.armatiek.xslweb.saxon.functions.response.SetStatus;
import nl.armatiek.xslweb.saxon.functions.script.Invoke;
import nl.armatiek.xslweb.saxon.functions.serialize.Serialize;
import nl.armatiek.xslweb.saxon.functions.session.Invalidate;
import nl.armatiek.xslweb.saxon.functions.session.SetMaxInactiveInterval;
import nl.armatiek.xslweb.saxon.functions.sql.Close;
import nl.armatiek.xslweb.saxon.functions.sql.Commit;
import nl.armatiek.xslweb.saxon.functions.sql.ExecuteQuery;
import nl.armatiek.xslweb.saxon.functions.sql.ExecuteUpdate;
import nl.armatiek.xslweb.saxon.functions.sql.GetConnection;
import nl.armatiek.xslweb.saxon.functions.sql.GetNextRow;
import nl.armatiek.xslweb.saxon.functions.sql.ResultSetToNode;
import nl.armatiek.xslweb.saxon.functions.sql.Rollback;
import nl.armatiek.xslweb.saxon.functions.util.DiscardDocument;
import nl.armatiek.xslweb.saxon.functions.util.Parse;
import nl.armatiek.xslweb.saxon.functions.uuid.UUID;
import nl.armatiek.xslweb.saxon.functions.zip.Unzip;
import nl.armatiek.xslweb.saxon.functions.zip.Zip;
import nl.armatiek.xslweb.saxon.uriresolver.XSLWebURIResolver;

public class XSLWebInitializer implements Initializer {
  
  private Set<String> functionClassNames = new HashSet<String>(); 

  @Override
  public void initialize(Configuration configuration) throws TransformerException {    
    configuration.setXIncludeAware(true);
        
    configuration.setConfigurationProperty(FeatureKeys.RECOVERY_POLICY_NAME, "recoverWithWarnings");
    configuration.setConfigurationProperty(FeatureKeys.SUPPRESS_XSLT_NAMESPACE_CHECK, Boolean.TRUE);
    
    configuration.setURIResolver(new XSLWebURIResolver());
    
    /* Log */
    registerEXPathFunction(new Log(), configuration);
    
    /* Request */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.request.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.request.SetAttribute(), configuration);
    
    /* Response */
    registerEXPathFunction(new AddCookie(), configuration);
    registerEXPathFunction(new AddDateHeader(), configuration);
    registerEXPathFunction(new AddHeader(), configuration);
    registerEXPathFunction(new AddIntHeader(), configuration);    
    registerEXPathFunction(new EncodeRedirectURL(), configuration);
    registerEXPathFunction(new EncodeURL(), configuration);    
    registerEXPathFunction(new IsCommitted(), configuration);    
    registerEXPathFunction(new SetBufferSize(), configuration);
    registerEXPathFunction(new SetStatus(), configuration);
    
    /* Base64 */
    registerEXPathFunction(new Base64Encode(), configuration);
    registerEXPathFunction(new Base64Decode(), configuration);
    
    /* Context */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.context.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.context.SetAttribute(), configuration);
    
    /* Session */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.session.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.session.SetAttribute(), configuration);
    registerEXPathFunction(new Invalidate(), configuration);
    registerEXPathFunction(new SetMaxInactiveInterval(), configuration);
    
    /* Webapp */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.GetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.SetAttribute(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.GetCacheValue(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.SetCacheValue(), configuration);
    
    /* Email */
    registerEXPathFunction(new SendMail(), configuration);
    
    /* Cache */
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.cache.Remove(), configuration);
    
    /* Serialize */
    registerEXPathFunction(new Serialize(), configuration);   
    
    /* EXPath File: */
    registerEXPathFunction(new Append(), configuration);
    registerEXPathFunction(new AppendBinary(), configuration);
    registerEXPathFunction(new AppendText(), configuration);
    registerEXPathFunction(new AppendTextLines(), configuration);
    registerEXPathFunction(new Children(), configuration);
    registerEXPathFunction(new Copy(), configuration);
    registerEXPathFunction(new CreateDir(), configuration);
    registerEXPathFunction(new CreateTempDir(), configuration);
    registerEXPathFunction(new CreateTempFile(), configuration);
    registerEXPathFunction(new Delete(), configuration);
    registerEXPathFunction(new DirName(), configuration);
    registerEXPathFunction(new DirSeparator(), configuration);
    registerEXPathFunction(new Exists(), configuration);
    registerEXPathFunction(new IsDir(), configuration);
    registerEXPathFunction(new IsFile(), configuration);
    registerEXPathFunction(new LastModified(), configuration);
    registerEXPathFunction(new LineSeparator(), configuration);
    registerEXPathFunction(new nl.armatiek.xslweb.saxon.functions.expath.file.List(), configuration);
    registerEXPathFunction(new Move(), configuration);
    registerEXPathFunction(new Name(), configuration);
    registerEXPathFunction(new Parent(), configuration);
    registerEXPathFunction(new PathSeparator(), configuration);
    registerEXPathFunction(new PathToNative(), configuration);
    registerEXPathFunction(new PathToURI(), configuration);
    registerEXPathFunction(new ReadBinary(), configuration);
    registerEXPathFunction(new ReadText(), configuration);
    registerEXPathFunction(new ReadTextLines(), configuration);
    registerEXPathFunction(new ResolvePath(), configuration);
    registerEXPathFunction(new Size(), configuration);
    registerEXPathFunction(new TempDir(), configuration);
    registerEXPathFunction(new Write(), configuration);
    registerEXPathFunction(new WriteBinary(), configuration);
    registerEXPathFunction(new WriteText(), configuration);
    registerEXPathFunction(new WriteTextLines(), configuration);

    /* EXPath Zip: */
    // registerEXPathFunction(new EntriesFunction(), configuration);
    // registerEXPathFunction(new UpdateEntriesFunction(), configuration);
    // registerEXPathFunction(new ZipFileFunction(), configuration);
    // registerEXPathFunction(new BinaryEntryFunction(), configuration);
    // registerEXPathFunction(new HtmlEntryFunction(), configuration);
    // registerEXPathFunction(new TextEntryFunction(), configuration);
    // registerEXPathFunction(new XmlEntryFunction(), configuration);
    
    /* EXPath HttpClient: */           
    registerEXPathFunction(new SendRequestFunction(), configuration);  
    
    /* Script */
    registerEXPathFunction(new Invoke(), configuration);
    
    /* JSON */
    registerEXPathFunction(new ParseJSON(), configuration);
    registerEXPathFunction(new SerializeJSON(), configuration);
    registerEXPathFunction(new EscapeJSON(), configuration);
    registerEXPathFunction(new UnescapeJSON(), configuration);
    
    /* Sql */
    registerEXPathFunction(new Close(), configuration);
    registerEXPathFunction(new Commit(), configuration);
    registerEXPathFunction(new ExecuteQuery(), configuration);
    registerEXPathFunction(new ExecuteUpdate(), configuration);
    registerEXPathFunction(new GetConnection(), configuration);
    registerEXPathFunction(new GetNextRow(), configuration);
    registerEXPathFunction(new Rollback(), configuration);
    registerEXPathFunction(new ResultSetToNode(), configuration);
    
    /* UUID */
    registerEXPathFunction(new UUID(), configuration);
    
    /* Util */
    registerEXPathFunction(new DiscardDocument(), configuration);
    registerEXPathFunction(new Parse(), configuration);
    
    /* Zip */
    registerEXPathFunction(new Zip(), configuration);
    registerEXPathFunction(new Unzip(), configuration);
    
    /* Image */
    registerEXPathFunction(new Scale(), configuration);
    
    /* IO */
    registerEXPathFunction(new RegisterTempFile(), configuration);
    
    /* ExecExternal */
    registerEXPathFunction(new ExecExternal(), configuration);
    
    /* Diff */
    registerEXPathFunction(new DiffXML(), configuration);
    registerEXPathFunction(new DiffText(), configuration);
    
  }
  
  private void registerEXPathFunction(ExtensionFunctionDefinition function, Configuration configuration) {
    if (function instanceof EXPathFunctionDefinition) {
      ((EXPathFunctionDefinition) function).setConfiguration(configuration);
    }              
    configuration.registerExtensionFunction(function);
    functionClassNames.add(function.getClass().getName());
  }
  
  public boolean isFunctionRegistered(String className) {
    return functionClassNames.contains(className);
  }
  
}