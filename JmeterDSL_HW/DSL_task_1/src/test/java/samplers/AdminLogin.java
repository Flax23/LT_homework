package samplers;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;

public class AdminLogin {

    public DslSimpleController get(String baseUrl) {
        return simpleController("Авторизация админа ->",
                httpSampler("<_/", baseUrl + "/")
                        .header("Upgrade-Insecure-Requests", "1")
                        .method(HTTPConstants.GET)
                        .children(
                                regexExtractor("CSRF", "n\" value=\"(.*)\"")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("CSRF_ERROR")
                        ),
                httpSampler("<_/login", baseUrl + "/login/")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Referer", baseUrl + "/")
                        .method(HTTPConstants.GET),
                httpSampler(">_/login", baseUrl + "/login/")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Referer", baseUrl + "/login/?next=/")
                        .header("Origin", baseUrl)
                        .method(HTTPConstants.POST)
                        .param("username", "${ADM_LOGIN}")
                        .param("password", "${ADM_PASSWORD}")
                        .param("next", "/")
                        .param("csrfmiddlewaretoken", "${CSRF}")
                        .children(
                                responseAssertion()
                                        .fieldToTest(DslResponseAssertion.TargetField.RESPONSE_CODE)
                                        .equalsToStrings("200"),
                                regexExtractor("CSRF", "n\" value=\"(.*)\"")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("CSRF_ERROR")
                        ),
                httpSampler(
                        "<_/datatables_ticket_list/___query_encoded___", baseUrl +
                                "/datatables_ticket_list/eyJmaWx0ZXJpbmciOiB7InN0YXR1c19faW4iOiBbMSwgMl19LCAic29yd" +
                                "GluZyI6ICJjcmVhdGVkIiwgInNlYXJjaF9zdHJpbmciOiAiIiwgInNvcnRyZXZlcnNlIjogZmFsc2V9"
                )
                        .param("start", "0")
                        .param("length", "25")
                        .method(HTTPConstants.GET)
        );
    }
}
