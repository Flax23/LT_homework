import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer.*;

import java.io.IOException;
import java.time.Duration;
import samplers.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class FinalHomeWorkTest {

    AdminLogin adminLogin = new AdminLogin();
    UserCreation userCreation = new UserCreation();


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
                threadGroup("UC1", 1, 1,
                        vars().set("USER_LOGIN", "user_${__Random(1,100000,)}"),
                        vars().set("USER_PASSWORD", "${__RandomString(10,abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789)}"),
                        transaction("TC: Лог - Авторизация админа",
                                adminLogin.get(baseUrl),
                                userCreation.get(baseUrl)
                                // добавить задержку 1-5
                        )
                ),
                influxDbListener("http://127.0.0.1:8086/write?db=test")
                        .title("Test")
                        .samplersRegex(".*")
                        .tag("nodeName", "${__machineName()}")
                        .percentiles(90, 95)
                        .application("HOMEWORK")
                        .measurement("HOMEWORK"),
                dashboardVisualizer(),
                resultsTreeVisualizer()
        ).run();
        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    }
}