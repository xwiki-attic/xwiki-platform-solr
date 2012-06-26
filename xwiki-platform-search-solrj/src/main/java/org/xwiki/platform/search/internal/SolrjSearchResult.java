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

import javax.inject.Inject;

import org.apache.solr.common.SolrDocument;
import org.xwiki.platform.search.IndexFields;
import org.xwiki.platform.search.SearchResult;
import org.slf4j.Logger;


public class SolrjSearchResult implements SearchResult
{
    private String id;

    private float score;

    private String title;

    private String name;
    
    private String space;

    private String fullName;
    
    private String language;
    
    @Inject
    private Logger logger;
    
    
    public SolrjSearchResult(SolrDocument doc)
    {
        language = (String) doc.getFieldValue("language");
        logger.info("The language for the document is - " + language);
        
        fullName = (String) doc.getFieldValue(IndexFields.DOCUMENT_FULLNAME
            +"_"+ language);
        name = (String) doc.getFieldValue(IndexFields.DOCUMENT_NAME
            +"_"+ language);
        title = (String) doc.getFieldValue(IndexFields.DOCUMENT_TITLE 
            +"_"+ language);
        score = (Float) doc.getFieldValue("score");
        id = (String) doc.getFieldValue("id");
    }

    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResult#getId()
     */
    @Override
    public String getId()
    {
        return this.id;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResult#getTitle()
     */
    @Override
    public String getTitle()
    {
        return this.title;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResult#getName()
     */
    @Override
    public String getName()
    {
        return this.name;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResult#getScore()
     */
    public float getScore()
    {
        return this.score;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResult#getLanguage()
     */
    @Override
    public String getLanguage()
    {
        return this.language;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchResult#getFullName()
     */
    @Override
    public String getFullName()
    {
        return this.fullName;
    }

    
}
