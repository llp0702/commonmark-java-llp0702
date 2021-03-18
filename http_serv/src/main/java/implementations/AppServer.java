package implementations;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class AppServer {



    private static int HTTP_PORT = 8080;



    public static void run(String []args) throws Exception {
        for (int i = 0; i < args.length; ++i) {
            if ("-p".equalsIgnoreCase(args[i]) || "--port".equalsIgnoreCase(args[i])) {
                HTTP_PORT = Integer.parseInt(args[i + 1]);
                System.out.println("ok");
            }
        }




        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup workerGroup = new NioEventLoopGroup();



        try {


            ServerBootstrap httpBootstrap = new ServerBootstrap();


            httpBootstrap.group(bossGroup, workerGroup)

                    .channel(NioServerSocketChannel.class)

                    .childHandler(new ServerInitializer()) // <-- Our handler created here

                    .option(ChannelOption.SO_BACKLOG, 128)

                    .childOption(ChannelOption.SO_KEEPALIVE, true);



            // Bind and start to accept incoming connections.

            ChannelFuture httpChannel = httpBootstrap.bind(HTTP_PORT).sync();


            System.out.println("Server started, Hit Enter to stop.\n");
            System.out.println("\nRunning! Point your browsers to http://localhost:"+HTTP_PORT+"/ \n");

//Ici pour incrementale
/*
            httpChannel.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println("test");
                }
            }, 1, 1, TimeUnit.SECONDS);
            */

            System.in.read();
            workerGroup.shutdownGracefully();

            bossGroup.shutdownGracefully();

            httpChannel.channel().closeFuture().sync();


        }

        finally {

            workerGroup.shutdownGracefully();

            bossGroup.shutdownGracefully();

        }

    }



    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

         AppServer.run(args);

    }



}
