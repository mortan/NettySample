import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class Client {
    private static final Logger log = LogManager.getLogger(Client.class);

    public static void main(String[] args) throws InterruptedException {
        log.info(String.format("Starting up in thread: %d", Thread.currentThread().getId()));

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            List<Channel> channelList = new ArrayList<>();
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ClientInitializer());

            for (int i = 1; i <= 1; i++) {
                // Connect to the server sample application
                Channel ch = b.connect("localhost", 8888).sync().channel();

                // Create some http content
                ByteBuf buffer = Unpooled.copiedBuffer(String.format("Hello from client %d", i), CharsetUtil.UTF_8);

                // Create the request
                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
                request.headers().add(CONTENT_TYPE, "text/plain");
                request.headers().add(CONTENT_LENGTH, buffer.readableBytes());
                request.content().clear().writeBytes(buffer);

                log.info(String.format("Sending message from client %d to server", i));

                // Send the request.
                ch.writeAndFlush(request);
                channelList.add(ch);

                // Wait some time before before continuing with the next http request
                Thread.sleep(2000);
            }

            channelList.forEach(Channel::close);

        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }
    }
}
