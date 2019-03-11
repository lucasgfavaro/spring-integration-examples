package examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Channels;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;



@Configuration
@EnableIntegration
@IntegrationComponentScan
public class HelloWorldApp {

	private static Log logger = LogFactory.getLog(HelloWorldApp.class);

	@Bean
	public PollableChannel outputChannel() {
		return new QueueChannel();
	}

	@Bean
	public IntegrationFlow upcaseFlow() {
		return IntegrationFlows.from("inputChannel").handle((p, h) -> {
			return p;
		})
				.channel("outputChannel").get();
	}

	public static void main(final String... args) {
		final AbstractApplicationContext context = new AnnotationConfigApplicationContext(HelloWorldApp.class);
		MessageChannel inputChannel = context.getBean("inputChannel", MessageChannel.class);
		PollableChannel outputChannel = context.getBean("outputChannel", PollableChannel.class);
		inputChannel.send(new GenericMessage<String>("World FUNCIONA 1"));
		inputChannel.send(new GenericMessage<String>("World FUNCIONA 2"));
		logger.info("==> HelloWorldDemo: " + outputChannel.receive(0).getPayload());
		logger.info("==> HelloWorldDemo: " + outputChannel.receive(0).getPayload());
		context.close();
	}
}
