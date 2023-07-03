package channal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.channels.SocketChannel;

/**
 * @author chengengwei
 * @description 自定义NioSocketChannel
 * @date 2023/6/16
 */
public class ShortIdNioSocketChannel extends NioSocketChannel {

    public ShortIdNioSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    @Override
    protected ChannelId newId() {
        return ShortChannelId.newId();
    }
}
