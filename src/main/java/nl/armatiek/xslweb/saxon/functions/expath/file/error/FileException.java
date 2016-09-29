package nl.armatiek.xslweb.saxon.functions.expath.file.error;

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

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;

public class FileException extends XPathException {
  
  private static final long serialVersionUID = 1L;
  
  public static final String ERROR_PATH_NOT_EXIST = "not-found";
  public static final String ERROR_PATH_EXISTS = "exists";
  public static final String ERROR_PATH_NOT_DIRECTORY = "no-dir";
  public static final String ERROR_PATH_IS_DIRECTORY = "is-dir";
  public static final String ERROR_UNKNOWN_ENCODING = "unknown-encoding";
  public static final String ERROR_INDEX_OUT_OF_BOUNDS = "out-of-range";
  public static final String ERROR_IO = "io-error";

  public FileException(String message, String code) {
    super(message);
    setErrorCodeQName(new StructuredQName("file", "http://expath.org/ns/file", code));
  }
  
  public FileException(String message, Exception cause, String code) {
    super(message, cause);
    setErrorCodeQName(new StructuredQName("file", "http://expath.org/ns/file", code));
  }

}
