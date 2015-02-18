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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * XPath extension function class
 * 
 * @author Maarten Kroon
 */
public class ExecuteQuery extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SQL, "execute-query");

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
    return new SequenceType[] {
        SequenceType.makeSequenceType(new JavaExternalObjectType(Connection.class), StaticProperty.ALLOWS_ONE),
        SequenceType.SINGLE_STRING 
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(new JavaExternalObjectType(ResultSet.class), StaticProperty.ALLOWS_ONE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ExecuteQueryCall();
  }
  
  private static class ExecuteQueryCall extends ExtensionFunctionCall {

    @SuppressWarnings("unchecked")
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      String sql = null;
      try {
        Connection con = ((ObjectValue<Connection>) arguments[0].head()).getObject();
        sql = ((StringValue) arguments[1].head()).getStringValue();        
        Statement stmt = null;        
        ResultSet rset = null;
        try {
          stmt = con.createStatement();          
          rset = stmt.executeQuery(sql);
        } catch (SQLException se) {          
          if (stmt != null) {
            stmt.close();            
          }
          throw se;
        }
        addCloseable(new CloseableAutoCloseableWrapper(stmt), context);
        addCloseable(new CloseableAutoCloseableWrapper(rset), context);
        return new ObjectValue<ResultSet>(rset);        
      } catch (Exception e) {
        throw new XPathException("Error executing query \"" + sql + "\"", e);
      }
    }
  }
}