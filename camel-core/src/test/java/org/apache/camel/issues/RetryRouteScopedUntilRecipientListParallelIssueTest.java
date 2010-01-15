/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.issues;

import org.apache.camel.builder.RouteBuilder;

/**
 * @version $Revision$
 */
public class RetryRouteScopedUntilRecipientListParallelIssueTest extends RetryRouteScopedUntilRecipientListIssueTest {

    public void testRetryUntilRecipientListOkAndFail() throws Exception {
        invoked = 0;

        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:foo").expectedMessageCount(1);

        template.sendBodyAndHeader("seda:start", "Hello World", "recipientListHeader", "direct:foo,fail");

        assertMockEndpointsSatisfied();

        context.stop();

        assertEquals(3, invoked);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("seda:start")
                    .onException(Exception.class).retryUntil(bean("myRetryBean")).end()
                    .recipientList(header("recipientListHeader")).parallelProcessing()
                    .to("mock:result");

                from("direct:foo").to("log:foo").to("mock:foo");
            }
        };
    }

}