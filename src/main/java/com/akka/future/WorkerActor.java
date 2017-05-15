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

public class WorkerActor extends UntypedActor{
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	@Override
	public void onReceive(Object arg0) throws Throwable {
		// TODO Auto-generated method stub
		log.info("akka.future.WorkerActor.onReceive:" + arg0);
		if(arg0 instanceof Integer){
			Thread.sleep(1000);
            int i = Integer.parseInt(arg0.toString());
            getSender().tell(i*i, getSelf());
		}else{
			unhandled(arg0);
		}
	}
	
}