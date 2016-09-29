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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import net.sf.saxon.event.SequenceOutputter;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * XPath extension function class
 * 
 * @author Maarten Kroon
 */
public class ResultSetToNode extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SQL, "resultset-to-node");

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
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] {         
        SequenceType.makeSequenceType(new JavaExternalObjectType(ResultSet.class), StaticProperty.ALLOWS_ONE) 
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new SerializeResultSetCall();
  }
  
  private static class SerializeResultSetCall extends ExtensionFunctionCall {
    
    @SuppressWarnings({ "unchecked" })
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {        
        ResultSet rset = ((ObjectValue<ResultSet>) arguments[0].head()).getObject();
        
        ResultSetMetaData metaData = rset.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        NodeName resultSetName = new NoNamespaceName("resultset");
        NodeName rowName = new NoNamespaceName("row");
        NodeName colName = new NoNamespaceName("col");
        NodeName nameName = new NoNamespaceName("name");
        
        SequenceOutputter out = context.getController().allocateSequenceOutputter(50);
        out.startElement(resultSetName, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0); // 9.7: null for third argument?
        while (rset.next()) {          
          out.startElement(rowName, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
          for (int col = 1; col <= columnCount; col++) {            
            out.startElement(colName, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
            out.attribute(nameName, BuiltInAtomicType.UNTYPED_ATOMIC, metaData.getColumnName(col), null, 0);            
            String value = rset.getString(col);
            if (value != null) {            
              out.characters(value, null, 0);
            }
            out.endElement();                                               
          }
          out.endElement();          
        }
        out.endElement();
        
        return out.getSequence();
      } catch (Exception e) {
        throw new XPathException("Error converting resultset to node", e);
      }
    }
  }
}