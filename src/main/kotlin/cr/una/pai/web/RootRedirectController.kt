package cr.una.pai.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
class RootRedirectController {
    @GetMapping("/")
    fun redirectToSwagger(): RedirectView {
        return RedirectView("/swagger-ui/index.html")
    }
}

