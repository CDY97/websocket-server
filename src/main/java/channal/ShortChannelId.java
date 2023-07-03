package channal;

import io.netty.channel.ChannelId;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chengengwei
 * @description 自定义ChannelId
 * @date 2023/6/16
 */
public class ShortChannelId implements ChannelId {
    private static final AtomicLong incrId = new AtomicLong();
    private String shortId = String.valueOf(incrId.getAndIncrement());
    private volatile String longId;

    private ShortChannelId() {
    }

    public static ShortChannelId newId() {
        return new ShortChannelId();
    }

    @Override
    public String asShortText() {
        return shortId;
    }

    @Override
    public String asLongText() {
        if (longId == null) {
            synchronized (this) {
                if (longId == null) {
                    longId = UUID.randomUUID().toString();
                }
            }
        }
        return longId;
    }

    @Override
    public int compareTo(ChannelId o) {
        return shortId.compareTo(o.asShortText());
    }
}
