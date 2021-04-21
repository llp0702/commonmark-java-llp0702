package upariscommonmarkjava.http_serv.implementations.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import upariscommonmarkjava.buildsite.SiteFormatException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static upariscommonmarkjava.http_serv.implementations.server.UtilConstants.*;

//Code provenant en partie de la classe d'exemple HttpStaticFileHandler
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Map<Pattern, BiConsumer<ChannelHandlerContext,FullHttpRequest>> routingTable = new HashMap<>();

    private final SsgApi ssgApi;
    private final ObjectMapper mapper = new ObjectMapper();


    public ServerHandler(final SsgApi ssgApi) {
        this.ssgApi = ssgApi;
        initRoutingTable();
    }
    private void initRoutingTable(){
        routingTable.put(API_GET_INPUT_FILES_PATHS_URL, (ctx, msg)->{
            try {
                sendJson(ctx, ssgApi.getInputFiles());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        routingTable.put(API_GET_OUTPUT_FILES_PATHS_URL, (ctx, msg)->{
            try {
                sendJson(ctx, ssgApi.getOutputFiles());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        routingTable.put(API_GET_UPDATE_SITE, (ctx, msg)->{
            try {
                ssgApi.updateInputHandler();
                sendJson(ctx, "Success");
            } catch (IOException| SiteFormatException e) {
                e.printStackTrace();
            }
        });

        routingTable.put(API_POST_UPDATE_FILE, (ctx, msg)->{
            if(msg.headers().contains(HEADER_FILE_PATH)
                    && !msg.headers().get(HEADER_FILE_PATH).isEmpty()
                    && !msg.headers().get(HEADER_FILE_PATH).equals("undefined")){
                Path pathToUpdate = Paths.get(msg.headers().get(HEADER_FILE_PATH));
                String newContent = msg.content().toString(StandardCharsets.UTF_8);
                try {
                    ssgApi.updateFile(pathToUpdate, newContent);
                    sendJson(ctx, "Success");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        messageReceived(ctx,msg);
    }

    private HttpResponseStatus checkError(FullHttpRequest request) {
        if (!request.decoderResult().isSuccess())return HttpResponseStatus.BAD_REQUEST;
        return null;
    }

    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {

        HttpResponseStatus eventualError = checkError(request);
        if(eventualError != null) {
            sendError(ctx, eventualError);
            return;
        }

        URI uri = URI.create(request.uri());
        //Get home file
        if(uri.getPath().startsWith("/index.html") || "/".equals(uri.getPath())){
            sendRegularFile(ctx, PATH_INTO_HOME_HTML);
            return;
        }
        //Get JS File
        if(uri.getPath().startsWith("/home.js")){
            sendRegularFile(ctx, PATH_INTO_HOME_JS);
            return;
        }
        //Get simply a file
        if(request.headers().contains(HEADER_GET_ANY_FILE) && "true".equals(request.headers().get(HEADER_GET_ANY_FILE)) ){
            sendRegularFile(ctx, Paths.get(uri.getPath()));
            return;
        }

        //REST API matching
        routingTable.entrySet().stream().filter(entry->
            entry.getKey().matcher(request.uri()).matches()
        ).findFirst().ifPresent(entry-> entry.getValue().accept(ctx, request));

        sendError(ctx,HttpResponseStatus.NOT_FOUND);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private static void sendRegularFile(ChannelHandlerContext ctx, Path filePath) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

        response.content().writeBytes(Files.readAllBytes(filePath));
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    private  <T> void   sendJson(ChannelHandlerContext ctx, T object) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/json; charset=UTF-8");

        response.content().writeBytes(mapper.writeValueAsBytes(object));
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }


    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar time = new GregorianCalendar();
        response.headers().set("Date", dateFormatter.format(time.getTime()));
    }

}
