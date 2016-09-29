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
import java.util.ArrayList;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.OneOrMore;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.ExtensionFunctionCall;

/**
 * XPath extension function class
 * 
 * @author Maarten Kroon
 */
public class GetNextRow extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = 
      new StructuredQName("", Definitions.NAMESPACEURI_XSLWEB_FX_SQL, "get-next-row");

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
    return SequenceType.ATOMIC_SEQUENCE;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new GetRowCall();
  }
  
  private static class GetRowCall extends ExtensionFunctionCall {
    
    @SuppressWarnings("unchecked")
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {            
      try {        
        ResultSet rset = ((ObjectValue<ResultSet>) arguments[0].head()).getObject();
        if (!rset.next()) {
          return EmptySequence.getInstance();
        }        
        ArrayList<AtomicValue> values = new ArrayList<AtomicValue>();
        ResultSetMetaData metaData = rset.getMetaData();           
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {                    
          AtomicValue value = convertJavaObjectToAtomicValue(rset.getObject(i));
          values.add(value);
        }                
        return new OneOrMore<AtomicValue>(values.toArray(new AtomicValue[values.size()]));        
      } catch (Exception e) {
        throw new XPathException("Could not get row", e);
      }
    }
  }
}