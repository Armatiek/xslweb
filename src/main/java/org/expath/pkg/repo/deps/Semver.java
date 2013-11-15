/****************************************************************************/
/*  File:       Semver.java                                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.repo.deps;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.expath.pkg.repo.PackageException;

/**
 * Represents a SemVer template, or a SemVer version number.
 *
 * See http://semver.org/.  A template is like a version number, but it can
 * have less parts.  For instance "2.3" is a valid template, even though it
 * is not a valid version number (because it does not have any patch number).
 * This template matches any version number starting with "2.3", for instance
 * "2.3.0", "2.3.99" and "2.3.4beta5".
 * 
 * SemVer has released a new version of the spec, and the pre-release part
 * (like "pre1" or "beta5") must now be preceded by a slash (it was forbidden
 * before...)  TODO: Change this class to accept both kinds.  And adapt the
 * Packaging spec too.
 *
 * @author Florent Georges
 * @date   2010-11-15
 */
public class Semver
{
    public Semver(String semver)
            throws PackageException
    {
        myParts = parse(semver);
        myString = semver;
    }

    /**
     * Does {@code rhs} (a SemVer version) match this SemVer template?
     */
    public boolean matches(Semver rhs)
            throws PackageException
    {
        int this_len = myParts.length;
        int rhs_len = rhs.myParts.length;
        // TODO: We should probably relax the rule to accept only two components
        // in a semver (two components version numbers are very common, and we
        // should be able to treat them as semvers).  Maybe emit a warning or
        // even an error based on a config option.
        if ( rhs_len < 3 ) {
            throw new PackageException("RHS is not a SemVer version: '" + rhs.myString + "'");
        }
        if ( rhs_len < this_len ) {
            // if the template has a special string, but the RHS has not,
            // then it does not match
            return false;
        }
        for ( int i = 0; i < this_len; ++i ) {
            String p1 = myParts[i];
            String p2 = rhs.myParts[i];
            if ( ! p1.equals(p2) ) {
                // if one part is not equal, then it does not match
                return false;
            }
        }
        // if all parts are equal, then the version matches the template
        return true;
    }

    /**
     * Does {@code rhs} (a SemVer version) match this SemVer template as a minimum?
     * 
     * Return true if {@code rhs} is equal or above this template.
     */
    public boolean matchesMin(Semver rhs)
            throws PackageException
    {
        int this_len = myParts.length;
        int rhs_len = rhs.myParts.length;
        int max = this_len == 4 ? 3 : this_len;
        for ( int i = 0; i < max; ++i ) {
            String p1 = myParts[i];
            String p2 = rhs.myParts[i];
            int cmp = compareNumbers(p1, p2);
            if ( cmp < 0 ) {
                return true;
            }
            if ( cmp > 0 ) {
                return false;
            }
        }
        if ( this_len < 3 ) { // template is only 1 or 2 parts-long
            return true;
        }
        if ( this_len == 4 ) { // template has a special part
            if ( rhs_len == 4 ) {
                String p1 = myParts[3];
                String p2 = rhs.myParts[3];
                return p1.compareTo(p2) <= 0;
            }
            return true; // 1.0.0alpha is before 1.0.0
        }
        if ( rhs_len == 4 ) {
            return false;
        }
        return true; // perfect 3 parts equality
    }

    /**
     * Does {@code rhs} (a SemVer version) match this SemVer template as a maximum?
     *
     * Return true if {@code rhs} is equal or below this template.
     */
    public boolean matchesMax(Semver rhs)
            throws PackageException
    {
        int this_len = myParts.length;
        int rhs_len = rhs.myParts.length;
        int max = this_len == 4 ? 3 : this_len;
        for ( int i = 0; i < max; ++i ) {
            String p1 = myParts[i];
            String p2 = rhs.myParts[i];
            int cmp = compareNumbers(p1, p2);
            if ( cmp < 0 ) {
                return false;
            }
            if ( cmp > 0 ) {
                return true;
            }
        }
        if ( this_len < 3 ) { // template is only 1 or 2 parts-long
            return true;
        }
        if ( this_len == 4 ) { // template has a special part
            if ( rhs_len == 4 ) {
                String p1 = myParts[3];
                String p2 = rhs.myParts[3];
                return p1.compareTo(p2) >= 0;
            }
            return false; // 1.0.0alpha is before 1.0.0
        }
        if ( rhs_len == 4 ) {
            return true;
        }
        return true; // perfect 3 parts equality
    }

    private static int compareNumbers(String lhs, String rhs)
    {
        int lhs_len = lhs.length();
        int rhs_len = rhs.length();
        if ( lhs_len < rhs_len ) {
            return -1;
        }
        if ( lhs_len > rhs_len ) {
            return 1;
        }
        return lhs.compareTo(rhs);
    }

    /**
     * Parse the different parts of a SemVer template.
     *
     * Has the package visibility, in order to be unit-tested.  MUST NOT be
     * called from the outside!
     */
    static String[] parse(String semver)
            throws PackageException
    {
        // TODO: Check the doc of split(): regex or not regex?, etc.
        String[] parts = semver.split("\\.");
        int len = parts.length;
        if ( len > 3 ) {
            parseError(semver, "too much version parts");
        }
        if ( len == 0 ) {
            parseError(semver, "no version parts");
        }
        if ( ! NUMBER_RE.matcher(parts[0]).matches() ) {
            parseError(semver, "first part is not a number");
        }
        if ( len == 1 ) {
            return parts;
        }
        if ( ! NUMBER_RE.matcher(parts[1]).matches() ) {
            parseError(semver, "second part is not a number");
        }
        if ( len == 2 ) {
            return parts;
        }
        if ( NUMBER_RE.matcher(parts[2]).matches() ) {
            return parts;
        }
        Matcher m = LAST_PART_RE.matcher(parts[2]);
        if ( m.matches() ) {
            String[] result = new String[4];
            result[0] = parts[0];
            result[1] = parts[1];
            result[2] = m.group(1); // TODO: Do groups start at 1 ??? (with 0 = whole match)
            result[3] = m.group(3);
            return result;
        }
        parseError(semver, "third part is invalid");
        // to make javac happy (it does not detect parseError() always throws an exception)
        return null;
    }

    private static void parseError(String semver, String msg)
            throws PackageException
    {
        throw new PackageException("Invalid SemVer pattern '" + semver + "': " + msg);
    }

    private static final Pattern NUMBER_RE = Pattern.compile("^([1-9][0-9]*)|0$");
    // TODO: Check the regex against the SemVer spec...
    private static final Pattern LAST_PART_RE =
            Pattern.compile("^(([1-9][0-9]*)|0)([a-zA-Z][-a-zA-Z0-9]*)$");

    private String   myString; // for reporting purposes
    private String[] myParts;
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
