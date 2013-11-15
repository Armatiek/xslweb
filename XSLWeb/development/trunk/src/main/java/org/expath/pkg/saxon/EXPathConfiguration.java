/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.expath.pkg.saxon;

import net.sf.saxon.Configuration;

/**
 *
 * @author georgfl
 */
public class EXPathConfiguration
        extends Configuration
{
    public EXPathConfiguration(EXPathRepo repo)
    {
        myRepo = repo;
    }

    private EXPathRepo myRepo;
}
