package upariscommonmarkjava.http_serv.implementations.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;

//Code provenant en partie de la classe d'exemple HttpStaticFileHandler
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Map<Pattern, BiFunction<ChannelHandlerContext,FullHttpRequest,Object>> routingTable = new HashMap<>();
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    final SsgApi ssgApi;

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public ServerHandler(final SsgApi ssgApi) {
        this.ssgApi = ssgApi;
        initRoutingTable();
    }
    private void initRoutingTable(){
        routingTable.put(Pattern.compile("getInp"), (ctx, msg)->{
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(CONTENT_TYPE, "text/json; charset=UTF-8");

            //response.content().writeBytes();
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            ctx.flush();
            return null;
        });


    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        messageReceived(ctx,msg);
    }

    private HttpResponseStatus checkError(FullHttpRequest request) throws IOException {
        if (!request.decoderResult().isSuccess())return HttpResponseStatus.BAD_REQUEST;
        if(request.method() != HttpMethod.GET)return  HttpResponseStatus.METHOD_NOT_ALLOWED;
        String uri = request.uri();
        Path path = sanitizeUri(uri, ssgApi.getOutputBasePath());
        if(Files.isHidden(path) || !Files.exists(path)){
            return HttpResponseStatus.NOT_FOUND;
        }
        return null;
    }

    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws ParseException, IOException {

        HttpResponseStatus eventualError = checkError(request);
        if(eventualError != null) {
            sendError(ctx, eventualError);
            return;
        }

        String uri = request.uri();
        /*if("index.html".equals(uri)){
            sendRegularFile(ctx, Path.of("http_serv/resources/index.html"));
        }else{

        }*/
        Path filePath = sanitizeUri(uri, ssgApi.getOutputBasePath());

        if (Files.isDirectory(filePath)) {
            sendListing(ctx, filePath);
            return;
        }
        //Is a regular file
        sendRegularFile(ctx,  filePath);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Path sanitizeUri(String uri){
        return sanitizeUri(uri, Paths.get(""));
    }
    private Path sanitizeUri(String uri, Path relativeTo)throws IllegalArgumentException {
        uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);

        if (!uri.startsWith("/")) {
            throw new IllegalArgumentException("Invalid uri");
        } else {
            uri = uri.replaceFirst("/", "");
            uri = uri.replace('/', File.separatorChar);
            if(uri.contains(File.separator + '.') ||
                    uri.contains('.' + File.separator) ||
                    uri.startsWith(".") && !uri.endsWith(".") ||
                    INSECURE_URI.matcher(uri).matches()){
                throw new IllegalArgumentException("Invalid uri");
            }else{
                return  relativeTo.resolve(uri);
            }
        }
    }

    private static void sendRegularFile(ChannelHandlerContext ctx, Path filePath) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

        response.content().writeBytes(Files.readAllBytes(filePath));
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }


    private static void sendListing(ChannelHandlerContext ctx, Path dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();

        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append("Listing of output: ");
        buf.append(dir);
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>Listing of: ");
        buf.append(dir);
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li><a href=\"").append("\">..</a></li>\r\n");

        try(Stream<Path> subFilesPathsList = Files.list(dir)) {
            subFilesPathsList.forEach(curSubFilePath ->{
                if (Files.isReadable(curSubFilePath) &&
                        ALLOWED_FILE_NAME.matcher(curSubFilePath.getFileName().toString()).matches()) {
                    buf.append("<li><a href=\"")
                            .append(curSubFilePath)
                            .append("\">")
                            .append(curSubFilePath.getFileName())
                            .append("</a>")
                            .append("</li>\r\n");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        buf.append("</ul></body></html>\r\n");
        response.content().writeBytes(Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8));
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set("Location", newUri);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    private static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
        setDateHeader(response);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        ctx.flush();
    }

    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar time = new GregorianCalendar();
        response.headers().set("Date", dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar time = new GregorianCalendar();
        response.headers().set("Date", dateFormatter.format(time.getTime()));
        time.add(13, 60);
        response.headers().set("Expires", dateFormatter.format(time.getTime()));
        response.headers().set("Cache-Control", "private, max-age=60");
        response.headers().set("Last-Modified", dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
    }
}
