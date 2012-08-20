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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.platform.search.SearchRequest;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.XWikiStubContextProvider;

/**
 * @version $Id$
 */
public abstract class AbstractSearchRequest implements SearchRequest
{
    /**
     * Field Seperator.
     */
    public static final String SEPERATOR = "_";

    /**
     * Search phrase seperator.
     */
    public static final String COLON = ":";

    /**
     * Boost Index.
     */
    public static final String BOOST_INDEX = "^";

    /**
     * searchParametersMap.
     */
    protected Map<String, String> searchParametersMap;

    /**
     * filterParametersMap.
     */
    protected Map<String, String> filterParametersMap;

    /**
     * query String.
     */
    protected String queryString;

    /**
     * List of languages.
     */
    protected List<String> languages;

    /**
     * EntityReference.
     */
    protected EntityReference entityReference;

    /**
     * List of fields.
     */
    protected List<String> fields;

    /**
     * XWikiStubContextProvider conponent.
     */
    @Inject
    protected XWikiStubContextProvider contextProvider;

    /**
     * Logger component.
     */
    @Inject
    protected Logger logger;

    /**
     * ExecutionContextManager component.
     */
    @Inject
    protected ExecutionContextManager executionContextManager;

    /**
     * Execution component.
     */
    @Inject
    protected Execution execution;

    /**
     * gets the XWikiContext.
     * 
     * @return the XWikiContext
     */
    protected XWikiContext getXWikiContext()
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        if (context == null) {
            context = this.contextProvider.createStubContext();
            logger.info(context.toString());
            this.execution.getContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
        }
        return context;
    }

    /**
     * @return the queryString
     */
    @Override
    public String getQueryString()
    {
        return queryString;
    }

    /**
     * @param queryString the queryString to set
     */
    @Override
    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }

    /**
     * @return the searchParametersMap
     */
    public Map<String, String> getSearchParametersMap()
    {
        if (searchParametersMap == null) {
            searchParametersMap = new HashMap<String, String>();
        }
        return searchParametersMap;
    }

    /**
     * @param searchParametersMap the searchParametersMap to set
     */
    public void setSearchParametersMap(Map<String, String> searchParametersMap)
    {
        this.searchParametersMap = searchParametersMap;
    }

    /**
     * @return the filterParametersMap
     */
    public Map<String, String> getFilterParametersMap()
    {
        if (filterParametersMap == null) {
            filterParametersMap = new HashMap<String, String>();
        }
        return filterParametersMap;
    }

    /**
     * @param filterParametersMap the filterParametersMap to set
     */
    public void setFilterParametersMap(Map<String, String> filterParametersMap)
    {
        this.filterParametersMap = filterParametersMap;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchRequest#setLanguages(java.util.List)
     */
    @Override
    public void setLanguages(List<String> languages)
    {
        this.languages = languages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchRequest#setEntityReference(org.xwiki.model.reference.EntityReference)
     */
    @Override
    public void setEntityReference(EntityReference entityReference)
    {
        this.entityReference = entityReference;
    }

    /**
     * @return the languages
     */
    public List<String> getLanguages()
    {
        return languages;
    }

    /**
     * @return the entityReference
     */
    public EntityReference getEntityReference()
    {
        return entityReference;
    }

}
