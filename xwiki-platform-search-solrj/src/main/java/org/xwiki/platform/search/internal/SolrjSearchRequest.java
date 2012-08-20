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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.handler.admin.LukeRequestHandler;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.platform.search.SearchEngine;

/**
 * @version $Id$
 */
@Component
@Named(SolrjSearchRequest.HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class SolrjSearchRequest extends AbstractSearchRequest
{
    /**
     * solrj HINT.
     */
    public static final String HINT = "solrj";

    /**
     * SearchEngine component.
     */
    @Inject
    @Named(SolrjSearchEngine.HINT)
    private SearchEngine searchEngine;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchRequest#processRequestQuery(java.lang.String)
     */
    @Override
    public String processRequestQuery(String query)
    {
        fields = getFields();
        String language = getXWikiContext().getLanguage();
        String lang = SEPERATOR + language;
        String[] params = query.trim().split(" ");
        Map<String, String> paramMap = new HashMap<String, String>();
        for (String param : params) {
            if (param.contains(COLON)) {
                String[] pa = param.split(COLON);
                String cleanField = pa[0].replaceAll("[+-]", "");
                if (fields.contains(cleanField + lang)) {
                    pa[0] = pa[0] + lang;

                }
                paramMap.put(pa[0], pa[1]);
            } else {
                paramMap.put(param, null);
            }
        }

        StringBuilder builder = new StringBuilder();

        for (Entry<String, String> entry : paramMap.entrySet()) {
            builder.append(entry.getKey());
            if (!StringUtils.isEmpty(entry.getValue())) {
                builder.append(COLON + entry.getValue());
            }
            builder.append(" ");
        }

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.platform.search.SearchRequest#processQueryFrequency(java.lang.String)
     */
    public String processQueryFrequency(String qfString)
    {
        String[] qfArray = qfString.split(" ");
        Map<String, String> qfMap = new HashMap<String, String>();
        fields = getFields();
        String language = getXWikiContext().getLanguage();
        for (String qfItem : qfArray) {
            if (qfItem.contains(BOOST_INDEX)) {
                String[] qf = qfItem.split("\\^");
                if (fields.contains(qf[0] + SEPERATOR + language)) {
                    qfMap.put(qf[0] + SEPERATOR + language, qf[1]);
                } else if (fields.contains(qf[0])) {
                    qfMap.put(qf[0], qf[1]);
                }

            }
        }

        StringBuilder builder = new StringBuilder();
        for (Entry<String, String> entry : qfMap.entrySet()) {
            builder.append(entry.getKey());
            if (entry.getValue() != null) {
                builder.append(BOOST_INDEX + entry.getValue());
            }
            builder.append(" ");
        }

        return builder.toString().trim();
    }

    /**
     * @return List of Fields.
     */
    private List<String> getFields()
    {
        CoreContainer container = (CoreContainer) searchEngine.getCoreContainer();
        SolrCore core = null;
        for (SolrCore c : container.getCores()) {
            core = c;
        }

        Map<String, SolrInfoMBean> reg = core.getInfoRegistry();
        LukeRequestHandler handler = (LukeRequestHandler) reg.get("/admin/luke");
        LocalSolrQueryRequest req = new LocalSolrQueryRequest(core, new ModifiableSolrParams());
        SolrQueryResponse response = new SolrQueryResponse();
        handler.handleRequest(req, response);
        NamedList list = response.getValues();

        List<String> fieldsList = new ArrayList<String>();
        for (Object obj : list.getAll("fields")) {
            SimpleOrderedMap map = (SimpleOrderedMap) obj;
            Iterator<Map.Entry<String, Object>> entries = map.iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                fieldsList.add(entry.getKey());
            }
        }

        return fieldsList;

    }

}
