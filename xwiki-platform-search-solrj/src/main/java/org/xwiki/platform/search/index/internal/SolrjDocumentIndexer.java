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



import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.DocumentIndexerStatus;
import org.xwiki.platform.search.internal.SolrDocData;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;


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
    
    @Inject
    @Named("plain/1.0")
    private BlockRenderer renderer;
    StringBuilder retval = new StringBuilder();
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
            
            logger.info("Indexing a total of [" + docList.size() + "] documents testing");
            
            try {
                executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Solrj indexer's execution context", e);
            }

            execution.pushContext(context);

            try {
                solrServer.deleteByQuery("*:*");
                solrServer.commit();
            } catch (Exception e) {
                logger.error("Error while deleting the index");
            }

            long totalTime=0;
            

            try {
                for (int i = 0; i < docList.size(); i += 10) {
                    int start = i;
                    int end = (i + 10) < docList.size() ? (i + 10) : (docList.size() - 1);
                    List<DocumentReference> subList = docList.subList(start, end);
                    List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
                    long startTime, endTime, fetchTime = 0;
                    for (DocumentReference docRef : subList) {

                        try {
                            startTime = new Date().getTime();
                            DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                            
                            //Convert the XWiki syntax of document to plain text.
                            WikiPrinter printer=new DefaultWikiPrinter();
                            renderer.render(documentModelBridge.getXDOM(), printer);
                            
                            //get the language
                            String language = documentModelBridge.getRealLanguage();
                            if (language == null || language == "")
                            {
                                language = "en";
                            }
                            
                            SolrInputDocument sdoc = solrdoc.getSolrInputDocument(docRef, documentModelBridge, language, printer.toString());
                            docs.add(sdoc);
                            endTime = new Date().getTime();
                            fetchTime += (endTime - startTime);
                            
                            //indexing attachments
                            List<AttachmentReference> attachReferences=documentAccessBridge.getAttachmentReferences(docRef);
                            
                            for(AttachmentReference attachReference:attachReferences)
                            {   
                                indexAttachment(attachReference,documentModelBridge);
                                logger.info("successful indexing attachments");
                            }
                        } catch (Exception e) {
                            logger.error("Error fetching document [" + docRef.getName() + "]", e);
                        }
                    }

                    try {
                        UpdateResponse updateResponse=solrServer.add(docs);
                        UpdateResponse commitResponse=solrServer.commit();
                        // Send out a notification
                        indexerStatus.addStepDetails(fetchTime+updateResponse.getElapsedTime()+commitResponse.getElapsedTime(), 10);
                        totalTime += fetchTime+updateResponse.getElapsedTime()+commitResponse.getElapsedTime();
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

        public DeleteIndexThread(List<DocumentReference> docList)
        {
            this.docList = docList;
        }

        @Override
        public void run()
        {
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
                        
                        //deleting the index of the documents
                        DocumentModelBridge documentModelBridge = documentAccessBridge.getDocument(docRef);
                        String id = solrdoc.getId(documentModelBridge);
                        ids.add(id);
                        
                        //deleting the indexes of the attachments
                        List<AttachmentReference> attachReferences=documentAccessBridge.getAttachmentReferences(docRef);
                        for(AttachmentReference attachReference:attachReferences)
                        {
                            String attachId = getAttachmentID(documentModelBridge,attachReference);
                            ids.add(attachId);
                        }
                        
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
                //Convert the XWiki syntax of document to plain text.
                WikiPrinter printer=new DefaultWikiPrinter();
                renderer.render(documentModelBridge.getXDOM(), printer);
                
              //get the language
                String language = documentModelBridge.getRealLanguage();
                if (language == null || language == "")
                {
                    language = "en";
                }
                SolrInputDocument sdoc = solrdoc.getSolrInputDocument(doc, documentModelBridge, language,printer.toString());
               
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
            logger.info("Deleting document " + doc.getName());
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
            solrServer.deleteByQuery("*:*");
            solrServer.commit();
            return true;
        } catch (Exception e) {
            logger.error("Error deleting document.");
        }
        return false;
    }
    
    
    /**
     * indexes the attachment using the Solr cell
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#indexAttachment(org.xwiki.model.reference.AttachmentReference, org.xwiki.bridge.DocumentModelBridge)
     */
    public boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {  
        SolrDocData solrdoc = new SolrDocData();
        
        try
        {
        String Content = getFullText(attachment);
        
      //get the language
        String language = doc.getRealLanguage();
        if (language == null || language == "")
        {
            language = "en";
        }
        
        SolrInputDocument sdoc = solrdoc.getSolrInputAttachment(attachment, doc, language, Content);
        logger.info("Adding document " + attachment.getName());
        solrServer.add(sdoc);
        solrServer.commit();
        return true;
        } 
       catch (Exception e) {
        logger.error("Error indexing document - [" + attachment.getName() + "]");
        }      
        return false;
        
    }
    
   
    /**
     *
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DocumentIndexer#deleteIndexAttachment(org.xwiki.model.reference.AttachmentReference, org.xwiki.bridge.DocumentModelBridge)
     */
    public  boolean deleteIndexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        try {
            solrServer.deleteById(getAttachmentID(doc,attachment));
            solrServer.commit();
           
            logger.info("deleted attachment with id"+getAttachmentID(doc,attachment));
            
            return true;
        } catch (Exception e) {
            logger.error("Error deleting attachment.");
        }
        return false;
    }
      
    
    
    // get the attachment unique id
       private String getAttachmentID(DocumentModelBridge doc,AttachmentReference attachment)
       {   
           StringBuilder retval = new StringBuilder();
           retval.append(doc.getDocumentReference().getName()).append(".");
           retval.append(doc.getDocumentReference().getLastSpaceReference().getName()).append(".");
           retval.append(doc.getDocumentReference().getWikiReference().getName()).append(".");
           retval.append(doc.getRealLanguage());
           retval.toString();
           return retval.append(".file.").append(attachment.getName()).toString();
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
    
    private String getFullText(AttachmentReference attachment)
    {
        
        String contentText = getContentAsText(attachment);
          
        if (contentText != null) {
            if (retval.length() > 0) {
                retval.append(" ");
            }
            
        }
        return (retval.append(contentText)).toString();
    }

    /**
     * 
     * @param attachment
     * @return
     */
    private String getContentAsText(AttachmentReference attachment)
    {
        String contentText = null;

        try {
           
            logger.info("begining to parse the attachment");

            Tika tika = new Tika();

            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY,attachment.getName());
            
            InputStream in =documentAccessBridge.getAttachmentContent(attachment);

            contentText = StringUtils.lowerCase(tika.parseToString(in,metadata ));
        } catch (Throwable ex) {
           logger.info(contentText);
                
        }

        return contentText;
    }
    
}
