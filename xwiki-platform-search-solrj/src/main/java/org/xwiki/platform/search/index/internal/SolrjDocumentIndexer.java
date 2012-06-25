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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.internal.SolrDocData;

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

    List<DocumentReference> docList;

    private SolrServer solrServer;

    @Inject
    private Execution execution;

    private Thread thread;


    private class IndexThread implements Runnable{

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            ExecutionContext context=new ExecutionContext();
            // Create a SolrDocData object
            SolrDocData solrdoc = new SolrDocData();

            try {
                executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
            }

            execution.pushContext(context);

            try {

                for (DocumentReference docRef : docList) {

                    try {
                        DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                        SolrInputDocument sdoc = solrdoc.getSolrInputDocument(docRef, documentModelBridge, null);
                        logger.info("Adding document " + docRef.getName());
                        solrServer.add(sdoc);

                    } catch (Exception e) {
                        logger.error("Error retrieving document." + e.getMessage());
                    }
                }

                try {
                    solrServer.commit();
                    logger.info("XWiki documents are successfully committed.");
                } catch (Exception e) {
                    logger.error("Error commiting solr documents", e);
                }

            } finally {
                execution.removeContext();
            }

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexDocuments(java.util.List)
     */
    @Override
    public void indexDocuments(List<DocumentReference> docList)
    {
        if (docList.size() > 0) {
            this.docList = docList;
            IndexThread indexThread=new IndexThread();
            thread = new Thread(indexThread);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {

        logger.info("" + solrServer);

        // Create a clean Execution Context
        ExecutionContext context = new ExecutionContext();

        // Create a SolrDocData object
        SolrDocData solrdoc = new SolrDocData();

        try {
            this.executionContextManager.initialize(context);
        } catch (ExecutionContextException e) {
            throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
        }

        this.execution.pushContext(context);

        try {

            for (DocumentReference docRef : this.docList) {

                try {
                    DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                    SolrInputDocument sdoc = solrdoc.getSolrInputDocument(docRef, documentModelBridge, null);
                    logger.info("Adding document " + docRef.getName());
                    solrServer.add(sdoc);

                } catch (Exception e) {
                    logger.error("Error retrieving document." + e.getMessage());
                }
            }

            try {
                solrServer.commit();
                logger.info("XWiki documents are successfully committed.");
            } catch (Exception e) {
                logger.error("Error commiting solr documents", e);
            }

        } finally {
            this.execution.removeContext();
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteIndex(java.util.List)
     */
    @Override
    public void deleteIndex(List<DocumentReference> docList)
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexDocument(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean indexDocument(DocumentReference doc)
    {
        SolrDocData solrdoc = new SolrDocData();
        try {
            if (documentAccessBridge.exists(doc) && !doc.getName().contains("WatchList")) {

                DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(doc);
                SolrInputDocument sdoc = solrdoc.getSolrInputDocument(doc, documentModelBridge, null);
                logger.info("Adding document " + doc.getName());
                solrServer.add(sdoc);
                solrServer.commit();
                logger.debug("Document [" + doc.getName() + "] is added to solr index.");
                return true;
            }
        } catch (Exception e) {
            logger.error("Error indexing document - [" + doc.getName() + "]");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteIndex(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean deleteIndex(DocumentReference doc)
    {
        try {

            DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(doc);
            SolrDocData solrdoc = new SolrDocData();

            logger.info("Adding document " + doc.getName());
            solrServer.deleteById(solrdoc.getId(documentModelBridge));
        } catch (Exception e) {
            logger.error("Error deleting document.");
        }
        try {
            solrServer.commit();
            logger.info("XWiki documents are successfully committed.");
            return true;
        } catch (SolrServerException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteEntireIndex()
     */
    @Override
    public boolean deleteEntireIndex()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#setSearchEngineObject(java.lang.Object)
     */
    @Override
    public void setSearchEngineObject(Object server)
    {
        if (server instanceof SolrServer) {
            this.solrServer = (SolrServer) server;
        }

    }

}
