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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.platform.search.PageTool;

/**
 * PageTool implementation for convenient view of the results
 *
 * @version $Id$
 */
@Component
@Singleton

public class SolrjPageTool implements PageTool
{
    
    long start = 1;
    int result_page=5;
    int result_found=20;
    int page_count=0;
    
    public SolrjPageTool()
    {
        int page_count = (result_found/result_page);
    }

    public long getStart()
    {  
       
        return start;
    }
    
    public int getResults_per_page()
    {   
        
        return result_page;
    }
    
    
    public long getResults_found()
    {
        
        return result_found;
    }
     
    
    public int getPage_count()
    {
        
        return page_count;
        
    }

    public int getCurrent_page_number()
    {
        return 0;
    }
    
    }
        
    

