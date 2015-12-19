package org.jboss.ddoyle.camel.hystrix.command;

import org.apache.camel.Exchange;

import com.netflix.hystrix.HystrixExecutable;

public interface CamelHystrixCommand extends HystrixExecutable<Exchange> {

}
