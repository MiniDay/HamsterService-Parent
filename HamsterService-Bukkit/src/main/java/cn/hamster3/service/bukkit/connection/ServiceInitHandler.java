package cn.hamster3.service.bukkit.connection;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class ServiceInitHandler extends ChannelInitializer<NioSocketChannel> {
    private final ServiceConnection connection;

    public ServiceInitHandler(ServiceConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void initChannel(NioSocketChannel channel) {
        channel.pipeline()
                .addLast(new LengthFieldPrepender(8))
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8))
                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                .addLast(new StringEncoder(StandardCharsets.UTF_8))
                .addLast(new ServiceReadHandler(connection))
        ;
    }
}
