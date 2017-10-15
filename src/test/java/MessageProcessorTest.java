import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MessageProcessorTest {

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class, "jmsservice.war")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml").addClass(FuseMessage.class);
	}

	@Resource(mappedName = "/jms/queue/test")
	Queue testQueue;

	@Resource(mappedName = "/ConnectionFactory")
	ConnectionFactory factory;

	@Test
	public void shouldBeAbleToSendMessage() throws Exception {
		Connection connection = null;
		Session session = null;
		try {
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			MessageProducer producer = session.createProducer(testQueue);
			MessageConsumer consumer = session.createConsumer(testQueue);

			connection.start();

			Message request = session.createObjectMessage(new FuseMessage());

			producer.send(request);
			Message response = consumer.receive(5 * 1000);
			assertNotNull(response);
			FuseMessage responseBody = ((ObjectMessage) response).getBody(FuseMessage.class);
			assertEquals("knock, knock!", responseBody.payload);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

}
