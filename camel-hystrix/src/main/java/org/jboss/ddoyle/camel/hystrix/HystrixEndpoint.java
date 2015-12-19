package org.jboss.ddoyle.camel.hystrix;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * Represents a camel-hystrix-component endpoint.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@UriEndpoint(scheme = "hystrix", title = "camel-hystrix-component", syntax="hystrix:name", label = "camel-hystrix-component")
public class HystrixEndpoint extends DefaultEndpoint {
	
	@UriPath 
    @Metadata(required = "true")
    private String to;
	
	@UriParam
    private String fallback;
	
	@UriParam
	private HystrixEndpointConfiguration configuration;
    
    
    public HystrixEndpoint(String uri, HystrixComponent component, String to, HystrixEndpointConfiguration configuration) {
    	super(uri, component);
    	this.to = to;
    	this.configuration = configuration;
    }
	
	public Producer createProducer() throws Exception {
        return new HystrixProducer(this);
    }

    //We don't support Hystrix Consumers.
    public Consumer createConsumer(Processor processor) throws Exception {
    	throw new UnsupportedOperationException("Consumer not supported for Hystrix endpoint");
    }
    

	public boolean isSingleton() {
        return true;
    }

	public String getTo() {
		return to;
	}

	/**
	 * The Camel <code>to</code> URI to which the Camel Exchange needs to be send, wrapped in a Hystrix execution.
	 * 
	 * @param to
	 */
	public void setTo(String to) {
		this.to = to;
	}

	public String getFallback() {
		return fallback;
	}

	/**
	 * The Camel <code>to</code> URI to which the Camel Exchange needs to be send when the Hystrix fallback logic is executed.
	 * 
	 * @param fallback
	 */
	public void setFallback(String fallback) {
		this.fallback = fallback;
	}
	
	public HystrixEndpointConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(HystrixEndpointConfiguration configuration) {
		this.configuration = configuration;
	}

     
}