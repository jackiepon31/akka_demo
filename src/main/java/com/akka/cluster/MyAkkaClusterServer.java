package com.akka.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.ConfigFactory;
import static com.akka.cluster.TransformationMessages.BACKEND_REGISTRATION;

public class MyAkkaClusterServer extends UntypedActor {

    LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    Cluster cluster = Cluster.get(getContext().system());

    // subscribe to cluster changes  
    @Override  
    public void preStart() {  
        // #subscribe  
        cluster.subscribe(getSelf(), ClusterEvent.MemberUp.class);
        // #subscribe  
    }  

    // re-subscribe when restart  
    @Override  
    public void postStop() {  
        cluster.unsubscribe(getSelf());  
    }  

    @Override  
    public void onReceive(Object message) {
        if (message instanceof TransformationMessages.TransformationJob) {
            TransformationMessages.TransformationJob job = (TransformationMessages.TransformationJob) message;
            logger.info(job.getText());
            getSender().tell(new TransformationMessages.TransformationResult(job.getText().toUpperCase()), getSelf());

        } else if (message instanceof ClusterEvent.CurrentClusterState) {
            /**
             * 当前节点在刚刚加入集群时，会收到CurrentClusterState消息，从中可以解析出集群中的所有前端节点（即roles为frontend的），并向其发送BACKEND_REGISTRATION消息，用于注册自己
             */
            ClusterEvent.CurrentClusterState state = (ClusterEvent.CurrentClusterState) message;
            logger.info("本节点加入，通知其他client注册自己");
            for (Member member : state.getMembers()) {
            	logger.info(member.getRoles().toString());
                if (member.status().equals(MemberStatus.up())) {
                    register(member);
                }
            }

        } else if (message instanceof ClusterEvent.MemberUp) {
            /**
             * 有新的节点加入
             */
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            logger.info("有新节点加入");
            register(mUp.member());

        } else {
            unhandled(message);
        }

    }

    /**
     * 如果是客户端角色，则像客户端注册自己的信息。客户端收到消息以后会讲这个服务端存到本机服务列表中
     * @param member
     */
    void register(Member member) {
        if (member.hasRole("client"))
            getContext().actorSelection(member.address() + "/user/myAkkaClusterClient").tell(BACKEND_REGISTRATION, getSelf());
    }

    public static void main(String [] args){
        System.out.println("Start MyAkkaClusterServer");
        ActorSystem system = ActorSystem.create("akkaClusterTest", ConfigFactory.load("server.conf"));
        system.actorOf(Props.create(MyAkkaClusterServer.class), "myAkkaClusterServer");
        System.out.println("Started MyAkkaClusterServer");

    }
}