import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class GoogleInitializer extends ChannelInitializer<SocketChannel> {

    private GoogleResponseHandler responseHandler;

    public GoogleInitializer(GoogleResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(512 * 1024));

        p.addLast(new GoogleHandler(responseHandler));
    }
}
