/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.expath.pkg.saxon;

import java.io.File;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.s9api.Processor;

/**
 *
 * @author georgfl
 */
public class EXPathRepo
{
    public EXPathRepo(File repo)
    {
        myRepo = repo;
    }

    public Configuration newConfiguration()
    {
        return new EXPathConfiguration(this);
    }

    public TransformerFactoryImpl newTransformerFactory()
    {
        Configuration conf = new EXPathConfiguration(this);
        return new TransformerFactoryImpl(conf);
    }

    // TODO: No.  Would require a proxy configuration, reusing the original
    // config object, except for a few methods...
    // TODO: Same principle for Processor.
    //
//    public void configTransformerFactory(TransformerFactoryImpl factory)
//    {
//        Configuration conf = new EXPathConfiguration(this);
//        factory.setConfiguration(conf);
//    }

    // TODO: There is no way to set the config object on a processor...
    //
//    public Processor newProcessor()
//    {
//        // TODO: Support an SA processor...
//        Processor proc = new Processor(false);
//        Configuration conf = new EXPathConfiguration(this);
//        ...
//    }

    private File myRepo;
}
