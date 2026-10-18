package se.comerit.avanza.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

    @GetMapping({
        "/innehav",
        "/portfoljhalsa",
        "/notiser",
        "/login"
    })

    public String forwardToClient() {
        return "forward:/index.html";
    }

}
