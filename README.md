# Working with Quarkus, LangChain4j, Podman AI Lab and InstructLab

This brief tutorial shows you how you can interact with AI models, such as those from the Open Source InstructLab project, using the LangChain4j Quarkus extension. Watch the demo on YouTube: https://youtu.be/Zogg2c1z1P0

## Installing Podman Desktop AI

Download and install Podman Desktop on your operating system. [The instructions can be found here](https://podman-desktop.io/downloads).

> **_NOTE:_** For Windows/Mac users, if you can, give the podman machine at least 8GB of memory and 4 CPU (Generative AI Models are resource hungry!). The model will run with less resources, but it will be significantly slower.

Once installed, go ahead and start the application and go through the setup process. After that, you should see an "AI Lab" extension in the left menu. If you don't, you may need to install the extension first. If so, go to Extensions -> Catalog and install Podman AI Lab.

![Podman Desktop with the Podman AI menu item](/assets/podman-desktop-ai.png)

Once you see the menu item, go ahead and click on it, and then in the AI Lab, select the "Catalog"

![Podman AI Lab Catalog](/assets/podman-desktop-ai-catalog.png)

You should now see a list of available AI Models to choose from. You can also import different ones (eg. from Huggingface), but we will use one of the InstructLab models that are already available.

> **_NOTE:_** If you haven't heard of [Instructlab](https://developers.redhat.com/articles/2024/05/07/instructlab-open-source-generative-ai), it's a crowd sourced open source project for enhancing large language models (LLMs) used in generative artificial intelligence (gen AI) applications. You can even contribute to it yourself!

To start using the model, we'll first need to download it, so go ahead and do that with the download button ![Download Button](/assets/podman-desktop-model-download.png) next to the instructlab/merlinite-7b-lab-GGUF entry (this might take a little while).

Once downloaded, you can create a new model service by clicking on the rocket button ![rocket button](/assets/podman-desktop-create-model-service.png) that will appear where you previously clicked the download button.

You will be taken to the "Creating Model Service" page where you can set the port that should be exposed for the service. Podman Desktop assigns a random available port by default, but let's set it to `35000` so we can remember more easily what the port is when we configure our Quarkus application.

![Create Merlinite Podman AI service](/assets/podman-desktop-create-merlinite-service.png)

After a few moments, your very own Model service will be running locally on your laptop! You an check the details on the Service details page, including some samples to test out the service with cURL (or even Java!).

Now it's time to go back to our Quarkus application.

> **_NOTE:_** The InstructLab service uses the OpenAI protocol, so we can simply use the quarkus-langchain4j-openai extension

## Creating the Quarkus project

Create a new Quarkus project with the langchain4j-openai and rest extensions. You can go to [code.quarkus.io](https://code.quarkus.io) and select the needed dependencies, or simply create a new project with maven, gradle or the quarkus CLI. Here's an example of how to do it with Maven.

```bash
mvn "io.quarkus.platform:quarkus-maven-plugin:create" -DprojectGroupId="com.redhat.developers" -DprojectArtifactId="quarkus-podman-ai" -DprojectVersion="1.0-SNAPSHOT" -Dextensions=langchain4j-openai,rest
```

Go ahead and open the project with your favorite IDE.

### Connect to the InstructLab Model

Add the following properties in the `application.properties` file available in `src/main/resources` to point to the model service and to set the correct model name (this is what we selected in Podman AI Lab).

```properties
quarkus.langchain4j.openai.base-url=http://localhost:35000/v1 
# Configure openai server to use a specific model
quarkus.langchain4j.openai.chat-model.model-name=instructlab/merlinite-7b-lab-GGUF 
# Set timeout to 3 minutes
quarkus.langchain4j.openai.timeout=180s
# Enable logging of both requests and responses
quarkus.langchain4j.openai.log-requests=true
quarkus.langchain4j.openai.log-responses=true
```

### Create the AI service

Now, we need to create an interface for the AI service and annotate it with `@RegisterAIService`

Create a new `AiService.java` Java interface in `src/main/java` in the `com.redhat.developers` package with the following contents:

```java
package com.redhat.developers;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService()
@SessionScoped
public interface AiService {

    @SystemMessage({
            "You are a Java developer who likes to over engineer things"
    })
    String chat(@UserMessage String userMessage);
}
```

The `@SystemMessage` gives the AI Model some context about the scenario.

### Create the prompt-base resource

Let's implement a REST resource so we can call the service from the browser or cURL.

Create a new `InstructLabResource.java` Java class in `src/main/java` in the `com.redhat.developers` package with the following contents:

```java
package com.redhat.developers;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/instructlab")
public class InstructLabResource {

    @Inject
    AiService assistant;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String prompt() {
        // feel free to update this message to any question you may have for the LLM.
        String message = "Generate a class that returns the square root of a given number";
        return assistant.chat(message);
    }
}
```

### Invoke the endpoint

We're all set!

You can test it out by starting Quarkus in Dev Mode:

```bash
./mvnw quarkus:dev
```

This will start the application (and live-reloads when you make changes to the code, such as changing the prompt message).

You can the test implementation by pointing your browser to http://localhost:8080/instructlab

Or if you'd rather test in the CLI, you can the following cURL command:

```bash
curl http://localhost:8080/instructlab
```

An example of output (remember, your result will likely be different):

````bash
Here is a simple Java class to calculate the square root of a given number using the built-in `Math` class in Java:

```java
public class SquareRootCalculator {
    public static void main(String[] args) {
        int num = 16; // square root of 16 is 4.0
        double result = Math.sqrt(num);
        System.out.println("Square root of " + num + ": " + result);
    }
}
```

Alternatively, if you want to handle negative numbers or non-integer inputs, you can use the `Math.sqrt()` function directly:

```java
public class SquareRootCalculator {
    public static void main(String[] args) {
        double num = -16; // square root of -16 is -4.0
        double result = Math.sqrt(num);
        System.out.println("Square root of " + num + ": " + result);
    }
}
```

This will allow you to calculate the square root of any given number, positive or negative, and handle non-integer inputs.
````

> **_NOTE:_** depending on your local resources, this might take a up to a few minutes. If you run into timeouts, you can try changing the `quarkus.langchain4j.openai.timeout` value in the application.properties file. If you're running on Mac/Windows, you could also try to give the podman machine more CPU/Memory resources.

Notice that (at least in our case) the LLM responded with a Java class, since we provided in the SystemMessage that the LLM should respond as if they were a Java engineer.  

## Add Fault Tolerance

You might want to add fault tolerance to your calls to the AI inference server so you can handle failures gracefully in case the model is not reachable or misbehaving. With SmallRye Fault Tolerance (based on the MicroProfile spec) it is fairly trivial to do.

You will need to add the smallrye-fault-tolerance extension to your application, eg.:

```bash
./mvnw quarkus:add-extension -D"extensions=quarkus-smallrye-fault-tolerance"
```

Now you can add @FallBack and/or @Retry annotations to your Assistant call like in the following example:

```java
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
```

In the above example, we are going to retry up to 3 times to call the inference server with a delay of 100ms between attempts. If after 3 times we still don't have a successful response, the AssistantForInstrctLabFallback class will be called, which will return a simple failure string.

## Going further

Feel free to play around with the different models Podman Desktop AI Lab provides. You will notice that some are faster than others, and some will respond better to specific questions than others, based on how they have been trained.

> **_NOTE:_** If you want to help improve the answers generated by the InstructLab model, feel free to [contribute to the project](https://github.com/instructlab/community/blob/main/README.md).
