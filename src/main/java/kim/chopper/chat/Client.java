package kim.chopper.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Client {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AsynchronousSocketChannel channel;
    private final List<Client> clientList;
    private final ChatRepository repository;
    private String nickName;
    private String address;
    private int portNumber;

    public Client(AsynchronousSocketChannel channel, List<Client> clientList, ChatRepository repository) {
        this.channel = channel;
        this.clientList = clientList;
        this.repository = repository;
        try {
            String[] addressArr = channel.getRemoteAddress().toString().split(":");
            address = addressArr[0];
            portNumber = Integer.parseInt(addressArr[1]);
        } catch (Exception e) {
            e.printStackTrace();
            address = "127.0.0.1";
            portNumber = 0;
        }
        receive();
    }

    // 클라이언트로부터 데이터 받기
    private void receive() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        channel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1) {
                    disconnectClient();
                    return;
                }
                attachment.flip();
                String data = Charset.forName(StandardCharsets.UTF_8.name()).decode(attachment).toString();
                logger.debug(data);
                // 2020/09/16 메시지 전송되었으므로 DB에 저장
                if (data.contains(":")) {
                    String[] dataArr = data.split(":");
                    String msg = dataArr[1].trim();
                    if (msg.equals("newUser")) {
                        nickName = dataArr[0].trim();
                    } else {
                        repository.save(new Chat(nickName, msg, address, portNumber));
                    }
                }
                // 받은 데이터를 현재 접속한 모든 클라이언트에게 전달
                for (Client client : clientList) {
                    client.send(data);
                }

                // 다시 다음 데이터 받을 준비
//                ByteBuffer buffer = ByteBuffer.allocate(128);
                byteBuffer.clear();
                channel.read(byteBuffer, byteBuffer, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                disconnectClient();
            }
        });
    }

    private void send(String msg) {
        ByteBuffer byteBuffer = Charset.forName(StandardCharsets.UTF_8.name()).encode(msg);
        channel.write(byteBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {

            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                disconnectClient();
            }
        });
    }

    private void disconnectClient() {
        logger.debug("{}님 연결 종료", nickName);
        clientList.remove(this);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
