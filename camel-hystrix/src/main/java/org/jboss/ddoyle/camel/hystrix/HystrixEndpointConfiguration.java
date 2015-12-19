package org.jboss.ddoyle.camel.hystrix;

import org.apache.camel.spi.UriParams;

import com.netflix.hystrix.HystrixCommand;


/**
 * Holds the Hystrix configuration options for our {@link HystrixCommand HystrixCommands}. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@UriParams
public class HystrixEndpointConfiguration {

	/* ------ Hystrix Command key options -------- */
	private String commandKey = "camelCommand";

	private String commandGroupKey = "camelCommandGroup";
	
	
	/* ------ Hystrix CircuitBreaker configuration options -------- */
	private boolean circuitBreakerEnabled = true;
	
	private Integer circuitBreakerErrorThresholdPercentage;
	
	private Integer circuitBreakerRequestVolumeThreshold;
	
	private Integer circuitBreakerSleepWindowInMilliseconds;


	/* ------ Getters & Setters -------- */
	
	public String getCommandKey() {
		return commandKey;
	}

	public void setCommandKey(String commandKey) {
		this.commandKey = commandKey;
	}

	public String getCommandGroupKey() {
		return commandGroupKey;
	}

	public void setCommandGroupKey(String commandGroupKey) {
		this.commandGroupKey = commandGroupKey;
	}

	public boolean isCircuitBreakerEnabled() {
		return circuitBreakerEnabled;
	}

	public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
		this.circuitBreakerEnabled = circuitBreakerEnabled;
	}

	public Integer getCircuitBreakerErrorThresholdPercentage() {
		return circuitBreakerErrorThresholdPercentage;
	}

	public void setCircuitBreakerErrorThresholdPercentage(Integer circuitBreakerErrorThresholdPercentage) {
		this.circuitBreakerErrorThresholdPercentage = circuitBreakerErrorThresholdPercentage;
	}

	public Integer getCircuitBreakerRequestVolumeThreshold() {
		return circuitBreakerRequestVolumeThreshold;
	}

	public void setCircuitBreakerRequestVolumeThreshold(Integer circuitBreakerRequestVolumeThreshold) {
		this.circuitBreakerRequestVolumeThreshold = circuitBreakerRequestVolumeThreshold;
	}

	public Integer getCircuitBreakerSleepWindowInMilliseconds() {
		return circuitBreakerSleepWindowInMilliseconds;
	}

	public void setCircuitBreakerSleepWindowInMilliseconds(Integer circuitBreakerSleepWindowInMilliseconds) {
		this.circuitBreakerSleepWindowInMilliseconds = circuitBreakerSleepWindowInMilliseconds;
	}
	
	
}

