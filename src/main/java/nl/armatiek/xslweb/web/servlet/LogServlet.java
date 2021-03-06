package nl.armatiek.xslweb.web.servlet;

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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.armatiek.xslweb.configuration.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogServlet extends HttpServlet {
  
  private static final Logger logger = LoggerFactory.getLogger(LogServlet.class);
  
  private static final long serialVersionUID = 1L;
  
  private File logDir;
    
  @Override
  public void init(ServletConfig config) throws ServletException {    
    super.init(config);    
    logDir = new File(Context.getInstance().getHomeDir(), "logs");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      resp.setContentType("text/plain");
      if (req.getParameter("refresh") != null) {
        resp.setHeader("Refresh", req.getParameter("refresh"));
      }
          
      String lines = req.getParameter("lines");
      int numLines = ((lines != null) && (lines.length() > 0)) ? Integer.parseInt(lines) : 25;
      
      String logName = req.getParameter("name");            
      
      File file = new File(logDir, logName);
      if (!file.isFile()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      
      RandomAccessFile logFile = new RandomAccessFile(file, "r");      
      try {
        long len = logFile.length(); // Set position at the end of the file
        long pos = len-1;
        
        while ((pos >= 0) && (numLines > 0)) { // Skip back numLines
          logFile.seek(pos--);
          if (logFile.read() == 10) numLines--;
        }
        logFile.seek(pos+1);
    
        while (true) {
          if (len > logFile.length()) {
             logFile.seek(0); // File was truncated!
          }
          len = logFile.length();
          String s = logFile.readLine(); // Try reading one line from file
          if (s == null) { // We're at EOF
            resp.getWriter().println("EOF");
            break;
          } else resp.getWriter().println(s); // We successfully read one line
        }
      } finally { 
        logFile.close();
      }
      resp.flushBuffer();
    } catch (Exception e) {
      logger.error("Error processing request", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error executing command");      
    }
  }
  
}