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

/* Customized version of org.omnifaces.servlet.FileServlet */
package nl.armatiek.xslweb.web.servlet;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static Logger logger = LoggerFactory.getLogger(FileServlet.class);

  private static final Long DEFAULT_EXPIRE_TIME_IN_SECONDS = TimeUnit.DAYS.toSeconds(30);
  private static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);
  private static final String ETAG = "W/\"%s-%s\"";
  private static final Pattern RANGE_PATTERN = Pattern.compile("^bytes=[0-9]*-[0-9]*(,[0-9]*-[0-9]*)*$");
  private static final String MULTIPART_BOUNDARY = UUID.randomUUID().toString();
  private static final String CONTENT_DISPOSITION_HEADER = "%s;filename=\"%2$s\"; filename*=UTF-8''%2$s";
  private static final int DEFAULT_STREAM_BUFFER_SIZE = 10240;
  private static final URLCodec codec = new URLCodec();

  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doRequest(request, response, true);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doRequest(request, response, false);
  }

  private void doRequest(HttpServletRequest request, HttpServletResponse response, boolean head) throws IOException {
    response.reset();
    Resource resource;

    try {
      resource = new Resource(getFile(request));
    } catch (IllegalArgumentException e) {
      logger.warn("Got an IllegalArgumentException from user code; interpreting it as 400 Bad Request.", e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (resource.file == null) {
      handleFileNotFound(request, response);
      return;
    }

    if (preconditionFailed(request, resource)) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      return;
    }

    setCacheHeaders(response, resource, getExpireTime(request, resource.file));

    if (notModified(request, resource)) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }

    List<Range> ranges = getRanges(request, resource);

    if (ranges == null) {
      response.setHeader("Content-Range", "bytes */" + resource.length);
      response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
      return;
    }

    if (!ranges.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    } else {
      ranges.add(new Range(0, resource.length - 1)); // Full content.
    }

    String contentType = setContentHeaders(request, response, resource, ranges);

    if (head) {
      return;
    }

    writeContent(response, resource, ranges, contentType);
  }

  /**
   * Returns the file associated with the given HTTP servlet request. If this
   * method throws {@link IllegalArgumentException}, then the servlet will
   * return a HTTP 400 error. If this method returns <code>null</code>, or if
   * {@link File#isFile()} returns <code>false</code>, then the servlet will
   * invoke
   * {@link #handleFileNotFound(HttpServletRequest, HttpServletResponse)}.
   * 
   * @param request
   *          The involved HTTP servlet request.
   * @return The file associated with the given HTTP servlet request.
   * @throws IllegalArgumentException
   *           When the request is mangled in such way that it's not
   *           recognizable as a valid file request. The servlet will then
   *           return a HTTP 400 error.
   */
  protected abstract File getFile(HttpServletRequest request);

  /**
   * Handles the case when the file is not found.
   * <p>
   * The default implementation sends a HTTP 404 error.
   * 
   * @param request
   *          The involved HTTP servlet request.
   * @param response
   *          The involved HTTP servlet response.
   * @throws IOException
   *           When something fails at I/O level.
   * @since 2.3
   */
  protected void handleFileNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  /**
   * Returns how long the resource may be cached by the client before it
   * expires, in seconds.
   * <p>
   * The default implementation returns 30 days in seconds.
   * 
   * @param request
   *          The involved HTTP servlet request.
   * @param file
   *          The involved file.
   * @return The client cache expire time in seconds (not milliseconds!).
   */
  protected long getExpireTime(HttpServletRequest request, File file) {
    return DEFAULT_EXPIRE_TIME_IN_SECONDS;
  }

  /**
   * Returns the content type associated with the given HTTP servlet request and
   * file.
   * <p>
   * The default implementation delegates {@link File#getName()} to
   * {@link ServletContext#getMimeType(String)} with a fallback default value of
   * <code>application/octet-stream</code>.
   * 
   * @param request
   *          The involved HTTP servlet request.
   * @param file
   *          The involved file.
   * @return The content type associated with the given HTTP servlet request and
   *         file.
   */
  protected String getContentType(HttpServletRequest request, File file) {
    return ObjectUtils.firstNonNull(request.getServletContext().getMimeType(file.getName()), "application/octet-stream");
  }

  /**
   * Returns <code>true</code> if we must force a "Save As" dialog based on the
   * given HTTP servlet request and content type as obtained from
   * {@link #getContentType(HttpServletRequest, File)}.
   * <p>
   * The default implementation will return <code>true</code> if the content
   * type does <strong>not</strong> start with <code>text</code> or
   * <code>image</code>, and the <code>Accept</code> request header is either
   * <code>null</code> or does not match the given content type.
   * 
   * @param request
   *          The involved HTTP servlet request.
   * @param contentType
   *          The content type of the involved file.
   * @return <code>true</code> if we must force a "Save As" dialog based on the
   *         given HTTP servlet request and content type.
   */
  protected boolean isAttachment(HttpServletRequest request, String contentType) {
    String accept = request.getHeader("Accept");
    return !StringUtils.startsWithAny(contentType, "text", "image") && (accept == null || !accepts(accept, contentType));
  }

  /**
   * Returns the file name to be used in <code>Content-Disposition</code>
   * header. This does not need to be URL-encoded as this will be taken care of.
   * <p>
   * The default implementation returns {@link File#getName()}.
   * 
   * @param request
   *          The involved HTTP servlet request.
   * @param file
   *          The involved file.
   * @return The file name to be used in <code>Content-Disposition</code>
   *         header.
   * @since 2.3
   */
  protected String getAttachmentName(HttpServletRequest request, File file) {
    return file.getName();
  }

  // Sub-actions
  // ----------------------------------------------------------------------------------------------------

  /**
   * Returns true if it's a conditional request which must return 412.
   */
  private boolean preconditionFailed(HttpServletRequest request, Resource resource) {
    String match = request.getHeader("If-Match");
    long unmodified = request.getDateHeader("If-Unmodified-Since");
    return (match != null) ? !matches(match, resource.eTag) : (unmodified != -1 && modified(unmodified, resource.lastModified));
  }
  
  private void setNoCacheHeaders(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
    response.setDateHeader("Expires", 0);
    response.setHeader("Pragma", "no-cache"); // Backwards compatibility for HTTP 1.0.
  }
  
  private void setCacheHeaders(HttpServletResponse response, long expires) {
    if (expires > 0) {
      response.setHeader("Cache-Control", "public,max-age=" + expires + ",must-revalidate");
      response.setDateHeader("Expires", System.currentTimeMillis() + SECONDS.toMillis(expires));
      response.setHeader("Pragma", ""); // Explicitly set pragma to prevent container from overriding it.
    } else {
      setNoCacheHeaders(response);
    }
  }

  /**
   * Set cache headers.
   */
  private void setCacheHeaders(HttpServletResponse response, Resource resource, long expires) {
    setCacheHeaders(response, expires);
    response.setHeader("ETag", resource.eTag);
    response.setDateHeader("Last-Modified", resource.lastModified);
  }

  /**
   * Returns true if it's a conditional request which must return 304.
   */
  private boolean notModified(HttpServletRequest request, Resource resource) {
    String noMatch = request.getHeader("If-None-Match");
    long modified = request.getDateHeader("If-Modified-Since");
    return (noMatch != null) ? matches(noMatch, resource.eTag) : (modified != -1 && !modified(modified, resource.lastModified));
  }

  /**
   * Get requested ranges. If this is null, then we must return 416. If this is
   * empty, then we must return full file.
   */
  private List<Range> getRanges(HttpServletRequest request, Resource resource) {
    List<Range> ranges = new ArrayList<>(1);
    String rangeHeader = request.getHeader("Range");

    if (rangeHeader == null) {
      return ranges;
    } else if (!RANGE_PATTERN.matcher(rangeHeader).matches()) {
      return null; // Syntax error.
    }

    String ifRange = request.getHeader("If-Range");

    if (ifRange != null && !ifRange.equals(resource.eTag)) {
      try {
        long ifRangeTime = request.getDateHeader("If-Range");

        if (ifRangeTime != -1 && modified(ifRangeTime, resource.lastModified)) {
          return ranges;
        }
      } catch (IllegalArgumentException ifRangeHeaderIsInvalid) {
        logger.warn("If-Range header is invalid. Just return full file then.", ifRangeHeaderIsInvalid);
        return ranges;
      }
    }

    for (String rangeHeaderPart : rangeHeader.split("=")[1].split(",")) {
      Range range = parseRange(rangeHeaderPart, resource.length);

      if (range == null) {
        return null; // Logic error.
      }

      ranges.add(range);
    }

    return ranges;
  }

  /**
   * Parse range header part. Returns null if there's a logic error (i.e. start
   * after end).
   */
  private Range parseRange(String range, long length) {
    long start = sublong(range, 0, range.indexOf('-'));
    long end = sublong(range, range.indexOf('-') + 1, range.length());

    if (start == -1) {
      start = length - end;
      end = length - 1;
    } else if (end == -1 || end > length - 1) {
      end = length - 1;
    }

    if (start > end) {
      return null; // Logic error.
    }

    return new Range(start, end);
  }
  
  public String formatContentDispositionHeader(String filename, boolean attachment) {
    try {
      return format(CONTENT_DISPOSITION_HEADER, (attachment ? "attachment" : "inline"), codec.encode(filename, "UTF-8"));
    } catch (Exception e) {
      throw new UnsupportedOperationException(e);
    }
  }

  /**
   * Set content headers.
   */
  private String setContentHeaders(HttpServletRequest request, HttpServletResponse response, Resource resource, List<Range> ranges) {
    String contentType = getContentType(request, resource.file);
    String filename = getAttachmentName(request, resource.file);
    boolean attachment = isAttachment(request, contentType);
    response.setHeader("Content-Disposition", formatContentDispositionHeader(filename, attachment));
    response.setHeader("Accept-Ranges", "bytes");

    if (ranges.size() == 1) {
      Range range = ranges.get(0);
      response.setContentType(contentType);
      response.setHeader("Content-Length", String.valueOf(range.length));

      if (response.getStatus() == HttpServletResponse.SC_PARTIAL_CONTENT) {
        response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + resource.length);
      }
    } else {
      response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
    }

    return contentType;
  }
  
  /**
   * Stream the given input to the given output via NIO {@link Channels} and a
   * directly allocated NIO {@link ByteBuffer}. Both the input and output
   * streams will implicitly be closed after streaming, regardless of whether an
   * exception is been thrown or not.
   * 
   * @param input
   *          The input stream.
   * @param output
   *          The output stream.
   * @return The length of the written bytes.
   * @throws IOException
   *           When an I/O error occurs.
   */
  public static long stream(InputStream input, OutputStream output) throws IOException {
    try (ReadableByteChannel inputChannel = Channels.newChannel(input); WritableByteChannel outputChannel = Channels.newChannel(output)) {
      ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_STREAM_BUFFER_SIZE);
      long size = 0;

      while (inputChannel.read(buffer) != -1) {
        buffer.flip();
        size += outputChannel.write(buffer);
        buffer.clear();
      }

      return size;
    }
  }
  
  /**
   * Stream a specified range of the given file to the given output via NIO
   * {@link Channels} and a directly allocated NIO {@link ByteBuffer}. The
   * output stream will only implicitly be closed after streaming when the
   * specified range represents the whole file, regardless of whether an
   * exception is been thrown or not.
   * 
   * @param file
   *          The file.
   * @param output
   *          The output stream.
   * @param start
   *          The start position (offset).
   * @param length
   *          The (intented) length of written bytes.
   * @return The (actual) length of the written bytes. This may be smaller when
   *         the given length is too large.
   * @throws IOException
   *           When an I/O error occurs.
   * @since 2.2
   */
  public static long stream(File file, OutputStream output, long start, long length) throws IOException {
    if (start == 0 && length >= file.length()) {
      return stream(new FileInputStream(file), output);
    }

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), StandardOpenOption.READ)) {
      WritableByteChannel outputChannel = Channels.newChannel(output);
      ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_STREAM_BUFFER_SIZE);
      long size = 0;

      while (fileChannel.read(buffer, start + size) != -1) {
        buffer.flip();

        if (size + buffer.limit() > length) {
          buffer.limit((int) (length - size));
        }

        size += outputChannel.write(buffer);

        if (size >= length) {
          break;
        }

        buffer.clear();
      }

      return size;
    }
  }

  /**
   * Write given file to response with given content type and ranges.
   */
  private void writeContent(HttpServletResponse response, Resource resource, List<Range> ranges, String contentType) throws IOException {
    ServletOutputStream output = response.getOutputStream();

    if (ranges.size() == 1) {
      Range range = ranges.get(0);
      stream(resource.file, output, range.start, range.length);
    } else {
      for (Range range : ranges) {
        output.println();
        output.println("--" + MULTIPART_BOUNDARY);
        output.println("Content-Type: " + contentType);
        output.println("Content-Range: bytes " + range.start + "-" + range.end + "/" + resource.length);
        stream(resource.file, output, range.start, range.length);
      }

      output.println();
      output.println("--" + MULTIPART_BOUNDARY + "--");
    }
  }

  // Helpers
  // --------------------------------------------------------------------------------------------------------

  /**
   * Returns true if the given match header matches the given ETag value.
   */
  private static boolean matches(String matchHeader, String eTag) {
    String[] matchValues = matchHeader.split("\\s*,\\s*");
    Arrays.sort(matchValues);
    return Arrays.binarySearch(matchValues, eTag) > -1 || Arrays.binarySearch(matchValues, "*") > -1;
  }

  /**
   * Returns true if the given modified header is older than the given last
   * modified value.
   */
  private static boolean modified(long modifiedHeader, long lastModified) {
    return (modifiedHeader + ONE_SECOND_IN_MILLIS <= lastModified); 
  }

  /**
   * Returns a substring of the given string value from the given begin index to
   * the given end index as a long. If the substring is empty, then -1 will be
   * returned.
   */
  private static long sublong(String value, int beginIndex, int endIndex) {
    String substring = value.substring(beginIndex, endIndex);
    return substring.isEmpty() ? -1 : Long.parseLong(substring);
  }

  /**
   * Returns true if the given accept header accepts the given value.
   */
  private static boolean accepts(String acceptHeader, String toAccept) {
    String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
    Arrays.sort(acceptValues);
    return Arrays.binarySearch(acceptValues, toAccept) > -1 || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1 || Arrays.binarySearch(acceptValues, "*/*") > -1;
  }

  // Nested classes
  // -------------------------------------------------------------------------------------------------

  /**
   * Convenience class for a file resource.
   */
  private static class Resource {
    private final File file;
    private final long length;
    private final long lastModified;
    private String eTag;

    public Resource(File file) {
      if (file != null && file.isFile()) {
        this.file = file;
        length = file.length();
        lastModified = file.lastModified();
        try {
          eTag = format(ETAG, codec.encode(file.getName(), "UTF-8"), lastModified);
        } catch (Exception e) {
          throw new UnsupportedOperationException(e);
        }
      } else {
        this.file = null;
        length = 0;
        lastModified = 0;
        eTag = null;
      }
    }

  }

  /**
   * Convenience class for a byte range.
   */
  private static class Range {
    private final long start;
    private final long end;
    private final long length;

    public Range(long start, long end) {
      this.start = start;
      this.end = end;
      length = end - start + 1;
    }

  }

}
