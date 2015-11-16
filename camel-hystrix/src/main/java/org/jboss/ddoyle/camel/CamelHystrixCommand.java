package org.jboss.ddoyle.camel;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * {@link HystrixCommand} implementation for Apache Camel {@link Exchange Exchanges}.
 * 
 * This command is configured with an {@link #mainEndpointUri}, a {@link #fallbackEndpointUri}. These URIs are Apache Camel URIs (.e.g
 * <code>direct</code>, <code>direct-vm</code>, etc.). A Camel {@link ProducerTemplate} is used to send the {@link Exchange} to the given
 * endpoint within a {@link HystrixCommand}, which provides the fault-tolerance and resilient execution through its CircuitBreaker and Bulkhead
 * semantics.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class CamelHystrixCommand extends HystrixCommand<Exchange> {

	private final Exchange exchange;

	private final ProducerTemplate producerTemplate;

	private final String mainEndpointUri;

	private final String fallbackEndpointUri;

	public CamelHystrixCommand(HystrixCommandGroupKey hcGroupKey, ProducerTemplate producerTemplate, Exchange exchange,
			String mainEndpointUri, String fallbackEndpointUri) {
		super(hcGroupKey);
		this.exchange = exchange;
		this.producerTemplate = producerTemplate;
		this.mainEndpointUri = mainEndpointUri;
		this.fallbackEndpointUri = fallbackEndpointUri;
	}

	/**
	 * Sends the {@link Exchange} to the {@link #mainEndpointUri}.
	 */
	@Override
	protected Exchange run() throws Exception {
		return producerTemplate.send(mainEndpointUri, exchange);
	}

	/**
	 * Sends the {@link Exchange} to the {@link #fallbackEndpointUri}.
	 */
	@Override
	protected Exchange getFallback() {
		return producerTemplate.send(fallbackEndpointUri, exchange);
	}

}
