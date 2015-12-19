package org.jboss.ddoyle.camel.hystrix.command;

import org.apache.camel.Exchange;

/**
 * Factory for {@link CamelHystrixCommand CamelHystrixCommands}. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface HystrixCommandFactory {
	
	public abstract CamelHystrixCommand getCamelHystrixCommand(Exchange exchange);
	
}
