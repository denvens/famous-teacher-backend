package com.qingclass.squirrel.utils;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.service.WxService;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

@Component
public class HttpHelper {

    //定义两个成员变量常量
    //获取临时素材(视频不能使用https协议)
    public final String GET_TMP_MATERIAL = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
    //获取临时素材(视频)
    public final String GET_TMP_MATERIAL_VIDEO = "http://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private WxService wxService;

    private static HttpHelper httpHelper;
    @PostConstruct
    public void init(){
        httpHelper = this;
        httpHelper.wxService = this.wxService;
    }

    public InputStream sendGet(String urlNameString) {
        String result = "";
        BufferedReader in = null;
        InputStream is = null;
        try {
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();

            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
             is = connection.getInputStream();

        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return is;
    }

    @Bean
    public HttpClient initHttpClient(){
        int CONNECTION_TIMEOUT_MS = 5 * 1000;

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
                .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                .setSocketTimeout(CONNECTION_TIMEOUT_MS)
                .build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(200);
        connManager.setMaxTotal(200);

        CloseableHttpClient client = HttpClients
                .custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return client;
    }

    /**
     * @Param times 递归调用次数
     * */
    private HttpResponse wxPost(String media_id) throws IOException {
        String accessToken = httpHelper.wxService.getAccessToken();
        String url = String.format(GET_TMP_MATERIAL_VIDEO, accessToken, media_id);


        HttpClient httpClient = initHttpClient();

        HttpUriRequest httpUriRequest = new HttpPost(url);
        HttpResponse execute = httpClient.execute(httpUriRequest);
        Header[] allHeaders = execute.getAllHeaders();

        Map<String,String> headers = new HashMap<>();

        int i = 0;
        while (i < allHeaders.length) {
            headers.put(allHeaders[i].getName(),allHeaders[i].getValue());
            i++;
        }

        if(headers.get("Content-disposition") == null){
            logger.warn("获取失败,重新尝试...");
            return null;
        }

        return execute;
    }


    public File fetchTmpFile(String media_id, String type) {
        try {

            HttpResponse response = null;
            for(int i = 0 ; i < 4 ; i ++){
                response = wxPost(media_id);
                if(response != null){
                    break;
                }
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            if(response == null){//获取资源失败
                return null;
            }

            Header[] allHeaders = response.getAllHeaders();

            Map<String,String> headers = new HashMap<>();

            int i = 0;
            while (i < allHeaders.length) {
                headers.put(allHeaders[i].getName(),allHeaders[i].getValue());
                i++;
            }

            logger.info("responseHeaders : " + headers.toString());
            String content_disposition = headers.get("Content-disposition");

            //微信服务器生成的文件名称
            String file_name ="";
            String[] content_arr = content_disposition.split(";");
            if(content_arr.length  == 2){
                String tmp = content_arr[1];
                int index = tmp.indexOf("\"");
                file_name =tmp.substring(index+1, tmp.length()-1);
            }

            HttpEntity entity = response.getEntity();
            BufferedInputStream bis = new BufferedInputStream(entity.getContent());
            //生成不同文件名称
            File file = new File(file_name);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[2048];
            int length = bis.read(buf);
            while(length != -1){
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            bos.close();
            bis.close();
            return file;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 云知声评测接口
     * */
    public Map<String,Object> sendPostYunZ(String text, String mode, File voice){

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://edu.hivoice.cn:8085/eval/mp3");
        MultipartEntity customMultiPartEntity = new MultipartEntity();
        Map<String, Object> map = new HashMap<>();
        try {
            HttpResponse response = null;
            JSONObject json = new JSONObject();
            json.put("mode", mode);
            json.put("text", text);
            customMultiPartEntity.addPart("mode", new StringBody(mode, Charset.forName("UTF-8")));
            customMultiPartEntity.addPart("text", new StringBody(text, Charset.forName("UTF-8")));
            ContentBody fileBody = new FileBody(voice);
            customMultiPartEntity.addPart("voice", fileBody);
            httpPost.setEntity(customMultiPartEntity);
            httpPost.setHeader("appkey", "3yvkshwmte7voouhlm3ltwca33zyqqkkddhdvgqb");
            httpPost.setHeader("session-id", UUID.randomUUID().toString());
            httpPost.setHeader("score-coefficient", "1.9");
            response = httpclient.execute(httpPost);
            System.out.println(response);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                // file.setLastModified(System.currentTimeMillis());
                String resp = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                JSONObject jsonObject = JSONObject.parseObject(resp);

                map.put("score",jsonObject.get("score"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        httpclient.getConnectionManager().shutdown();

        return map;
    }

}
