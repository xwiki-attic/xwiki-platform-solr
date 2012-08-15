package org.xwiki.platform.search;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.EntityReference;

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

/**
 * @version $Id$
 */
@Role
public interface SearchRequest
{

    /**
     * 
     * @param query to be processed
     * @return String 
     */
    String processRequestQuery(String query);
    
    /**
     * 
     * @param qfString query fields 
     * @return String
     */
    String processQueryFrequency(String qfString);
    
    /**
     * 
     * @return the search Parameters
     */
    Map<String, String> getSearchParametersMap();
    
    /**
     * 
     * @return the filter Parameter
     */
    Map<String, String> getFilterParametersMap();
    
    /**
     * 
     * @param query from the user
     */
    void setQueryString(String query);
    
    /**
     * 
     * @return the Query String
     */
    String getQueryString();
    
    /**
     * 
     * @param languages 
     */
    void setLanguages(List<String> languages);
    
    /**
     * 
     * @param entityReference gives the references to Document, Attachment.
     */
    void setEntityReference(EntityReference entityReference);
    
    /**
     * 
     * @param searchParametersMap map of the search Parameters
     */
    void setSearchParametersMap(Map<String, String> searchParametersMap);
    
    /**
     * 
     * @param filterParametersMap filter Parameters Map 
     */
    void setFilterParametersMap(Map<String, String> filterParametersMap);

}
