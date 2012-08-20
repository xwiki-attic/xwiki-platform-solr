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
import org.xwiki.model.reference.WikiReference;
import org.xwiki.platform.search.index.BuildIndex;
import org.xwiki.platform.search.index.SearchIndexingException;
import org.xwiki.platform.search.internal.DocumentHelper;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * 
 * @version $Id$
 */
public abstract class AbstractBuildIndex extends DocumentHelper implements BuildIndex
{
    /**
     * Logger.
     */
    @Inject
    protected Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.index.BuildIndex#buildWikiFarmIndex()
     */
    @Override
    public int buildWikiFarmIndex() throws SearchIndexingException, XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        int totalDocCount = 0;
        if (xcontext.getWiki().isVirtualMode()) {
            List<String> wikis = xcontext.getWiki().getVirtualWikisDatabaseNames(xcontext);
            for (String wikiName : wikis) {
                WikiReference wikiReference = new WikiReference(wikiName);
                totalDocCount += buildWikiIndex(wikiReference);
            }
        }
        return totalDocCount;
    }

}
