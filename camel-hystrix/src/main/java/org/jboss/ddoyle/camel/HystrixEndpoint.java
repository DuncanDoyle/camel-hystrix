package org.jboss.ddoyle.camel;

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
	
    @UriParam(name="bla")
    private String hystrixCommandGroupKey;
    
    @UriPath(name = "to") @Metadata(required = "true")
    private String to;
    
    @UriParam(name="blabla")
    private String fallback;
    
    public HystrixEndpoint() {
    }

    public HystrixEndpoint(String uri, HystrixComponent component) {
        super(uri, component);
    }

    public HystrixEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new HystrixProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
    	throw new UnsupportedOperationException("Consumer not supported for Hystrix endpoint");
    }

    public boolean isSingleton() {
        return true;
    }

	public String getHystrixCommandGroupKey() {
		return hystrixCommandGroupKey;
	}

	/**
	 * The key that is used by the Hystrix platform to group Hystrix commands.
	 * 
	 * @param hystrixCommandGroupKey
	 */
	public void setHystrixCommandGroupKey(String hystrixCommandGroupKey) {
		this.hystrixCommandGroupKey = hystrixCommandGroupKey;
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
     
}
