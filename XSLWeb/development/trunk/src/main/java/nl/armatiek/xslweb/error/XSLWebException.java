/* 
 * (C) Copyright 2010-2011, by Armatiek BV and Contributors.
 *
 * Project Info: http://sourceforge.net/projects/infofuze/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA. 
 */
package nl.armatiek.xslweb.error;

/**
 * XSLWeb Exception class
 * 
 * @author Maarten Kroon
 */
public class XSLWebException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public XSLWebException(String message) {
    super(message);
  }
  
  public XSLWebException(Throwable cause) {
    super(cause);
  }

  public XSLWebException(String message, Throwable cause) {
    super(message, cause);
  }
}