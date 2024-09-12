package com.joshmalbrecht.broker_demo;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;

import java.util.HashMap;
import java.util.Map;

public class BrokerDemoApplication {

	public static void main(String[] args) throws Exception {

		// Since the ActiveMQ brokers run in their own network you must get the IP addresses of the containers
		// using `docker network inspect <Network Name>`. You must also map the IP address to 'primary' and 
		// 'backup' in your /etc/hosts file so the hostname can be resolved to the right IP address. This is
		// because the cluster topology is sent from the ActiveMQ server to the clien after the initial
		//connection is made.
		
		Map<String, Object> primaryMap = new HashMap<String, Object>();
		primaryMap.put("host", "192.168.0.2");
		primaryMap.put("port", "61616");
		TransportConfiguration primary = new TransportConfiguration(NettyConnectorFactory.class.getName(), primaryMap);

		HashMap<String, Object> backupMap = new HashMap<String, Object>();
		backupMap.put("host", "192.168.0.3");
		backupMap.put("port", "61616");
		TransportConfiguration backup = new TransportConfiguration(NettyConnectorFactory.class.getName(), backupMap);

		ServerLocator locator = ActiveMQClient.createServerLocatorWithHA(primary, backup)
			.setReconnectAttempts(10)
			.setRetryInterval(1000)
			.setRetryIntervalMultiplier(1.0);
			// .setFailoverAttempts(1);

		ClientSessionFactory sessionFactory = locator.createSessionFactory();

		try {	
			while (true) {
				ClientSession session = sessionFactory
					.createSession("artemis", "artemis", true, true, true, true, 10);
	
				System.out.println("Session created");

				process(session);
			}
		}
		finally {
			sessionFactory.close();
		}
	}

	public static void process(ClientSession session) throws ActiveMQException {
		System.out.println("starting to process");
		try {
			while (true) {
				session.commit();
				Thread.sleep(100);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
