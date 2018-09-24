package cluster;

import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class Runner {
    public static void main(String[] args) {
        List<ActorSystem> actorSystems = args.length == 0
                ? startupClusterNodes(Arrays.asList("2551", "2552", "0"))
                : startupClusterNodes(Arrays.asList(args));

        hitEnterToStop();

        actorSystems.forEach(actorSystem -> {
            Cluster cluster = Cluster.get(actorSystem);
            cluster.leave(cluster.selfAddress());
        });
    }

    private static List<ActorSystem> startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);
        List<ActorSystem> actorSystems = new ArrayList<>();

        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));
            actorSystems.add(actorSystem);

            actorSystem.actorOf(ClusterListenerActor.props(), "clusterListener");

            addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());

            actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
        });

        return actorSystems;
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port) +
                        String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }

    private static void addCoordinatedShutdownTask(ActorSystem actorSystem, String coordindateShutdownPhase) {
        CoordinatedShutdown.get(actorSystem).addTask(
                coordindateShutdownPhase,
                coordindateShutdownPhase,
                () -> {
                    actorSystem.log().warning("Coordinated shutdown phase {}", coordindateShutdownPhase);
                    return CompletableFuture.completedFuture(Done.getInstance());
                });
    }

    private static void hitEnterToStop() {
        System.out.println("Hit Enter to stop");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
