package samplers;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;


import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class Filtration {

    private final String sorting; // created, title, queue, status, priority, owner
    private final String status; // 1 - Open, 2 - Reopened, 3 - Resolved, 4 - Closed, 5 - Duplicate

    public Filtration(String sorting, String status) {
        this.sorting = sorting;
        this.status = status;
    }

    public DslSimpleController get(String baseUrl) {
        return simpleController("Фильтрация ->",
                httpHeaders()
                        .header("Sec-GPC", "1")
                        .header("DNT", "1")
                        .header("Upgrade-Insecure-Requests", "1"),
                httpSampler("<_/tickets/___filtered___", baseUrl + "/tickets/")
                        .header("Referer", baseUrl + "/tickets")
                        .method(HTTPConstants.GET)
                        .param("sortx", sorting)
                        .param("status", status),
                httpSampler(
                        "<_/datatables_ticket_list/___query_encoded___", baseUrl +
                                "/datatables_ticket_list/eyJmaWx0ZXJpbmciOiB7InN0YXR1c19faW4iOiBbMV19LCAiZmlsdGVya" +
                                "W5nX29yIjogeyJzdGF0dXNfX2luIjogWzFdfSwgInNvcnRpbmciOiAiY3JlYXRlZCIsICJzb3J0cmV2ZX" +
                                "JzZSI6IG51bGwsICJzZWFyY2hfc3RyaW5nIjogIiJ9"
                )
                        .header("Referer", baseUrl + "/tickets/")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("start", "0")
                        .param("length", "10")
                        .method(HTTPConstants.GET),
                httpSampler(">_/save_query", baseUrl + "/save_query/")
                        .header("Referer", baseUrl + "/tickets/")
                        .header("Origin", baseUrl)
                        .method(HTTPConstants.POST)
                        .param("csrfmiddlewaretoken", "${CSRF}")
                        .param("query_encoded", "eyJmaWx0ZXJpbmciOiB7InN0YXR1c19faW4iOiBbMV19LCAiZmlsd" +
                                "GVyaW5nX29yIjogeyJzdGF0dXNfX2luIjogWzFdfSwgInNvcnRpbmciOiAiY3JlYXRlZCIsICJzb3J0cm" +
                                "V2ZXJzZSI6IG51bGwsICJzZWFyY2hfc3RyaW5nIjogIiJ9")
                        .param("title", "${__RandomString(9,abcdefghijklmnopqrstuvwxyz,)}"),
                httpSampler("<_/tickets", baseUrl + "/tickets/")
                        .header("Referer", baseUrl + "/tickets")
                        .method(HTTPConstants.GET)
                        .param("saved_query", "1")
                        .children(
                                responseAssertion()
                                        .fieldToTest(DslResponseAssertion.TargetField.RESPONSE_BODY)
                                        .containsSubstrings("selected='selected'>Open")
                        ),
                httpSampler(
                        "<_/datatables_ticket_list/___query_encoded___", baseUrl +
                                "/datatables_ticket_list/eyJzZWFyY2hfc3RyaW5nIjogIiIsICJmaWx0ZXJpbmciOiB7InN0YXR1c" +
                                "19faW4iOiBbMV19LCAiZmlsdGVyaW5nX29yIjogeyJzdGF0dXNfX2luIjogWzFdfSwgInNvcnRpbmciOi" +
                                "AiY3JlYXRlZCIsICJzb3J0cmV2ZXJzZSI6IG51bGx9"
                )
                        .header("Referer", baseUrl + "/tickets/?saved_query=1")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("start", "0")
                        .param("length", "10")
                        .method(HTTPConstants.GET)
        );
    }
}
