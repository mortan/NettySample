import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerHandler extends SimpleChannelInboundHandler<Object> implements GoogleResponseHandler {
    private final Logger logger = LogManager.getLogger(ServerHandler.class);

    private EventExecutor executor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            logger.info("Got request from client: " + request.content().toString(CharsetUtil.UTF_8));

            Bootstrap b = new Bootstrap();
            b.group(ctx.channel().eventLoop().parent()).channel(NioSocketChannel.class).handler(new GoogleInitializer(this));
            httpRequestToGoogle(b);
        }
    }

    private static void httpRequestToGoogle(Bootstrap b) throws InterruptedException {
        Channel ch = b.connect("google.com", 80).sync().channel();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        ch.writeAndFlush(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void googleResponseReceived(String response) {
        logger.info("Received google response in original handler: " + response);
    }

    @Override
    public EventExecutor getExecutor() {
        return executor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        executor = ctx.executor();
    }
}
