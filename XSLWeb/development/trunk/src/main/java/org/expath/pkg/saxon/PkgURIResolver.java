package org.expath.pkg.saxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.URISpace;
import org.expath.pkg.repo.util.Logger;

/**
 * URI Resolver to resolve within a repository.
 *
 * @author Florent Georges
 * @date   2010-05-02
 */
public class PkgURIResolver
        implements URIResolver
{
    public PkgURIResolver(Map<String, String> overrides, SaxonRepository repo, URIResolver parent, URISpace space)
            throws PackageException
    {
        myOverrides = overrides;
        myRepo = repo;
        myParent = parent;
        mySpace = space;
    }

    @Override
    public Source resolve(String href, String base)
            throws TransformerException
    {
        LOG.fine("resolve: {0} with base: {1}", href, base);
        // try the override URIs
        String override = myOverrides.get(href);
        if ( override != null ) {
            File f = new File(override);
            StreamSource s;
            try {
                s = new StreamSource(new FileInputStream(f));
            }
            catch ( FileNotFoundException ex ) {
                throw new TransformerException("Error resolving the URI", ex);
            }
            s.setSystemId(f.toURI().toString());
            return s;
        }
        // try a Saxon-specific stuff
        try {
            Source s = myRepo.resolve(href, mySpace);
            if ( s != null ) {
                return s;
            }
        }
        catch ( PackageException ex ) {
            throw new TransformerException("Error resolving the URI", ex);
        }
        // delegate to pkg-repo's resolver
        return myParent.resolve(href, base);
    }

    /** The overrides (take precedence over the catalog resolver). */
    private Map<String, String> myOverrides;
    /** The Saxon repo used to resolve Saxon-specific stuff. */
    private SaxonRepository myRepo;
    /** The parent resolver, from pkg-repo. */
    private URIResolver myParent;
    /** ... */
    private URISpace mySpace;
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(PkgURIResolver.class);
}
