/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.webdav.methods;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.IMethodExecutor;
import net.sf.webdav.ITransaction;
import net.sf.webdav.WebdavStatus;

public class DoNotImplemented implements IMethodExecutor {

  private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DoNotImplemented.class);
  private boolean _readOnly;

  public DoNotImplemented(boolean readOnly) {
    _readOnly = readOnly;
  }

  public void execute(ITransaction transaction, HttpServletRequest req, HttpServletResponse resp) throws IOException {
    LOG.trace("-- " + req.getMethod());

    if (_readOnly) {
      resp.sendError(WebdavStatus.SC_FORBIDDEN);
    } else
      resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
  }
}
