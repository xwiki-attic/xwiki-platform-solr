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
package org.xwiki.platform.search;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiException;

/**
 * XWiki Search API.
 *
 * @version $Id$
 */
@Role
public interface Search
{
    /**
     * Delete the index for document.
     *
     * @param document Document reference to which index needs to be deleted.
     * @return true if document index deletion is successful.
     */
    boolean deleteDocumentIndex(DocumentReference document);

    /**
     * Object with indexing process metadata.
     *
     * @return IndexProcess object for the active indexing process.
     */
    IndexerProcess getCurrentIndexerProcess();

    /**
     * Gets the backend implementation.It could be embedded Solr, remote Solr, internal Lucene, distributed Lucene.
     *
     * @return the implementation.
     */
    String getImplementation();

    /**
     * Index the document.
     *
     * @param document reference to the document to be indexed.
     * @return true if document indexing is successful.
     */
    boolean indexDocument(DocumentReference document);

    /**
     * Index the documents.
     *
     * @param documents List of documents to be indexed.
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    int indexDocuments(List<DocumentReference> documents);

    /**
     * Build the index for the current wiki.
     *
     * @return number of documents to index.
     * @throws SearchIndexingException Exception thrown in case of indexing errors.
     * @throws XWikiException
     */
    int indexWiki() throws SearchIndexingException, XWikiException;

    /**
     * Build the index for the current wiki farm.
     *
     * @return number of documents to index.
     * @throws SearchIndexingException
     * @throws XWikiException
     */
    int indexWikiFarm() throws SearchIndexingException, XWikiException;

    /**
     * To initialize and load configuration for the search component.
     *
     * @throws SearchException in case of error while initializing the component.
     */
    void initialize() throws SearchException;

    /**
     * Parses the query entered by the user into the Solrquery object.
     *
     * @return the solrQuery object.
     */
    SolrQuery parseQuery();

    /**
     * Searches the configured Indexes using the specified solrquery for documents in the given languages belonging to
     * one of the given virtual wikis.Searches the configured Indexes using the specified query for documents in the
     * given languages.With virtual wikis enabled in your xwiki installation this will deliver results from all virtual
     * wikis.
     *
     * @return the search response.
     */

    /**
     * Rebuilds the index for the whole wiki farm.
     *
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    int rebuildFarmIndex();

    /**
     * Rebuilds the index for the given wiki's in a wiki farm.
     *
     * @param wikis List of wikis to be indexed.
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    int rebuildFarmIndex(List<WikiReference> wikis);

    /**
     * Rebuilds the index for the current wiki.
     *
     * @return the Number of documents scheduled for indexing. -1 in case of errors
     */
    int rebuildWikiIndex();

    /**
     * Rebuilds the index for given spaces in the current wiki.
     *
     * @param spaces List of spaces to be indexed
     * @return the Number of documents scheduled for indexing. -1 in case of errors
     */
    int rebuildWikiIndex(List<SpaceReference> spaces);

    /**
     * Search for the query in the current wiki for all the languages.
     *
     * @param query to be searched.
     * @return SearchResponse to the query searched.
     */
    SearchResponse search(String query);

    /**
     * Search for the query in the current wiki for given languages.
     *
     * @param query to be searched.
     * @param languages to be searched.
     * @return SearchResponse to the query searched.
     */
    SearchResponse search(String query, List<String> languages);

    /**
     * Search for the query in the current wiki for given languages in a wiki.
     *
     * @param query to be searched.
     * @param languages Wiki reference to be searched for the query.
     * @param wikiReference Space reference to be searched for the query.
     * @return SearchResponse to the query searched.
     */
    SearchResponse search(String query, List<String> languages, WikiReference wikiReference);

    /**
     * Search for the query in the current wiki for given languages in a wiki and space.
     *
     * @param query to be searched.
     * @param languages List of document languages to search
     * @param wiki Wiki reference to be searched for the query.
     * @param space Space reference to be searched for the query.
     * @return SearchResponse to the query searched.
     */
    SearchResponse search(String query, List<String> languages, WikiReference wiki, SpaceReference space);

}
