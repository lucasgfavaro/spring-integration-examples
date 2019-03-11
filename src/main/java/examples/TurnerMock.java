package examples;

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

@Configuration
@EnableIntegration
@IntegrationComponentScan
@SpringBootApplication
public class TurnerMock {

	// CARACTERISTICAS A PROBAR
	// * Entender THROUGHPUT (Ver cuantos puede atender en paralelo)
	// * Ver como liberar el thread inicial inmendiatamente dando una respuesta
	
	
	
	private static Log logger = LogFactory.getLog(TurnerMock.class);

	@Bean
	public HttpRequestHandlingMessagingGateway esbHttpGateWay() {
		HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway(true);

		RequestMapping requestMapping = new RequestMapping();
		requestMapping.setMethods(HttpMethod.POST, HttpMethod.OPTIONS);
		requestMapping.setPathPatterns("esb/api/v1/catalogue");
		requestMapping.setConsumes("application/json");
		requestMapping.setProduces("application/json");
		Map<String, Expression> headerExpressions = new HashMap<>();
		gateway.setHeaderExpressions(headerExpressions);
		gateway.setCountsEnabled(true);
		gateway.setRequestMapping(requestMapping);
		gateway.setRequestChannel(esbRequestChannel());
		gateway.setReplyChannelName("esbOutBoundChannel");
		gateway.setReplyMapper(null);
		gateway.setAutoStartup(true);
		//gateway.setErrorChannelName("esbInFlowErrorChannel");
		return gateway;
	}

	@Bean
	public DirectChannel esbRequestChannel() {
		return MessageChannels.direct().get();
	}
	
	@Bean
	public DirectChannel esbOutBoundChannel() {
		return MessageChannels.direct().get();
	}

	@Bean
	@Autowired
	public IntegrationFlow createCatalogueChannelFlow() {
		return IntegrationFlows.from(esbRequestChannel())
				.transform(Transformers.fromJson(TestPayload.class))
				.handle((p, h) -> {
					Long number = Long.parseLong(((TestPayload)p).getNumber());
					
					System.out.println("COMIENZA PROCESO " + number);
									
					try {
						Thread.sleep(10000* number);
					} catch (Exception e) {

					}
					
					System.out.println("FINALIZA PROCESO " + number);
					
					return p;
				})
				// Log input for tracking purposes
				.get();
	}

    public static void main(String[] args) {
        SpringApplication.run(TurnerMock.class, args);
    }
}
