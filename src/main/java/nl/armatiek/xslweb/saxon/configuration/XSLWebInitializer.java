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

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.Initializer;
import nl.armatiek.xslweb.saxon.functions.base64.Base64Decode;
import nl.armatiek.xslweb.saxon.functions.base64.Base64Encode;
import nl.armatiek.xslweb.saxon.functions.dynfunc.Call;
import nl.armatiek.xslweb.saxon.functions.dynfunc.IsRegistered;
import nl.armatiek.xslweb.saxon.functions.dynfunc.Register;
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
import nl.armatiek.xslweb.saxon.functions.httpclient.SendRequest;
import nl.armatiek.xslweb.saxon.functions.image.Scale;
import nl.armatiek.xslweb.saxon.functions.io.RegisterTempFile;
import nl.armatiek.xslweb.saxon.functions.json.EscapeJSON;
import nl.armatiek.xslweb.saxon.functions.json.ParseJSON;
import nl.armatiek.xslweb.saxon.functions.json.SerializeJSON;
import nl.armatiek.xslweb.saxon.functions.json.UnescapeJSON;
import nl.armatiek.xslweb.saxon.functions.log.Log;
import nl.armatiek.xslweb.saxon.functions.mail.SendMail;
import nl.armatiek.xslweb.saxon.functions.queue.AddRequest;
import nl.armatiek.xslweb.saxon.functions.queue.GetInfo;
import nl.armatiek.xslweb.saxon.functions.queue.GetStatus;
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
    registerXPathFunction(new Log(), configuration);
    
    /* Request */
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.request.GetAttribute(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.request.SetAttribute(), configuration);
    
    /* Response */
    registerXPathFunction(new AddCookie(), configuration);
    registerXPathFunction(new AddDateHeader(), configuration);
    registerXPathFunction(new AddHeader(), configuration);
    registerXPathFunction(new AddIntHeader(), configuration);    
    registerXPathFunction(new EncodeRedirectURL(), configuration);
    registerXPathFunction(new EncodeURL(), configuration);    
    registerXPathFunction(new IsCommitted(), configuration);    
    registerXPathFunction(new SetBufferSize(), configuration);
    registerXPathFunction(new SetStatus(), configuration);
    
    /* Base64 */
    registerXPathFunction(new Base64Encode(), configuration);
    registerXPathFunction(new Base64Decode(), configuration);
    
    /* Context */
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.context.GetAttribute(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.context.SetAttribute(), configuration);
    
    /* Session */
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.session.GetAttribute(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.session.SetAttribute(), configuration);
    registerXPathFunction(new Invalidate(), configuration);
    registerXPathFunction(new SetMaxInactiveInterval(), configuration);
    
    /* Webapp */
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.GetAttribute(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.SetAttribute(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.GetCacheValue(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.webapp.SetCacheValue(), configuration);
    
    /* Email */
    registerXPathFunction(new SendMail(), configuration);
    
    /* Cache */
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.cache.Remove(), configuration);
    
    /* Serialize */
    registerXPathFunction(new Serialize(), configuration);   
    
    /* EXPath File: */
    registerXPathFunction(new Append(), configuration);
    registerXPathFunction(new AppendBinary(), configuration);
    registerXPathFunction(new AppendText(), configuration);
    registerXPathFunction(new AppendTextLines(), configuration);
    registerXPathFunction(new Children(), configuration);
    registerXPathFunction(new Copy(), configuration);
    registerXPathFunction(new CreateDir(), configuration);
    registerXPathFunction(new CreateTempDir(), configuration);
    registerXPathFunction(new CreateTempFile(), configuration);
    registerXPathFunction(new Delete(), configuration);
    registerXPathFunction(new DirName(), configuration);
    registerXPathFunction(new DirSeparator(), configuration);
    registerXPathFunction(new Exists(), configuration);
    registerXPathFunction(new IsDir(), configuration);
    registerXPathFunction(new IsFile(), configuration);
    registerXPathFunction(new LastModified(), configuration);
    registerXPathFunction(new LineSeparator(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.expath.file.List(), configuration);
    registerXPathFunction(new Move(), configuration);
    registerXPathFunction(new Name(), configuration);
    registerXPathFunction(new Parent(), configuration);
    registerXPathFunction(new PathSeparator(), configuration);
    registerXPathFunction(new PathToNative(), configuration);
    registerXPathFunction(new PathToURI(), configuration);
    registerXPathFunction(new ReadBinary(), configuration);
    registerXPathFunction(new ReadText(), configuration);
    registerXPathFunction(new ReadTextLines(), configuration);
    registerXPathFunction(new ResolvePath(), configuration);
    registerXPathFunction(new Size(), configuration);
    registerXPathFunction(new TempDir(), configuration);
    registerXPathFunction(new Write(), configuration);
    registerXPathFunction(new WriteBinary(), configuration);
    registerXPathFunction(new WriteText(), configuration);
    registerXPathFunction(new WriteTextLines(), configuration);

    /* EXPath Zip: */
    // registerEXPathFunction(new EntriesFunction(), configuration);
    // registerEXPathFunction(new UpdateEntriesFunction(), configuration);
    // registerEXPathFunction(new ZipFileFunction(), configuration);
    // registerEXPathFunction(new BinaryEntryFunction(), configuration);
    // registerEXPathFunction(new HtmlEntryFunction(), configuration);
    // registerEXPathFunction(new TextEntryFunction(), configuration);
    // registerEXPathFunction(new XmlEntryFunction(), configuration);
    
    /* EXPath HttpClient: */           
    registerXPathFunction(new SendRequest(), configuration);  
    
    /* Script */
    registerXPathFunction(new Invoke(), configuration);
    registerXPathFunction(new Register(), configuration);
    registerXPathFunction(new IsRegistered(), configuration);
    registerXPathFunction(new Call(), configuration);
    
    /* JSON */
    registerXPathFunction(new ParseJSON(), configuration);
    registerXPathFunction(new SerializeJSON(), configuration);
    registerXPathFunction(new EscapeJSON(), configuration);
    registerXPathFunction(new UnescapeJSON(), configuration);
    
    /* Sql */
    registerXPathFunction(new Close(configuration), configuration);
    registerXPathFunction(new Commit(configuration), configuration);
    registerXPathFunction(new ExecuteQuery(configuration), configuration);
    registerXPathFunction(new ExecuteUpdate(configuration), configuration);
    registerXPathFunction(new GetConnection(configuration), configuration);
    registerXPathFunction(new GetNextRow(configuration), configuration);
    registerXPathFunction(new Rollback(configuration), configuration);
    registerXPathFunction(new ResultSetToNode(configuration), configuration);
    
    /* UUID */
    registerXPathFunction(new UUID(), configuration);
    
    /* Util */
    registerXPathFunction(new DiscardDocument(), configuration);
    registerXPathFunction(new Parse(), configuration);
    
    /* Zip */
    registerXPathFunction(new Zip(), configuration);
    registerXPathFunction(new Unzip(), configuration);
    
    /* Image */
    registerXPathFunction(new Scale(), configuration);
    registerXPathFunction(new nl.armatiek.xslweb.saxon.functions.image.Size(), configuration);
    
    /* IO */
    registerXPathFunction(new RegisterTempFile(), configuration);
    
    /* ExecExternal */
    registerXPathFunction(new ExecExternal(), configuration);
    
    /* Queue */
    registerXPathFunction(new AddRequest(), configuration);
    registerXPathFunction(new GetStatus(), configuration);
    registerXPathFunction(new GetInfo(), configuration);
    
  }
  
  private void registerXPathFunction(ExtensionFunctionDefinition function, Configuration configuration) {           
    configuration.registerExtensionFunction(function);
    functionClassNames.add(function.getClass().getName());
  }
  
  public boolean isFunctionRegistered(String className) {
    return functionClassNames.contains(className);
  }
  
}