package cluster;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class Runner {
    public static void main(String[] args) {
        List<ActorSystem> actorSystems;

        if (args.length == 0) {
            String[] ports = new String[]{"2551", "2552", "0"};
            writef("Start cluster on default ports %s%n", (Object[]) ports);

            actorSystems = startup(ports);
        } else {
            writef("Start cluster on port(s) %s%n", (Object[]) args);
            actorSystems = startup(args);
        }

        writef("Hit enter to stop%n");
        readLine();

        for (ActorSystem actorSystem : actorSystems) {
            Cluster cluster = Cluster.get(actorSystem);
            cluster.leave(cluster.selfAddress());
        }
    }

    private static List<ActorSystem> startup(String[] ports) {
        List<ActorSystem> actorSystems = new ArrayList<>();

        for (String port : ports) {
            ActorSystem actorSystem = ActorSystem.create("cluster", setupConfig(port));

            actorSystem.actorOf(ClusterListenerActor.props(), "clusterListener");
            actorSystem.actorOf(ClusterAwareActor.props(), "clusterAware");

            actorSystems.add(actorSystem);
        }
        return actorSystems;
    }

    private static Config setupConfig(String port) {
        return ConfigFactory.parseString(
                String.format("akka.remote.netty.tcp.port=%s%n", port) +
                        String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load()
                );
    }

    private static void writef(String format, Object... args) {
        System.out.printf(format, args);
    }

    private static void readLine() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
