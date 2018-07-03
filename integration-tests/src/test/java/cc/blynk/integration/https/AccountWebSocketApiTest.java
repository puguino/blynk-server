package cc.blynk.integration.https;

import cc.blynk.integration.model.websocket.AppWebSocketClient;
import cc.blynk.server.core.model.Profile;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.auth.UserStatus;
import cc.blynk.server.core.model.web.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static cc.blynk.utils.AppNameUtil.BLYNK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 24.12.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountWebSocketApiTest extends APIBaseTest {

    @Before
    public void init() throws Exception {
        super.init();
    }

    @After
    public void shutdown() {
        super.shutdown();
    }

    @Test
    public void getOwnProfileNotAuthorized() throws Exception {
        AppWebSocketClient appWebSocketClient = defaultClient();
        appWebSocketClient.start();
        appWebSocketClient.getAccount();
        while (!appWebSocketClient.isClosed()) {
            sleep(50);
        }
        assertTrue(appWebSocketClient.isClosed());
    }

    @Test
    public void getOwnProfileWorks() throws Exception {
        AppWebSocketClient appWebSocketClient = loggedDefaultClient(regularUser);
        appWebSocketClient.getAccount();
        User user = appWebSocketClient.parseAccount();
        assertNotNull(user);
        assertEquals("user@blynk.cc", user.email);
        assertEquals("user@blynk.cc", user.name);
    }

    @Test
    public void getOwnProfileReturnOnlySpecificFields() throws Exception {
        AppWebSocketClient appWebSocketClient = loggedDefaultClient(admin);
        appWebSocketClient.getAccount();
        User user = appWebSocketClient.parseAccount();
        assertNotNull(user);
        assertEquals("admin@blynk.cc", user.email);
        assertEquals("admin@blynk.cc", user.name);
        assertEquals(BLYNK, user.appName);
        assertNull(user.pass);
        assertEquals(Role.SUPER_ADMIN, user.role);
        assertEquals(UserStatus.Active, user.status);
        assertEquals(new Profile(), user.profile);
    }

    @Test
    public void updateOwnProfileWorks() throws Exception {
        AppWebSocketClient appWebSocketClient = loggedDefaultClient(admin);
        admin.name = "123@123.com";
        appWebSocketClient.updateAccount(admin);
        appWebSocketClient.getAccount();
        User updatedUser = appWebSocketClient.parseAccount(2);
        assertNotNull(updatedUser);
        assertEquals("admin@blynk.cc", updatedUser.email);
        assertEquals( "123@123.com",  updatedUser.name);
    }

}
