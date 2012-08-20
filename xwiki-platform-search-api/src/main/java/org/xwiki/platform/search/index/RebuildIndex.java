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
package org.xwiki.platform.search.index;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

/**
 * @version $Id$
 */
@Role
public interface RebuildIndex
{
    /**
     * Rebuilds the index for the whole wiki farm.
     * 
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    int reBuildFarmIndex();

    /**
     * Rebuilds the index for the given wiki's in a wiki farm.
     * 
     * @param wikis List of wikis to be indexed.
     * @return the Number of documents scheduled for indexing. -1 in case of errors.
     */
    int reBuildFarmIndex(List<WikiReference> wikis);

    /**
     * Rebuilds the index for the current wiki.
     * 
     * @param wikiReference WikiReference reference to the Wiki
     * @return the Number of documents scheduled for indexing. -1 in case of errors
     */
    int reBuildWikiIndex(WikiReference wikiReference);

    /**
     * Rebuilds the index for given spaces in the current wiki.
     * 
     * @param spaceReference List of spaces to be indexed
     * @return the Number of documents scheduled for indexing. -1 in case of errors
     */
    int reBuildSpaceIndex(SpaceReference spaceReference);

}
