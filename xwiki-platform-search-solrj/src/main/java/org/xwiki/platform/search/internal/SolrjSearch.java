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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;
import org.xwiki.platform.search.SearchEngine;
import org.xwiki.platform.search.SearchException;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.DocumentIndexerStatus;
import org.xwiki.platform.search.index.internal.SolrjDocumentIndexer;

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

        indexer.indexDocuments(docsList);

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
    public boolean deleteindexWiki()
    {
        return indexer.deleteEntireIndex();
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
                // solrServer.deleteByQuery("*:*");
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
     * @see org.xwiki.platform.search.Search#search(java.lang.String, java.util.List,
     *      org.xwiki.model.reference.WikiReference)
     */
    @Override
    public SearchResponse search(String query, List<String> languages, EntityReference entityReference,
        Map<String,String> searchParameters)
    {
        // SolrQuery
        SolrServer solrserver;
        QueryResponse queryResponse;
        SolrjSearchResponse searchResponse;
        try {
            SolrQuery solrQuery = new SolrQuery(query);
            if (searchParameters != null && searchParameters.size() > 0) {
                for (Entry<String, String> entry : searchParameters.entrySet()) {
                    solrQuery.add(entry.getKey(), entry.getValue());
                }
            }
            solrserver = (SolrServer) searchEngine.getSearchEngine();
            queryResponse = solrserver.query(solrQuery);
            searchResponse = this.componentManager.getInstance(SearchResponse.class, SolrjSearchResponse.HINT);
            searchResponse.processQueryResult(queryResponse,languages,entityReference);
	    return searchResponse;

        } catch (Exception e) {
            logger.info("Failed to retrieve the solrserver object");
        }

        return null;

    }
    
    
    /**
     * 
     * @param attachment
     * @param doc
     * @return the boolean true if successfully indexed
     */
    public boolean indexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
    {
        return indexer.indexAttachment(attachment, doc);
    }
    
    public  boolean deleteIndexAttachment(AttachmentReference attachment, DocumentModelBridge doc)
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
                
             AttachmentReference attachref=new AttachmentReference(((AbstractAttachmentEvent)
                 event).getName(), ((DocumentModelBridge)source).getDocumentReference());
             indexAttachment(attachref, (DocumentModelBridge)source);

            } else if (event instanceof AttachmentDeletedEvent) {
                  
                AttachmentReference attachref=new AttachmentReference(((AbstractAttachmentEvent)
                    event).getName(), ((DocumentModelBridge)source).getDocumentReference());
                deleteIndexAttachment(attachref,(DocumentModelBridge)source); 

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

}
