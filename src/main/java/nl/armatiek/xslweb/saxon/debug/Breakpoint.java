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

public class Breakpoint {
  
  private String path;
  private int line;
  private int column;
  private String condition;
  private boolean active;
  
  public Breakpoint(String path, int line, int column, String condition, boolean active) {
    this.path = path;
    this.line = line;
    this.column = column;
    this.condition = condition;
    this.active = active;
  }
  
  public String getPath() {
    return path;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public String getCondition() {
    return condition;
  }

  public boolean isActive() {
    return active;
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }
  
  public void setCondition(String condition) {
    this.condition = condition;
  }
  
  /*
  public String toJSON() {
    StringBuilder json = new StringBuilder();
    json.append(
        String.format(
            "{\"path\":\"%s\",\"line\":%d,\"column\":%d,\"active\":%b,\"condition\":\"%s\"", 
            StringEscapeUtils.escapeJson(path), line, column, active, StringEscapeUtils.escapeJson((condition != null) ? condition : "")));
    return json.toString();
  }
  */

}