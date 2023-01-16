package kgg.cloudstructure.network;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import kgg.cloudstructure.CloudStructureMod;
import kgg.cloudstructure.config.CloudStructureConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraftforge.fml.loading.JarVersionLookupHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


public class RequestTool {
    public static RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setConnectionRequestTimeout(3000)
            .setSocketTimeout(3000)
            .build();
    private static final String USER_AGENT;
    static {
        Optional<String> ver = JarVersionLookupHandler.getImplementationVersion(RequestTool.class);
        if (ver.isEmpty()) {
            USER_AGENT = "java-"+CloudStructureMod.MOD_ID;
        } else {
            USER_AGENT = "java-"+CloudStructureMod.MOD_ID+"/"+ver.get();
        }
    }
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    public static String getToken(String method) throws IOException {
        return getToken(CloudStructureConfig.CONFIG.getUrl(), CloudStructureConfig.CONFIG.getUser(), CloudStructureConfig.CONFIG.getPasswd(), method);
    }

    public static String getToken(String url, String user, String passwd, String method) throws IOException {
        url = url+"/get_token";
        CloudStructureNetwork.LOGGER.info("get token from \"{}\",user:{}", url, user);

        JsonObject object = new JsonObject();
        object.addProperty("user", user);
        object.addProperty("passwd", passwd);
        object.addProperty("method", method);

        JsonObject token = postJson(url, object);
        if (token.get("token") != null) {
            CloudStructureNetwork.LOGGER.info("token get success");
            try {
                return token.get("token").getAsString();
            } catch (Exception ignored) {
            }
        }
        throw new RequestException("cannot contact server");
    }

    public static void uploadStructure(String url, String token, CompoundTag structureTag, String user, String path) throws IOException {
        url = url + "/upload?token=" + token + "&path=" + path;
        CloudStructureNetwork.LOGGER.info("upload structure to \"{}\",player:{}", url, user);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        NbtIo.writeCompressed(structureTag, new DataOutputStream(stream));
        postFile(url, new ByteArrayInputStream(stream.toByteArray()));
    }

    public static CompoundTag downloadStructure(String url, String token, String user, String path) throws IOException {
        url = url + "/download?token=" + token + "&" + "path=" + path;
        CloudStructureNetwork.LOGGER.info("download structure from \"{}\" ,player:{}", url, user);
        InputStream stream = get(url);
        return NbtIo.readCompressed(stream);
    }

    public static InputStream get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", USER_AGENT);
        httpGet.setConfig(config);
        CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            entity.writeTo(stream);

            IOUtils.closeQuietly(response);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
            if (inputStream.read() == '{') {
                inputStream.reset();
                try {
                    verifyResult((JsonObject) JsonParser.parseString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)));
                } catch (JsonSyntaxException ignored) {
                }
                throw new RequestException("cannot contact server");
            }
            inputStream.reset();
            return inputStream;
        }
        IOUtils.closeQuietly(response);
        throw new RequestException("error code "+response.getStatusLine().getStatusCode());
    }

    public static void postFile(String url,  InputStream stream) throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("file", stream, ContentType.DEFAULT_BINARY, "");
        post(url, multipartEntityBuilder.build());
    }

    public static JsonObject postJson(String url, JsonObject param) throws IOException{
        StringEntity entity = new StringEntity(param.toString(), ContentType.APPLICATION_JSON);
        entity.setContentEncoding("UTF-8");
        return post(url, entity);
    }

    public static JsonObject post(String url, HttpEntity entity) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("User-Agent", USER_AGENT);
        httpPost.setConfig(config);
        httpPost.setEntity(entity);

        CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JsonObject result;
            try {
                result = (JsonObject) JsonParser.parseString(EntityUtils.toString(response.getEntity()));
            } catch (JsonSyntaxException e) {
                throw new RequestException("cannot contact server");
            }
            verifyResult(result);
            return result;
        }
        IOUtils.closeQuietly(response);
        throw new RequestException("error code "+response.getStatusLine().getStatusCode());
    }

    public static void verifyResult(JsonObject object) throws RequestException {
        int code;
        try {
            code = object.get("code").getAsInt();
        } catch (Exception e) {
            throw new RequestException("cannot contact server");
        }
        if (code != 200) {
            String msg;
            try {
                msg = object.get("message").getAsString();
            } catch (Exception e) {
                throw new RequestException("error code "+code);
            }
            throw new RequestException(msg);
        }
    }

    public static class RequestException extends IOException {
        public RequestException(String s) {
            super(s);
        }
    }
}
