package com.example.Ai.Service.Controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("Ai")
@Validated
public class AiController {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @PostMapping("/openAi")
    public String openAiChatMessage(@RequestBody AiRequest request) {
        return openAiChatModel.call(request.getQuery());
    }

    @Data
    public static class AiRequest {
        @NotBlank(message = "Query is required")
        @Size(max = 500, message = "Query must not exceed 500 characters")
        private String query;
    }
}
