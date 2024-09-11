package com.joshmalbrecht.broker_demo;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.FailoverEventListener;
import org.apache.activemq.artemis.api.core.client.FailoverEventType;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

public class BrokerDemoApplication {

	// Primary: 172.25.0.3
	// Backup: 172.25.0.2

	public static final String url = "tcp://localhost:61616?ha=true&retryInterval=1000&retryIntervalMultiplier=1.0&failoverAttempts=10";

	public static void main(String[] args) throws Exception {
		ServerLocator locator = ActiveMQClient.createServerLocator(url);

		System.out.println("size: " + locator.getTopology().getMembers().size());

		locator.getTopology().getMembers().forEach(m -> {
			System.out.println("Primary: " + m.getPrimary().getName());
			System.out.println("Backup: " + m.getBackup().getName());
		});

		final ClientSessionFactory sessionFactory = locator.createSessionFactory();

		sessionFactory.addFailoverListener(new FailoverEventListener() {

			@Override
			public void failoverEvent(FailoverEventType eventType) {
				try {
					System.out.println("failover event: " + eventType.name());
					ClientSession newSession = sessionFactory
						.createSession("artemis", "artemis", true, true, true, true, 10);
					System.out.println("New session created after failover event");
					process(newSession);
				} catch (ActiveMQException e) {
					System.out.println("failed to recreate session during failover event");
					e.printStackTrace();
				}
			}
			
		});

		System.out.println("Session created");
		ClientSession session = sessionFactory
			.createSession("artemis", "artemis", true, true, true, true, 10);

		process(session);
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
		finally {
			if (session != null) {
				session.close();
			}
		}
	}
}
