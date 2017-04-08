package cc.blynk.integration.https;

import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.web.Role;
import cc.blynk.server.core.model.web.UserInvite;
import cc.blynk.utils.JsonParser;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 24.12.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class InvitationAPITest extends APIBaseTest {

    @Test
    public void sendInvitationNotAuthorized() throws Exception {
        String email = "dmitriy@blynk.cc";
        HttpPost inviteReq = new HttpPost(httpsAdminServerUrl + "/organization/invite");
        List <NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("name", "Dmitriy"));
        nvps.add(new BasicNameValuePair("role", "SUPER_ADMIN"));
        inviteReq.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = httpclient.execute(inviteReq)) {
            assertEquals(401, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void sendInvitationForNonExistingOrganization() throws Exception {
        login(admin.email, admin.pass);

        String email = "dmitriy@blynk.cc";
        HttpPost inviteReq = new HttpPost(httpsAdminServerUrl + "/organization/invite");
        String data = JsonParser.mapper.writeValueAsString(new UserInvite(100, email, "Dmitriy", Role.STAFF));
        inviteReq.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(inviteReq)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
            assertEquals("{\"error\":{\"message\":\"Wrong organization id.\"}}", consumeText(response));
        }
    }

    @Test
    public void userCantSendInvitation() throws Exception {
        login(regularUser.email, regularUser.pass);

        String email = "dmitriy@blynk.cc";
        HttpPost inviteReq = new HttpPost(httpsAdminServerUrl + "/organization/invite");
        String data = JsonParser.mapper.writeValueAsString(new UserInvite(1, email, "Dmitriy", Role.STAFF));
        inviteReq.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(inviteReq)) {
            assertEquals(401, response.getStatusLine().getStatusCode());
            assertEquals("{\"error\":{\"message\":\"You are not allowed to perform this action.\"}}", consumeText(response));
        }
    }

    @Test
    public void sendInvitationFromSuperUser() throws Exception {
        login(admin.email, admin.pass);

        String email = "dmitriy@blynk.cc";
        HttpPost inviteReq = new HttpPost(httpsAdminServerUrl + "/organization/invite");
        String data = JsonParser.mapper.writeValueAsString(new UserInvite(1, email, "Dmitriy", Role.STAFF));
        inviteReq.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(inviteReq)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
        }

        verify(mailWrapper).sendHtml(eq(email), eq("Invitation to Blynk dashboard."), contains("/dashboard/invite?token="));
    }

    @Test
    public void invitationLandingWorks() throws Exception {
        HttpGet inviteGet = new HttpGet(httpsAdminServerUrl + "/invite?token=123");

        try (CloseableHttpResponse response = httpclient.execute(inviteGet)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void invitationFullFlow() throws Exception {
        login(admin.email, admin.pass);

        int orgId = 1;
        String email = "dmitriy@blynk.cc";
        String name = "Dmitriy";
        Role role = Role.STAFF;

        HttpPost inviteReq = new HttpPost(httpsAdminServerUrl + "/organization/invite");
        String data = JsonParser.mapper.writeValueAsString(new UserInvite(orgId, email, name, role));
        inviteReq.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(inviteReq)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
        }

        ArgumentCaptor<String> bodyArgumentCapture = ArgumentCaptor.forClass(String.class);
        verify(mailWrapper, timeout(1000).times(1)).sendHtml(eq(email), eq("Invitation to Blynk dashboard."), bodyArgumentCapture.capture());
        String body = bodyArgumentCapture.getValue();

        String url = body.substring(body.indexOf("https"), body.length() - 3);

        String token = body.substring(body.indexOf("=") + 1, body.length() - 3);
        assertEquals(32, token.length());

        url = url.replace("knight-qa.blynk.cc", "localhost:" + httpsPort);
        assertTrue(url.startsWith("https://localhost:" + httpsPort + "/invite?token="));

        verify(mailWrapper).sendHtml(eq(email), eq("Invitation to Blynk dashboard."), contains("/invite?token="));

        HttpGet inviteGet = new HttpGet(url);

        //we don't need cookie from initial login here
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(initUnsecuredSSLContext(), new MyHostVerifier());
        CloseableHttpClient newHttpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        try (CloseableHttpResponse response = newHttpClient.execute(inviteGet)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
        }

        HttpPost loginRequest = new HttpPost(httpsAdminServerUrl + "/invite");
        List <NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("token", token));
        nvps.add(new BasicNameValuePair("password", "123"));
        loginRequest.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = newHttpClient.execute(loginRequest)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            Header cookieHeader = response.getFirstHeader("set-cookie");
            assertNotNull(cookieHeader);
            assertTrue(cookieHeader.getValue().startsWith("session="));
            User user = JsonParser.parseUserFromString(consumeText(response));
            assertNotNull(user);
            assertEquals(email, user.email);
            assertEquals(name, user.name);
            assertEquals("123", user.pass);
            assertEquals(role, user.role);
            assertEquals(orgId, user.orgId);
        }
    }


}
