package cluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberJoined;
import akka.cluster.ClusterEvent.MemberWeaklyUp;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberExited;
import akka.cluster.ClusterEvent.MemberLeft;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.ClusterEvent.ReachableMember;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.LeaderChanged;
import akka.cluster.ClusterEvent.ReachabilityEvent;
import akka.cluster.Member;

class ClusterListenerActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(context().system());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CurrentClusterState.class, this::currentClusterState)
                .match(MemberJoined.class, this::memberJoined)
                .match(MemberWeaklyUp.class, this::memberWeaklyUp)
                .match(MemberUp.class, this::memberUp)
                .match(MemberExited.class, this::memberExited)
                .match(MemberLeft.class, this::memberLeft)
                .match(MemberRemoved.class, this::memberRemoved)
                .match(UnreachableMember.class, this::unreachableMember)
                .match(ReachableMember.class, this::reachableMember)
                .match(LeaderChanged.class, this::leaderChanged)
                .build();
    }

    @Override
    public void preStart() {
        log().debug("Start");
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                MemberEvent.class,
                ReachabilityEvent.class,
                LeaderChanged.class);
    }

    @Override
    public void postStop() {
        log().debug("Stop");
        cluster.unsubscribe(self());
    }

    static Props props() {
        return Props.create(ClusterListenerActor.class);
    }

    private void currentClusterState(CurrentClusterState currentClusterState) {
        log().info("{}", currentClusterState);
        logClusterMembers(currentClusterState);
    }

    private void memberJoined(MemberJoined memberJoined) {
        log().info("{}", memberJoined);
        logClusterMembers();
    }

    private void memberWeaklyUp(MemberWeaklyUp memberWeaklyUp) {
        log().info("{}", memberWeaklyUp);
        logClusterMembers();
    }

    private void memberUp(MemberUp memberUp) {
        log().info("{}", memberUp);
        logClusterMembers();
    }

    private void memberExited(MemberExited memberExited) {
        log().info("{}", memberExited);
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

    private void unreachableMember(UnreachableMember unreachableMember) {
        log().info("{}", unreachableMember);
        logClusterMembers();
    }

    private void reachableMember(ReachableMember reachableMember) {
        log().info("{}", reachableMember);
        logClusterMembers();
    }

    private void leaderChanged(LeaderChanged leaderChanged) {
        log().info("{}", leaderChanged);
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
