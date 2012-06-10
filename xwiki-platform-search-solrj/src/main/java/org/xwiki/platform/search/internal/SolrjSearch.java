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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.platform.search.SearchException;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.SolrQuery;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Search implementation with Solrj backend.
 *
 * @version $Id$
 */
@Component
@Named("solrj")
@Singleton
public class SolrjSearch extends AbstractSearch
{

    /**
     * Properties.
     */
    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configuration;

    /**
     * Solr Server.
     */
    private static EmbeddedSolrServer solrServer;

    /**
     * {@inheritDoc}
     *
     * @throws SearchException
     * @see org.xwiki.platform.search.Search#initialize()
     */
    @Override
    public void initialize() throws SearchException
    {
        String solrHome = null;

        try {
            // Start embedded solr server.
            logger.info("Starting embedded solr server..");
            solrHome = configuration.getProperty("search.solr.home");
            System.setProperty("solr.solr.home", solrHome);

            /* Initialize the SOLR backend using an embedded server */
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer container = initializer.initialize();
            solrServer = new EmbeddedSolrServer(container, "");

            // Delete the existing index.
            solrServer.deleteByQuery("*:*");

            // Index Wiki.
            indexWiki();



        } catch (Exception ex) {
            throw new SearchException("Failed to initialize the solr embedded server with solr.solr.home [" + solrHome
                + "].", ex);
        }

    }

    protected int indexWiki(String wikiName) throws XWikiException
    {
        final XWikiContext xcontext = getXWikiContext();

        String currentWikiName = xcontext.getWiki().getName();

        if (!currentWikiName.equalsIgnoreCase(wikiName)) {
            xcontext.setDatabase(wikiName);
        }

        // Total number of documents.
        String hql = "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc";
        List<Object[]> documents = xcontext.getWiki().search(hql, xcontext);

        int totalDocs = documents.size();

        try {
            // A daemon thread to index wiki pages
            Thread thread = new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    xcontext.getWiki();

                }

            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            thread.setName("Indexing documents by solrj search");
            thread.start();

        } catch (Exception ex) {
            logger.error("Exception while initializing embedded solr server.", ex.getMessage());
        } finally {
            xcontext.setDatabase(currentWikiName);
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#deleteDocumentIndex(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean deleteDocumentIndex(DocumentReference document)
    {
        XWikiDocument xdoc;
        try {
            xdoc = getXWikiContext().getWiki().getDocument(document, getXWikiContext());
            solrServer.deleteById(xdoc.getId() + "");
        } catch (Exception e) {
            logger.error("Failure in deleting the index for ["+document.getName()+"]", e);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#getImplementation()
     */
    @Override
    public String getImplementation()
    {
        return "Embedded Solr";
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#indexDocument(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean indexDocument(DocumentReference document)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#indexDocuments(java.util.List)
     */
    @Override
    public int indexDocuments(List<DocumentReference> documents)
    {

        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#parseQuery()
     */
    @Override
    public SolrQuery parseQuery()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#rebuildFarmIndex()
     */
    @Override
    public int rebuildFarmIndex()
    {
        int docCount = 0;

        if (getXWikiContext().getWiki().isVirtualMode()) {
            try {
                // Delete the existing index.
                solrServer.deleteByQuery("*:*");
                docCount = indexWikiFarm();
            } catch (Exception e) {
                logger.error("Failure in rebuilding the farm index.", e);
            }
        }
        return docCount;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#rebuildFarmIndex(java.util.List)
     */
    @Override
    public int rebuildFarmIndex(List<WikiReference> wikis)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#rebuildWikiIndex()
     */
    @Override
    public int rebuildWikiIndex()
    {
        int docCount = 0;
        try {
            // Delete the existing index.
            solrServer.deleteByQuery("*:*");
            docCount = indexWiki();
        } catch (Exception e) {
            logger.error("Failure in rebuilding the farm index.", e);
        }
        return docCount;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#rebuildWikiIndex(java.util.List)
     */
    @Override
    public int rebuildWikiIndex(List<SpaceReference> spaces)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#search(java.lang.String)
     */
    @Override
    public SearchResponse search(String query)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#search(java.lang.String, java.util.List)
     */
    @Override
    public SearchResponse search(String query, List<String> languages)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#search(java.lang.String, java.util.List,
     *      org.xwiki.model.reference.WikiReference)
     */
    @Override
    public SearchResponse search(String query, List<String> languages, WikiReference wikiReference)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.Search#search(java.lang.String, java.util.List,
     *      org.xwiki.model.reference.WikiReference, org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public SearchResponse search(String query, List<String> languages, WikiReference wiki, SpaceReference space)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
