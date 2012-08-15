/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.platform.search.internal;

import groovy.lang.Singleton;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.platform.search.SearchEngine;
/**
 * @version $Id$
 */
@Component
@Named(SolrjSearchEngine.HINT)
@Singleton
public class SolrjSearchEngine implements SearchEngine, Initializable
{
    /**
     * SOLR HOME KEY.
     */
    public static final String SOLR_HOME_KEY = "solr.solr.home";
    
    /**
     * solrjsearchengine HINT.
     */
    public static final String HINT = "solrjsearchengine";
    
    /**
     * solrserver HINT.
     */
    private static SolrServer solrServer;
    
    /**
     * corecontainer HINT.
     */
    private static CoreContainer coreContainer;
    
    
    /**
     *  Logger component.
     */
    @Inject
    private Logger logger;

    /**
     * Properties.
     */
    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configuration;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        String solrHome = "";
        try {
            // Start embedded solr server.
            logger.info("Starting embedded solr server..");
            if (System.getProperty(SOLR_HOME_KEY) == null) {
                solrHome = configuration.getProperty("search.solr.home");
                System.setProperty(SOLR_HOME_KEY, solrHome);
            }

            /* Initialize the SOLR backend using an embedded server */
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            coreContainer = initializer.initialize();
            solrServer = new EmbeddedSolrServer(coreContainer, "");

        } catch (Exception e) {
            logger.error("Failed to initialize the solr embedded server with solr.solr.home [" + solrHome + "] :: "
                + e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchEngine#getSearchEngine()
     */
    @Override
    public SolrServer getSearchEngine()
    {
        return solrServer;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchEngine#getCoreContainer()
     */
    public CoreContainer getCoreContainer()
    {
        return coreContainer;
    }

}
