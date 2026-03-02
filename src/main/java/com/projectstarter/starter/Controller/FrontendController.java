package com.projectstarter.starter.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Gestisce il fallback per React Router.
 * Qualsiasi URL senza estensione (non un file .js/.css) viene inoltrato a index.html,
 * così React Router può gestire il routing lato client.
 */
@Controller
public class FrontendController {

    @RequestMapping(value = {"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
