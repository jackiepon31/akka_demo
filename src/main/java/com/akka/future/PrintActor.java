/**
 * 
 */
/**
 * @author root
 *
 */
package com.akka.future;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class PrintActor extends UntypedActor{
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	@Override
	public void onReceive(Object arg0) throws Throwable {
		// TODO Auto-generated method stub
		if(arg0 instanceof Integer){
			log.info("print:"+arg0);
		}else{
			unhandled(arg0);
		}
	}
	
}