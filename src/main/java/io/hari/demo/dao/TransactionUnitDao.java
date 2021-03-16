package io.hari.demo.dao;

import io.hari.demo.entity.TransactionUnit;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Repository
public interface TransactionUnitDao extends BaseDao<TransactionUnit> {
    List<TransactionUnit> findAllByUserId(Long userId);
}
