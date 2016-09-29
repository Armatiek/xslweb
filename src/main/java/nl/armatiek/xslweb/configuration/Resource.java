package nl.armatiek.xslweb.configuration;

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

import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.w3c.dom.Element;

public class Resource {
  
  private static String defaultDuration = "P0DT4H0M0S";
  
  private Pattern pattern; 
  private String mediaType;
  private Duration duration;
  
  public Resource(Element resourceElem) throws DatatypeConfigurationException {
    this.pattern = Pattern.compile(resourceElem.getAttribute("pattern"));
    this.mediaType = resourceElem.getAttribute("media-type");
    String duration;
    if (resourceElem.hasAttribute("duration")) {
      duration = resourceElem.getAttribute("duration");
    } else {
      duration = defaultDuration;
    }
    this.duration = DatatypeFactory.newInstance().newDurationDayTime(duration);
  }
  
  public Pattern getPattern() {
    return pattern;
  }

  public String getMediaType() {
    return mediaType;
  }
  
  public Duration getDuration() {
    return duration;
  }

}