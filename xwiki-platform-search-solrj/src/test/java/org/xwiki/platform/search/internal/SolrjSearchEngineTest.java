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

import java.net.URL;

import junit.framework.Assert;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.platform.search.SearchEngine;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * @version $Id$
 */
public class SolrjSearchEngineTest extends AbstractComponentTestCase
{

    private SearchEngine searchEngine;

    private SolrServer server;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        URL url = this.getClass().getClassLoader().getResource("solrhome");
        System.setProperty(SolrjSearchEngine.SOLR_HOME_KEY, url.getPath());
        this.searchEngine = getComponentManager().getInstance(SearchEngine.class, SolrjSearchEngine.HINT);
        this.server = (SolrServer) searchEngine.getSearchEngine();
    }

    @Test
    public void testSolrServer() throws Exception
    {
        Assert.assertNotNull(this.searchEngine);
        Assert.assertNotNull(this.server);
        Assert.assertTrue(this.server instanceof EmbeddedSolrServer);

    }

}
