package baidu.com;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QwenController {

    @GetMapping("/test")
    public String test() {
        return "API is working!";
    }

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    private static final String API_KEY = "sk-b2ab33989c444e088030b4b233c8b935";
    private static final String APP_ID = "YOUR_APP_ID";

    @PostMapping("/qwen")
    public String chat(@RequestBody String question) {
        try {
            System.out.println("Received question: " + question);

            JSONObject requestBody = new JSONObject();
            JSONObject input = new JSONObject();
            input.put("prompt", question);
            requestBody.put("input", input);

            System.out.println("Request body: " + requestBody.toString());

            JSONObject parameters = new JSONObject();
            parameters.put("has_thoughts", true);
            parameters.put("stream", false);
            requestBody.put("parameters", parameters);
            requestBody.put("debug", new JSONObject());

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBody.toString());

            Request request = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/apps/" + APP_ID + "/completion")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("X-DashScope-SSE", "disable")
                .build();

            Response response = HTTP_CLIENT.newCall(request).execute();
            System.out.println("Response code: " + response.code());

            if (!response.isSuccessful()) {
                String errorBody = response.body().string();
                System.out.println("Error response: " + errorBody);
                return new JSONObject()
                    .put("error_code", response.code())
                    .put("error_msg", "API调用失败: " + errorBody)
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
} 