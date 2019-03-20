## Akka Java Cluster Example

### Introduction

This is a Java, Maven, Akka project that demonstrates how to setup a basic
[Akka Cluster](https://doc.akka.io/docs/akka/current/index-cluster.html).

This project is one in a series of projects that starts with a simple Akka Cluster project and progressively builds up to examples of event sourcing and command query responsibility segregation.

The project series is composed of the following GitHub repos:
* [akka-java-cluster](https://github.com/mckeeh3/akka-java-cluster) (this project)
* [akka-java-cluster-aware](https://github.com/mckeeh3/akka-java-cluster-aware)
* [akka-java-cluster-singleton](https://github.com/mckeeh3/akka-java-cluster-singleton)
* [akka-java-cluster-sharding](https://github.com/mckeeh3/akka-java-cluster-sharding)
* [akka-java-cluster-persistence](https://github.com/mckeeh3/akka-java-cluster-persistence)
* [akka-java-cluster-persistence-query](https://github.com/mckeeh3/akka-java-cluster-persistence-query)

Each project can be cloned, built, and runs independently of the other projects.

This project contains an example implementation of a cluster. Here we will focus on cluster configuration and on running an Akka cluster with multiple nodes.

### About Akka Clustering

According to the [Akka documentation](https://doc.akka.io/docs/akka/current/common/cluster.html),
"*Akka Cluster provides a fault-tolerant decentralized peer-to-peer based cluster membership service with no single point of failure or single point of bottleneck. It does this using gossip protocols and an automatic failure detector.*

*Akka cluster allows for building distributed applications, where one application or service spans multiple nodes.*"

### Installation

~~~~bash
git clone https://github.com/mckeeh3/akka-java-cluster.git
cd akka-java-cluster
mvn clean package
~~~~

### Running from the command line

The following Maven command runs a signle JVM with 3 Akka actor systems on ports 2551, 2552, and a radmonly selected port.
~~~~bash
mvn exec:java
~~~~
To run on specific ports use the following `-D` option for passing in command line arguements.
~~~~bash
mvn exec:java -Dexec.args="2551"
~~~~
The default no arguments is equilevalant to the following.
~~~~bash
mvn exec:java -Dexec.args="2551 2552 0"
~~~~
A common way to run tests is to start single JVMs in multiple command windows. This simulates running a multi-node Akka cluster.
For example, run the following 4 commands in 4 command windows.
~~~~bash
mvn exec:java -Dexec.args="2551" > /tmp/$(basename $PWD)-1.log
~~~~
~~~~bash
mvn exec:java -Dexec.args="2552" > /tmp/$(basename $PWD)-2.log
~~~~
~~~~bash
mvn exec:java -Dexec.args="0" > /tmp/$(basename $PWD)-3.log
~~~~
~~~~bash
mvn exec:java -Dexec.args="0" > /tmp/$(basename $PWD)-4.log
~~~~
This runs a 4 node Akka cluster starting 2 nodes on ports 2551 and 2552, which are the cluster seed nodes as configured and the `application.conf` file.
And 2 nodes on randomly selected port numbers.
The optional redirect `> /tmp/$(basename $PWD)-4.log` is an example for pushing the log output to filenames based on the project direcctory name.

For convenience, in a Linux command shell define the following aliases.

~~~~bash
alias p1='cd ~/akka-java/akka-java-cluster'
alias p2='cd ~/akka-java/akka-java-cluster-aware'
alias p3='cd ~/akka-java/akka-java-cluster-singleton'
alias p4='cd ~/akka-java/akka-java-cluster-sharding'
alias p5='cd ~/akka-java/akka-java-cluster-persistence'
alias p6='cd ~/akka-java/akka-java-cluster-persistence-query'

alias m1='clear ; mvn exec:java -Dexec.args="2551" > /tmp/$(basename $PWD)-1.log'
alias m2='clear ; mvn exec:java -Dexec.args="2552" > /tmp/$(basename $PWD)-2.log'
alias m3='clear ; mvn exec:java -Dexec.args="0" > /tmp/$(basename $PWD)-3.log'
alias m4='clear ; mvn exec:java -Dexec.args="0" > /tmp/$(basename $PWD)-4.log'
~~~~

The p1-6 alias commands are shortcuts for cd'ing into one of the six project directories.
The m1-4 alias commands start and Akka node with the appropriate port. Stdout is also redirected to the /tmp directory.

### Run Scripts

The project contains 5 scripts that can be used to start and stop individual cluster nodes or start and stop a cluster of nodes.

Use the `./start-node N` and `./stop-node N` scripts to start and stop individual nodes. The N argument is the node number,
which must be between 1 and 9. The start script will start an Akka node running on port 255N. Both `stdin` and `stderr`
output is set to a file in the `/tmp` directory using the naming convention `/tmp/<project-dir-name>-N.log`.

Start node 1 on port 2551 and node 2 on port 2552.
~~~bash
./start-node 1
./start-node 2
~~~

Stop node 3 on port 2553.
~~~bash
./stop-node 3
~~~

Use the `./start-cluster N` and `./stop-cluster` scripts to start and stop multiple cluster nodes. The N argument is the
number of cluster nodes to be started. The `./stop-cluster` script stops all current running nodes.

Start a cluster of four nodes on ports 2551, 2552, 2553, and 2554.
~~~bash
./start-cluster 4
~~~

Stop all currently running cluster nodes.
~~~bash
./stop-cluster
~~~

You can use the `start-cluster` script to start multiple nodes and then use `start-node` and `stop-node`
to start and stop individual nodes.

Use the `./tail-node N` script to `tail -f` the log file for the node N.
