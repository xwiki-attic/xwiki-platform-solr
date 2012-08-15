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
    /**
     * solrJ HINT.
     */
    public static final String HINT = "solrj";

    /**
     * Document Indexer for solrj.
     */
    @Inject
    @Named(SolrjDocumentIndexer.HINT)
    private DocumentIndexer indexer;
    
    /**
     * SearchEngine component.
     */
    @Inject
    @Named(SolrjSearchEngine.HINT)
    private SearchEngine searchEngine;
    
    /**
     *  ComponentManager component.
     */
    @Inject
    private ComponentManager componentManager;
    
    /**
     * DocumentAccessBridge component.
     */
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
     * @param attachment reference to the attachment.
     * @param doc  DocumentModelBridge.
     * @return the boolean true if successfully indexed
     */
    public boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        return indexer.indexAttachment(attachment, doc);
    }
    
    /**
     * 
     * @param attachment attachment reference.
     * @param doc DocumentModelBridge.
     * @return boolean if Index of the attachment is deleted.
     */
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
                buildDocumentIndex(((DocumentModelBridge) source).getDocumentReference());
            } else if (event instanceof DocumentDeletedEvent) {
                deleteDocumentIndex(((DocumentModelBridge) source).getDocumentReference());
            } else if (event instanceof AttachmentUpdatedEvent || event instanceof AttachmentAddedEvent) {
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
              //TO DO
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
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#getVelocityUtils()
     */
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
                        if (entry.getKey().equals("qf")) {
                            String value = request.processQueryFrequency(entry.getValue());
                            if (!StringUtils.isEmpty(value)) {
                                solrQuery.add(entry.getKey(), value);
                            }
                        } else {
                            if (!StringUtils.isEmpty(entry.getValue())) {
                                solrQuery.add(entry.getKey(), entry.getValue());
                            }
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
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#buildDocumentIndex(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean buildDocumentIndex(DocumentReference document)
    {
        return indexer.indexDocument(document);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#buildDocumentIndex(java.util.List)
     */
    @Override
    public int buildDocumentIndex(List<DocumentReference> documents)
    {
        indexer.indexDocuments(documents);
        return documents.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#buildWikiIndex(org.xwiki.model.reference.WikiReference)
     */
    @Override
    public int buildWikiIndex(WikiReference wikiReference) throws SearchIndexingException, XWikiException
    {
        String wikiName = wikiReference.getName();

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

        indexer.indexDocuments(wikiReference, docsList);

        return docsList.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#buildWikiSpaceIndex(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public int buildWikiSpaceIndex(SpaceReference spaceReference) throws SearchIndexingException, XWikiException
    {
        logger.info("Indexing space [" + spaceReference.getName() + "]");

        final XWikiContext xcontext = getXWikiContext();

        WikiReference wikiReference = (WikiReference) spaceReference.getParent();

        String currentWikiName = xcontext.getWiki().getName();

        if (!currentWikiName.equalsIgnoreCase(wikiReference.getName())) {
            xcontext.setDatabase(wikiReference.getName());
        }

        String hql =
            "select doc.space, doc.name, doc.version, doc.language from XWikiDocument as doc where doc.space='"
                + spaceReference.getName() + "'";
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

        indexer.indexDocuments(spaceReference, docsList);

        return docsList.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#deleteWikiIndex(org.xwiki.model.reference.WikiReference)
     */
    @Override
    public boolean deleteWikiIndex(WikiReference wikiReference) throws SearchIndexingException, XWikiException
    {
        try {
            indexer.deleteIndex(wikiReference, null);
            return true;
        } catch (Exception e) {
            logger.error("Deleting the wiki index for the Wiki[" + wikiReference.getName() + "]");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#deleteSpaceIndex(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public boolean deleteSpaceIndex(SpaceReference spaceReference) throws SearchIndexingException, XWikiException
    {
        logger.info("Deleting Space Index for " + spaceReference.getName());
        try {
            indexer.deleteIndex(spaceReference, null);
            return true;
        } catch (Exception e) {
            logger.error("Deleting the space index for the Space[" + spaceReference.getName() + "]");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#reBuildFarmIndex()
     */
    @Override
    public int reBuildFarmIndex()
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#reBuildFarmIndex(java.util.List)
     */
    @Override
    public int reBuildFarmIndex(List<WikiReference> wikis)
    {

        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#reBuildWikiIndex(org.xwiki.model.reference.WikiReference)
     */
    @Override
    public int reBuildWikiIndex(WikiReference wikiReference)
    {

        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.Search#reBuildSpaceIndex(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public int reBuildSpaceIndex(SpaceReference spaceReference)
    {
        return 0;
    }

    @Override
    public boolean deleteEntireIndex() throws SearchIndexingException, XWikiException
    {
        return this.indexer.deleteEntireIndex();
    }
}
