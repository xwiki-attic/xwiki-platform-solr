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

/**
 * @version $Id$
 */
public interface SearchResult
{
    /**
     * @return the document id as indexed
     */
    public String getId();
    
    /**
     * @return the title of the document.
     */
    public String getTitle();
    
    /**
     * @return Returns the name of the document.
     */
    public String getName();
    
    /**
     * @return Returns the score of this search result.Is a float between zero and 1.
     */
    public float getScore();
    
    /**
     * @return the language of the Document
     */
    public String getLanguage();
    
    /**
     * 
     * @return Returns the Full name of the document
     */
    public String getFullName();

}
