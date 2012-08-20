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

import static org.xwiki.platform.search.DocumentField.ATTACHMENT_CONTENT;
import static org.xwiki.platform.search.DocumentField.DOCUMENT_CONTENT;
import static org.xwiki.platform.search.DocumentField.FILENAME;
import static org.xwiki.platform.search.DocumentField.ID;
import static org.xwiki.platform.search.DocumentField.LANGUAGE;
import static org.xwiki.platform.search.DocumentField.MIME_TYPE;
import static org.xwiki.platform.search.DocumentField.NAME;
import static org.xwiki.platform.search.DocumentField.OBJECT;
import static org.xwiki.platform.search.DocumentField.OBJECT_CONTENT;
import static org.xwiki.platform.search.DocumentField.PROPERTY_NAME;
import static org.xwiki.platform.search.DocumentField.SCORE;
import static org.xwiki.platform.search.DocumentField.SPACE;
import static org.xwiki.platform.search.DocumentField.TITLE;
import static org.xwiki.platform.search.DocumentField.TYPE;
import static org.xwiki.platform.search.DocumentField.WIKI;

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
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.SearchResult;

/**
 * SearchResponse implementation.
 * 
 * @version $Id$
 */
@Component
@Named(SolrjSearchResponse.HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class SolrjSearchResponse extends DocumentHelper implements SearchResponse
{
    /**
     * Solrj search response HINT.
     */
    public static final String HINT = "solrsearchresponse";

    /**
     * Underscore.
     */
    private static final String USCORE = "_";

    /**
     * Logger component.
     */
    @Inject
    private Logger logger;

    /**
     * declare a QueryResponse object.
     */
    private QueryResponse queryResponse;

    /**
     * SolrDocumentList.
     */
    private SolrDocumentList solrDocumentList;

    /**
     * List of Search Results.
     */
    private List<SearchResult> searchResults;

    /**
     * highlighted text.
     */
    private Map<String, Map<String, List<String>>> highlightingMap;

    /**
     * DocumentAccessBridge component.
     */
    @Inject
    private DocumentAccessBridge documentAccessBridge;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResponse#getEndIndex(int, int)
     */
    public int getEndIndex(int beginIndex, int items)
    {

        int retval = beginIndex + items - 1;
        final int resultcount = (int) getTotalNumber();
        if (retval > resultcount) {
            return resultcount;
        }

        return retval;
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
        final int itemCount = items;
        final int resultcount = (int) getTotalNumber();
        int retval = beginIndex + itemCount;

        return retval > resultcount ? (resultcount - itemCount + 1) : retval;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResponse#getPreviousIndex(int, int)
     */
    public int getPreviousIndex(int beginIndex, int items)
    {
        int retval = beginIndex - items;

        return 0 < retval ? retval : 1;
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
        final int itemCount = items;
        final int begin = beginIndex;

        return begin + itemCount - 1 < getTotalNumber();

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResponse#hasPrevious(int)
     */
    public boolean hasPrevious(int beginIndex)
    {
        return beginIndex > 1;
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

    /**
     * @param string .
     * @return String.
     */
    private String cleanUp(String string)
    {
        String retString = string;
        if (string.startsWith("[")) {
            retString = string.substring(1);
        }

        if (string.endsWith("]")) {
            retString = string.substring(0, string.length() - 1);
        }

        return retString;
    }

    /**
     * @param object .
     * @return value of the Object.
     */
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

    /**
     * @param solrDoc Solr Dcument.
     * @return SearchResult Results of search.
     */
    private SearchResult getSearchResult(SolrDocument solrDoc)
    {
        SearchResult searchResult = null;

        try {

            String language = (String) solrDoc.getFieldValue(LANGUAGE);
            String type = (String) solrDoc.getFieldValue(TYPE);

            String id = getStringValue(solrDoc.getFieldValue(ID));
            String wikiName = getStringValue(solrDoc.getFieldValue(WIKI));
            String spaceName = getStringValue(solrDoc.getFieldValue(SPACE));
            String pageName = getStringValue(solrDoc.getFieldValue(NAME + USCORE + language));
            DocumentReference docref = new DocumentReference(wikiName, spaceName, pageName, language);

            searchResult = new SearchResult(id, wikiName, spaceName, pageName, language);
            searchResult.setType(type);
            Map<String, List<String>> docMap = this.highlightingMap.get(id);

            if ("DOCUMENT".equals(type)) {
                this.processDocumentResponse(searchResult, docMap, solrDoc, language);
            } else if ("ATTACHMENT".equals(type)) {
                this.processAttachmentResponse(searchResult, docMap, solrDoc, language, docref);

            } else if ("OBJECT".equals(type)) {
                // Hightlight text/content
                if (docMap != null && docMap.containsKey(OBJECT_CONTENT + USCORE + language)) {
                    searchResult.setHighlightText(cleanUp(docMap.get(OBJECT_CONTENT + USCORE + language).toString()));
                }
                searchResult.setObjectName(getStringValue(solrDoc.getFieldValue(OBJECT)));

            } else if ("PROPERTY".equals(type)) {
                String propertyName = getStringValue(solrDoc.getFieldValue(PROPERTY_NAME));
                searchResult.setPropertyName(propertyName);
                searchResult.setPropertyValue(getStringValue(solrDoc.getFieldValue(propertyName)));
            }

            float score = (Float) solrDoc.getFieldValue(SCORE);
            searchResult.setScore(score);

            // checks if the user has access to view the page.
            if ((documentAccessBridge.exists(docref)) && (documentAccessBridge.isDocumentViewable(docref))) {
                return searchResult;
            }

        } catch (Exception ex) {
            logger.info("Error while retieving search result" + ex.getMessage(), ex);
        }

        return null;
    }

    /**
     * @param searchResult SearchResult.
     * @param docMap highlight map.
     * @param solrDoc solr document.
     * @param language of the document.
     * @param docref reference to the parent.
     */
    private void processAttachmentResponse(SearchResult searchResult, Map<String, List<String>> docMap,
        SolrDocument solrDoc, String language, DocumentReference docref)
    {
        // Hightlight text/content
        if (docMap != null && docMap.containsKey(ATTACHMENT_CONTENT + USCORE + language)) {
            searchResult.setHighlightText(cleanUp(docMap.get(ATTACHMENT_CONTENT + USCORE + language).toString()));
        }

        String fileName = getStringValue(solrDoc.getFieldValue(FILENAME + USCORE + language));
        searchResult.setFileName(fileName);
        AttachmentReference attachmentReference = new AttachmentReference(fileName, docref);
        String url = documentAccessBridge.getAttachmentURL(attachmentReference, true);
        searchResult.setURL(url);
        searchResult.setMimeType(getStringValue(solrDoc.getFieldValue(MIME_TYPE)));

    }

    /**
     * @param searchResult SearchResult.
     * @param docMap highlight map.
     * @param solrDoc solr document.
     * @param language of the document.
     */
    private void processDocumentResponse(SearchResult searchResult, Map<String, List<String>> docMap,
        SolrDocument solrDoc, String language)
    {
        // Hightlight text/content
        if (docMap != null && docMap.containsKey(DOCUMENT_CONTENT + USCORE + language)) {
            searchResult.setHighlightText(cleanUp(docMap.get(DOCUMENT_CONTENT + USCORE + language).toString()));
        }

        if (docMap != null && docMap.containsKey(TITLE + USCORE + language)) {
            searchResult.setTitle((docMap.get(TITLE + USCORE + language).toString()));
        } else {
            searchResult.setTitle(getStringValue(solrDoc.getFieldValue(TITLE + USCORE + language)));
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResponse#processQueryResult(java.lang.Object)
     */
    @Override
    public void processQueryResult(Object queryResponse)
    {
        if (queryResponse instanceof QueryResponse) {
            this.queryResponse = (QueryResponse) queryResponse;
        }

        this.solrDocumentList = this.queryResponse.getResults();
        this.highlightingMap = this.queryResponse.getHighlighting();

        this.searchResults = new ArrayList<SearchResult>();

        if (solrDocumentList != null) {
            for (SolrDocument solrDoc : solrDocumentList) {
                SearchResult searchResult = this.getSearchResult(solrDoc);
                // && !isDocumentInParentEntity(searchResult.getReference(), entityReference)
                // && languages.contains(searchResult.getLanguage())
                if (searchResult != null) {
                    this.searchResults.add(searchResult);
                }
            }
        }
    }
}
