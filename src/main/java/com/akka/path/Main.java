package com.akka.path;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

public class Main {

  public static void main(String[] args) {
    //创建ActorSystem。一般来说，一个系统只需要一个ActorSystem。
    //参数1：系统名称。参数2：配置文件
    ActorSystem system = ActorSystem.create("Hello", ConfigFactory.load("akka.config"));
    ActorRef myWork = system.actorOf(Props.create(MyWork.class), "MyWork");
    ActorRef watchActor = system.actorOf(Props.create(WatchActor.class, myWork), "WatchActor");

    myWork.tell(MyWork.Msg.WORKING, ActorRef.noSender());
    myWork.tell(MyWork.Msg.DONE, ActorRef.noSender());

    //中断myWork
    myWork.tell(PoisonPill.getInstance(), ActorRef.noSender());
  }
}
