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

import org.slf4j.Logger;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.platform.search.index.DeleteIndex;
import org.xwiki.platform.search.index.SearchIndexingException;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
public abstract class AbstractDeleteIndex implements DeleteIndex
{
    /**
     * Logger.
     */
    @Inject
    protected Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DeleteIndex#deleteDocumentIndex(org.xwiki.model.reference.DocumentReference)
     */
    @Override
    public boolean deleteDocumentIndex(DocumentReference documentReference)
    {
        return this.deleteIndex(documentReference);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DeleteIndex#deleteWikiIndex(org.xwiki.model.reference.WikiReference)
     */
    @Override
    public boolean deleteWikiIndex(WikiReference wikiReference) throws SearchIndexingException, XWikiException
    {
        return this.deleteIndex(wikiReference);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.DeleteIndex#deleteSpaceIndex(org.xwiki.model.reference.SpaceReference)
     */
    @Override
    public boolean deleteSpaceIndex(SpaceReference spaceReference) throws SearchIndexingException, XWikiException
    {
        return this.deleteIndex(spaceReference);
    }

    /**
     * Abstract delete index implementation.
     * 
     * @param entityReference entity reference for which index is to be deleted.
     * @return true if index deltion is successful.
     */
    public abstract boolean deleteIndex(EntityReference entityReference);

}
