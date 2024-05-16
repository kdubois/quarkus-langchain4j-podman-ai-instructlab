package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class InstructLabResource {

    @Inject
    AssistantForInstructLab assistant;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String prompt() {
        // feel free to update this message to any question you may have for the LLM.
        String message = "Generate a class that returns the square root of a given number";
        return assistant.chat(message);
    }
}
