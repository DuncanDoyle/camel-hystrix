package org.jboss.ddoyle.camel.hystrix;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultProducer;
import org.jboss.ddoyle.camel.hystrix.command.CamelHystrixCommand;
import org.jboss.ddoyle.camel.hystrix.command.HystrixCommandFactory;
import org.jboss.ddoyle.camel.hystrix.command.HystrixCommandFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The camel-hystrix-component {@link Producer}.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class HystrixProducer extends DefaultProducer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HystrixProducer.class);

	private HystrixCommandFactory factory;

	public HystrixProducer(HystrixEndpoint endpoint) {
		super(endpoint);
		this.factory = new HystrixCommandFactoryBuilder().to(endpoint.getTo()).fallback(endpoint.getFallback())
				.producerTemplate(endpoint.getCamelContext().createProducerTemplate()).configuration(endpoint.getConfiguration()).build();
	}

	public void process(Exchange exchange) throws Exception {
		CamelHystrixCommand chCommand = factory.getCamelHystrixCommand(exchange);

		LOGGER.debug("Executing HystrixCommand");
		chCommand.execute();
	}

}
