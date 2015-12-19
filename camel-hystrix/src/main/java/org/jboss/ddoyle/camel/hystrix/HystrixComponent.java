package org.jboss.ddoyle.camel.hystrix;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link camel-hystrix-componentEndpoint}.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class HystrixComponent extends UriEndpointComponent {
    
    public HystrixComponent() {
        super(HystrixEndpoint.class);
    }

    public HystrixComponent(CamelContext context) {
        super(context, HystrixEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        
    	//Build a new HystrixEndpointConfiguration
    	HystrixEndpointConfiguration config = new HystrixEndpointConfiguration();
    	setProperties(config, parameters);
    	
    	Endpoint endpoint = new HystrixEndpoint(uri, this, remaining, config);
        setProperties(endpoint, parameters);
        
        return endpoint;
    }

	
}
