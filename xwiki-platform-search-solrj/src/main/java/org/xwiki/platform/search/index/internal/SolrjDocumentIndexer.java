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
package org.xwiki.platform.search.index.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.search.SearchEngine;
import org.xwiki.platform.search.SearchException;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.internal.SolrjSearchEngine;

/**
 * @version $Id$
 */
@Component
@Named(SolrjDocumentIndexer.HINT)
@Singleton
public class SolrjDocumentIndexer implements DocumentIndexer, Runnable
{

    @Inject
    private Logger logger;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private ExecutionContextManager executionContextManager;

    public static final String HINT = "solrjindexer";

    List<DocumentReference> docs;

    private SolrServer solrServer;

    @Inject
    private Execution execution;

    @Inject
    @Named(SolrjSearchEngine.HINT)
    private SearchEngine searchEngine;

    private Thread thread;

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexDocuments(java.util.List)
     */
    @Override
    public void indexDocuments(List<DocumentReference> docs)
    {

        this.docs = docs;
        try {
            solrServer = (SolrServer) searchEngine.getSearchEngine();
        } catch (SearchException e1) {
            logger.error("Error retrieving Search engine object");
        }
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {

        logger.info(""+solrServer);

        // Create a clean Execution Context
        ExecutionContext context = new ExecutionContext();

        try {
            this.executionContextManager.initialize(context);
        } catch (ExecutionContextException e) {
            throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
        }

        this.execution.pushContext(context);

        try {
            for (DocumentReference docRef : this.docs) {
                try {
                    DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                    // logger.info("Document:" + documentModelBridge.getTitle());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            this.execution.removeContext();
        }

    }

}
