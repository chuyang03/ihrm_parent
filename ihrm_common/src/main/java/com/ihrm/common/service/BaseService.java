package com.ihrm.common.service;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class BaseService<T> {

    protected Specification<T> getSpec(String companyId){

        //根据companyId查询，如果条件不是这个不能使用该构造条件
        Specification<T> spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                /**
                 * 用户构造查询条件：
                 * root.get("companyId") 表示获取department类的属性
                 * companyId 表示传入进来的参数
                 *
                 * 构造的条件拼接在sql上就是  company_id = "1"
                 */

                return criteriaBuilder.equal(root.get("companyId").as(String.class), companyId);
            }
        };

        return spec;
    }
}
