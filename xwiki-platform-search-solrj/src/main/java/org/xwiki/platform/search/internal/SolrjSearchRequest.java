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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.handler.admin.LukeRequestHandler;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.platform.search.SearchEngine;
import org.xwiki.platform.search.SearchRequest;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.XWikiStubContextProvider;

/**
 * @version $Id$
 */
@Component
@Named(SolrjSearchRequest.HINT)
@Singleton
public class SolrjSearchRequest implements SearchRequest
{

    public static final String HINT = "solrjrequest";

    @Inject
    private ExecutionContextManager executionContextManager;

    @Inject
    private Execution execution;

    @Inject
    @Named(SolrjSearchEngine.HINT)
    private SearchEngine searchEngine;
    
    @Inject
    private Logger logger;
    
    @Inject
    protected XWikiStubContextProvider contextProvider;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchRequest#processRequestQuery(java.lang.String)
     */
    @Override
    public String processRequestQuery(String query)
    {
        List<String> fields=getFields();
        String language=getXWikiContext().getLanguage();
        String[] items=query.trim().split(" ");
        StringBuilder builder=new StringBuilder();
        for(String item:items){
            if(item.contains(":")){
                String[] pair=item.split(":");
                if(fields.contains(pair[0]+"_"+language)){
                    item=pair[0]+"_"+language+":"+pair[1];
                }
            }
            builder.append(item+" ");
        }
        
        return builder.toString().trim();
        
    }

    private List<String> getFields()
    {
        CoreContainer container=(CoreContainer) searchEngine.getCoreContainer();
        SolrCore core = null;
        for (SolrCore c : container.getCores()) {
            core = c;
        }
        
        Map<String, SolrInfoMBean> reg = core.getInfoRegistry();
        LukeRequestHandler handler=(LukeRequestHandler) reg.get("/admin/luke");
        LocalSolrQueryRequest req = new LocalSolrQueryRequest(core, new ModifiableSolrParams());
        SolrQueryResponse response = new SolrQueryResponse();
        handler.handleRequest(req, response);
        NamedList list=response.getValues();
        
        ArrayList<String> fieldsList=new ArrayList<String>();
        for(Object obj:list.getAll("fields")){
           SimpleOrderedMap map=(SimpleOrderedMap) obj;
           Iterator<Map.Entry<String,Object>> entries=map.iterator();
           while(entries.hasNext()){
               Map.Entry<String, Object> entry = entries.next();
               fieldsList.add(entry.getKey());
           }
        }
        
        return fieldsList;
        
    }
    
    /**
     * gets the XWikiContext
     * 
     * @return the XWikiContext
     */
    protected XWikiContext getXWikiContext()
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        if (context == null) {
            context = this.contextProvider.createStubContext();
            logger.info(context.toString());
            this.execution.getContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
        }
        return context;
    }
}
