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
import static org.xwiki.platform.search.DocumentField.NAME;
import static org.xwiki.platform.search.DocumentField.OBJECT;
import static org.xwiki.platform.search.DocumentField.OBJECT_CONTENT;
import static org.xwiki.platform.search.DocumentField.SCORE;
import static org.xwiki.platform.search.DocumentField.SPACE;
import static org.xwiki.platform.search.DocumentField.TITLE;
import static org.xwiki.platform.search.DocumentField.TYPE;
import static org.xwiki.platform.search.DocumentField.WIKI;
import static org.xwiki.platform.search.DocumentField.PROPERTY_NAME;
import static org.xwiki.platform.search.DocumentField.MIME_TYPE;

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
import org.xwiki.model.reference.AttachmentReference;
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
        SearchResult searchResult = null;

        try {

            String language = (String) solrDoc.getFieldValue(LANGUAGE);
            String type = (String) solrDoc.getFieldValue(TYPE);

            String id = getStringValue(solrDoc.getFieldValue(ID));
            String wikiName = getStringValue(solrDoc.getFieldValue(WIKI));
            String spaceName = getStringValue(solrDoc.getFieldValue(SPACE));
            String pageName = getStringValue(solrDoc.getFieldValue(NAME + "_" + language));
            DocumentReference docref = new DocumentReference(wikiName, spaceName, pageName, language);

            searchResult = new SearchResult(id, wikiName, spaceName, pageName, language);
            searchResult.setType(type);
            Map<String, List<String>> docMap = this.highlightingMap.get(id);

            if ("DOCUMENT".equals(type)) {
                // Hightlight text/content
                if (docMap != null && docMap.containsKey(DOCUMENT_CONTENT + "_" + language)) {
                    searchResult.setHighlightText(cleanUp(docMap.get(DOCUMENT_CONTENT + "_" + language).toString()));
                }

                if (docMap != null && docMap.containsKey(TITLE + "_" + language)) {
                    searchResult.setTitle((docMap.get(TITLE + "_" + language).toString()));
                } else {
                    searchResult.setTitle(getStringValue(solrDoc.getFieldValue(TITLE + "_" + language)));
                }
            } else if ("ATTACHMENT".equals(type)) {
                // Hightlight text/content
                if (docMap != null && docMap.containsKey(ATTACHMENT_CONTENT + "_" + language)) {
                    searchResult.setHighlightText(cleanUp(docMap.get(ATTACHMENT_CONTENT + "_" + language).toString()));
                }

                String fileName = getStringValue(solrDoc.getFieldValue(FILENAME + "_" + language));
                searchResult.setFileName(fileName);
                AttachmentReference attachmentReference = new AttachmentReference(fileName, docref);
                String url = documentAccessBridge.getAttachmentURL(attachmentReference, true);
                searchResult.setURL(url);
                searchResult.setMimeType(getStringValue(solrDoc.getFieldValue(MIME_TYPE)));

            } else if ("OBJECT".equals(type)) {
                // Hightlight text/content
                if (docMap != null && docMap.containsKey(OBJECT_CONTENT + "_" + language)) {
                    searchResult.setHighlightText(cleanUp(docMap.get(OBJECT_CONTENT + "_" + language).toString()));
                }
                searchResult.setObjectName(getStringValue(solrDoc.getFieldValue(OBJECT)));

            } else if ("PROPERTY".equals(type)) {
                String propertyName=getStringValue(solrDoc.getFieldValue(PROPERTY_NAME));
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
