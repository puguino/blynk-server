package cc.blynk.server.web.handlers.logic.organization.roles;

import cc.blynk.server.Holder;
import cc.blynk.server.api.http.dashboard.dto.RoleDTO;
import cc.blynk.server.core.PermissionBasedLogic;
import cc.blynk.server.core.dao.OrganizationDao;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.permissions.PermissionsTable;
import cc.blynk.server.core.model.permissions.Role;
import cc.blynk.server.core.model.serialization.JsonParser;
import cc.blynk.server.core.model.web.Organization;
import cc.blynk.server.core.protocol.exceptions.JsonException;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.core.session.web.WebAppStateHolder;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.server.internal.CommonByteBufUtil.makeUTF8StringMessage;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 22.11.18.
 */
public final class WebEditRoleLogic implements PermissionBasedLogic<WebAppStateHolder> {

    private final OrganizationDao organizationDao;
    private final SessionDao sessionDao;

    public WebEditRoleLogic(Holder holder) {
        this.organizationDao = holder.organizationDao;
        this.sessionDao = holder.sessionDao;
    }

    @Override
    public boolean hasPermission(Role role) {
        return role.canEditRole();
    }

    @Override
    public int getPermission() {
        return PermissionsTable.ROLE_EDIT;
    }

    @Override
    public void messageReceived0(ChannelHandlerContext ctx, WebAppStateHolder state, StringMessage message) {
        int orgId = state.selectedOrgId;
        RoleDTO roleDTO = JsonParser.readAny(message.body, RoleDTO.class);
        if (roleDTO == null) {
            throw new JsonException("Could not parse the role.");
        }

        log.debug("{} updates role {} for orgId {}.", state.user.email, roleDTO, orgId);
        Organization org = organizationDao.getOrgByIdOrThrow(orgId);
        Role updatedRole = roleDTO.toRole();
        org.updateRole(updatedRole);

        Session session = sessionDao.getOrgSession(orgId);
        session.applyRoleChanges(updatedRole);

        if (ctx.channel().isWritable()) {
            String roleString = updatedRole.toString();
            ctx.writeAndFlush(makeUTF8StringMessage(message.command, message.id, roleString),
                    ctx.voidPromise());
        }
    }

}
