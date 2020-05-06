package com.ihrm.company.dao;

import com.ihrm.domain.company.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 部门dao接口
 *
 * JpaRepository<Department, String>  范型第一个参数是操作的实体类名称，第二个参数是实体类对应的主键类型
 */
public interface DepartmentDao extends JpaRepository<Department, String>, JpaSpecificationExecutor<Department> {
    Department findByCodeAndCompanyId(String code, String companyId);
}
