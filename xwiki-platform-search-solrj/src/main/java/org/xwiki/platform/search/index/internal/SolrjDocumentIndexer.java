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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
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
import org.xwiki.platform.search.index.DocumentIndexerStatus;
import org.xwiki.platform.search.internal.SolrDocData;

/**
 * @version $Id$
 */
@Component
@Named(SolrjDocumentIndexer.HINT)
@Singleton
public class SolrjDocumentIndexer implements DocumentIndexer
{

    @Inject
    private Logger logger;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private ExecutionContextManager executionContextManager;

    @Inject
    private Execution execution;

    private Thread thread;

    public static final String HINT = "solrjindexer";

    private SolrServer solrServer;

    private Map<String, DocumentIndexerStatus> indexerStatusMap = Collections
        .synchronizedMap(new WeakHashMap<String, DocumentIndexerStatus>());

    private class IndexThread implements Runnable
    {

        private List<DocumentReference> docList;

        public IndexThread(List<DocumentReference> docList)
        {
            this.docList = docList;
        }

        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            ExecutionContext context = new ExecutionContext();
            // Create a SolrDocData object
            SolrDocData solrdoc = new SolrDocData();
            DocumentIndexerStatus indexerStatus = new DocumentIndexerStatus();
            indexerStatusMap.put(Thread.currentThread().getName(), indexerStatus);

            indexerStatus.setTotalDocCount(docList.size());

            try {
                executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
            }

            execution.pushContext(context);

            try {

                for (int i = 0; i < docList.size(); i += 10) {

                    long totalTime = 0;
                    int start = i;
                    int end = (i + 10) < docList.size() ? (i + 10) : (docList.size() - 1);
                    List<DocumentReference> subList = docList.subList(start, end);

                    for (DocumentReference docRef : subList) {

                        try {
                            long startTime = new Date().getTime();
                            DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                            SolrInputDocument sdoc = solrdoc.getSolrInputDocument(docRef, documentModelBridge, null);
                            long endTime = new Date().getTime();
                            UpdateResponse response = solrServer.add(sdoc);
                            totalTime += (endTime - startTime) + response.getElapsedTime();
                            logger.info("Added document " + docRef.getName() + " Elapsed time [" + totalTime + "] ms");
                        } catch (Exception e) {
                            logger.error("Error indexing document [" + docRef.getName() + "]", e);
                        }
                    }

                    // Send out a notification
                    indexerStatus.addStepDetails(totalTime, 10);

                    try {
                        UpdateResponse commitResponse = solrServer.commit();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            } finally {
                execution.removeContext();
            }

            logger.info("Total time taken to index [" + indexerStatus.getTotalDocCount() + "] documents is "
                + indexerStatus.getEstimatedCompletionTime());

            indexerStatusMap.remove(Thread.currentThread().getName());

        }
    }

    private class DeleteIndexThread implements Runnable
    {

        private List<DocumentReference> docList;

        public DeleteIndexThread(List<DocumentReference> docList)
        {
            this.docList = docList;
        }

        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            ExecutionContext context = new ExecutionContext();
            // Create a SolrDocData object
            SolrDocData solrdoc = new SolrDocData();

            try {
                executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
            }

            execution.pushContext(context);

            try {
                List<String> ids = new ArrayList<String>();
                for (DocumentReference docRef : docList) {

                    try {
                        DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                        String id = solrdoc.getId(documentModelBridge);
                        ids.add(id);
                    } catch (Exception e) {
                        logger.error("Error retrieving document." + e.getMessage());
                    }
                }

                try {
                    solrServer.deleteById(ids);
                    solrServer.commit();
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
            IndexThread indexThread = new IndexThread(docList);
            thread = new Thread(indexThread);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
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
        if (docList.size() > 0) {
            DeleteIndexThread deleteIndexThread = new DeleteIndexThread(docList);
            thread = new Thread(deleteIndexThread);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
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
            solrServer.commit();
            return true;
        } catch (Exception e) {
            logger.error("Error deleting document.");
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
        try {
            // TODO : Fix the query param value
            solrServer.deleteByQuery("*.*");
            solrServer.commit();
            return true;
        } catch (Exception e) {
            logger.error("Error deleting document.");
        }
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

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#getStatus()
     */
    @Override
    public Map<String, DocumentIndexerStatus> getStatus()
    {
        return indexerStatusMap;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.index.DocumentIndexer#getStatus(java.lang.String)
     */
    @Override
    public DocumentIndexerStatus getStatus(String threadId)
    {
        return indexerStatusMap.get(threadId);
    }
}
