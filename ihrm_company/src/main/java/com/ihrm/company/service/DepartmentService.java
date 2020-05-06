package com.ihrm.company.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.company.dao.DepartmentDao;
import com.ihrm.domain.company.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
public class DepartmentService extends BaseService {

    @Autowired
    private DepartmentDao departmentDao;
    
    @Autowired
    private IdWorker idWorker;

    //添加新部门
    public void save(Department department){

        //前端获取的部门信息不带有主键id,所以需要设置主键id，使用雪花算法
        String id = idWorker.nextId() + "";
        department.setId(id);
        
        departmentDao.save(department);
    }

    //修改部门信息
    public void update(Department department){
        
        //首先获取该部门在数据库中的
        Department dept = departmentDao.findById(department.getId()).get();

        //设置部门属性
        dept.setName(department.getName());
        dept.setCode(department.getCode());
        dept.setIntroduce(department.getIntroduce());

        departmentDao.save(department);
    }

    //根据id查询部门信息
    public Department findById(String id){

        Department department = departmentDao.findById(id).get();

        return department;


    }

    //查询所有部门列表,这个方法需要根据企业id查询当前企业的所有部门
    public List<Department> findAll(String companyId){

        /**
         * 构造sql语句的查询条件
         */
//        Specification<Department> spec = new Specification<Department>() {
//            @Override
//            public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
//
//                /**
//                 * 用户构造查询条件：
//                 * root.get("companyId") 表示获取department类的属性
//                 * companyId 表示传入进来的参数
//                 *
//                 * 构造的条件拼接在sql上就是  company_id = "1"
//                 */
//
//                return criteriaBuilder.equal(root.get("companyId").as(String.class), companyId);
//            }
//        };

        //因为父类中方法实现了sql查询条件的构造，所以只需要在子类中调用方法即可。getSpec(companyId)是父类的方法
        return departmentDao.findAll(getSpec(companyId));
    }

    //根据id删除部门
    public void delete(String id){

        departmentDao.deleteById(id);
    }


    public Department findByCode(String code, String companyId) {

        return departmentDao.findByCodeAndCompanyId(code, companyId);
    }
}
