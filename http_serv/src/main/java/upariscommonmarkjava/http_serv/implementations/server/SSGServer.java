package upariscommonmarkjava.http_serv.implementations.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        prepareHomeJs();
    }


    private void prepareHomeJs(){
        try{
            Path homeJSPath = UtilConstants.PATH_INTO_HOME_JS;
            String homeJSContent = Files.readString(homeJSPath).replace("@__PORT__@", String.valueOf(port));
            final String regex = "const[ ]+BASE_PATH[ ]*=.*";
            Files.writeString(
                    homeJSPath,
                    homeJSContent.replaceAll(regex, "const BASE_PATH = \"http://localhost:"+port+"\"")
            );
        }catch(IOException e){
            logger.warning("Error when initializing home.html");
        }
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
