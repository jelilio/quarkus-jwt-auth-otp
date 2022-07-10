package io.github.jelilio.jwtauthotp;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "JWT-Authentication-OTP API",
        description = "This API allows provides endpoints to consumes and interact with the application Backend",
        version = "1.0",
        contact = @Contact(name = "@jelilio", url = "https://twitter.com/jelilio_")
    )
)
public class MainApplication extends Application  {
}
