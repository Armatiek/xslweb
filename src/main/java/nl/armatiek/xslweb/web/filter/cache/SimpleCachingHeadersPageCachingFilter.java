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

/*
 * Based on a contribution from Craig Andrews which has been released also under the Apache 2 license at
 * http://candrews.integralblue.com/2009/02/http-caching-header-aware-servlet-filter/. Copyright notice follows.
 *
 * Copyright 2009 Craig Andrews
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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.Header;
import net.sf.ehcache.constructs.web.HttpDateFormatter;
import net.sf.ehcache.constructs.web.PageInfo;
import net.sf.ehcache.constructs.web.ResponseHeadersNotModifiableException;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.pipeline.PipelineHandler;

/**
 * This Filter extends {@link SimplePageCachingFilter}, adding support for the
 * HTTP cache headers: ETag, Last-Modified and Expires.
 * <p>
 * Because browsers and other HTTP clients have the expiry information returned
 * in the response headers, they do not even need to request the page again.
 * Even once the local browser copy has expired, the browser will do a
 * conditional GET.
 * <p>
 * So why would you ever want to use SimplePageCachingFilter, which does not set
 * these headers? Because in some caching scenarios you may wish to remove a
 * page before its natural expiry. Consider a scenario where a web page shows
 * dynamic data. Under Ehcache the Element can be removed at any time. However
 * if a browser is holding expiry information, those browsers will have to wait
 * until the expiry time before getting updated. The caching in this scenario is
 * more about defraying server load rather than minimising browser calls.
 * <p>
 * 
 * @author Craig Andrews
 * @author Greg Luck
 * @see SimplePageCachingFilter
 */
public class SimpleCachingHeadersPageCachingFilter extends SimplePageCachingFilter {

  /**
   * The name of the filter. This should match a cache name in ehcache.xml
   */
  public static final String NAME = "SimpleCachingHeadersPageCachingFilter";

  private static final long ONE_YEAR_IN_MILLISECONDS = 60 * 60 * 24 * 365 * 1000L;
  private static final int MILLISECONDS_PER_SECOND = 1000;

  private HttpDateFormatter httpDateFormatter;

  /**
   * Builds the PageInfo object by passing the request along the filter chain
   * <p>
   * The following headers are set:
   * <ul>
   * <li>Last-Modified
   * <li>Expires
   * <li>Cache-Control
   * <li>ETag
   * </ul>
   * Any of these headers aleady set in the response are ignored, and new ones
   * generated. To control your own caching headers, use
   * {@link SimplePageCachingFilter}.
   * 
   * 
   * @param request
   * @param response
   * @param chain
   * @return a Serializable value object for the page or page fragment
   * @throws AlreadyGzippedException
   *           if an attempt is made to double gzip the body
   * @throws Exception
   * 
   */
  @Override
  protected PageInfo buildPage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws AlreadyGzippedException, Exception {
    PageInfo pageInfo = super.buildPage(request, response, chain);

    PipelineHandler pipelineHandler = (PipelineHandler) request.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
    if (pipelineHandler.getCacheHeaders()) {
      final List<Header<? extends Serializable>> headers = pageInfo.getHeaders();
  
      long ttlMilliseconds = calculateTimeToLiveMilliseconds();
  
      // Remove any conflicting headers
      for (final Iterator<Header<? extends Serializable>> headerItr = headers.iterator(); headerItr.hasNext();) {
        final Header<? extends Serializable> header = headerItr.next();
  
        final String name = header.getName();
        if ("Last-Modified".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name) || "Cache-Control".equalsIgnoreCase(name) || "ETag".equalsIgnoreCase(name)) {
          headerItr.remove();
        }
      }
  
      // add expires and last-modified headers
  
      // trim the milliseconds off the value since the header is only accurate
      // down to the second
      long lastModified = pageInfo.getCreated().getTime();
      lastModified = TimeUnit.MILLISECONDS.toSeconds(lastModified);
      lastModified = TimeUnit.SECONDS.toMillis(lastModified);
  
      headers.add(new Header<Long>("Last-Modified", lastModified));
      headers.add(new Header<Long>("Expires", System.currentTimeMillis() + ttlMilliseconds));
      headers.add(new Header<String>("Cache-Control", "max-age=" + ttlMilliseconds / MILLISECONDS_PER_SECOND));
      headers.add(new Header<String>("ETag", generateEtag(ttlMilliseconds)));
    }

    return pageInfo;
  }

  /**
   * ETags are required to have double quotes around the value, unlike any other
   * header.
   * <p/>
   * The ehcache eTag is effectively the Expires time, but accurate to
   * milliseconds, i.e. no conversion to the nearest second is done as is done
   * for the Expires tag. It therefore is the most precise indicator of whether
   * the client cached version is the same as the server version.
   * <p/>
   * MD5 is not used to calculate ETag, as it is in some implementations,
   * because it does not add any extra value in this situation, and it has a
   * higher cost.
   * 
   * @see "http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.3"
   */
  private String generateEtag(long ttlMilliseconds) {
    StringBuffer stringBuffer = new StringBuffer();
    Long eTagRaw = System.currentTimeMillis() + ttlMilliseconds;
    String eTag = stringBuffer.append("\"").append(eTagRaw).append("\"").toString();
    return eTag;
  }

  /**
   * @return A lazily created HttpDateFormatter instance scoped to this filter
   */
  protected final HttpDateFormatter getHttpDateFormatter() {
    if (httpDateFormatter == null) {
      // Delay init since SimpleDateFormat is expensive to create
      httpDateFormatter = new HttpDateFormatter();
    }

    return this.httpDateFormatter;
  }

  /**
   * Writes the response from a PageInfo object.
   * 
   * This method actually performs the conditional GET and returns 304 if not
   * modified, short-circuiting the normal writeResponse.
   * <p/>
   * Indeed, if the short cicruit does not occur it calls the super method.
   */
  @Override
  protected void writeResponse(HttpServletRequest request, HttpServletResponse response, PageInfo pageInfo) throws IOException, DataFormatException, ResponseHeadersNotModifiableException {
    PipelineHandler pipelineHandler = (PipelineHandler) request.getAttribute(Definitions.ATTRNAME_PIPELINEHANDLER);
    if (pipelineHandler.getCacheHeaders()) {
      final List<Header<? extends Serializable>> headers = pageInfo.getHeaders();
      for (final Header<? extends Serializable> header : headers) {
        if ("ETag".equals(header.getName())) {
          String requestIfNoneMatch = request.getHeader("If-None-Match");
          if (header.getValue().equals(requestIfNoneMatch)) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            // use the same date we sent when we created the ETag the first time
            // through
            // response.setHeader("Last-Modified",
            // request.getHeader("If-Modified-Since"));
            return;
          }
          break;
        }
        if ("Last-Modified".equals(header.getName())) {
          long requestIfModifiedSince = request.getDateHeader("If-Modified-Since");
          if (requestIfModifiedSince != -1) {
            final Date requestDate = new Date(requestIfModifiedSince);
            final Date pageInfoDate;
            switch (header.getType()) {
            case STRING:
              pageInfoDate = this.getHttpDateFormatter().parseDateFromHttpDate((String) header.getValue());
              break;
            case DATE:
              pageInfoDate = new Date((Long) header.getValue());
              break;
            default:
              throw new IllegalArgumentException("Header " + header + " is not supported as type: " + header.getType());
            }
  
            if (!requestDate.before(pageInfoDate)) {
              response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
              response.setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
              return;
            }
          }
        }
      }
    }
    super.writeResponse(request, response, pageInfo);
  }

  /**
   * Get the time to live for a page, in milliseconds
   * 
   * @return time to live in milliseconds
   */
  protected long calculateTimeToLiveMilliseconds() {
    if (blockingCache.isDisabled()) {
      return -1;
    } else {
      CacheConfiguration cacheConfiguration = blockingCache.getCacheConfiguration();
      if (cacheConfiguration.isEternal()) {
        return ONE_YEAR_IN_MILLISECONDS;
      } else {
        return cacheConfiguration.getTimeToLiveSeconds() * MILLISECONDS_PER_SECOND;
      }
    }
  }
  
}
