package com.akka.router;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.akka.inbox.InboxTest;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

/**
 * Created by pengyk on 2017/1/12.
 */
public class RouterTest extends UntypedActor {
	 public Router router;
	 {
	        ArrayList<Routee> routees = new ArrayList<Routee>();
	        for(int i = 0; i < 5; i ++) {
	            //借用上面的 inboxActor
	            ActorRef worker = getContext().actorOf(Props.create(InboxTest.class), "worker_" + i);
	            getContext().watch(worker);//监听
	            routees.add(new ActorRefRoutee(worker));
	        }
	        /**
	         * RoundRobinRoutingLogic: 轮询
	         * BroadcastRoutingLogic: 广播
	         * RandomRoutingLogic: 随机
	         * SmallestMailboxRoutingLogic: 空闲
	         */
	        router = new Router(new RoundRobinRoutingLogic(), routees);
	    }
	 @Override
	    public void onReceive(Object o) throws Throwable {
	        if(o instanceof InboxTest.Msg){
	            router.route(o, getSender());//进行路由转发
	        }else if(o instanceof Terminated){
	            router = router.removeRoutee(((Terminated)o).actor());//发生中断，将该actor删除。当然这里可以参考之前的actor重启策略，进行优化，为了简单，这里仅进行删除处理
	            System.out.println(((Terminated)o).actor().path() + " 该actor已经删除。router.size=" + router.routees().toList().size());
	            if(router.routees().toList().size() == 0){//没有可用actor了
	                System.out.print("没有可用actor了，系统关闭。");
	                flag.compareAndSet(true, false);
	                getContext().system().shutdown();
	            }
	        }else {
	            unhandled(o);
	        }

	    }


	    public  static AtomicBoolean flag = new AtomicBoolean(true);

	    public static void main(String[] args) throws InterruptedException {
	        ActorSystem system = ActorSystem.create("strategy", ConfigFactory.load("akka.config"));
	        ActorRef routerTest = system.actorOf(Props.create(RouterTest.class), "RouterTest");

	        int i = 1;
	        while(flag.get()){
	            routerTest.tell(InboxTest.Msg.WORKING, ActorRef.noSender());

	            if(i % 10 == 0) routerTest.tell(InboxTest.Msg.CLOSE, ActorRef.noSender());

	            Thread.sleep(500);

	            i ++;
	        }
	    }
	}
