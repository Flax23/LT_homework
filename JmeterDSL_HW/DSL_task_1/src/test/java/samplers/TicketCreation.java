package samplers;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class TicketCreation {

    public DslSimpleController get(String baseUrl) {
        return simpleController("Создание тикета ->",
                httpHeaders()
                        .header("Sec-GPC", "1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("DNT", "1"),
                httpSampler("<_/tickets/submit", baseUrl + "/tickets/submit/")
                        .header("Referer", baseUrl + "/tickets/")
                        .method(HTTPConstants.GET)
                        .children(
                                regexExtractor("CSRF", "n\" value=\"(.*)\"")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("CSRF_ERROR")
                        ),
                httpSampler(">_/tickets/submit", baseUrl + "/tickets/submit/")
                        .header("Referer", baseUrl + "/tickets/submit/")
                        .header("Origin", baseUrl)
                        .method(HTTPConstants.POST)
                        .param("csrfmiddlewaretoken", "${CSRF}")
                        .param("queue", "1")
                        .param("title", "test problem")
                        .param("body", "test test")
                        .param("priority", "4")
                        .param("due_date", "2024-05-02 00:00:00")
                        .param("submitter_email", "test@test.com")
                        .param("assigned_to", "1")
                        .children(
                                regexExtractor("TICKET_NUM", "href=\"/tickets/(.*)/hold/\"")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("TICKET_NUM ERROR")
                        ),
                httpSampler("<_/tickets/___TICKET_NUM___", baseUrl + "/tickets/${TICKET_NUM}/")
                        .header("Referer", baseUrl + "/tickets/submit/")
                        .method(HTTPConstants.GET)
        );
    }
}
