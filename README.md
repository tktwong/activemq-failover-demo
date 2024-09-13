
# ActiveMQ Failover Testing

This repo is for testing how the ActiveMQ core Java client can successfully recover from a failover.

## Steps to test

1. Run `docker-compose up -d` to start the primary and backup broker containers.
2. Run `docker network inspect activemq-failover-demo_default` to get the IP addresses of the containers.
3. Add an entry in `/etc/hosts` to resolve the `primary` and `backup` hostname to your container IP addresses.
4. Update `BrokerDemoApplication.java` with your IP addresses.
5. Run the main method.
6. Stop the primary container to create a failover event.
