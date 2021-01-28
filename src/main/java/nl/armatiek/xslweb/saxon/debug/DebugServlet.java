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
package nl.armatiek.xslweb.saxon.debug;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import nl.armatiek.xslweb.configuration.Context;
import nl.armatiek.xslweb.configuration.Definitions;

@WebServlet(asyncSupported = true)
public class DebugServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!Context.getInstance().getDebugEnable()) {
      super.doGet(req, resp);
      return;
    }
    String path = req.getPathInfo();
    String responseText = "No message";
    String jsonText = null;
    String errorText = null;  
    String data = "{}";
    HttpSession session = req.getSession(false);
    DebugClient client = (session != null) ? (DebugClient) session.getAttribute(Definitions.ATTRNAME_DEBUGCLIENT) : null;  
    try {
      if (path.equals("/get-file-contents")) {
        File webappsDir = new File(Context.getInstance().getHomeDir(), "webapps");
        File file = new File(webappsDir, req.getParameter("path"));
        if (path.contains("..") || !file.exists()) {
          resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
          resp.setCharacterEncoding("UTF-8");
          resp.setContentType("text/plain");
          resp.setStatus(HttpServletResponse.SC_OK);
          OutputStream os = new WriterOutputStream(resp.getWriter(), StandardCharsets.UTF_8);
          FileUtils.copyFile(file, os);
          os.flush();
          os.close();
        }
        resp.flushBuffer();
        return;
      } else if (path.equals("/get-directory-listing")) {
        StringBuilder json = new StringBuilder();
        File base = new File(Context.getInstance().getHomeDir(), "webapps");
        File dir = base;
        directory(base, dir, null, json);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json.toString());
        resp.flushBuffer();
        return;
      }
      
      if (client == null) {
        errorText = "No active session or no debug client object available";
      } if (path.equals("/activate-debug-session")) {
        client.setActive(true);
        responseText = "Debug session activated";
      } else if (path.equals("/deactivate-debug-session")) {
        client.setActive(false);
        responseText = "Debug session deactivated";
      } else if (path.equals("/close-debug-session")) {
        client.close();
        session.removeAttribute(Definitions.ATTRNAME_DEBUGCLIENT);
        responseText = "Debug session closed";
      } else if (path.equals("/set-breakpoint")) {
        client.setBreakpoint(req.getParameter("path"), Integer.parseInt(req.getParameter("line")));
        responseText = "Breakpoint set";
      } else if (path.equals("/remove-breakpoint")) {
        client.removeBreakpoint(req.getParameter("path"), Integer.parseInt(req.getParameter("line")));
        responseText = "Breakpoint removed";
      } else if (path.equals("/toggle-breakpoint")) {
        client.toggleBreakpoint(req.getParameter("path"), Integer.parseInt(req.getParameter("line")));
        responseText = "Breakpoint toggled";
      } else if (path.equals("/set-breakpoint-condition")) {
        client.setBreakpointCondition(req.getParameter("path"), Integer.parseInt(req.getParameter("line")), req.getParameter("condition"));
        responseText = "Breakpoint condition set";
      } else if (path.equals("/remove-all-breakpoints")) {
        client.removeAllBreakpoints();
        responseText = "All breakpoints removed";
      } else if (path.equals("/get-breakpoints")) {
        Integer[] breakpoints = client.getBreakpointLines(req.getParameter("path"));
        data = "[" + StringUtils.join(breakpoints, ',') + "]";
      } else if (path.equals("/get-all-breakpoints")) {
        jsonText = client.getAllBreakpoints();
      } else if (path.equals("/run")) {
        client.run();
        responseText = "Run initiated";
      } else if (path.equals("/step")) {
        client.step();
        responseText = "Step initiated";
      } else if (path.equals("/evaluate-xpath")) {
        responseText = client.evaluateXPath(req.getParameter("expression"));
      } else if (path.equals("/get-serialized-sequence")) {
        responseText = client.getSerializedSequence(req.getParameter("id"));
      } else if (path.equals("/refresh-session")) {
        responseText = "Session refreshed";
      }
    } catch (Exception e) {
      errorText = e.getMessage();
    }
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("application/json");
    resp.setStatus(200);
    String json;
    if (jsonText != null) {
      json = jsonText;
    } else {
      boolean isError = errorText != null;
      json = String.format("{\"code\":%d,\"message\":\"%s\",\"data\":%s}", (isError) ? 500 : 200, StringEscapeUtils.escapeJson((isError) ? errorText : responseText), data);
    }
    resp.getWriter().write(json);
    resp.flushBuffer();
  }
  
  private void directory(File base, File dir, String key, StringBuilder json) {
    if (key != null) {
      json.append(",\"" + key + "\": [");
    } else {
      json.append("[");
    }
    
    File[] files = dir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        boolean isWebAppDir = file.isDirectory() && file.getParentFile().equals(base);
        if (isWebAppDir && !Context.getInstance().getWebApp("/" + file.getName()).getDebugMode()) {
          return false;
        }
        return file.isDirectory() || StringUtils.equalsAny(FilenameUtils.getExtension(file.getName()).toLowerCase(), "xsl", "xslt", "css", "js", "properties", "xsd");
      }
    });
    
    Arrays.sort(files, new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        if (o1.isDirectory() && o2.isFile()) {
          return -1;
        }
        if (o1.isFile() && o2.isDirectory()) {
          return 1;
        }
        return o1.getName().compareTo(o2.getName());
      }
    });
    
    for (int i=0; i<files.length; i++) {
      File file = files[i];
      String relativePath = "/" + base.toPath().relativize(file.toPath()).toString().replace('\\', '/');
      json.append(String.format("{\"id\":\"%s\",\"text\":\"%s\",\"img\":\"%s\",\"path\":\"%s\",\"type\":\"%s\"", 
          Integer.toString(file.hashCode()), StringEscapeUtils.escapeJson(file.getName()), (file.isDirectory()) ? "icon-folder" : "icon-page", 
              StringEscapeUtils.escapeJson(relativePath), (file.isDirectory()) ? "folder" : "file"));
      
      if (file.isDirectory()) {
        directory(base, file, "nodes", json);
      }
      
      json.append("}");
     
      if (i < files.length-1) {
        json.append(",");
      }
      
    }
    json.append("]");
  }
  
}