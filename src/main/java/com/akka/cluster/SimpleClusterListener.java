/**
 * 
 */
/**
 * @author root
 *
 */
package com.akka.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.ConfigFactory;

public class SimpleClusterListener extends UntypedActor{
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    Cluster cluster = Cluster.get(getContext().system());
    //subscribe to cluster changes

    @Override
    public void preStart() throws Exception {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    @Override
    public void postStop() throws Exception {
        cluster.unsubscribe(getSelf());
    }

    public void onReceive(Object message) throws Throwable {
        if(message instanceof ClusterEvent.MemberUp){
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            log.info("Member is up:{}",mUp.member());
        }else if(message instanceof ClusterEvent.UnreachableMember){
            ClusterEvent.UnreachableMember un = (ClusterEvent.UnreachableMember)message;
            log.info("member detected as unreachable:{}",un.member());
        }else{
            unhandled(message);
        }
    }

    public static void main(String [] args){
        System.out.println("start monitor");
        ActorSystem system = ActorSystem.create("akkaClusterTest", ConfigFactory.load("application.conf"));
        system.actorOf(Props.create(SimpleClusterListener.class),"simpleclusterlisterner");
        System.out.println("started monitor");
    }
}