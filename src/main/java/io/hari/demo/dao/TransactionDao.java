package io.hari.demo.dao;

import io.hari.demo.entity.BaseEntity;
import io.hari.demo.entity.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Repository
public interface TransactionDao extends BaseDao<Transaction> {
    List<Transaction> findAllByTransactionUnits_Id(Long transactionId);
}
