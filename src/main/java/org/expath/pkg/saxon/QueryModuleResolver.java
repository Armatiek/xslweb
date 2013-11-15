package org.expath.pkg.saxon;

import java.io.File;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.lib.ModuleURIResolver;

import net.sf.saxon.trans.XPathException;
import org.expath.pkg.repo.FileSystemStorage;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.repo.URISpace;

/**
 * FIXME: Why does this class instantiate Repository?  Why is it not constructed
 * with a SaxonRepository as param?
 * 
 * @author fgeorges
 */
public class QueryModuleResolver
        implements ModuleURIResolver
{
    /** Constructor */
    public QueryModuleResolver()
            throws PackageException
    {
        // TODO: FIXME: Clean up!
        String repo_name = System.getenv("EXPATH_REPO");
        File repo_file = new File(repo_name);
        Storage storage = new FileSystemStorage(repo_file);
        myRepo = new Repository(storage);
    }

    public StreamSource[] resolve(String module_uri, String base_uri, String[] locations)
            throws XPathException
    {
        // can arise for instance when compiling XQuery from a file and using
        // the option -u (treat everything as a URI, so here, we only have a
        // location, and it is tried to be resolved...)
        if ( module_uri == null ) {
            return null;
        }

        StreamSource result = null;
        try {
            result = myRepo.resolve(module_uri, URISpace.XQUERY);
        }
        catch ( PackageException ex ) {
            // ignore
            // TODO: Really?
        }
        if ( result == null ) {
            return null;
        }
        return new StreamSource[]{ result };
    }

    /** The underlying catalog */
    private Repository myRepo;
}
