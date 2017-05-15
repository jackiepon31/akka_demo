package com.akka.future;

import java.util.concurrent.TimeUnit;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ActorSystem actorSystem = ActorSystem.create("system", ConfigFactory.load("akka.config"));
		ActorRef printActor = actorSystem.actorOf(Props.create(PrintActor.class), "printActor");
		ActorRef workerActor = actorSystem.actorOf(Props.create(WorkerActor.class), "workerActor");
		Future<Object> future = Patterns.ask(workerActor, 5, 1000);
		int result = (int)Await.result(future,Duration.create(3, TimeUnit.SECONDS));
		System.out.println("result:"+result);
		Future<Object> future1 = Patterns.ask(workerActor, 8, 1000);
		Patterns.pipe(future1, actorSystem.dispatcher()).to(printActor);
	}

}
