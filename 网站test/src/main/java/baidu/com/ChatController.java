package baidu.com;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    private static final String APP_ID = "d3fb23d6-aba7-48ba-be69-d1c114e4f224";
    private static final String AUTH_TOKEN = "bce-v3/ALTAK-wv8hGe1D7z7Wo1yhg1BYd/5ff9b24892bc884975f4c6797f8ebaa16aed446f";
    private static String conversationId = null;
    private static final long CONVERSATION_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 7天的毫秒数
    private static long lastConversationTime = 0;

    @PostMapping("/chat")
    public String chat(@RequestBody String question) {
        try {
            // 检查是否需要创建新的会话
            if (conversationId == null || System.currentTimeMillis() - lastConversationTime > CONVERSATION_EXPIRE_TIME) {
                createNewConversation();
            }

            // 获取当前UTC时间
            String utcDate = java.time.Instant.now().toString();

            JSONObject requestBody = new JSONObject();
            requestBody.put("app_id", APP_ID);
            requestBody.put("conversation_id", conversationId);
            requestBody.put("messages", new JSONObject[]{
                new JSONObject().put("role", "user").put("content", question)
            });
            requestBody.put("stream", false);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBody.toString());
            
            Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/app/conversation")
                .post(body)
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .addHeader("x-bce-date", utcDate)
                .addHeader("Host", "qianfan.baidubce.com")
                .addHeader("User-Agent", "okhttp/4.12.0")
                .addHeader("X-Appbuilder-Authorization", "Bearer " + AUTH_TOKEN)
                .build();

            Response response = HTTP_CLIENT.newCall(request).execute();
            
            // 检查响应头
            String contentType = response.header("Content-Type");
            String requestId = response.header("x-bce-request-id");
            
            if (!response.isSuccessful()) {
                return new JSONObject()
                    .put("error_code", response.code())
                    .put("error_msg", "API调用失败")
                    .toString();
            }

            return response.body().string();
            
        } catch (Exception e) {
            return new JSONObject()
                .put("error_code", 500)
                .put("error_msg", e.getMessage())
                .toString();
        }
    }

    // 创建新的会话
    private void createNewConversation() throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("app_id", APP_ID);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestBody.toString());

        Request request = new Request.Builder()
            .url("https://qianfan.baidubce.com/v2/app/conversation")
            .post(body)
            .addHeader("Content-Type", "application/json;charset=utf-8")
            .addHeader("X-Appbuilder-Authorization", "Bearer " + AUTH_TOKEN)
            .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            conversationId = jsonResponse.getString("conversation_id");
            lastConversationTime = System.currentTimeMillis();
        } else {
            throw new IOException("Failed to create conversation: " + response.code());
        }
    }
} 