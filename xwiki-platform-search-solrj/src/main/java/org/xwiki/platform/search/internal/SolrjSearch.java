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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;
import org.xwiki.platform.search.SearchEngine;
import org.xwiki.platform.search.SearchException;
import org.xwiki.platform.search.SearchRequest;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.DocumentIndexerStatus;
import org.xwiki.platform.search.index.SearchIndexingException;
import org.xwiki.platform.search.index.internal.SolrjDocumentIndexer;

import com.google.gson.Gson;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.internal.event.AbstractAttachmentEvent;
import com.xpn.xwiki.internal.event.AttachmentAddedEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;

/**
 * Search implementation with Solrj backend.
 * 
 * @version $Id$
 */
@Component
@Named(SolrjSearch.HINT)
@Singleton
public class SolrjSearch extends AbstractSearch
{

    public static final String HINT = "solrj";

    /**
     * Document Indexer for solrj
     */
    @Inject
    @Named(SolrjDocumentIndexer.HINT)
    private DocumentIndexer indexer;

    @Inject
    @Named(SolrjSearchEngine.HINT)
    private SearchEngine searchEngine;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    /**
     * {@inheritDoc}
     * 
     * @throws SearchException
     * @see org.xwiki.platform.search.Search#initialize()
     */
    @Override
    public void initialize() throws SearchException
    {
        try {
            indexer.setSearchEngineObject(searchEngine.getSearchEngine());
            // this.indexWiki();
        } catch (Exception e) {
            logger.error("Failed to index current wiki");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#indexWiki(java.lang.String)
     */
    @Override
    protected int indexWiki(String wikiName) throws XWikiException
    {
        logger.info("Indexing wiki [" + wikiName + "]");

        final XWikiContext xcontext = getXWikiContext();

        String currentWikiName = xcontext.getWiki().getName();

        if (!currentWikiName.equalsIgnoreCase(wikiName)) {
            xcontext.setDatabase(wikiName);
        }

        String hql = "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc";
        List<Object[]> documents = xcontext.getWiki().search(hql, xcontext);

        List<DocumentReference> docsList = new ArrayList<DocumentReference>();

        for (Object[] document : documents) {

            String spaceName = (String) document[0];
            DocumentReference documentReference = new DocumentReference(wikiName, spaceName, (String) document[1]);
            docsList.add(documentReference);
        }

        WikiReference wikiReference = new WikiReference(wikiName);
        indexer.indexDocuments(wikiReference, docsList);

        return docsList.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#indexSpace(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public int indexSpace(SpaceReference reference) throws SearchIndexingException, XWikiException
    {
        logger.info("Indexing space [" + reference.getName() + "]");

        final XWikiContext xcontext = getXWikiContext();

        WikiReference wikiReference = (WikiReference) reference.getParent();

        String currentWikiName = xcontext.getWiki().getName();

        if (!currentWikiName.equalsIgnoreCase(wikiReference.getName())) {
            xcontext.setDatabase(wikiReference.getName());
        }

        String hql =
            "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc where doc.space='"
                + reference.getName() + "'";
        List<Object[]> documents = xcontext.getWiki().search(hql, xcontext);

        List<DocumentReference> docsList = new ArrayList<DocumentReference>();

        for (Object[] document : documents) {

            String spaceName = (String) document[0];
            String language = (String) document[3];

            DocumentReference documentReference =
                new DocumentReference(wikiReference.getName(), spaceName, (String) document[1], language);
            if (documentAccessBridge.exists(documentReference)) {
                docsList.add(documentReference);
            }
        }

        indexer.indexDocuments(reference, docsList);

        return docsList.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#deleteDocumentIndex(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean deleteDocumentIndex(DocumentReference document)
    {
        return indexer.deleteIndex(document);
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
        return indexer.indexDocument(document);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#deleteindexWiki()
     */
    @Override
    public boolean deleteindexWiki(String wiki) throws XWikiException
    {
        try {
            // gets the final index Wiki
            final XWikiContext xcontext = getXWikiContext();
            String wikiName = xcontext.getWiki().getName();
            logger.info("Deleting wiki.." + wikiName);

            if (!wikiName.equalsIgnoreCase(wiki)) {
                xcontext.setDatabase(wikiName);
            }

            String hql = "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc";
            List<Object[]> documents = xcontext.getWiki().search(hql, xcontext);

            List<DocumentReference> docsList = new ArrayList<DocumentReference>();

            for (Object[] document : documents) {

                String spaceName = (String) document[0];
                DocumentReference documentReference = new DocumentReference(wikiName, spaceName, (String) document[1]);
                docsList.add(documentReference);
            }
            WikiReference wikiReference = new WikiReference(wikiName);
            indexer.deleteIndex(wikiReference, docsList);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#indexDocuments(java.util.List)
     */
    @Override
    public int indexDocuments(List<DocumentReference> documents)
    {
        indexer.indexDocuments(documents);
        return documents.size();
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
                indexer.deleteEntireIndex();
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
        int docCount = 0;
        if (getXWikiContext().getWiki().isVirtualMode()) {
            try {
                for (WikiReference wiki : wikis) {
                    // delete the wiki indexes
                    boolean result = deleteindexWiki(wiki.getName());
                    // build index
                    if (result == true)
                        docCount = indexWiki(wiki.getName());
                    docCount = docCount + docCount;
                }
            } catch (Exception e) {
                logger.info("error during rebuildFarmIndex" + e);
            }
        }
        return docCount;
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
            // delete existing index
            boolean result = deleteindexWiki(getXWikiContext().getWiki().getName());
            // build index
            if (result == true)
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
        int docCount = 0;
        return docCount;
    }

    /**
     * @param attachment
     * @param doc
     * @return the boolean true if successfully indexed
     */
    public boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        return indexer.indexAttachment(attachment, doc);
    }

    public boolean deleteIndexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        return indexer.deleteIndexAttachment(attachment, doc);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        try {
            if (event instanceof DocumentUpdatedEvent || event instanceof DocumentCreatedEvent) {
                indexDocument(((DocumentModelBridge) source).getDocumentReference());
            } else if (event instanceof DocumentDeletedEvent) {
                deleteDocumentIndex(((DocumentModelBridge) source).getDocumentReference());
            }

            else if (event instanceof AttachmentUpdatedEvent || event instanceof AttachmentAddedEvent) {

                AttachmentReference attachref =
                    new AttachmentReference(((AbstractAttachmentEvent) event).getName(),
                        ((DocumentModelBridge) source).getDocumentReference());
                indexAttachment(attachref, (DocumentModelBridge) source);

            } else if (event instanceof AttachmentDeletedEvent) {

                AttachmentReference attachref =
                    new AttachmentReference(((AbstractAttachmentEvent) event).getName(),
                        ((DocumentModelBridge) source).getDocumentReference());
                deleteIndexAttachment(attachref, (DocumentModelBridge) source);

            } else if (event instanceof WikiDeletedEvent) {

            }
        } catch (Exception e) {
            logger.error("error in notify", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#getStatus()
     */
    @Override
    public Map<String, DocumentIndexerStatus> getStatus()
    {
        return indexer.getStatus();
    }

    @Override
    public String getStatusAsJson()
    {
        Gson gson = new Gson();
        return gson.toJson(indexer.getStatus());
    }

    public VelocityUtils getVelocityUtils()
    {
        CoreContainer container = (CoreContainer) searchEngine.getCoreContainer();
        SolrServer server = (SolrServer) searchEngine.getSearchEngine();
        return new VelocityUtils(container, server);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#search(org.xwiki.platform.search.SearchRequest)
     */
    @Override
    public SearchResponse search(SearchRequest request)
    {
        SearchResponse response = null;
        if (request != null) {
            // SolrQuery
            SolrServer solrserver;
            QueryResponse queryResponse;
            SolrjSearchResponse searchResponse;
            try {
                StringBuilder queryString = new StringBuilder();
                queryString.append(request.getQueryString());

                if (request.getFilterParametersMap() != null) {
                    for (Entry<String, String> entry : request.getFilterParametersMap().entrySet()) {
                        queryString.append(" " + entry.getKey() + ":" + entry.getValue());
                    }
                }

                String queryPostProcessing = request.processRequestQuery(queryString.toString());
                logger.info("Query :" + queryPostProcessing);
                SolrQuery solrQuery = new SolrQuery(queryPostProcessing);

                if (request.getSearchParametersMap() != null && request.getSearchParametersMap().size() > 0) {
                    for (Entry<String, String> entry : request.getSearchParametersMap().entrySet()) {
                        if(entry.getKey().equals("qf")){
                            String value=request.processQueryFrequency(entry.getValue());
                            if(!StringUtils.isEmpty(value))       
                                solrQuery.add(entry.getKey(),value);
                        }else{
                            if(!StringUtils.isEmpty(entry.getValue()))
                                solrQuery.add(entry.getKey(), entry.getValue());
                        }
                       
                    }
                    
                    
                }
                solrserver = (SolrServer) searchEngine.getSearchEngine();
                queryResponse = solrserver.query(solrQuery);
                searchResponse = this.componentManager.getInstance(SearchResponse.class, SolrjSearchResponse.HINT);
                searchResponse.processQueryResult(queryResponse);
                logger.info("Returning search response : \n" + searchResponse);
                return searchResponse;

            } catch (Exception e) {
                logger.info("Failed to retrieve the solrserver object");
            }
        }
        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#getSearchRequest()
     */
    @Override
    public SearchRequest getSearchRequest()
    {
        SearchRequest searchRequest = null;
        try {
            searchRequest = this.componentManager.getInstance(SearchRequest.class, SolrjSearchRequest.HINT);
        } catch (ComponentLookupException e) {
            logger.error("Error in SearchRequest component looup with Hint[" + SolrjSearchRequest.HINT + "]");
        }
        return searchRequest;
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
}
