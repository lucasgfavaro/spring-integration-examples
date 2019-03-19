package examples.httpInboundGateway;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;

import examples.TestPayload;

@Configuration
@EnableIntegration
@IntegrationComponentScan(value = { "examples.httpGateway" })
@SpringBootApplication
public class HttpInboundGateway {

	private static Log logger = LogFactory.getLog(HttpInboundGateway.class);

	@Bean
	public HttpRequestHandlingMessagingGateway httpGateWay() {
		HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway(false);

		RequestMapping requestMapping = new RequestMapping();
		requestMapping.setMethods(HttpMethod.POST, HttpMethod.OPTIONS);
		requestMapping.setPathPatterns("/resource");
		requestMapping.setConsumes("application/json");
		requestMapping.setProduces("application/json");
		
		Map<String, Expression> headerExpressions = new HashMap<>();
		gateway.setHeaderExpressions(headerExpressions);
		gateway.setCountsEnabled(true);
		gateway.setRequestMapping(requestMapping);
		gateway.setRequestChannel(requestChannel());
		gateway.setReplyChannel(outBoundChannel());
		gateway.setReplyMapper(null);
		gateway.setAutoStartup(true);
		gateway.setReplyTimeout(0);
		gateway.setErrorChannelName("errorChannel");
		return gateway;
	}

	@Bean
	public DirectChannel requestChannel() {
		return MessageChannels.direct().get();
	}
	
	@Bean
	public DirectChannel outBoundChannel() {
		return MessageChannels.direct().get();
	}
	
	@Bean
	public DirectChannel errorChannel() {
		return MessageChannels.direct().get();
	}
	
	@Bean
	@Autowired
	public IntegrationFlow channelFlow() {
		return IntegrationFlows.from(requestChannel())
				.transform(Transformers.fromJson(TestPayload.class))
				.handle((p, h) -> {
					Long number = Long.parseLong(((TestPayload)p).getNumber());
				
					System.out.println("HANDLER STARTS" + number);
									
					try {
						Thread.sleep(1000 * number);
					} catch (Exception e) {}
					
					System.out.println("HANDLER ENDS" + number);
					
					return p;
				})
				.handle((p,h) -> {return null;})
				.get();
	}

    public static void main(String[] args) {
        SpringApplication.run(HttpInboundGateway.class, args);
    }
}
