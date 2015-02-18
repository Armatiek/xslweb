package nl.armatiek.xslweb.saxon.functions.sql;

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

import java.sql.Connection;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.configuration.WebApp;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

import org.apache.commons.lang3.StringUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * XPath extension function class 
 * 
 * @author Maarten Kroon
 */
public class GetConnection extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SQL, "get-connection");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 5;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_STRING,
        SequenceType.SINGLE_BOOLEAN,
        SequenceType.SINGLE_BOOLEAN
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(new JavaExternalObjectType(Connection.class), StaticProperty.ALLOWS_ONE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new GetConnectionCall();
  }
  
  private static class GetConnectionCall extends ExtensionFunctionCall {

    @Override
    public ObjectValue<Connection> call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {
        String name = ((StringValue) arguments[0].head()).getStringValue();        
        String userName = null;
        String password = null;
        Boolean readOnly = null;
        Boolean autoCommit = null; 
        if (arguments.length > 1) {
          userName = ((StringValue) arguments[1].head()).getStringValue(); 
        }
        if (arguments.length > 2) {
          password = ((StringValue) arguments[2].head()).getStringValue();
        }
        if (arguments.length > 3) {
          readOnly = ((BooleanValue) arguments[3].head()).getBooleanValue();
        }
        if (arguments.length > 4) {
          autoCommit = ((BooleanValue) arguments[4].head()).getBooleanValue();
        }
        WebApp webapp = getWebApp(context);
        ComboPooledDataSource dataSource = webapp.getDataSource(name);        
        Connection connection;
        if (StringUtils.isNotBlank(userName)) {
          connection = dataSource.getConnection(userName, password);
        } else {
          connection = dataSource.getConnection();
        }        
        if (readOnly != null) {
          connection.setReadOnly(readOnly.booleanValue());
        }
        if (autoCommit != null) {
          connection.setAutoCommit(autoCommit.booleanValue());
        }          
        addCloseable(new CloseableAutoCloseableWrapper(connection), context);                        
        return new ObjectValue<Connection>(connection);        
      } catch (Exception e) {
        throw new XPathException("Could not create connection", e);
      }
    }
  }
}