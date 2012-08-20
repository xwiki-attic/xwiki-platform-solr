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

import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.platform.search.index.internal.AbstractDocumentIndexerStatus;

/**
 * XWiki Search API.
 * 
 * @version $Id$
 */
@Role
public interface Search
{

    /**
     * Gets the backend implementation.It could be embedded Solr, remote Solr, internal Lucene, distributed Lucene.
     * 
     * @return the implementation.
     */
    String getImplementation();

    /**
     * To initialize and load configuration for the search component.
     * 
     * @throws SearchException in case of error while initializing the component.
     */
    void initialize() throws SearchException;

    /**
     * Search for the query in the current wiki for all the languages.
     * 
     * @param query to be searched.
     * @return SearchResponse to the query searched.
     */
    SearchResponse search(String query);

    /**
     * Search for the query in the current wiki for all the languages.
     * 
     * @param request as a SearchRequest object.
     * @return SearchResponse to the query searched.
     */
    SearchResponse search(SearchRequest request);

    /**
     * @return map containing the Indexer Sattus.
     */
    Map<String, AbstractDocumentIndexerStatus> getStatus();

    /**
     * @return String.
     */
    String getStatusAsJson();

    /**
     * @return Object.
     */
    Object getVelocityUtils();

    /**
     * Search request implementation for given hint.
     * 
     * @return search request object.
     */
    SearchRequest getSearchRequest();

}
