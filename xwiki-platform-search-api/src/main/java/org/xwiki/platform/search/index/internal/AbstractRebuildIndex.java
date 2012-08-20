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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.platform.search.index.RebuildIndex;

/**
 * 
 * @version $Id$
 */
public abstract class AbstractRebuildIndex implements RebuildIndex
{
    /**
     * Logger.
     */
    @Inject
    protected Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.RebuildIndex#reBuildFarmIndex()
     */
    @Override
    public int reBuildFarmIndex()
    {

        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.RebuildIndex#reBuildFarmIndex(java.util.List)
     */
    @Override
    public int reBuildFarmIndex(List<WikiReference> wikis)
    {

        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.RebuildIndex#reBuildWikiIndex(org.xwiki.model.reference.WikiReference)
     */
    @Override
    public int reBuildWikiIndex(WikiReference wikiReference)
    {

        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.RebuildIndex#reBuildSpaceIndex(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public int reBuildSpaceIndex(SpaceReference spaceReference)
    {

        return 0;
    }

}
