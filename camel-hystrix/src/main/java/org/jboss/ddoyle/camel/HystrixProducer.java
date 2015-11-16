package org.jboss.ddoyle.camel;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * The camel-hystrix-component producer.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class HystrixProducer extends DefaultProducer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HystrixProducer.class);
	
	private HystrixEndpoint endpoint;

	private ProducerTemplate producerTemplate;

	public HystrixProducer(HystrixEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
		this.producerTemplate = endpoint.getCamelContext().createProducerTemplate();
	}

	public void process(Exchange exchange) throws Exception {
		//Create the HystrixCommand and execute it.
		CamelHystrixCommand chCommand = new CamelHystrixCommand(HystrixCommandGroupKey.Factory.asKey(endpoint.getHystrixCommandGroupKey()),
				producerTemplate, exchange, endpoint.getTo(), endpoint.getFallback());
		LOGGER.debug("Executing HystrixCommand");
		chCommand.execute();
	}

}
