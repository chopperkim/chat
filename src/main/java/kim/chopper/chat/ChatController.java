package kim.chopper.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Controller
public class ChatController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private List<Client> clientList = new ArrayList<>();

    @Autowired
    private ChatRepository repository;

    @Value("${socket.server.port}")
    private int port;

    @PostConstruct
    public void init() {
        int nThreads = Runtime.getRuntime().availableProcessors();
        logger.info("쓰레드 갯수: {}", nThreads);
        ExecutorService executor = new ThreadPoolExecutor(nThreads, nThreads * 10, 1L, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>());
        try {
            channelGroup = AsynchronousChannelGroup.withThreadPool(executor);
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
            if (serverChannel.isOpen()) {
                stopServer();
            }
            return;
        }
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                try {
                    logger.debug("[{}]{} 연결", currentTime(), result.getRemoteAddress().toString());
                    clientList.add(new Client(result, clientList, repository));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 다음 사용자 대기
                serverChannel.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                if (serverChannel.isOpen()) {
                    stopServer();
                }
            }
        });
        logger.debug("채널 서버 시작: {}", currentTime());
    }

    private void stopServer() {
        clientList.clear();
        if (channelGroup != null && !channelGroup.isShutdown()) {
            channelGroup.shutdown();
        }
    }

    private String currentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
