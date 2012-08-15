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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;

/**
 * @version $Id$
 */
public class VelocityUtils
{
   /**
    *  SolrServer .
    */
    private SolrServer solrServer;
    
    /**
     * SolrCore object.
     */
    private SolrCore core;
    
    /**
     * CoreContainer.
     */
    private CoreContainer coreContainer;
    
    /**
     * 
     * @param coreContainer CoreContainer.
     * @param solrServer SolrServer.
     */
    public VelocityUtils(CoreContainer coreContainer, SolrServer solrServer)
    {
        this.coreContainer = coreContainer;
        this.solrServer = solrServer;
        // Embedded XWiki runs on a single core.
        for (SolrCore c : coreContainer.getCores()) {
            this.core = c;
        }
    }
    
    /**
     * 
     * 
     */
    public NamedList getIndexingStatistics()
    {
        RefCounted<SolrIndexSearcher> searcher = core.getSearcher();
        searcher.decref();

        SolrIndexSearcher s = searcher.get();
        NamedList list = s.getStatistics();
        return list;
    }
    
    /**
     * 
     * @param key
     * @return
     */
    private NamedList getStatistics(String key)
    {
        Map<String, SolrInfoMBean> reg = this.core.getInfoRegistry();
        LocalSolrQueryRequest req = new LocalSolrQueryRequest(core, new ModifiableSolrParams());
        SolrQueryResponse response = new SolrQueryResponse();
        if (key.startsWith("/admin") && !key.equals("/admin/")) {
            SolrRequestHandler base = (SolrRequestHandler) reg.get(key);
            base.handleRequest(req, response);
            return response.getValues();
        }
        return null;
    }
    
    /**
     * 
     * @return
     */
    public NamedList getLukeStatistics()
    {
        return getStatistics("/admin/luke");
    }
    
    /**
     * 
     * @return
     */
    public NamedList getSystemStatistics()
    {
        return getStatistics("/admin/system");
    }
    
    /**
     * 
     * @return
     */
    public NamedList getFileStatistics()
    {
        return getStatistics("/admin/file");
    }
    
    /**
     * 
     * @return
     */
    public NamedList getPluginsStatistics()
    {
        return getStatistics("/admin/plugins");
    }
    
    /**
     * 
     * @return
     */
    public NamedList getJvmStatistics()
    {
        return getStatistics("/admin/properties");
    }
    
    /**
     * 
     * @return
     */
    public NamedList getThreadStatistics()
    {
        return getStatistics("/admin/threads");
    }
    
    /**
     * 
     * @return property.
     */
    private String getSolrConfigHome()
    {
        String property = System.getProperty("solr.solr.home");
        return property;
    }
    
    /**
     * 
     * @param filePath 
     * @return
     */
    private String readFileFromPath(String filePath)
    {
        File file = new File(filePath);
        if (file.exists()) {
            StringBuilder contents = new StringBuilder();
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));
                try {
                    String line = null;
                    while ((line = input.readLine()) != null) {
                        contents.append(line);
                        contents.append(System.getProperty("line.separator"));
                    }
                } finally {
                    input.close();
                }
            } catch (Exception ex) {
                // Do Nothing.
                ex.printStackTrace();
            }
            return contents.toString();
        }
        return null;
    }
    
    /**
     * 
     * @return gets the path of Solrschema.xml  file
     */
    public String getSolrSchemaXml()
    {
        String filePath = getSolrConfigHome() + File.separatorChar + "conf" + File.separatorChar + "schema.xml";
        System.out.println(filePath);
        return readFileFromPath(filePath);
    }
    
    /**
     * 
     * @return gets the path of SolrConfig.xml  file.
     */
    public String getSolrConfigXml()
    {
        String filePath = getSolrConfigHome() + File.separatorChar + "conf" + File.separatorChar + "solrconfig.xml";
        return readFileFromPath(filePath);
    }
}
