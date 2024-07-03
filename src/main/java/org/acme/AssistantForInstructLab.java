package org.acme;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService()
public interface AssistantForInstructLab {

    @SystemMessage({
            "You are a Java developer who likes to over engineer things"
    })
    @Retry(maxRetries = 3, delay = 100)
    @Fallback(AssistantForInstructLabFallback.class)
    String chat(@UserMessage String userMessage);

    public static class AssistantForInstructLabFallback implements FallbackHandler<String> {

        private static final String EMPTY_RESPONSE = "Failed to get a response from the AI Model. Are you sure it's up and running, and configured correctly?";
        @Override
        public String handle(ExecutionContext context) {
            return EMPTY_RESPONSE;
        }
    
    }
}


