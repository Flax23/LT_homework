import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer.*;

import java.io.IOException;
import java.time.Duration;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.controllers.DslTransactionController;

public class FinalHomeWorkTest {

    private DslTransactionController adminLogin(String baseUrl) {
        return transaction("TC: Лог - Авторизация админа",
                simpleController("Авторизация админа ->",
                        httpSampler("<_/", baseUrl + "/")
                                .header("Upgrade-Insecure-Requests", "1")
                                .children(
                                        regexExtractor("CSRF", "n\" value=\"(.*)\"")
                                                .template("$1$")
                                                .matchNumber(1)
                                                .defaultValue("CSRF_ERROR")
                                ),
                        httpSampler("<_/login", baseUrl + "/login/")
                                .header("Upgrade-Insecure-Requests", "1")
                                .header("Referer", baseUrl + "/"),
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
                ),
                simpleController("Создание пользователя ->",
                        httpSampler("<_/system_settings", baseUrl + "/system_settings/")
                                .header("Referer", baseUrl + "/tickets")
                                .header("Sec-GPC", "1")
                                .header("Upgrade-Insecure-Requests", "1")
                                .header("DNT", "1"),
                        httpSampler("<_/admin/auth/user", baseUrl + "/admin/auth/user/")
                                .header("Referer", baseUrl + "/system_settings/")
                                .header("Sec-GPC", "1")
                                .header("Upgrade-Insecure-Requests", "1")
                                .header("DNT", "1"),
                        httpSampler("<_/admin/auth/user/add", baseUrl + "/admin/auth/user/add/")
                                .header("Referer", baseUrl + "/admin/auth/user/")
                                .header("Sec-GPC", "1")
                                .header("Upgrade-Insecure-Requests", "1")
                                .header("DNT", "1"),
                        httpSampler(">_/admin/auth/user/add", baseUrl + "/admin/auth/user/add/")
                                .header("Referer", baseUrl + "/admin/auth/user/add")
                                .header("Origin", baseUrl)
                                .header("DNT", "1")
                                .header("Sec-GPC", "1")
                                .header("Upgrade-Insecure-Requests", "1")
                                .method(HTTPConstants.POST)
                                .param("username", "${USER_LOGIN}")
                                .param("password1", "${USER_PASSWORD}")
                                .param("password2", "${USER_PASSWORD}")
                                .param("csrfmiddlewaretoken", "${CSRF}")
                                .param("_save", "Save")


                )
        );
    }

    @Test
    public void testPerformance() throws IOException {
        String protocol = "http://";
        String hostname = "sandbox";
        String port = ":23232";
        String baseUrl = protocol + hostname + port;

        TestPlanStats stats = testPlan(
                httpCookies()
                        .clearCookiesBetweenIterations(true),
                httpDefaults()
                        .connectionTimeout(Duration.ofSeconds(10))
                        .responseTimeout(Duration.ofMinutes(1)),
                httpHeaders()
                        .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                        .header("Accept-Encoding", "gzip, deflate")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"),
                csvDataSet(testResource("admin.csv")),
                vars().set("USER_LOGIN", "user_${__Random(1,100000,)}"),
                vars().set("USER_PASSWORD", "${__RandomString(10,abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789)}"),
                threadGroup("UC1", 1, 1,
                        adminLogin(baseUrl)
                ),
                dashboardVisualizer(),
                resultsTreeVisualizer()
        ).run();
        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    }
}