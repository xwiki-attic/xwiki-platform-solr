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

import static org.xwiki.platform.search.DocumentField.FULLTEXT;
import static org.xwiki.platform.search.DocumentField.ID;
import static org.xwiki.platform.search.DocumentField.LANGUAGE;
import static org.xwiki.platform.search.DocumentField.NAME;
import static org.xwiki.platform.search.DocumentField.SCORE;
import static org.xwiki.platform.search.DocumentField.SPACE;
import static org.xwiki.platform.search.DocumentField.TITLE;
import static org.xwiki.platform.search.DocumentField.WIKI;
import static org.xwiki.platform.search.DocumentField.TYPE;
import static org.xwiki.platform.search.DocumentField.FILENAME;
import static org.xwiki.platform.search.DocumentField.DOC_REFERENCE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.SearchResult;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.XWikiStubContextProvider;

/**
 * SearchResponse implementation.
 *
 * @version $Id$
 */
@Component
@Named(SolrjSearchResponse.HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class SolrjSearchResponse implements SearchResponse
{

    @Inject
    private Logger logger;
    
    @Inject
    protected Execution execution;
    
    @Inject
    protected XWikiStubContextProvider contextProvider;

    public static final String HINT = "solrsearchresponse";

    private List<String> languages;

    private EntityReference entityReference;

    /**
     * declare a QueryResponse object
     */
    private QueryResponse queryResponse;

    private SolrDocumentList solrDocumentList;

    private List<SearchResult> searchResults;

    Map<String, Map<String, List<String>>> highlightingMap = null;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getEndIndex(int, int)
     */
    public int getEndIndex(int beginIndex, int items)
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getMaxScore()
     */
    public float getMaxScore()
    {
        return this.solrDocumentList.getMaxScore();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getNextIndex(int, int)
     */
    public int getNextIndex(int beginIndex, int items)
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getPreviousIndex(int, int)
     */
    public int getPreviousIndex(int beginIndex, int items)
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getResults(int, int)
     */
    public List<SearchResult> getResults(int beginIndex, int items)
    {
        List<SearchResult> searchResultsSubList = this.searchResults.subList(beginIndex, beginIndex + items);
        return searchResultsSubList;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getTotalResults()
     */
    public List<SearchResult> getTotalResults()
    {
        return this.searchResults;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#hasNext(int, int)
     */
    public boolean hasNext(int beginIndex, int items)
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#hasPrevious(int)
     */
    public boolean hasPrevious(int beginIndex)
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.platform.search.SearchResponse#getTotalNumber()
     */
    @Override
    public long getTotalNumber()
    {
        return this.solrDocumentList.getNumFound();
    }

    /**
     * @return the queryResponse
     */
    public QueryResponse getQueryResponse()
    {
        return queryResponse;
    }

    private String cleanUp(String string)
    {
        if (string.startsWith("[")) {
            string = string.substring(1);
        }

        if (string.endsWith("]")) {
            string = string.substring(0, string.length() - 1);
        }

        return string;
    }

    private String getStringValue(Object object)
    {
        String string = "";
        if (object instanceof List) {
            List list = (List) object;
            if (!list.isEmpty()) {
                string = (String) list.get(0);
            }
        } else if (object instanceof String) {
            string = (String) object;
        }

        return string;
    }

    private SearchResult getSearchResult(SolrDocument solrDoc)
    {
        try {
            
            String language = (String) solrDoc.getFieldValue(LANGUAGE);
            
            if (language == null || language.trim().equals("")) {
                language = "en";
            }

            String id = getStringValue(solrDoc.getFieldValue(ID));
           
            String wikiName = getStringValue(solrDoc.getFieldValue(WIKI + "_" + language));           
            String spaceName = getStringValue(solrDoc.getFieldValue(SPACE + "_" + language));           
            String pageName = getStringValue(solrDoc.getFieldValue(NAME + "_" + language));          
            String fulltext = getStringValue(solrDoc.getFieldValue(FULLTEXT + "_" + language));           
            float score = (Float) solrDoc.getFieldValue(SCORE);           
            SearchResult searchResult = new SearchResult(id, wikiName, spaceName, pageName,language);
            searchResult.setScore(score);   
            searchResult.setContent(fulltext);
                       
            if(getStringValue(solrDoc.getFieldValue(TYPE)).equals("DOCUMENT"))
            {   
                
                String title = getStringValue(solrDoc.getFieldValue(TITLE + "_" + language));
                searchResult.setTitle(title);
                if (this.highlightingMap != null) {
                    Map<String, List<String>> docMap = this.highlightingMap.get(id);
                    if (docMap != null && docMap.containsKey(FULLTEXT + "_" + language)) {
                        fulltext = cleanUp(docMap.get(FULLTEXT + "_" + language).toString());
                    }

                    if (docMap != null && docMap.containsKey(TITLE + "_" + language)) {
                        title = cleanUp(docMap.get(TITLE + "_" + language).toString());
                    }
                }                
            }
           
            if(getStringValue(solrDoc.getFieldValue(TYPE)).equals("ATTACHMENT"))
            {
                
                String filename = getStringValue(solrDoc.getFieldValue(FILENAME +"_" + language));
                XWikiContext context = getXWikiContext();
                String fullname= new StringBuffer(wikiName).append(":").append(spaceName).append(".").append(pageName).toString();
                String url=context.getWiki().getAttachmentURL(fullname, filename, context);
                url="http://localhost:8080"+url;
                searchResult.setURL(url);
            }

           
           
            
            DocumentReference docref = new DocumentReference(wikiName, spaceName, pageName);
            
            // checks if the user has access to view the page.
            if ((documentAccessBridge.exists(docref)) && (documentAccessBridge.isDocumentViewable(docref))) {
                return searchResult;
            }
            
            
           

        } catch (Exception ex) {
           logger.info("error while retieving search result");
        }

        return null;
    }

    /**
     * @param queryResponse
     * @param languages
     * @param entityReference
     */
    public void processQueryResult(QueryResponse queryResponse, List<String> languages, EntityReference entityReference)
    {
        this.queryResponse = queryResponse;
        this.languages = languages;
        this.entityReference = entityReference;

        this.solrDocumentList = queryResponse.getResults();
        this.highlightingMap = queryResponse.getHighlighting();

        this.searchResults = new ArrayList<SearchResult>();

        if (solrDocumentList != null) {
            for (SolrDocument solrDoc : solrDocumentList) {
                SearchResult searchResult = getSearchResult(solrDoc);
                //&& !isDocumentInParentEntity(searchResult.getReference(), entityReference)
                //&& languages.contains(searchResult.getLanguage())
                if (searchResult != null ) {
                    this.searchResults.add(searchResult);
                }
            }
        }
    }

    private boolean isDocumentInParentEntity(DocumentReference docRef, EntityReference parentRef)
    {
        EntityReference reference = docRef.extractReference(parentRef.getType());
        return parentRef.equals(reference);
    }
    
    public XWikiContext getXWikiContext()
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        if (context == null) {

            context = this.contextProvider.createStubContext();
            logger.info(context.toString());
            getExecutionContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
        }
        return context;
    }
    
    private ExecutionContext getExecutionContext()
    {
        return this.execution.getContext();
    }
}
