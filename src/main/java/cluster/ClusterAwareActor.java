package cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.Member;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class ClusterAwareActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(getContext().getSystem());
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
        log().debug("tick {}", me);

        for (Member member : cluster.state().getMembers()) {
            if (!me.equals(member)) {
                tick(member);
            }
        }
    }

    private void tick(Member member) {
        String path = member.address().toString() + getSelf().path().toStringWithoutAddress();
        ActorSelection actorSelection = getContext().actorSelection(path);
        log().debug("ping -> {}", actorSelection);
        actorSelection.tell("ping", getSelf());
    }

    private void ping() {
        log().debug("ping <- {}", getSender());
        getSender().tell("pong", getSelf());
    }

    private void pong() {
        log().debug("pong <- {}", getSender());
    }

    @Override
    public void preStart() {
        log().debug("start");
        ticker = getContext().getSystem().scheduler()
                .schedule(Duration.Zero(),
                        tickInterval,
                        getSelf(),
                        "tick",
                        getContext().getSystem().dispatcher(),
                        null);
    }

    @Override
    public void postStop() {
        ticker.cancel();
        log().debug("stop");
    }

    static Props props() {
        return Props.create(ClusterAwareActor.class);
    }
}
