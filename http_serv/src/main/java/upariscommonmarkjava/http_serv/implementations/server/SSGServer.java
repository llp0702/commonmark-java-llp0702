package upariscommonmarkjava.http_serv.implementations.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
public class SSGServer {
    Logger logger = Logger.getLogger("SSGServer");
    public static final int DEF_PORT = 8080;
    private final  int port;

    private final SsgApi api;

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap httpBootstrap = new ServerBootstrap();

    ChannelFuture httpChannel;

    public SSGServer(int port, Path inputBasePath,  Path outputBasePath){
        this.port = port;
        api = new SsgApi(inputBasePath, outputBasePath);
    }


    public void run() throws InterruptedException{

            httpBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(api)) // <-- Our handler created here
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            httpChannel = httpBootstrap.bind(port).sync();


            logger.log(Level.INFO, "Running! Point your browsers to http://localhost:{0}. Enter 'exit' to stop the server",
                    String.valueOf(port));

        new Thread(() -> {
            try{
                Scanner sc = new Scanner(System.in);
                String input = "";
                while(!"exit".equals(input)){
                    input = sc.nextLine();
                }
                logger.info("Exit ssg");
                stopServer();
            }catch (Exception e){
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void stopServer() throws InterruptedException {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        httpChannel.channel().closeFuture().sync();
    }


}
