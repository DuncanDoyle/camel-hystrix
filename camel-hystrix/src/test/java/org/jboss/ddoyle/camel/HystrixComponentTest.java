package org.jboss.ddoyle.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

public class HystrixComponentTest extends CamelTestSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(HystrixComponentTest.class);

	private static final String INPUT_ENDPOINT = "direct:input";

	private static final String TO_ENDPOINT = "direct:hystrixTo";

	private static final String FALLBACK_ENDPOINT = "direct:hystrixFallback";

	private static final String COMMAND_KEY = "myHystruxUnitTestKey";

	private static final String COMMAND_GROUP_KEY = "myHystrixUnitTestGroupKey";

	@Produce(uri = INPUT_ENDPOINT)
	private ProducerTemplate producerTemplate;

	/**
	 * Tests the main route accessed via Hystrix.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHystrixComponent() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:advicedTo");
		mock.expectedMinimumMessageCount(1);

		// Advice the route to which Hystrix is going to send a message, so we can check whether it arrived.
		context.getRouteDefinition("hystrix-to-route").adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom().log("Processing message in advice.").to("mock:advicedTo").stop();

			}
		});
		context.start();

		// Build the exchange.
		ExchangeBuilder exchangeBuilder = new ExchangeBuilder(context);
		exchangeBuilder.withBody("This is a test exchange for my unit-test.");
		Exchange exchange = exchangeBuilder.build();

		// Send a message to the actual endpoint
		LOGGER.info("Sending exchange.");
		producerTemplate.send(exchange);

		assertMockEndpointsSatisfied();
		context.stop();
	}

	/**
	 * Test the fallback route accessed via Hystrix.
	 * <p/>
	 * We advise the main route so it throws an exception, which should cause Hystrix to access the fallback logic.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHystrixComponentFallback() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:advicedFallback");
		mock.expectedMinimumMessageCount(1);

		// Advice the 'to' rout to throw an exception
		context.getRouteDefinition("hystrix-to-route").adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom().log("Processing message in advice.").process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						throw new IllegalArgumentException("Forcing fallback logic.");
					}
				});
			}
		});
		// Advice the fallback route to which Hystrix is going to send a message, so we can check whether Hystrix went into fallback-mode.
		context.getRouteDefinition("hystrix-fallback-route").adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom(FALLBACK_ENDPOINT).log("Processing message in advice.").to("mock:advicedFallback").stop();

			}
		});

		context.start();

		// Build the exchange.
		ExchangeBuilder exchangeBuilder = new ExchangeBuilder(context);
		exchangeBuilder.withBody("This is a test exchange for my unit-test.");
		Exchange exchange = exchangeBuilder.build();

		// Send a message to the actual endpoint
		LOGGER.info("Sending exchange.");
		producerTemplate.send(exchange);
		assertMockEndpointsSatisfied();
		context.stop();
	}

	/**
	 * Tests the Hystrix CircuitBreak logic.
	 * 
	 * To test the Hystrix CircuitBreaker, we will send a number of messages via Hystrix, for which we will all throw an error. After X
	 * errors (TODO: define X), the circuit-breaker should open and send the messages directly to the fallback endpoint. Hence, we test that
	 * when sending N messages to the TO endpoint, that endpoint will have received X messages and the fallback endpoint will have receive N
	 * messages, where N-X messages have been send to the fallback route by the circuit-breaker.
	 * 
	 * The precise way that the circuit opening and closing occurs is as follows:
	 * 
	 * Assuming the volume across a circuit meets a certain threshold (HystrixCommandProperties.circuitBreakerRequestVolumeThreshold())...
	 * And assuming that the error percentage exceeds the threshold error percentage
	 * (HystrixCommandProperties.circuitBreakerErrorThresholdPercentage())... Then the circuit-breaker transitions from CLOSED to OPEN.
	 * While it is open, it short-circuits all requests made against that circuit-breaker. After some amount of time
	 * (HystrixCommandProperties.circuitBreakerSleepWindowInMilliseconds()), the next single request is let through (this is the HALF-OPEN
	 * state). If the request fails, the circuit-breaker returns to the OPEN state for the duration of the sleep window. If the request
	 * succeeds, the circuit-breaker transitions to CLOSED and the logic in 1. takes over again.
	 * 
	 */
	@Test
	public void testHystrixComponentCircuitBreaker() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:advicedFallback");
		// mock.expectedMinimumMessageCount(1);
		mock.expectedMessageCount(5);

		/*
		 * Advice the 'to' route to throw an exception. We never want to end up in the main route anyway, so we can always throw an
		 * exception. The only thing we want to accomplish in the main route is to open the circuit-breaker.
		 */
		context.getRouteDefinition("hystrix-to-route").adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom().log("Processing message in advice.").process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						throw new IllegalArgumentException("Forcing fallback logic.");
					}
				});
			}
		});
		// Advice the fallback route to which Hystrix is going to send a message, so we can check whether Hystrix went into fallback-mode.
		context.getRouteDefinition("hystrix-fallback-route").adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptFrom(FALLBACK_ENDPOINT).log("Processing message in advice.").to("mock:advicedFallback").stop();

			}
		});

		context.start();

		// Build the exchange.
		ExchangeBuilder exchangeBuilder = new ExchangeBuilder(context);
		exchangeBuilder.withBody("This is a test exchange for my unit-test.");

		// Send a message to the actual endpoint
		LOGGER.info("Sending exchange.");
		// TODO: Maybe we need to loop here a couple of times to open the circuit-breaker.
		int loops = 5;
		for (int counter = 0; counter < loops; counter++) {
			Exchange exchange = exchangeBuilder.build();
			producerTemplate.send(exchange);
		}

		assertMockEndpointsSatisfied();

		// Retrieve the circuit-breaker and check whether it is open.
		// The circuit-breaker is retrieved via the HystrixCommandKey, which, by default is generated from the HystrixCommand implementation
		// class-name.
		HystrixCircuitBreaker circuitBreaker = getCircuitBreaker(COMMAND_KEY);

		if (circuitBreaker == null) {
			throw new IllegalStateException("We should have a circuit-breaker for this command-group.");
		}
		
		/*
		 * We need to give Hystrix some time to flip the CircuitBreaker. That seems to be done asynchronously.
		 * A bit crap to use Thread.sleep in a unit test though, maybe we can do something smarter with a Phaser.
		 * 
		 * We wait for max 1 second.
		 */
		boolean isCircuitBreakerOpen = false;
		int maxWaitLoop = 20;
		int waitLoop = 0;
		int threadSleepTime = 50;
		while (isCircuitBreakerOpen == false && waitLoop < maxWaitLoop) {
			isCircuitBreakerOpen = circuitBreaker.isOpen();
			Thread.sleep(threadSleepTime);
			waitLoop++;
		}
		assertEquals(true, isCircuitBreakerOpen);
		context.stop();
	}

	/**
	 * Returns the {@link HystrixCircuitBreaker} for the given {@link HystrixCommandKey} name.
	 * <p/>
	 * Note that the {@link HystrixCommandKey} is different from the {@link HystrixCommandGroupKey}. The latter one can be specified when
	 * creating a {@link HystrixCommand}, the first one is internally computed by Hystrix based on the classname of the
	 * {@link HystrixCommand} implementation.
	 * 
	 * @param commandKey
	 * @return
	 */
	private HystrixCircuitBreaker getCircuitBreaker(String commandKey) {
		return HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey(commandKey));
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() {
				from(INPUT_ENDPOINT).id("hystrix-main-route").to(
						"hystrix://" + TO_ENDPOINT + "?fallback=" + FALLBACK_ENDPOINT + "&commandGroupKey=" + COMMAND_GROUP_KEY
								+ "&commandKey=" + COMMAND_KEY
								+ "&circuitBreakerErrorThresholdPercentage=50&circuitBreakerRequestVolumeThreshold=2");
								//We count statistics over the last 1 second with 10 buckets, so each bucket is 100ms.
								//+ "&metricsRollingStatisticalWindowInMilliseconds=10000&metricsRollingStatisticalWindowBuckets=10");

				// to route.
				from(TO_ENDPOINT).id("hystrix-to-route").log("to-route");

				// fallback route.
				from(FALLBACK_ENDPOINT).id("hystrix-fallback-route").log("fallback-route").setBody().simple("Fallback result");

			}
		};
	}
}
