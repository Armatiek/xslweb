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
package nl.armatiek.xslweb.saxon.functions.httpclient;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class TrustAllCerts {
  
  private static TrustManager[] trustAllTrustManager = new TrustManager[] { new X509TrustManager() {
    
    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new java.security.cert.X509Certificate[]{};
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) { }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
    
  }};
  
  private static HostnameVerifier trustAllHostnameVerifier = new HostnameVerifier() {
    
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
    
  };
  
  private static SSLContext sslContext;
  static {
    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllTrustManager, new java.security.SecureRandom());
    } catch (Exception e) {
      //
    }
  }
  
  public static void setTrustAllCerts(final OkHttpClient.Builder clientBuilder) {
    clientBuilder
      .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllTrustManager[0])
      .hostnameVerifier(trustAllHostnameVerifier);
  }

}