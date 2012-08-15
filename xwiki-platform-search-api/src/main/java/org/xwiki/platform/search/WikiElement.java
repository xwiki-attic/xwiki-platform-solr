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
public enum WikiElement
{   
    /**
     * 
     */
    DOCUMENT(1),
    /**
     * 
     */
    ATTACHMENT(2),
    /**
     * 
     */
    OBJECT(3),
    /**
     * 
     */
    PROPERTY(4);
    
    /**
     * 
     */
    private int id;
    
    /**
     * 
     * @param id of the element
     */
    WikiElement(int id)
    {
        this.id = id;
    }
    
    /**
     * 
     * @return teh id.
     */
    public int getId()
    {
        return this.id;
    }
}
