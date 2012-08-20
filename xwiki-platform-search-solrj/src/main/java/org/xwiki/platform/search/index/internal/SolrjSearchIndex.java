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
package org.xwiki.platform.search.index.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.platform.search.index.BuildIndex;
import org.xwiki.platform.search.index.DeleteIndex;
import org.xwiki.platform.search.index.RebuildIndex;
import org.xwiki.platform.search.index.SearchIndexingException;

/**
 * @version $Id$
 */
@Component
@Named("solrj")
@Singleton
public class SolrjSearchIndex extends AbstractSearchIndex
{

    /**
     * Component manager.
     */
    @Inject
    protected ComponentManager componentManager;

    /**
     * Component hint.
     */
    private String componentHint = "solrj";

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.SearchIndex#getBuildIndexInstance()
     */
    @Override
    public BuildIndex getBuildIndexInstance() throws SearchIndexingException
    {
        BuildIndex buildIndex = null;
        try {
            buildIndex = this.componentManager.getInstance(BuildIndex.class, componentHint);
        } catch (ComponentLookupException e) {
            logger.error("Error instantiating build index component " + componentHint + e.getMessage());
            throw new SearchIndexingException("Exception instantiating build index component with hint["
                + componentHint + "]", e);
        }
        return buildIndex;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.SearchIndex#getDeleteIndexInstance()
     */
    @Override
    public DeleteIndex getDeleteIndexInstance() throws SearchIndexingException
    {
        DeleteIndex deleteIndex = null;
        try {
            deleteIndex = this.componentManager.getInstance(DeleteIndex.class, componentHint);
        } catch (ComponentLookupException e) {
            logger.error("Error instantiating delete index component " + componentHint + e.getMessage());
            throw new SearchIndexingException("Exception instantiating delete index component with hint["
                + componentHint + "]", e);
        }
        return deleteIndex;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.SearchIndex#getRebuildIndexInstance()
     */
    @Override
    public RebuildIndex getRebuildIndexInstance() throws SearchIndexingException
    {
        RebuildIndex rebuildIndex = null;
        try {
            rebuildIndex = this.componentManager.getInstance(RebuildIndex.class, componentHint);
        } catch (ComponentLookupException e) {
            logger.error("Error instantiating rebuild index component " + componentHint + e.getMessage());
            throw new SearchIndexingException("Exception instantiating rebuild index component with hint["
                + componentHint + "]", e);
        }
        return rebuildIndex;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getName()
     */
    @Override
    public String getName()
    {
        return componentHint;
    }

}
