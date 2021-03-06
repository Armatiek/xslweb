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

package net.sf.webdav;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.fromcatalina.MD5Encoder;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.methods.DoCopy;
import net.sf.webdav.methods.DoDelete;
import net.sf.webdav.methods.DoGet;
import net.sf.webdav.methods.DoHead;
import net.sf.webdav.methods.DoLock;
import net.sf.webdav.methods.DoMkcol;
import net.sf.webdav.methods.DoMove;
import net.sf.webdav.methods.DoNotImplemented;
import net.sf.webdav.methods.DoOptions;
import net.sf.webdav.methods.DoPropfind;
import net.sf.webdav.methods.DoProppatch;
import net.sf.webdav.methods.DoPut;
import net.sf.webdav.methods.DoUnlock;
import nl.armatiek.xslweb.configuration.Definitions;

public class WebDavServletBean extends HttpServlet {

  private static final long serialVersionUID = 9035655075637415074L;

  private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebDavServletBean.class);

  private static MimeType unknownMimeType = new MimeType(Definitions.MIMETYPE_BINARY);

  /**
   * MD5 message digest provider.
   */
  protected static MessageDigest MD5_HELPER;

  /**
   * The MD5 helper object for this class.
   */
  protected static final MD5Encoder MD5_ENCODER = new MD5Encoder();

  private static final boolean READ_ONLY = false;
  protected ResourceLocks _resLocks;
  protected IWebdavStore _store;
  private HashMap<String, IMethodExecutor> _methodMap = new HashMap<String, IMethodExecutor>();

  public WebDavServletBean() {
    _resLocks = new ResourceLocks();

    try {
      MD5_HELPER = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException();
    }
  }

  public void init(IWebdavStore store, String dftIndexFile, String insteadOf404, int nocontentLenghHeaders, boolean lazyFolderCreationOnPut) throws ServletException {

    _store = store;

    IMimeTyper mimeTyper = new IMimeTyper() {
      public String getMimeType(ITransaction transaction, String path) {
        String retVal = _store.getStoredObject(transaction, path).getMimeType();
        if (retVal == null) {
          // retVal = getServletContext().getMimeType(path);
          retVal = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(path, unknownMimeType)).toString();
        }
        return retVal;
      }
    };

    register("GET", new DoGet(store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, nocontentLenghHeaders));
    register("HEAD", new DoHead(store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, nocontentLenghHeaders));
    DoDelete doDelete = (DoDelete) register("DELETE", new DoDelete(store, _resLocks, READ_ONLY));
    DoCopy doCopy = (DoCopy) register("COPY", new DoCopy(store, _resLocks, doDelete, READ_ONLY));
    register("LOCK", new DoLock(store, _resLocks, READ_ONLY));
    register("UNLOCK", new DoUnlock(store, _resLocks, READ_ONLY));
    register("MOVE", new DoMove(_resLocks, doDelete, doCopy, READ_ONLY));
    register("MKCOL", new DoMkcol(store, _resLocks, READ_ONLY));
    register("OPTIONS", new DoOptions(store, _resLocks));
    register("PUT", new DoPut(store, _resLocks, READ_ONLY, lazyFolderCreationOnPut));
    register("PROPFIND", new DoPropfind(store, _resLocks, mimeTyper));
    register("PROPPATCH", new DoProppatch(store, _resLocks, READ_ONLY));
    register("*NO*IMPL*", new DoNotImplemented(READ_ONLY));
  }

  @Override
  public void destroy() {
    if (_store != null)
      _store.destroy();
    super.destroy();
  }

  protected IMethodExecutor register(String methodName, IMethodExecutor method) {
    _methodMap.put(methodName, method);
    return method;
  }

  /**
   * Handles the special WebDAV methods.
   */
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String methodName = req.getMethod();
    ITransaction transaction = null;
    boolean needRollback = false;

    if (LOG.isTraceEnabled())
      debugRequest(methodName, req);

    try {
      Principal userPrincipal = req.getUserPrincipal();
      transaction = _store.begin(userPrincipal);
      needRollback = true;
      _store.checkAuthentication(transaction);
      resp.setStatus(WebdavStatus.SC_OK);

      try {
        IMethodExecutor methodExecutor = (IMethodExecutor) _methodMap.get(methodName);
        if (methodExecutor == null) {
          methodExecutor = (IMethodExecutor) _methodMap.get("*NO*IMPL*");
        }

        methodExecutor.execute(transaction, req, resp);

        _store.commit(transaction);
        /**
         * Clear not consumed data
         *
         * Clear input stream if available otherwise later access include
         * current input. These cases occure if the client sends a request with
         * body to an not existing resource.
         */
        if (req.getContentLength() != 0 && req.getInputStream().available() > 0) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Clear not consumed data!");
          }
          while (req.getInputStream().available() > 0) {
            req.getInputStream().read();
          }
        }
        needRollback = false;
      } catch (IOException e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        LOG.error("IOException: " + sw.toString());
        resp.sendError(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
        _store.rollback(transaction);
        throw new ServletException(e);
      }

    } catch (UnauthenticatedException e) {
      resp.sendError(WebdavStatus.SC_FORBIDDEN);
    } catch (WebdavException e) {
      java.io.StringWriter sw = new java.io.StringWriter();
      java.io.PrintWriter pw = new java.io.PrintWriter(sw);
      e.printStackTrace(pw);
      LOG.error("WebdavException: " + sw.toString());
      throw new ServletException(e);
    } catch (Exception e) {
      java.io.StringWriter sw = new java.io.StringWriter();
      java.io.PrintWriter pw = new java.io.PrintWriter(sw);
      e.printStackTrace(pw);
      LOG.error("Exception: " + sw.toString());
    } finally {
      if (needRollback)
        _store.rollback(transaction);
    }

  }

  private void debugRequest(String methodName, HttpServletRequest req) {
    LOG.trace("-----------");
    LOG.trace("WebdavServlet\n request: methodName = " + methodName);
    LOG.trace("time: " + System.currentTimeMillis());
    LOG.trace("path: " + req.getRequestURI());
    LOG.trace("-----------");
    Enumeration<?> e = req.getHeaderNames();
    while (e.hasMoreElements()) {
      String s = (String) e.nextElement();
      LOG.trace("header: " + s + " " + req.getHeader(s));
    }
    e = req.getAttributeNames();
    while (e.hasMoreElements()) {
      String s = (String) e.nextElement();
      LOG.trace("attribute: " + s + " " + req.getAttribute(s));
    }
    e = req.getParameterNames();
    while (e.hasMoreElements()) {
      String s = (String) e.nextElement();
      LOG.trace("parameter: " + s + " " + req.getParameter(s));
    }
  }

}
