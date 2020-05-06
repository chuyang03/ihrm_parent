package com.ihrm.system.shiro.realm;

import com.ihrm.common.shiro.realm.IhrmRealm;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRealm extends IhrmRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;


    //用户登陆需要通过认证方法
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        //获取用户名和密码
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken)authenticationToken;
        String mobile = usernamePasswordToken.getUsername();
        String password = new String(usernamePasswordToken.getPassword());

        //从数据库中查询用户，比较密码是否一致
        User user = userService.findByMobile(mobile);

        if (user != null && user.getPassword().equals(password)){

            ProfileResult result = null;
            if ("user".equals(user.getLevel())){

                result = new ProfileResult(user);
            }else {

                Map map = new HashMap();
                //如果登陆用户是企业管理员
                if ("coAdmin".equals(user.getLevel())){
                    //这个enVisible的值等于1，表示查询用户所有的企业权限
                    map.put("enVisible", "1");
                }
                //根据map中的内容查询权限
                List<Permission> list = permissionService.findAll(map);

                result = new ProfileResult(user, list);
            }

            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(result, user.getPassword(), this.getName());
            return info;
        }


        return null;
    }
}
