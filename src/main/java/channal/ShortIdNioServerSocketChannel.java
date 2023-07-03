package channal;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * @author chengengwei
 * @description 自定义NioServerSocketChannel
 * @date 2023/6/16
 */
public class ShortIdNioServerSocketChannel extends NioServerSocketChannel {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(ShortIdNioServerSocketChannel.class);

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = SocketUtils.accept(this.javaChannel());
        try {
            if (ch != null) {
                buf.add(new ShortIdNioSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable var6) {
            logger.warn("Failed to create a new channel from an accepted socket.", var6);
            try {
                ch.close();
            } catch (Throwable var5) {
                logger.warn("Failed to close a socket.", var5);
            }
        }
        return 0;
    }
}
