package samplers;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class UserCreation {

    public DslSimpleController get(String baseUrl) {
        return simpleController("Создание пользователя ->",
                httpHeaders()
                        .header("Sec-GPC", "1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("DNT", "1"),
                httpSampler("<_/system_settings", baseUrl + "/system_settings/")
                        .header("Referer", baseUrl + "/tickets")
                        .method(HTTPConstants.GET),
                httpSampler("<_/admin/auth/user", baseUrl + "/admin/auth/user/")
                        .header("Referer", baseUrl + "/system_settings/")
                        .method(HTTPConstants.GET),
                httpSampler("<_/admin/auth/user/add", baseUrl + "/admin/auth/user/add/")
                        .header("Referer", baseUrl + "/admin/auth/user/")
                        .method(HTTPConstants.GET),
                httpSampler(">_/admin/auth/user/add", baseUrl + "/admin/auth/user/add/")
                        .header("Referer", baseUrl + "/admin/auth/user/add")
                        .header("Origin", baseUrl)
                        .method(HTTPConstants.POST)
                        .param("username", "${USER_LOGIN}")
                        .param("password1", "${USER_PASSWORD}")
                        .param("password2", "${USER_PASSWORD}")
                        .param("csrfmiddlewaretoken", "${CSRF}")
                        .param("_save", "Save")
                        .children(
                                regexExtractor("USER_ID", "href=\"/admin/auth/user/(.*)/change/\"")
                                        .template("$1$")
                                        .matchNumber(1)
                                        .defaultValue("ID_ERROR"),
                                jsr223PostProcessor(s -> {
                                    String username = s.vars.get("USER_LOGIN");
                                    String password = s.vars.get("USER_PASSWORD");
                                    // путь к файлу CSV
                                    String path = testResource("users.csv").filePath();
                                    // Записываем значения переменных в файл CSV
                                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
                                        writer.write(username + "," + password);
                                        writer.newLine();
                                    } catch (IOException e) {
                                        System.err.println("Ошибка при записи в файл: " + e);
                                    }
                                })
                        ),
                httpSampler("<_/admin/auth/user/___USER_ID___/change", baseUrl + "/admin/auth/user/${USER_ID}/change/")
                        .header("Referer", baseUrl + "/admin/auth/user/${USER_ID}/change/")
                        .method(HTTPConstants.GET),
                httpSampler(">_/admin/auth/user/___USER_ID___/change", baseUrl + "/admin/auth/user/${USER_ID}/change/")
                        .header("Referer", baseUrl + "/admin/auth/user/${USER_ID}/change/")
                        .header("Origin", baseUrl)
                        .method(HTTPConstants.POST)
                        .param("csrfmiddlewaretoken", "${CSRF}")
                        .param("username", "${USER_LOGIN}")
                        .param("is_active", "on")
                        .param("is_staff", "on")
                        .param("date_joined_0", "2024-04-2")
                        .param("date_joined_1", "13:30:52")
                        .param("initial-date_joined_0", "2024-04-2")
                        .param("initial-date_joined_1", "13:30:52")
                        .param("save", "Save"),
                httpSampler("<_/admin/auth/user", baseUrl + "/admin/auth/user/")
                        .header("Referer", baseUrl + "/admin/auth/user/${USER_ID}/change/")
                        .method(HTTPConstants.GET)

        );
    }
}
