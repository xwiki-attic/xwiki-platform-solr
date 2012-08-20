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
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.platform.search.DocumentField;
import org.xwiki.platform.search.SearchEngine;
import org.xwiki.platform.search.SearchException;
import org.xwiki.platform.search.SearchRequest;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.index.DocumentIndexer;
import org.xwiki.platform.search.index.internal.AbstractDocumentIndexerStatus;
import org.xwiki.platform.search.index.internal.SolrjDocumentIndexer;

import com.google.gson.Gson;

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
     * SearchEngine component.
     */
    @Inject
    @Named(SolrjSearchEngine.HINT)
    private SearchEngine searchEngine;

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
            this.indexer = this.componentManager.getInstance(DocumentIndexer.class, SolrjDocumentIndexer.HINT);
            this.indexer.setSearchEngineObject(searchEngine.getSearchEngine());
            // this.indexWiki();
        } catch (Exception e) {
            logger.error("Failed to index current wiki");
        }
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
     * @see org.xwiki.platform.search.Search#getStatus()
     */
    @Override
    public Map<String, AbstractDocumentIndexerStatus> getStatus()
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

                // Add filter parameters to query.
                String filterParams = this.addFilterParameters(request.getFilterParametersMap());
                queryString.append(filterParams);

                String queryPostProcessing = request.processRequestQuery(queryString.toString());
                logger.info("Query :" + queryPostProcessing);
                SolrQuery solrQuery = this.getSolrQuery(queryPostProcessing, request);

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
     * @param queryPostProcessing query string
     * @param request Search request.
     * @return solr query.
     */
    private SolrQuery getSolrQuery(String queryPostProcessing, SearchRequest request)
    {
        SolrQuery solrQuery = new SolrQuery(queryPostProcessing);
        Map<String, String> searchParametersMap = request.getSearchParametersMap();
        
        // If query doesn't have language, Add a language filter query.
        if (!queryPostProcessing.contains(DocumentField.LANGUAGE)) {
            solrQuery.add("fq", "lang:" + getXWikiContext().getLanguage());
        }


        if (searchParametersMap != null && searchParametersMap.size() > 0) {
            for (Entry<String, String> entry : searchParametersMap.entrySet()) {
                if (entry.getKey().equals("qf") && !StringUtils.isEmpty(entry.getValue())) {
                    String value = request.processQueryFrequency(entry.getValue());
                    solrQuery.add(entry.getKey(), value);
                } else {
                    if (!StringUtils.isEmpty(entry.getValue())) {
                        solrQuery.add(entry.getKey(), entry.getValue());
                    }
                }

            }

        }

        return solrQuery;
    }

    /**
     * @param filterParametersMap filter map to be added to the query string.
     * @return query string with filters.
     */
    private String addFilterParameters(Map<String, String> filterParametersMap)
    {
        StringBuilder builder = new StringBuilder();
        if (filterParametersMap != null) {
            for (Entry<String, String> entry : filterParametersMap.entrySet()) {
                builder.append(" " + entry.getKey() + ":" + entry.getValue());
            }
        }
        return builder.toString();
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
            searchRequest = this.componentManager.getInstance(SearchRequest.class, HINT);
        } catch (ComponentLookupException e) {
            logger.error("Error in SearchRequest component looup with Hint[" + HINT + "]");
        }
        return searchRequest;
    }
}
