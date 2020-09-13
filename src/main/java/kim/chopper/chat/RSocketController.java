package kim.chopper.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
public class RSocketController {
    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    @MessageMapping("channel")
    Flux<Chat> channel(Flux<Duration> settings) {
        return settings
                .doOnNext(setting -> logger.info("Channel frequency setting is {} second(s).", setting.getSeconds()))
                .doOnCancel(() -> logger.warn("The client cancelled the channel."))
                .switchMap(setting -> Flux.interval(setting).map(index -> new Chat("", "")));
    }
}
