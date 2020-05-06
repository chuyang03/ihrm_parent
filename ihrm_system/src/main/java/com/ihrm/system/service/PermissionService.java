package com.ihrm.system.service;

import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.BeanMapUtils;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.PermissionApi;
import com.ihrm.domain.system.PermissionMenu;
import com.ihrm.domain.system.PermissionPoint;
import com.ihrm.system.dao.PermissionApiDao;
import com.ihrm.system.dao.PermissionDao;
import com.ihrm.system.dao.PermissionMenuDao;
import com.ihrm.system.dao.PermissionPointDao;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PermissionService {

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private PermissionMenuDao permissionMenuDao;

    @Autowired
    private PermissionPointDao permissionPointDao;

    @Autowired
    private PermissionApiDao permissionApiDao;

    @Autowired
    private IdWorker idWorker;

    //添加新权限
    public void save(Map<String, Object> map) throws Exception {

        String id = idWorker.nextId() + "";

        //通过map构造permisssion对象
        Permission perm = BeanMapUtils.mapToBean(map, Permission.class);
        perm.setId(id);
        Integer permType = perm.getType();

        //根据类型不同构造不同的资源对象
        switch (permType){
            //保存菜单资源
            case PermissionConstants.PERMISSION_MENU:

                //从map中得到的数据构造对象
                PermissionMenu permissionMenu = BeanMapUtils.mapToBean(map, PermissionMenu.class);
                permissionMenu.setId(id);
                permissionMenuDao.save(permissionMenu);
                break;

            //保存按钮资源
            case PermissionConstants.PERMISSION_POINT:

                PermissionPoint permissionPoint = BeanMapUtils.mapToBean(map, PermissionPoint.class);
                permissionPoint.setId(id);
                permissionPointDao.save(permissionPoint);
                break;

            //保存api资源
            case PermissionConstants.PERMISSION_API:

                PermissionApi permissionApi = BeanMapUtils.mapToBean(map, PermissionApi.class);
                permissionApi.setId(id);

                permissionApiDao.save(permissionApi);
                break;

            default:
                throw new CommonException(ResultCode.FAIL);
        }

        permissionDao.save(perm);

    }

    //修改权限信息
    public void update(Map<String, Object> map) throws Exception{

        //通过map构造permisssion对象
        Permission perm = BeanMapUtils.mapToBean(map, Permission.class);

        //获取该数据库中的权限
        Permission permission = permissionDao.findById(perm.getId()).get();
        //对数据库中的权限信息进行修改
        permission.setCode(perm.getCode());
        permission.setDescription(perm.getDescription());
        permission.setEnVisible(perm.getEnVisible());
        permission.setName(perm.getName());

        int permType = perm.getType();

        //根据类型不同构造不同的资源对象
        switch (permType){
            //保存菜单资源
            case PermissionConstants.PERMISSION_MENU:

                //从map中得到的数据构造对象
                PermissionMenu permissionMenu = BeanMapUtils.mapToBean(map, PermissionMenu.class);
                permissionMenu.setId(perm.getId());
                permissionMenuDao.save(permissionMenu);
                break;

            //保存按钮资源
            case PermissionConstants.PERMISSION_POINT:

                PermissionPoint permissionPoint = BeanMapUtils.mapToBean(map, PermissionPoint.class);
                permissionPoint.setId(perm.getId());
                permissionPointDao.save(permissionPoint);
                break;

            //保存api资源
            case PermissionConstants.PERMISSION_API:

                PermissionApi permissionApi = BeanMapUtils.mapToBean(map, PermissionApi.class);
                permissionApi.setId(perm.getId());

                permissionApiDao.save(permissionApi);
                break;

            default:
                throw new CommonException(ResultCode.FAIL);
        }

        permissionDao.save(perm);
    }

    /**
     *   id共用
     *
     * 3.根据id查询
     *      //1.查询权限
     *      //2.根据权限的类型查询资源
     *      //3.构造map集合
     */
    public Map<String, Object> findById(String id) throws Exception {

        Permission perm = permissionDao.findById(id).get();

        //因为最后返回的json数据是根据不同的权限类型和父权限信息组合起来的，所以需要根据type进行判断，这个id是哪种权限
        int type = perm.getType();

        //使用Object接收权限对象
        Object object = null;

        if (type == PermissionConstants.PERMISSION_MENU){
            //根据id查询相应的权限
            object = permissionMenuDao.findById(id);
        }else if (type == PermissionConstants.PERMISSION_POINT){

            object = permissionPointDao.findById(id);
        }else if (type == PermissionConstants.PERMISSION_API){

            object = permissionApiDao.findById(id);
        }else {

            throw new CommonException(ResultCode.FAIL);
        }

        //需要构造返回的数据，其中包含父权限，和子权限的信息
        Map<String, Object> map = BeanMapUtils.beanToMap(object);

        map.put("name", perm.getName());
        map.put("type", perm.getType());
        map.put("code", perm.getCode());
        map.put("description", perm.getDescription());
        map.put("pid", perm.getPid());
        map.put("enVisible", perm.getEnVisible());


        return map;


    }

    /**
     * 4.查询全部
     * type      : 查询全部权限列表type：0：菜单 + 按钮（权限点） 1：菜单2：按钮（权限点）3：API接口
     * enVisible : 0：查询所有saas平台的最高权限，1：查询企业的权限
     * pid ：父id
     */
    public List<Permission> findAll(Map<String, Object> map){

        //1.构造查询条件
        Specification<Permission> spec = new Specification<Permission>() {

            /**
             * 动态拼接查询条件
             *
             */
            @Override
            public Predicate toPredicate(Root<Permission> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> list = new ArrayList<>();
                //根据父id构造查询条件
                if (!StringUtils.isEmpty(map.get("pid"))){

                    list.add(criteriaBuilder.equal(root.get("pid").as(String.class), (String)map.get("pid")));
                }

                //根据enVisible构造查询条件
                if (!StringUtils.isEmpty(map.get("enVisible"))){

                    list.add(criteriaBuilder.equal(root.get("enVisible").as(String.class), (String)map.get("enVisible")));
                }

                //根据type
                if (!StringUtils.isEmpty(map.get("type"))){

                    String ty = (String)map.get("type");
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("type"));

                    //type：0：菜单 + 按钮（权限点）,如果type为0，需要使用in的范围查询菜单和按钮
                    //查询条件就是   type in(1, 2)
                    if ("0".equals(ty)){

                        in.value(1).value(2);
                    }else {

                        //根据类型查询
                        in.value(Integer.parseInt(ty));
                    }

                    //要把查询条件加入到list中
                    list.add(in);
                }

                //并列构造的查询条件
                return criteriaBuilder.and(list.toArray(new Predicate[list.size()]));
            }
        };

        return permissionDao.findAll(spec);
    }

    //根据id删除权限
    //1。首先删除权限
    //2。删除权限包含的资源，菜单，按钮，api
    public void deleteById(String id) throws Exception {

        //获取该数据库中的权限
        Permission permission = permissionDao.findById(id).get();
        permissionDao.delete(permission);

        int permType = permission.getType();

        //根据类型不同构造不同的资源对象
        switch (permType){
            //删除菜单资源
            case PermissionConstants.PERMISSION_MENU:
                permissionMenuDao.deleteById(id);

                break;

            //删除按钮资源
            case PermissionConstants.PERMISSION_POINT:
                permissionPointDao.deleteById(id);

                break;

            //删除api资源
            case PermissionConstants.PERMISSION_API:
                permissionApiDao.deleteById(id);

                break;

            default:
                throw new CommonException(ResultCode.FAIL);
        }

    }
}
