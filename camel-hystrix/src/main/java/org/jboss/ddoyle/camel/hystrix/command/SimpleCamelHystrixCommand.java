package org.jboss.ddoyle.camel.hystrix.command;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

import com.netflix.hystrix.HystrixCommand;

/**
 * {@link HystrixCommand} implementation for Apache Camel {@link Exchange Exchanges}.
 * 
 * This command is configured with an {@link #mainEndpointUri}, a {@link #fallbackEndpointUri}. These URIs are Apache Camel URIs (.e.g
 * <code>direct</code>, <code>direct-vm</code>, etc.). A Camel {@link ProducerTemplate} is used to send the {@link Exchange} to the given
 * endpoint within a {@link HystrixCommand}, which provides the fault-tolerance and resilient execution through its CircuitBreaker and
 * Bulkhead semantics.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class SimpleCamelHystrixCommand extends HystrixCommand<Exchange> implements CamelHystrixCommand {

	private final Exchange exchange;

	private final ProducerTemplate producerTemplate;

	private final String mainEndpointUri;

	private String fallbackEndpointUri;
	
	public SimpleCamelHystrixCommand(Setter setter, ProducerTemplate producerTemplate, Exchange exchange, String mainEndpointUri) {
		super(setter);
		this.producerTemplate = producerTemplate;
		this.exchange = exchange;
		this.mainEndpointUri = mainEndpointUri;
	}
	
	public SimpleCamelHystrixCommand(Setter setter, ProducerTemplate producerTemplate, Exchange exchange, String mainEndpointUri, String fallbackEndpoinUri) {
		this(setter, producerTemplate, exchange, mainEndpointUri);
		this.fallbackEndpointUri = fallbackEndpoinUri;
	}

	/**
	 * Sends the {@link Exchange} to the {@link #mainEndpointUri}.
	 */
	@Override
	protected Exchange run() throws Exception {
		/*
		 * Make a copy of the exchange so we can use the original exchange in case we need to call the fallback logic.
		 * If we would send a failed exchange to the fallback route, including the exception, the fallback route will fail immediately.
		 */
		Exchange mainRouteExchange = exchange.copy(true);
		Exchange outputExchange = producerTemplate.send(mainEndpointUri, mainRouteExchange);
		/* 
		 * If the route we call throws an Exception, the Exception will be set on the Camel Exchange, and not be thrown by the ProducerTemplate#send method.
		 * Hence, we need to validate the Exchange, and throw the exception from this method to activate the Hystrix fallback semantics.
		 */
		if (outputExchange.isFailed()) {
			throw outputExchange.getException();
		}
		return  outputExchange;
	}

	/**
	 * Sends the {@link Exchange} to the {@link #fallbackEndpointUri}.
	 * <p/>
	 * If no fallback endpoint has been defined, we call the fallback method of our superclass.
	 */
	@Override
	protected Exchange getFallback() {
		if (fallbackEndpointUri == null || "".equals(fallbackEndpointUri)) {
			return super.getFallback();
		} else {
			Exchange fallbackExchange = exchange.copy(true);
			return getProducerTemplate().send(fallbackEndpointUri, fallbackExchange);
		}
	}

	protected Exchange getExchange() {
		return exchange;
	}

	protected ProducerTemplate getProducerTemplate() {
		return producerTemplate;
	}

	protected String getMainEndpointUri() {
		return mainEndpointUri;
	}

}
