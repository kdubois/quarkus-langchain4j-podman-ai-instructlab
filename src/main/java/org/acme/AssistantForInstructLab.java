package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService()
public interface AssistantForInstructLab {

    @SystemMessage({
            "You are a Java developer who likes to over engineer things"
    })
    String chat(@UserMessage String userMessage);
}