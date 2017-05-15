/**
 * 
 */
/**
 * @author root
 *
 */
package com.akka.procedure;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;

public class ProcedureTest extends UntypedActor {
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	public enum Msg {
		PLAY, SLEEP, CLOSE;
	}

	Procedure<Object> happy=new Procedure<Object>(){
		@Override public void apply(Object o)throws Exception
		{log.info("i am happy!"+o);
		if(o==Msg.PLAY){
			getSender().tell("i am alrady happy!!",getSelf());
			log.info("i am alrady happy!!");}
		else if(o==Msg.SLEEP)
		{
			log.info("i do not like sleep!");
			getContext().become(angray);
		}else{unhandled(o);
		}
		}
		};
	Procedure<Object> angray=new Procedure<Object>(){
		@Override
		public void apply(Object o)throws Exception
		{log.info("i am angray! "+o);
		if(o==Msg.SLEEP){
			getSender().tell("i am alrady angray!!",getSelf());
			log.info("i am alrady angray!!");
			}else if(o==Msg.PLAY){
				log.info("i like play.");
				getContext().become((Procedure<Object>)happy);
				}else{
					unhandled(o);
					}
		}};

	@Override
	public void onReceive(Object o) throws Throwable {
		log.info("onReceive msg: " + o);
	    if(o == Msg.SLEEP){
	        getContext().become(angray);
	    }else if(o == Msg.PLAY){
	        getContext().become(happy);
	    }else {
	        unhandled(o);
	    }
    }
	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("procedure", ConfigFactory.load("akka.config"));
		ActorRef actorRef = system.actorOf(Props.create(ProcedureTest.class), "ProcedureTest");
		actorRef.tell(Msg.PLAY, actorRef.noSender());
		actorRef.tell(Msg.SLEEP, actorRef.noSender());
		actorRef.tell(Msg.PLAY, actorRef.noSender());
		actorRef.tell(Msg.PLAY, actorRef.noSender());
		actorRef.tell(PoisonPill.getInstance(),actorRef.noSender());
	}
}