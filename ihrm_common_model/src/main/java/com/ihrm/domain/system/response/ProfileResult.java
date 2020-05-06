package com.ihrm.domain.system.response;

import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import lombok.Getter;
import lombok.Setter;
import org.crazycake.shiro.AuthCachePrincipal;

import java.io.Serializable;
import java.util.*;

/**
 * 因为认证的时候需要将这个对象作为安全数据存放到redis中，所以需要实现Serializable,AuthCachePrincipal 这两个类
 */
@Setter
@Getter
public class ProfileResult implements Serializable,AuthCachePrincipal {
    private String userId;
    private String mobile;
    private String username;
    private String company;
    private String companyId;
    private Map<String,Object> roles = new HashMap<>();

    /**
     *  如果用户是saas管理员或者是企业管理员，就不需要根据角色查询权限，只需要查询所有权限就可以了
     * @param user
     */
    public ProfileResult(User user, List<Permission> list) {
        this.mobile = user.getMobile();
        this.username = user.getUsername();
        this.company = user.getCompanyName();
        this.companyId = user.getCompanyId();
        this.userId = user.getId();

        Set<String> menus = new HashSet<>();
        Set<String> points = new HashSet<>();
        Set<String> apis = new HashSet<>();

        for (Permission perm : list) {
            String code = perm.getCode();
            if(perm.getType() == 1) {
                menus.add(code);
            }else if(perm.getType() == 2) {
                points.add(code);
            }else {
                apis.add(code);
            }
        }
        this.roles.put("menus",menus);
        this.roles.put("points",points);
        this.roles.put("apis",apis);
    }


    public ProfileResult(User user) {
        this.mobile = user.getMobile();
        this.username = user.getUsername();
        this.company = user.getCompanyName();
        this.companyId = user.getCompanyId();
        this.userId = user.getId();
        Set<Role> roles = user.getRoles();

        Set<String> menus = new HashSet<>();
        Set<String> points = new HashSet<>();
        Set<String> apis = new HashSet<>();

        for (Role role : roles) {
            Set<Permission> perms = role.getPermissions();
            for (Permission perm : perms) {
                String code = perm.getCode();
                if(perm.getType() == 1) {
                    menus.add(code);
                }else if(perm.getType() == 2) {
                    points.add(code);
                }else {
                    apis.add(code);
                }
            }
        }

        this.roles.put("menus",menus);
        this.roles.put("points",points);
        this.roles.put("apis",apis);
    }

    @Override
    public String getAuthCacheKey() {
        return null;
    }
}
