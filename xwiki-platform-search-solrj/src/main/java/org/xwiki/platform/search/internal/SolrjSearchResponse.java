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

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.xwiki.platform.search.SearchResponse;
import org.xwiki.platform.search.SearchResult;
import org.xwiki.platform.search.IndexFields;
import org.slf4j.Logger;
import javax.inject.Inject;

/**
 * SearchResponse implementation.
 *
 * @version $Id$
 */
public class SolrjSearchResponse implements SearchResponse
{   
    /**
     * declare a QueryResponse object
     */
    QueryResponse response;
    
    @Inject
    private Logger logger;
    
    public SolrjSearchResponse(QueryResponse res){
        response = res;
    }
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResponse#getEndIndex(int, int)
     */
    public int getEndIndex(int beginIndex, int items)
    {
        return 0;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResponse#getHitcount()
     */
   public int getHitcount()
   {
       return 0;
   }
   
   /**
    * 
    * {@inheritDoc}
    * 
    * @see org.xwiki.platform.search.SearchResponse#getNextIndex(int, int)
    */
   public int getNextIndex(int beginIndex, int items)
   {
       return 0;
   }
   
   /**
    * 
    * {@inheritDoc}
    * 
    * @see org.xwiki.platform.search.SearchResponse#getPreviousIndex(int, int)
    */
   public int getPreviousIndex(int beginIndex, int items)
   {
       return 0;
   }
   
   /**
    * 
    * {@inheritDoc}
    * 
    * @see org.xwiki.platform.search.SearchResponse#getRelevantResult()
    */
   public List<SearchResult> getRelevantResult()
   {
       return null;
   }
   
   /**
    * 
    * {@inheritDoc}
    * 
    * @see org.xwiki.platform.search.SearchResponse#getResults(int, int)
    */
  public List<SearchResult> getResults(int beginIndex, int items)
   {
       return null;
   }
  
  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.xwiki.platform.search.SearchResponse#getTotalResults()
   */
  public List<SearchResult> getTotalResults()
  {
      ArrayList<SearchResult> sresult = new ArrayList<SearchResult>();
      
      for (SolrDocument d : response.getResults()) {
          
          sresult.add(new SolrjSearchResult(d));       
      }
      return sresult;
      
  }
  
  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.xwiki.platform.search.SearchResponse#hasNext(int, int)
   */
  public boolean hasNext(int beginIndex, int items)
  {
     return false; 
  }
  
  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.xwiki.platform.search.SearchResponse#hasPrevious(int)
   */
 public boolean hasPrevious(int beginIndex)
  {
      return false;
  }
 
}
