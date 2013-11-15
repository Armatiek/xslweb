/****************************************************************************/
/*  File:       SaxonPkgInfo.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.pkg.saxon;

import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.trans.XPathException;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.*;
import org.expath.pkg.repo.Storage.PackageResolver;
import org.expath.pkg.repo.util.Logger;

/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2010-09-19
 */
public class SaxonPkgInfo
        extends PackageInfo
{
    public SaxonPkgInfo(Package pkg)
    {
        super("saxon", pkg);
    }

    public void registerExtensionFunctions(Configuration config)
            throws PackageException
    {
        try {
            // for ( String name : getExtensionClasses(config, root) ) {
            for ( String name : myFuns ) {
                LOG.fine("Register class {0}", name);
                Class clazz;
                try {
                    clazz = Class.forName(name);
                }
                catch ( ClassNotFoundException ex ) {
                    ClassLoader loader = getClassLoader(getPackage(), myJars);
                    clazz = Class.forName(name, true, loader);
                }
                if ( EXPathFunctionDefinition.class.isAssignableFrom(clazz) ) {
                    Class<EXPathFunctionDefinition> c = clazz.asSubclass(EXPathFunctionDefinition.class);
                    EXPathFunctionDefinition fun = c.newInstance();
                    fun.setConfiguration(config);
                    config.registerExtensionFunction(fun);
                }
                else if ( ExtensionFunctionDefinition.class.isAssignableFrom(clazz) ) {
                    Class<ExtensionFunctionDefinition> c = clazz.asSubclass(ExtensionFunctionDefinition.class);
                    ExtensionFunctionDefinition fun = c.newInstance();
                    config.registerExtensionFunction(fun);
                }
                else {
                    throw new PackageException("Not a proper ExtensionFunctionDefinition: " + clazz);
                }
            }
        }
        catch ( ClassNotFoundException ex ) {
            throw new PackageException("Error registering Java extension function", ex);
        }
        catch ( InstantiationException ex ) {
            throw new PackageException("Error registering Java extension function", ex);
        }
        catch ( IllegalAccessException ex ) {
            throw new PackageException("Error registering Java extension function", ex);
        }
        catch ( XPathException ex ) {
            throw new PackageException("Error registering extension functions", ex);
        }
    }

    @Override
    public StreamSource resolve(String href, URISpace space)
            throws PackageException
    {
        PackageResolver resolver = getPackage().getResolver();
        // if XQuery, try a direct module, or a wrapper for java
        if ( space == URISpace.XQUERY ) {
            String f = myXquery.get(href);
            if ( f != null ) {
                try {
                    return resolver.resolveComponent(f);
                }
                catch ( Storage.NotExistException ex ) {
                    throw new PackageException("Query not found in the package", ex);
                }
            }
            f = myXqueryWrappers.get(href);
            if ( f != null ) {
                try {
                    return resolveXqueryWrapper(href);
                }
                catch ( XPathException ex ) {
                    throw new PackageException("Error resolving the URI: " + href, ex);
                }
            }
        }
        // or if XSLT, try a direct stylesheet, or a wrapper for java
        else if ( space == URISpace.XSLT ) {
            String f  = myXslt.get(href);
            if ( f != null ) {
                try {
                    return resolver.resolveComponent(f);
                }
                catch ( Storage.NotExistException ex ) {
                    throw new PackageException("Stylesheet not found in the package", ex);
                }
            }
            f = myXsltWrappers.get(href);
            if ( f != null ) {
                return resolveXsltWrapper(href);
            }
        }
        // ignore any non-supported spaces
        else {
            // nothing
        }
        // return null if nothing has been found
        return null;
    }

    /**
     * Must resolve to the empty wrapper.
     */
    private StreamSource resolveXsltWrapper(String href)
    {
        Reader r = new StringReader(EMPTY_STYLESHEET);
        return new StreamSource(r);
    }

    /**
     * Must resolve to an empty wrapper, generated with the correct module URI.
     */
    private StreamSource resolveXqueryWrapper(String href)
            throws XPathException
    {
        String module = "module namespace tns = '" + href + "';\n";
        Reader r = new StringReader(module);
        StreamSource src = new StreamSource(r);
        src.setSystemId("http://expath.org/pkg/saxon/xquery#empty");
        return src;
    }

    // TODO: Actually, the resulting ClassLoader must be cached, as a same class
    // will be loaded twice if via 2 different loaders (even if they have the
    // exact same classpath... !!! (or maybe the resulting loaded class object?)
    private ClassLoader getClassLoader(Package pkg, Set<String> jars)
            throws XPathException
                 , PackageException
    {
        List<URL> cp = new ArrayList<URL>();
        PackageResolver resolver = pkg.getResolver();
        for ( String j : jars ) {
            try {
                StreamSource src = resolver.resolveComponent(j);
                String sysid = src.getSystemId();
                cp.add(new URL(sysid));
            }
            catch ( Storage.NotExistException ex ) {
                throw new PackageException("JAR not found in the package", ex);
            }
            catch ( MalformedURLException ex ) {
                throw new XPathException("The file does not result in a valid URI", ex);
            }
        }
        LOG.fine("Use the following CP for registerExtensionFunctions: {0}", cp);
        try {
            ClassLoader parent = SaxonPkgInfo.class.getClassLoader();
            return new URLClassLoader(cp.toArray(new URL[0]), parent);
        }
        // can be thrown in a servlet container with restrictions...
        catch ( AccessControlException ex ) {
            LOG.info("Access control error: {0}", ex);
            return null;
        }
    }

    public Set<String> getJars() {
        return myJars;
    }

    public void addJar(String jar) {
        myJars.add(jar);
    }
    public void addFunction(String fun) {
        myFuns.add(fun);
    }
    public void addXslt(String href, String file) {
        myXslt.put(href, file);
    }
    public void addXQuery(String href, String file) {
        myXquery.put(href, file);
    }
    public void addXsltWrapper(String href, String file) {
        myXsltWrappers.put(href, file);
    }
    public void addXQueryWrapper(String href, String file) {
        myXqueryWrappers.put(href, file);
    }

    private Set<String>         myJars = new HashSet<String>();
    private Set<String>         myFuns = new HashSet<String>();
    private Map<String, String> myXslt = new HashMap<String, String>();
    private Map<String, String> myXquery = new HashMap<String, String>();
    private Map<String, String> myXsltWrappers = new HashMap<String, String>();
    private Map<String, String> myXqueryWrappers = new HashMap<String, String>();

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SaxonPkgInfo.class);
    /** The content of the empty wrapper stylesheet. */
    private static String EMPTY_STYLESHEET =
            "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'/>\n";
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
