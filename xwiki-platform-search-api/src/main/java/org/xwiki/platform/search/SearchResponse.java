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
/**
 * @version $Id$
 */
@Role
public interface SearchResponse
{
    /**
     * @param beginIndex start index of search results set.
     * @param items number of results to return.
     * @return the index of the last displayed search result.
     */
    int getEndIndex(int beginIndex, int items);

    /**
     * Max score of the search results.
     *
     * @return maximum score of the search results.
     */
    float getMaxScore();

    /**
     * Total search results.
     *
     * @return total number of search results.
     */
    long getTotalNumber();

    /**
     * @param beginIndex start index of search results set.
     * @param items number of results to return.
     * @return the value to be used for the firstIndex URL parameter to build a link pointing to the next page of
     *         results.
     */
    int getNextIndex(int beginIndex, int items);

    /**
     * @param beginIndex start index of search results set.
     * @param items number of results to return.
     * @return the value to be used for the firstIndex URL parameter to build a link pointing to the previous page of
     *         results.
     */
    int getPreviousIndex(int beginIndex, int items);


    /**
     * Returns a list of search results. According to beginIndex and endIndex, only a subset of the results is returned.
     * To get the first ten results, one would use beginIndex=1 and items=10.
     *
     * @param beginIndex start index of search results set.
     * @param items number of results to return
     * @return List of SearchResult instances starting at beginIndex and containing up to items elements.
     */
    List<SearchResult> getResults(int beginIndex, int items);

    /**
     * This method returns all the relevant results.
     *
     * @return A list containing the searchResult instances. The searchResult class contains the index fields as member
     *         variables and the get methods to access them.
     */
    List<SearchResult> getTotalResults();

    /**
     * @param beginIndex start index of search results set.
     * @param items number of results to return.
     * @return true when there are more results than currently displayed.
     */
    boolean hasNext(int beginIndex, int items);

    /**
     * @param beginIndex start index of search results set.
     * @return true when there is a page before the one currently displayed, that is, when <code>beginIndex > 1</code>.
     */
    boolean hasPrevious(int beginIndex);

    /**
     * @param queryResponse 
     * 
     */
    void processQueryResult(Object queryResponse);

}
