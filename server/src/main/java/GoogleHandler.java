import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class GoogleHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final static Logger log = LogManager.getLogger(GoogleHandler.class);
    private GoogleResponseHandler responseHandler;

    public GoogleHandler(GoogleResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            String content = response.content().toString(CharsetUtil.UTF_8);

            log.info(String.format("Got response from google in thread: %d", Thread.currentThread().getId()));
            if (responseHandler.getExecutor().inEventLoop()) {
                responseHandler.googleResponseReceived(content);
            } else {
                responseHandler.getExecutor().schedule(() -> responseHandler.googleResponseReceived(content), 0, TimeUnit.MICROSECONDS);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
