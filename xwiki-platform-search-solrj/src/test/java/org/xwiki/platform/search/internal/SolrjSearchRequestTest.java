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

import java.io.StringReader;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.platform.search.SearchRequest;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * @version $Id$
 */
public class SolrjSearchRequestTest extends AbstractComponentTestCase
{
    private SearchRequest request;

    private List list;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.request = getComponentManager().getInstance(SearchRequest.class, SolrjSearchRequest.HINT);

    }

    @Test
    public void testSolrjSearchComponent()
    {
        Assert.assertNotNull(this.request);
    }

    @Test
    public void testConversion() throws Exception
    {

        XDOM xdom;
        // Use the Converter component to convert between one syntax to another.
        Converter converter = getComponentManager().getInstance(Converter.class);
        // Convert input in XWiki Syntax 2.0 into XHTML. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();

        converter.convert(new StringReader("This is **bold**  ==Header Text=="), Syntax.XWIKI_2_1, Syntax.XHTML_1_0,
            printer);
        Assert.assertEquals("<p>This is <strong>bold</strong> &nbsp;==Header Text==</p>", printer.toString());
    }
}
