package cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;

import java.time.Duration;

class ClusterListenerActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(context().system());
    private Cancellable showClusterStateCancelable;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ShowClusterState.class, this::showClusterState)
                .matchAny(this::logClusterEvent)
                .build();
    }

    private void showClusterState(ShowClusterState showClusterState) {
        log().info("{} sent to {}", showClusterState, cluster.selfMember());
        logClusterMembers(cluster.state());
        showClusterStateCancelable = null;
    }

    private void logClusterEvent(Object clusterEventMessage) {
        log().info("{} sent to {}", clusterEventMessage, cluster.selfMember());
        logClusterMembers();
    }

    @Override
    public void preStart() {
        log().debug("Start");
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent.class);
    }

    @Override
    public void postStop() {
        log().debug("Stop");
        cluster.unsubscribe(self());
    }

    static Props props() {
        return Props.create(ClusterListenerActor.class);
    }

    private void logClusterMembers() {
        logClusterMembers(cluster.state());

        if (showClusterStateCancelable == null) {
            showClusterStateCancelable = context().system().scheduler().scheduleOnce(
                    Duration.ofSeconds(15),
                    self(),
                    new ShowClusterState(),
                    context().system().dispatcher(),
                    null);
        }
    }

    private void logClusterMembers(CurrentClusterState currentClusterState) {
        Member oldest = cluster.selfMember();
        for (Member member : currentClusterState.getMembers()) {
            if (member.isOlderThan(oldest)) {
                oldest = member;
            }
        }

        int count = 0;
        for (Member member : currentClusterState.getMembers()) {
            if (member.equals(oldest)) {
                log().info(" {} (OLDEST) {}", ++count, member);
            } else {
                log().info(" {} {}", ++count, member);
            }
        }
    }

    private static class ShowClusterState {
        @Override
        public String toString() {
            return ShowClusterState.class.getSimpleName();
        }
    }
}
