package cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberJoined;
import akka.cluster.ClusterEvent.MemberLeft;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberWeaklyUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;

class ClusterListenerActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(getContext().getSystem());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CurrentClusterState.class, this::currentClusterState)
                .match(MemberUp.class, this::memberUp)
                .match(MemberEvent.class, this::memberEvent)
                .match(MemberJoined.class, this::memberJoined)
                .match(MemberLeft.class, this::memberLeft)
                .match(MemberRemoved.class, this::memberRemoved)
                .match(MemberWeaklyUp.class, this::memberWeaklyUp)
                .match(UnreachableMember.class, this::unreachableMember)
                .build();
    }

    @Override
    public void preStart() {
        log().debug("Start");
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                MemberEvent.class,
                UnreachableMember.class);
    }

    @Override
    public void postStop() {
        log().debug("Stop");
    }

    static Props props() {
        return Props.create(ClusterListenerActor.class);
    }

    private void currentClusterState(CurrentClusterState currentClusterState) {
        log().info("{}", currentClusterState);
        logClusterMembers(currentClusterState);
    }

    private void memberUp(MemberUp memberUp) {
        log().info("{}", memberUp);
        logClusterMembers();
    }

    private void memberEvent(MemberEvent memberEvent) {
        log().info("{}", memberEvent);
        logClusterMembers();
    }

    private void memberJoined(MemberJoined memberJoined) {
        log().info("{}", memberJoined);
        logClusterMembers();
    }

    private void memberLeft(MemberLeft memberLeft) {
        log().info("{}", memberLeft);
        logClusterMembers();
    }

    private void memberRemoved(MemberRemoved memberRemoved) {
        log().info("{}", memberRemoved);
        logClusterMembers();
    }

    private void memberWeaklyUp(MemberWeaklyUp memberWeaklyUp) {
        log().info("{}", memberWeaklyUp);
        logClusterMembers();
    }

    private void unreachableMember(UnreachableMember unreachableMember) {
        log().info("{}", unreachableMember);
        logClusterMembers();
    }

    private void logClusterMembers() {
        logClusterMembers(cluster.state());
    }

    private void logClusterMembers(CurrentClusterState currentClusterState) {
        int count = 0;
        for (Member member : currentClusterState.getMembers()) {
            log().info(" {} {}", ++count, member);
        }
    }
}
