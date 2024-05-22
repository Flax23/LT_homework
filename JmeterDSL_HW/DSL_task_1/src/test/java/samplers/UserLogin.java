package samplers;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class UserLogin {

    public DslSimpleController get(String baseUrl) {
        return simpleController("Авторизация пользователя ->",
                httpSampler("<_/", baseUrl + "/")
                        .header("Upgrade-Insecure-Requests", "1")
                        .method(HTTPConstants.GET)
                        .children(
                                regexExtractor("CSRF", "n\" value=\"(.*)\"")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("CSRF_ERROR")
                        ),
                httpSampler("<_/logout", baseUrl + "/logout/")
                        .header("Sec-GPC", "1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("DNT", "1")
                        .method(HTTPConstants.GET),
                httpSampler("<_/login", baseUrl + "/login/")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Referer", baseUrl + "/")
                        .method(HTTPConstants.GET),
                httpSampler(">_/login", baseUrl + "/login/")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Referer", baseUrl + "/login/?next=/")
                        .header("Origin", baseUrl)
                        .method(HTTPConstants.POST)
                        .param("username", "${USER_LOGIN}")
                        .param("password", "${USER_PASSWORD}")
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
                                "/datatables_ticket_list/eyJmaWx0ZXJpbmciOiB7InN0YXR1c19faW4iOiBbMSwgMl19LCAic29y" +
                                "dGluZyI6ICJjcmVhdGVkIiwgInNlYXJjaF9zdHJpbmciOiAiIiwgInNvcnRyZXZlcnNlIjogZmFsc2V9"
                )
                        .header("Referer", baseUrl + "/tickets/")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("start", "0")
                        .param("length", "25")
                        .method(HTTPConstants.GET)
        );
    }
}
