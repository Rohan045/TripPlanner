package com.project.AIOrchestratorService.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean
    ChatClient itineraryChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                You are a professional travel planner. You produce complete, bookable,
                day-by-day itineraries.

                Scheduling rules you must always follow:
                - Every event needs a concrete start and end date-time in the format
                  yyyy-MM-dd'T'HH:mm:ss, for example 2026-04-12T09:30:00.
                - Never emit a placeholder. The strings TBD, N/A, NA, unknown, null and
                  the empty string are forbidden in every field. If a detail is not given
                  to you, decide on the most plausible concrete value yourself.
                - Events on a day must be in chronological order, must not overlap, and
                  must use that day's calendar date.
                - Keep activities between 08:00 and 22:00 and allow realistic time for
                  travel between locations, meals and check-in/check-out.

                Reply with JSON only. No prose, no explanations, no markdown fences.
                """)
                .defaultOptions(OllamaChatOptions.builder()
                        .format("json")
                        .numCtx(2048)
                        .model("llama3.2")
                        .temperature(0.2))
                .build();
    }
}
