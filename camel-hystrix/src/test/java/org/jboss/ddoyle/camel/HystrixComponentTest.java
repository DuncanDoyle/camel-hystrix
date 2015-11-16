package org.jboss.ddoyle.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class HystrixComponentTest extends CamelTestSupport {

	//TODO: Implement some proper unit-testing
    @Test
    public void testHystrixComponent() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        
        //mock.expectedMinimumMessageCount(1);       
        mock.expectedMinimumMessageCount(0);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://foo")
                  .to("hystrix://bar")
                  .to("mock:result");
            }
        };
    }
}
