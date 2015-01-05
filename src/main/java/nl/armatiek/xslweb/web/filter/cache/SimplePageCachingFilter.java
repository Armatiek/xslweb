/**
 *  Copyright 2003-2009 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package nl.armatiek.xslweb.web.filter.cache;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.web.filter.SimplePageFragmentCachingFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple page {@link CachingFilter} suitable for most uses.
 * <p/>
 * It uses a Singleton CacheManager created with the default factory method.
 * Override to use a different CacheManager
 * <p/>
 * The meaning of <i>page</i> is:
 * <ul>
 * <li>A complete response i.e. not a fragment.
 * <li>A content type suitable for gzipping. e.g. text or text/html
 * </ul>
 * For jsp:included page fragments see {@link SimplePageFragmentCachingFilter}.
 * <h3>calculateKey</h3>
 * Pages are cached based on their key. The key for this cache is the URI
 * followed by the query string. An example is
 * <code>/admin/SomePage.jsp?id=1234&name=Beagle</code>.
 * <p/>
 * This key technique is suitable for a wide range of uses. It is independent of
 * hostname and port number, so will work well in situations where there are
 * multiple domains which get the same content, or where users access based on
 * different port numbers.
 * <p/>
 * A problem can occur with tracking software such as Google AdWords, where
 * unique ids are inserted into request query strings. Because each request
 * generates a unique key, there will never be a cache hit. For these situations
 * it is better to override
 * {@link #calculateKey(javax.servlet.http.HttpServletRequest)} with an
 * implementation that takes account of only the significant parameters.
 * 
 * <h3>Configuring the cacheName</h3> A cache entry in ehcache.xml should be
 * configured with the name of the filter.
 * <p/>
 * Names can be set using the init-param <code>cacheName</code>, or by
 * sub-classing this class and overriding the name.
 * <h3>Concurent Cache Misses</h3>
 * A cache miss will cause the filter chain, upstream of the caching filter to
 * be processed. To avoid threads requesting the same key to do useless
 * duplicate work, these threads block behind the first thread.
 * <p/>
 * The thead timeout can be set to fail after a certain wait by setting the
 * init-param <code>blockingTimeoutMillis</code>. By default threads wait
 * indefinitely. In the event upstream processing never returns, eventually the
 * web server may get overwhelmed with connections it has not responded to. By
 * setting a timeout, the waiting threads will only block for the set time, and
 * then throw a {@link net.sf.ehcache.constructs.blocking.LockTimeoutException}.
 * Under either scenario an upstream failure will still cause a failure.
 * <h3>Gzipping</h3> Significant network efficiencies can be gained by gzipping
 * responses.
 * <p/>
 * Whether a response can be gzipped depends on:
 * <ul>
 * <li>Whether the user agent can accept GZIP encoding. This feature is part of
 * HTTP1.1. If a browser accepts GZIP encoding it will advertise this by
 * including in its HTTP header: All common browsers except IE 5.2 on Macintosh
 * are capable of accepting gzip encoding. Most search engine robots do not
 * accept gzip encoding.
 * <li>Whether the user agent has advertised its acceptance of gzip encoding.
 * This is on a per request basis. If they will accept a gzip response to their
 * request they must include the following in the HTTP request header: <code>
 * Accept-Encoding: gzip
 * </code>
 * </ul>
 * Responses are automatically gzipped and stored that way in the cache. For
 * requests which do not accept gzip encoding the page is retrieved from the
 * cache, ungzipped and returned to the user agent. The ungzipping is high
 * performance.
 * <h3>Caching Headers</h3>
 * This filter does not set browser caching headers such as ETag, Last-Modified,
 * Expires, and If-None-Match. If you wish to minimise browser requests, use
 * SimpleCachingHeadersPageCachingFilter.
 * <p/>
 * <h3>Init-Params</h3>
 * The following init-params are supported:
 * <ol>
 * <li>cacheName - the name in ehcache.xml used by the filter.
 * <li>blockingTimeoutMillis - the time, in milliseconds, to wait for the filter
 * chain to return with a response on a cache miss. This is useful to fail fast
 * in the event of an infrastructure failure.
 * </ol>
 * <h3>Reentrance</h3>
 * Care should be taken not to define a filter chain such that the same
 * {@link CachingFilter} class is reentered. The {@link CachingFilter} uses the
 * {@link net.sf.ehcache.constructs.blocking.BlockingCache}. It blocks until the
 * thread which did a get which results in a null does a put. If reentry happens
 * a second get happens before the first put. The second get could wait
 * indefinitely. This situation is monitored and if it happens, an
 * IllegalStateException will be thrown.
 * 
 * @author Greg Luck
 * @see SimpleCachingHeadersPageCachingFilter
 */
public class SimplePageCachingFilter extends CachingFilter {

  /**
   * The name of the filter. This should match a cache name in ehcache.xml
   */
  public static final String DEFAULT_CACHE_NAME = "SimplePageCachingFilter";

  private static final Logger logger = LoggerFactory.getLogger(SimpleCachingHeadersPageCachingFilter.class);

  /**
   * A meaningful name representative of the page being cached.
   * <p/>
   * The name must match the name of a configured cache in ehcache.xml
   * 
   * @return the name of the cache to use for this filter.
   */
  protected String getCacheName() {
    if (cacheName != null && cacheName.length() > 0) {
      logger.debug("Using configured cacheName of {}.", cacheName);
      return cacheName;
    } else {
      logger.debug("No cacheName configured. Using default of {}.", DEFAULT_CACHE_NAME);
      return DEFAULT_CACHE_NAME;
    }
  }

  /**
   * Gets the CacheManager for this CachingFilter. It is therefore up to
   * subclasses what CacheManager to use.
   * <p/>
   * This method was introduced in ehcache 1.2.1. Older versions used a
   * singleton CacheManager instance created with the default factory method.
   * 
   * @return the CacheManager to be used
   * @since 1.2.1
   */
  protected CacheManager getCacheManager() {
    return CacheManager.getInstance();
  }

  /**
   * Pages are cached based on their key. The key for this cache is the URI
   * followed by the query string. An example is
   * <code>/admin/SomePage.jsp?id=1234&name=Beagle</code>.
   * <p/>
   * This key technique is suitable for a wide range of uses. It is independent
   * of hostname and port number, so will work well in situations where there
   * are multiple domains which get the same content, or where users access
   * based on different port numbers.
   * <p/>
   * A problem can occur with tracking software, where unique ids are inserted
   * into request query strings. Because each request generates a unique key,
   * there will never be a cache hit. For these situations it is better to parse
   * the request parameters and override
   * {@link #calculateKey(javax.servlet.http.HttpServletRequest)} with an
   * implementation that takes account of only the significant ones.
   * <p/>
   * The key should be unique.
   * 
   * Implementers should differentiate between GET and HEAD requests otherwise
   * blank pages can result.
   * 
   * @param httpRequest
   * @return the key, generally the URI plus request parameters
   */
  protected String calculateKey(HttpServletRequest httpRequest) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(httpRequest.getMethod()).append(httpRequest.getRequestURI()).append(httpRequest.getQueryString());
    String key = stringBuffer.toString();
    return key;
  }

}
