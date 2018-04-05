package cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class ClusterAwareActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(context().system());
    private final FiniteDuration tickInterval = Duration.create(10, TimeUnit.SECONDS);
    private Cancellable ticker;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("tick", s -> tick())
                .matchEquals("ping", p -> ping())
                .matchEquals("pong", p -> pong())
                .build();
    }

    private void tick() {
        Member me = cluster.selfMember();
        log().debug("Tick {}", me);

        for (Member member : cluster.state().getMembers()) {
            if (!me.equals(member) && member.status().equals(MemberStatus.up())) {
                tick(member);
            }
        }
    }

    private void tick(Member member) {
        String path = member.address().toString() + self().path().toStringWithoutAddress();
        ActorSelection actorSelection = context().actorSelection(path);
        log().debug("Ping -> {}", actorSelection);
        actorSelection.tell("ping", self());
    }

    private void ping() {
        log().debug("Ping <- {}", sender());
        sender().tell("pong", self());
    }

    private void pong() {
        log().debug("Pong <- {}", sender());
    }

    @Override
    public void preStart() {
        log().debug("Start");
        ticker = context().system().scheduler()
                .schedule(Duration.Zero(),
                        tickInterval,
                        self(),
                        "tick",
                        context().system().dispatcher(),
                        null);
    }

    @Override
    public void postStop() {
        ticker.cancel();
        log().debug("Stop");
    }

    static Props props() {
        return Props.create(ClusterAwareActor.class);
    }
}
