package implementations;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

//Code provenant en partie de la classe d'exemple HttpStaticFileHandler
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public ServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
messageReceived(ctx,msg);
    }

    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
        } else if (request.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        } else {
            String uri = request.uri();
            String path = sanitizeUri(uri);
            if (path == null) {
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
            } else {
                File file = new File(path);
                if (!file.isHidden() && file.exists()) {
                    if (file.isDirectory()) {
                        if (uri.endsWith("/")) {
                            sendListing(ctx, file);

                        } else {
                            sendRedirect(ctx, uri + '/');

                        }

                    } else if (!file.isFile()) {

                        sendError(ctx, HttpResponseStatus.FORBIDDEN);
                    } else {

                        String ifModifiedSince = request.headers().get("If-Modified-Since");
                        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
                            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000L;
                            long fileLastModifiedSeconds = file.lastModified() / 1000L;
                            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                                sendNotModified(ctx);
                                return;
                            }
                        }


                        RandomAccessFile raf;
                        try {
                            raf = new RandomAccessFile(file, "r");
                        } catch (FileNotFoundException var13) {
                            sendError(ctx, HttpResponseStatus.NOT_FOUND);
                            return;
                        }

                        long fileLength = raf.length();
                        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                        HttpUtil.setContentLength(response, fileLength);
                        setContentTypeHeader(response, file);
                        setDateAndCacheHeaders(response, file);
                        if (HttpUtil.isKeepAlive(request)) {
                            response.headers().set("Connection", "keep-alive");
                        }
                        response.headers().set(String.valueOf(HttpHeaderNames.CONTENT_TYPE), "text/html");
                        response.headers().set("Content-Length", file.length());



                        ctx.write(response);
                        if (ctx.pipeline().get(SslHandler.class) == null) {

                            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, file.length()));
                        }else {
                            ctx.write(new ChunkedNioFile(raf.getChannel())) ;
                        }



                        ChannelFuture writeFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);


                        if (!HttpUtil.isKeepAlive(request)) {

                           writeFuture.addListener(ChannelFutureListener.CLOSE);

                        }

                    }
                } else {
                    sendError(ctx, HttpResponseStatus.NOT_FOUND);
                }

            }
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private static String sanitizeUri(String uri) {
        uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);

        if (!uri.startsWith("/")) {
            return null;
        } else {
            uri = uri.replace('/', File.separatorChar);
            return !uri.contains(File.separator + '.') && !uri.contains('.' + File.separator) && !uri.startsWith(".") && !uri.endsWith(".") && !INSECURE_URI.matcher(uri).matches() ? System.getProperty("user.dir") + File.separator + uri : null;
        }
    }

    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set("Content-Type", "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append("Listing of: ");
        buf.append(dirPath);
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>Listing of: ");
        buf.append(dirPath);
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li><a href=\"../\">..</a></li>\r\n");
        File[] arr$ = dir.listFiles();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            File f = arr$[i$];
            if (!f.isHidden() && f.canRead()) {
                String name = f.getName();
                if (ALLOWED_FILE_NAME.matcher(name).matches()) {
                    buf.append("<li><a href=\"");
                    buf.append(name);
                    buf.append("\">");
                    buf.append(name);
                    buf.append("</a></li>\r\n");
                }
            }
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
        response.headers().set("Content-Type", "text/plain; charset=UTF-8");
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
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar time = new GregorianCalendar();
        response.headers().set("Date", dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
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
        response.headers().set("Content-Type", mimeTypesMap.getContentType(file.getPath()));
    }
}
