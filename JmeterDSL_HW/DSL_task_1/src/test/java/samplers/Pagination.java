package samplers;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor.JsonQueryLanguage;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class Pagination {

    public DslSimpleController get(String baseUrl) {
        return simpleController("Пагинация ->",
                httpSampler(
                        "<_/datatables_ticket_list/___query_encoded___", baseUrl +
                                "/datatables_ticket_list/eyJmaWx0ZXJpbmciOiB7InN0YXR1c19faW4iOiBbMSwgMl19LCAic29" +
                                "ydGluZyI6ICJjcmVhdGVkIiwgInNlYXJjaF9zdHJpbmciOiAiIiwgInNvcnRyZXZlcnNlIjogZmFsc2V9"
                )
                        .header("Referer", baseUrl + "/tickets/")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("start", "10")
                        .param("length", "10")
                        .method(HTTPConstants.GET)
                        .children(
                                jsonExtractor("ticket_id", "$.data[*].id")
                                        .queryLanguage(JsonQueryLanguage.JSON_PATH)
                                        .matchNumber(0)
                                        .defaultValue("NOT_FOUND")
                        )
        );
    }
}
