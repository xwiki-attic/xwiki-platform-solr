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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.platform.search.DocumentField;
import org.xwiki.platform.search.index.DocumentData;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.DocumentIndexerStatus;
import org.xwiki.platform.search.internal.SolrDocData;
import org.xwiki.rendering.renderer.BlockRenderer;

import com.google.gson.Gson;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

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
    private ExecutionContextManager executionContextManager;

    @Inject
    private Execution execution;

    private Thread thread;

    public static final String HINT = "solrjindexer";

    private SolrServer solrServer;

    @Inject
    @Named("plain/1.0")
    private BlockRenderer renderer;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    private Map<String, DocumentIndexerStatus> indexerStatusMap = Collections
        .synchronizedMap(new HashMap<String, DocumentIndexerStatus>());

    private class IndexThread implements Runnable
    {

        private List<DocumentReference> docList;

        EntityReference entityReference;

        public IndexThread(EntityReference entityReference, List<DocumentReference> docList)
        {
            this.docList = docList;
            this.entityReference = entityReference;
        }

        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            ExecutionContext context = new ExecutionContext();
            // Create a SolrDocData object
            SolrDocData solrdoc = new SolrDocData();
            DocumentIndexerStatus indexerStatus = new SolrjDocumentIndexerStatus();
            indexerStatusMap.put(Thread.currentThread().getName(), indexerStatus);

            indexerStatus.setTotalDocCount(docList.size());
            indexerStatus.setTitle(Thread.currentThread().getName());
            indexerStatus.setEntityName(entityReference.getName());
            indexerStatus.setEntityType(entityReference.getType().toString());

            logger.info("Indexing a total of [" + docList.size() + "] documents testing");

            try {
                executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
            }

            execution.pushContext(context);

            long totalTime = 0;

            try {
                for (int i = 0; i < docList.size(); i += 10) {
                    int start = i;
                    int end = (i + 10) < docList.size() ? (i + 10) : (docList.size());
                    List<DocumentReference> subList = docList.subList(start, end);
                    List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
                    long startTime, endTime, fetchTime = 0;
                    for (DocumentReference docRef : subList) {

                        try {
                            startTime = new Date().getTime();

                            endTime = new Date().getTime();
                            fetchTime += (endTime - startTime);

                            SolrjDocumentData sdocdata =
                                componentManager.getInstance(DocumentData.class, SolrjDocumentData.HINT);

                            // Document
                            SolrInputDocument sdoc = sdocdata.getInputDocument(docRef);
                            docs.add(sdoc);

                            // Attachments
                            List<SolrInputDocument> attachmentdocs = sdocdata.getInputAttachments(docRef);
                            if (attachmentdocs != null && !attachmentdocs.isEmpty())
                                docs.addAll(attachmentdocs);

                            // Objects
                            List<SolrInputDocument> objDocs = sdocdata.getInputObjects(docRef);
                            if (objDocs != null && !objDocs.isEmpty())
                                docs.addAll(objDocs);

                            // Properties
                            List<SolrInputDocument> propDocs = sdocdata.getInputProperties(docRef);
                            if (propDocs != null && !propDocs.isEmpty())
                                docs.addAll(propDocs);

                        } catch (Exception e) {
                            logger.error("Error fetching document [" + docRef.getName() + "]", e);
                        }
                    }

                    try {
                        UpdateResponse updateResponse = solrServer.add(docs);
                        UpdateResponse commitResponse = solrServer.commit();

                        for (SolrInputDocument doc : docs) {
                            String lang = (String) doc.getFieldValue(DocumentField.LANGUAGE);
                            if (doc.getField(DocumentField.DOCUMENT_CONTENT + "_" + lang) != null) {
                                doc.removeField(DocumentField.DOCUMENT_CONTENT + "_" + lang);
                            }
                            if (doc.getField(DocumentField.ATTACHMENT_CONTENT + "_" + lang) != null) {
                                doc.removeField(DocumentField.ATTACHMENT_CONTENT + "_" + lang);
                            }
                            if (doc.getField(DocumentField.OBJECT_CONTENT + "_" + lang) != null) {
                                doc.removeField(DocumentField.OBJECT_CONTENT + "_" + lang);
                            }
                        }

                        // Send out a notification
                        indexerStatus.addStepDetails(
                            fetchTime + updateResponse.getElapsedTime() + commitResponse.getElapsedTime(), 10, docs);
                        totalTime += fetchTime + updateResponse.getElapsedTime() + commitResponse.getElapsedTime();
                    } catch (Exception e) {
                        logger.error("Error commiting solr index updates");
                    }

                }

            } finally {
                execution.removeContext();
            }

            logger.info("Total time taken to index [" + indexerStatus.getTotalDocCount() + "] documents is "
                + totalTime);

            indexerStatusMap.remove(Thread.currentThread().getName());

        }
    }

    private class DeleteIndexThread implements Runnable
    {

        private List<DocumentReference> docList;

        private EntityReference entityReference;

        public DeleteIndexThread(EntityReference entityReference, List<DocumentReference> docList)
        {
            this.docList = docList;
            this.entityReference = entityReference;
        }

        @Override
        public void run()
        {
            ExecutionContext context = new ExecutionContext();

            try {
                executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
            }

            execution.pushContext(context);

            try {
                List<String> idList = new ArrayList<String>();
                SolrjDocumentData sdocdata = componentManager.getInstance(DocumentData.class, SolrjDocumentData.HINT);
                for (DocumentReference documentReference : docList) {

                    try {

                        //Add Document Id.
                        idList.add(sdocdata.getDocumentId(documentReference));
                        
                        //Attachments
                        List<AttachmentReference> attachmentReferenceList =
                            documentAccessBridge.getAttachmentReferences(documentReference);
                        for(AttachmentReference attachmentReference:attachmentReferenceList){
                            idList.add(sdocdata.getAttachmentId(attachmentReference));
                        }
                        
                        //Objects
                        idList.addAll(sdocdata.getObjectIdList(documentReference));
                        
                        //Properties
                        idList.addAll(sdocdata.getPropertyIdList(documentReference));
                        

                    } catch (Exception e) {
                        logger.error("Error while adding document ids for index deletion" + e.getMessage());
                    }
                }

                try {
                    solrServer.deleteById(idList);
                    solrServer.commit();
                } catch (Exception e) {
                    logger.error("Error commiting solr documents", e);
                }

            } catch (Exception ex) {
                logger.error("Error instantiating DocumentData component with hint["+SolrjDocumentData.HINT+"]");
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
    public void indexDocuments(List<DocumentReference> docs)
    {
        this.indexDocuments(null, docs);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteIndex(java.util.List)
     */
    @Override
    public void deleteIndex(List<DocumentReference> docs)
    {
        this.deleteIndex(null, docs);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexDocument(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean indexDocument(DocumentReference doc)
    {

        try {
            if (documentAccessBridge.exists(doc) && !doc.getName().contains("WatchList")) {
                SolrjDocumentData sdocdata =
                    this.componentManager.getInstance(DocumentData.class, SolrjDocumentData.HINT);
                UpdateResponse response = null;
                Gson gson = new Gson();
                // Document
                SolrInputDocument sdoc = sdocdata.getInputDocument(doc);
                if (sdoc != null) {
                    response = solrServer.add(sdoc);
                    logger.info(gson.toJson(response));
                }

                // Attachments
                List<SolrInputDocument> attachmentdocs = sdocdata.getInputAttachments(doc);
                if (attachmentdocs != null && !attachmentdocs.isEmpty()) {
                    response = solrServer.add(attachmentdocs);
                    logger.info(gson.toJson(response));
                }

                // Objects
                List<SolrInputDocument> objDocs = sdocdata.getInputObjects(doc);
                if (objDocs != null && !objDocs.isEmpty()) {
                    solrServer.add(objDocs);
                    logger.info(gson.toJson(response));
                }

                // Properties
                List<SolrInputDocument> propDocs = sdocdata.getInputProperties(doc);
                if (propDocs != null && !propDocs.isEmpty()) {
                    response = solrServer.add(propDocs);
                    logger.info(gson.toJson(response));
                }

                // Commit Response
                response = solrServer.commit();
                logger.info("Commit Response");
                logger.info(gson.toJson(response));
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
            SolrjDocumentData sdocdata = this.componentManager.getInstance(DocumentData.class, SolrjDocumentData.HINT);
            solrServer.deleteById(sdocdata.getDocumentId(doc));
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
            solrServer.deleteByQuery("*:*");
            solrServer.commit();
            return true;
        } catch (Exception e) {
            logger.error("Error deleting document.");
        }
        return false;
    }

    /**
     * indexes the attachment using the Solr cell {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexAttachment(org.xwiki.model.reference.AttachmentReference,
     *      org.xwiki.bridge.DocumentModelBridge)
     */
    public boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        SolrDocData solrdoc = new SolrDocData();

        try {
            // String Content = getFullText(attachment);

            // get the language
            String language = doc.getRealLanguage();
            if (language == null || language == "") {
                language = "en";
            }

            SolrInputDocument sdoc = null;
            solrServer.add(sdoc);
            solrServer.commit();
            return true;
        } catch (Exception e) {
            logger.error("Error indexing document - [" + attachment.getName() + "]");
        }
        return false;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteIndexAttachment(org.xwiki.model.reference.AttachmentReference,
     *      org.xwiki.bridge.DocumentModelBridge)
     */
    public boolean deleteIndexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        try {
            SolrjDocumentData sdocdata = this.componentManager.getInstance(DocumentData.class, SolrjDocumentData.HINT);
            solrServer.deleteById(sdocdata.getAttachmentId(attachment));
            solrServer.commit();
            return true;
        } catch (Exception e) {
            logger.error("Error deleting attachment.");
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

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexDocuments(org.xwiki.model.reference.EntityReference,
     *      java.util.List)
     */
    @Override
    public void indexDocuments(EntityReference reference, List<DocumentReference> docs)
    {
        if (docs.size() > 0) {
            IndexThread indexThread = new IndexThread(reference, docs);
            thread = new Thread(indexThread);
            thread.setName("SolrjIndexer[" + indexThread.hashCode() + "]");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteIndex(org.xwiki.model.reference.EntityReference,
     *      java.util.List)
     */
    @Override
    public void deleteIndex(EntityReference reference, List<DocumentReference> docs)
    {
        try {
            if(reference.getType()== EntityType.WIKI){
                solrServer.deleteByQuery("wiki:"+reference.getName());
            }else if(reference.getType() == EntityType.SPACE){
                solrServer.deleteByQuery("space:"+reference.getName());
            }
            solrServer.commit();
        } catch (Exception e) {
            logger.error("Error deleting index for EntityReference:"+reference);
        }      
    }

}
