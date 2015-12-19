package org.jboss.ddoyle.camel.hystrix.command;

import org.apache.camel.ProducerTemplate;
import org.jboss.ddoyle.camel.hystrix.HystrixEndpointConfiguration;


/**
 * Builder class for {@link HystrixCommandFactory HystrixCommandFactories}.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class HystrixCommandFactoryBuilder {
	
	private String to;
	
	private String fallback;
	
	private ProducerTemplate producerTemplate;
	
	private HystrixEndpointConfiguration configuration;
	
	
	public HystrixCommandFactoryBuilder to(final String to) {
		this.to = to;
		return this;
	}
	
	public HystrixCommandFactoryBuilder fallback(final String fallback) {
		this.fallback = fallback;
		return this;
	}
	
	public HystrixCommandFactoryBuilder producerTemplate(ProducerTemplate producerTemplate) {
		this.producerTemplate = producerTemplate;
		return this;
	}
	
	public HystrixCommandFactoryBuilder configuration(HystrixEndpointConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}
	
	public HystrixCommandFactory build() {
		HystrixCommandFactory factory = new SimpleCamelHystrixCommandFactory(producerTemplate, to, fallback, configuration);
		return factory;
	}

}
