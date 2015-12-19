package org.jboss.ddoyle.camel.hystrix.command;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.jboss.ddoyle.camel.hystrix.HystrixEndpointConfiguration;

import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * Factory for {@link SimpleCamelHystrixCommand}.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class SimpleCamelHystrixCommandFactory implements HystrixCommandFactory {

	private ProducerTemplate producerTemplate;
	
	private String to;

	private String fallback;
	
	private Setter setter;

	public SimpleCamelHystrixCommandFactory(ProducerTemplate producerTemplate, String to, String fallback, HystrixEndpointConfiguration configuration) {
		this.producerTemplate = producerTemplate;
		this.to = to;
		this.fallback = fallback;
		this.setter = buildSetter(configuration);
	}

	@Override
	public CamelHystrixCommand getCamelHystrixCommand(Exchange exchange) {
		CamelHystrixCommand command;
		if (fallback == null || "".equals(fallback)) {
			command = new SimpleCamelHystrixCommand(setter, producerTemplate, exchange, to);
		} else {
			command = new SimpleCamelHystrixCommand(setter, producerTemplate, exchange, to,
					fallback);
		}
		return command;
	}
	
	private Setter buildSetter(HystrixEndpointConfiguration configuration) {
		//Build the HystrixCommand.Setter from the HystrixEndpointConfiguration.
		Setter setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(configuration.getCommandGroupKey()));
		if (configuration.getCommandKey() != null && !("".equals(configuration.getCommandKey()))) {
			setter.andCommandKey(HystrixCommandKey.Factory.asKey(configuration.getCommandKey()));
		}
		
		//HystrixCommandProperties.Setter
		HystrixCommandProperties.Setter hcpSetter = HystrixCommandProperties.Setter();
		
		hcpSetter.withCircuitBreakerEnabled(configuration.isCircuitBreakerEnabled());
		if (configuration.getCircuitBreakerErrorThresholdPercentage() != null) {
			hcpSetter.withCircuitBreakerErrorThresholdPercentage(configuration.getCircuitBreakerErrorThresholdPercentage());
		}
		if (configuration.getCircuitBreakerRequestVolumeThreshold() != null) {
			hcpSetter.withCircuitBreakerRequestVolumeThreshold(configuration.getCircuitBreakerRequestVolumeThreshold());
		}
		if (configuration.getCircuitBreakerSleepWindowInMilliseconds() != null) {
			hcpSetter.withCircuitBreakerSleepWindowInMilliseconds(configuration.getCircuitBreakerSleepWindowInMilliseconds());
		}
		setter.andCommandPropertiesDefaults(hcpSetter);
		
		return setter;
	}

}
