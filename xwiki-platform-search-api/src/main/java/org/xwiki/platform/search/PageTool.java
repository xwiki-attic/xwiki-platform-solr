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

import org.xwiki.component.annotation.Role;

/**
 * 
 * It is contains methods that gives the current page no, page count, results found, results per page 
 * to the front end.
 *
 * @version $Id$
 */
@Role
public interface PageTool
{   
    /**
     * It calculates the current page number.
     *
     * @param 
     * @return the current page no.
     */
    public int getCurrent_page_number();
    
    /**
     * It calculates the page count
     *
     * @param 
     * @return the page count.
     */ 
    public int getPage_count();
    
    /**
     * It gives the number of results found
     *
     * @param 
     * @return the results found.
     */ 
    public long getResults_found();

    /**
     * It gives the number of results to be displayed per page.
     *
     * @param 
     * @return the number of results to be displayed in a page
     */ 
    public int getResults_per_page();

    public long getStart();

}
