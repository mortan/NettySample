import io.netty.util.concurrent.EventExecutor;

public interface GoogleResponseHandler {
    void googleResponseReceived(String response);

    EventExecutor getExecutor();
}
