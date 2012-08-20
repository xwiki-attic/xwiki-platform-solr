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

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.XWikiStubContextProvider;

/**
 * @version $Id$
 */
public class DocumentHelper
{

    /**
     * DocumentAccessBridge component.
     */
    @Inject
    protected DocumentAccessBridge documentAccessBridge;

    /**
     * ExecutionContextManager component.
     */
    @Inject
    protected ExecutionContextManager executionContextManager;

    /**
     * Execution component.
     */
    @Inject
    protected Execution execution;

    /**
     * XWikiStubContextProvider component.
     */
    @Inject
    protected XWikiStubContextProvider contextProvider;

    /**
     * @return Execution Context.
     */
    protected ExecutionContext getExecutionContext()
    {
        return this.execution.getContext();
    }

    /**
     * gets the XWikiContext.
     * 
     * @return the XWikiContext
     */
    protected XWikiContext getXWikiContext()
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        if (context == null) {
            context = this.contextProvider.createStubContext();
            getExecutionContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
        }
        return context;
    }

}
