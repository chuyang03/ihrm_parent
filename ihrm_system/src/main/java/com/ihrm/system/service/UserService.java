package com.ihrm.system.service;

import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.QiniuUploadUtil;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.system.dao.RoleDao;
import com.ihrm.system.dao.UserDao;
import com.ihrm.system.feignclient.DepartmentFeignClient;
import com.ihrm.system.utils.BaiduAiUtil;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    //添加新用户
    public void save(User user){

        //前端获取的用户信息不带有主键id,所以需要设置主键id，使用雪花算法
        String id = idWorker.nextId() + "";
        user.setId(id);

        //对密码进行加密
        String password = new Md5Hash("123456", user.getMobile(), 3).toString();
        user.setPassword(password);//设置初始密码
        user.setEnableState(1);

        userDao.save(user);
    }

    //修改用户信息
    public void update(User user){

        //首先获取该用户在数据库中的
        User target = userDao.findById(user.getId()).get();

        //设置用户信息
        target.setUsername(user.getUsername());
        target.setPassword(user.getPassword());
        target.setDepartmentId(user.getDepartmentId());
        target.setDepartmentName(user.getDepartmentName());


        userDao.save(target);
    }

    //根据id查询部门信息
    public User findById(String id){

        User user = userDao.findById(id).get();

        return user;


    }

    /**
     * 查询全部用户列表
     *
     *      但是有可能根据不同的参数查询不同的用户列表，比如企业id，部门id，参数不同查询出来的用户列表也不一样
     *      所以在此，设计使用map封装这些可能的查询参数
     *
     *      参数：map集合的方式
     *          hasDept
     *          departmentId
     *          companyId
     *
     */
    public Page findAll(Map<String, Object> map, int page, int size){

        //1.构造查询条件
        Specification<User> spec = new Specification<User>() {

            /**
             * 动态拼接查询条件
             *
             * @param root
             * @param criteriaQuery
             * @param criteriaBuilder
             * @return
             */
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> list = new ArrayList<>();
                //根据请求的companyId是否为空构造查询条件
                if (!StringUtils.isEmpty(map.get("companyId"))){

                    list.add(criteriaBuilder.equal(root.get("companyId").as(String.class), (String)map.get("companyId")));
                }

                //根据请求的部门id构造查询条件
                if (!StringUtils.isEmpty(map.get("departmentId"))){

                    list.add(criteriaBuilder.equal(root.get("departmentId").as(String.class), (String)map.get("departmentId")));
                }

                if (!StringUtils.isEmpty(map.get("hasDept"))){

                    //根据请求的hasDept判断  是否分配部门 0未分配（departmentId = null），1 已分配 （departmentId ！= null）
                    if ("0".equals((String)map.get("hasDept"))){

                        list.add(criteriaBuilder.isNull(root.get("departmentId")));
                    }else {

                        //root.get("departmentId")   这个语句就是获取实体类属性名称
                        list.add(criteriaBuilder.isNotNull(root.get("departmentId")));
                    }
                }



                //并列构造的查询条件
                return criteriaBuilder.and(list.toArray(new Predicate[list.size()]));
            }
        };

        //2。分页
        // new PageRequest(page-1, size)  page表示当前页码，因为page默认是从0开始的，但是传入参数是从1开始的，size表示页码大小

        Page<User> userPage = userDao.findAll(spec, new PageRequest(page-1, size));

        return userPage;
    }

    //根据id删除用户
    public void deleteById(String id){

        userDao.deleteById(id);
    }

    //给用户分配角色
    public void assignRoles(String userId, List<String> roleIds) {

        //1.根据id查询用户
        User user = userDao.findById(userId).get();

        //2.设置角色集合
        Set<Role> roles = new HashSet<>();

        for (String roleId: roleIds) {

            Role role = roleDao.findById(roleId).get();
            roles.add(role);
        }

        //设置用户和角色的关系
        user.setRoles(roles);

        //3.更新用户信息，用户就包含了角色集合，也就是把这些角色分配给用户
        userDao.save(user);

    }

    public User findByMobile(String mobile) {

        return userDao.findByMobile(mobile);
    }

    //根据导入的excel表格数据，批量添加用户
    @Transactional
    public void saveAll(List<User> list, String companyId, String companyName) {

        for (User user : list) {

            //设置默认密码
            user.setPassword(new Md5Hash("123456", user.getMobile(), 3).toString());
            user.setId(idWorker.nextId()+"");

            //用户基本属性
            user.setCompanyId(companyId);
            user.setCompanyName(companyName);
            user.setEnableState(1);
            //在职还是离职
            user.setInServiceStatus(1);
            user.setLevel("user");

            //设置用户所属的部门信息,user.getDepartmentId()刚才从excel设置值的时候设置的是部门标识code
            Department department = departmentFeignClient.findByCode(user.getDepartmentId(), companyId);
            if (department != null){

                user.setDepartmentId(department.getId());
                user.setDepartmentName(department.getName());
            }

            userDao.save(user);
        }
    }

    /**
     * 使用dataurl的方式，将图片数据存储到数据库，完成用户头像图片上传
     * 占用数据库空间，不好
     * @param id        ：用户id
     * @param file      ：用户上传的头像文件
     * @return          ：返回一个图片访问地址
     */
//    public String uploadImage(String id, MultipartFile file) throws IOException {
//
//        //根据用户id查询用户，将图片加密之后设置到用户的属性当中，然后保存到数据库
//        User user = userDao.findById(id).get();
//
//        String encode = "data:image/png;base64,"+Base64.encode(file.getBytes());
//
//        user.setStaffPhoto(encode);
//
//        userDao.save(user);
//        return encode;
//    }

    @Autowired
    private BaiduAiUtil baiduAiUtil;
    /**
     * 使用七牛云作为图片服务器，将图片上传到七牛云
     *
     * 注册到百度云ai人脸库：
     * 1.首先需要判断是否已经注册了
     * 2.如果注册过了，更新人脸库
     * 3.如果没有注册，注册
     */
    
    public String uploadImageQiniu(String id, MultipartFile file) throws IOException {
        //这个id表示用户id
        User user = userDao.findById(id).get();
        
        //上传到七牛云,使用用户id作为上传到七牛云显示的图片名称
        String imgUrl = new QiniuUploadUtil().upload(user.getId(), file.getBytes());
        //保存到数据库的就是图片在七牛云的访问地址
        user.setStaffPhoto(imgUrl);
        userDao.save(user);

        //判断百度云AI人脸库是否已经注册过头像了,传入函数的id是用户id
        Boolean aBoolean = baiduAiUtil.faceExist(id);
        //将图像进行base64编码
        String imgBase64 = Base64.encode(file.getBytes());
        if (aBoolean){
            //更新
            baiduAiUtil.faceUpdate(id,imgBase64);

        }else {
            //注册
            baiduAiUtil.faceRegister(id,imgBase64);

        }

        return imgUrl;
    }
}
