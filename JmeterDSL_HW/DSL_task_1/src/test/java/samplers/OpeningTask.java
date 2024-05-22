package samplers;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.ifController;

public class OpeningTask {

    public DslSimpleController get(String baseUrl) {
        return simpleController("Фильтрация ->",
                httpHeaders()
                        .header("Sec-GPC", "1")
                        .header("DNT", "1")
                        .header("Upgrade-Insecure-Requests", "1"),
                httpSampler("<_/tickets", baseUrl + "/tickets/${ticket_id}/")
                        .header("Referer", baseUrl + "/tickets/?saved_query=1")
                        .method(HTTPConstants.GET)
                        .children(
                                regexExtractor("Assigned_To", "<td>(.*) <strong>")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("ERROR"),
                                regexExtractor("NEXT_STATUS", "value='(\\d+)' id='(st_[^']+)'>([^<]+)")
                                        .template("$1$")
                                        .matchNumber(0)
                                        .defaultValue("ERROR")
                        ),
                ifController(s -> s.vars.get("Assigned_To") == "Unassigned",
                        httpSampler("<_/tickets", "/tickets/${ticket_id}/")
                                .header("Referer", baseUrl + "/tickets/${ticket_id}/")
                                .param("take", "")
                                .method(HTTPConstants.GET)
                )
        );
    }
}
