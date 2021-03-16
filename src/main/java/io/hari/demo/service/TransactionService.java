package io.hari.demo.service;

import io.hari.demo.dao.TransactionDao;
import io.hari.demo.entity.GiveOrTakeView;
import io.hari.demo.entity.Transaction;
import io.hari.demo.entity.TransactionUnit;
import io.hari.demo.entity.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Service
public class TransactionService {
    @Autowired
    TransactionDao transactionDao;

    public void createTransaction(Transaction transaction) {
        transactionDao.save(transaction);
    }

    //create 1 txn between 2 users
    public Transaction createSettleBetween2Users(User user1, User user2, BigDecimal settledAmount) {
        final Transaction transaction = createSettleBetween2Users(user1.getId(), user2.getId(), settledAmount);
        return transaction;
    }

    public Transaction createSettleBetween2Users(Long userid1, Long userId2, BigDecimal settledAmount) {
        final TransactionUnit transactionUnit1 = getUserTxn(userid1, settledAmount, BigDecimal.ZERO);
        final TransactionUnit transactionUnit2 = getUserTxn(userId2, BigDecimal.ZERO, settledAmount);
        Transaction transaction = Transaction.builder()
                .total(settledAmount)
                .transactionUnits(Arrays.asList(transactionUnit1, transactionUnit2))
                .build();
        final Transaction transaction1 = transactionDao.save(transaction);
        return transaction1;
    }

    public Map<Long, BigDecimal> allTransactionUserExpenses_usingStream() {
        final Map<Long, BigDecimal> map = transactionDao.findAll().stream().flatMap(i -> i.getTransactionUnits().stream())
                .collect(Collectors.groupingBy(i -> i.getUserId(),
                        Collectors.reducing(BigDecimal.ZERO, i -> i.getFinalValue(), BigDecimal::add)));
        return map;
    }

    public Map<Long, BigDecimal> allTransactionUserExpenses() {
        final List<Transaction> transactions = transactionDao.findAll();
        if (CollectionUtils.isEmpty(transactions)) return new HashMap<>();

        Collection<Map.Entry<Long, BigDecimal>> unionResult = new HashSet<>();
        for (Transaction t : transactions) {
            final Map<Long, BigDecimal> fetchedResult = userExpensesForSingleTxn(t.getId());
            unionResult = CollectionUtils.union(unionResult, fetchedResult.entrySet());
            System.out.println("unionResult = " + unionResult);
        }
        final Map<Long, BigDecimal> allTxnUserExpenses = unionResult.stream().collect(Collectors.groupingBy(
                obj -> obj.getKey(),
                Collectors.reducing(BigDecimal.ZERO, //initial value
                        obj -> obj.getValue(), //mapper
                        BigDecimal::add) //binary operator applied on mapper
        ));
        System.out.println("allTxnUserExpenses = " + allTxnUserExpenses);
        return allTxnUserExpenses;
    }

    public Map<Long, BigDecimal> userExpensesForSingleTxn_usingStream(long transactionId) {
        final Transaction transaction = transactionDao.findById(transactionId).orElseGet(() -> new Transaction());
        final Map<Long, BigDecimal> map = transaction.getTransactionUnits().stream().collect(Collectors.groupingBy(
                i -> i.getUserId(),
                Collectors.reducing(BigDecimal.ZERO, i -> i.getFinalValue(), BigDecimal::add)
        ));
        return map;
    }

    public Map<Long, BigDecimal> userExpensesForSingleTxn(long transactionId) {
        System.out.println("transactionId = " + transactionId);
        final Transaction transaction = transactionDao.findById(transactionId).get();
        if (transaction == null) return new HashMap<>();

        Map<Long, List<GiveOrTakeView>> takeOrGiveMap = transaction.getTakeOrGiveMap();
        final List<GiveOrTakeView> giveOrTakeViews = takeOrGiveMap.entrySet().stream().flatMap(i -> i.getValue().stream())
                .peek(i -> System.out.println("i = " + i)).collect(Collectors.toList());
        final Map<Long, BigDecimal> userExpensesFor1Txn = giveOrTakeViews.stream().collect(Collectors.groupingBy(
                obj -> obj.getCurrentUserId(), Collectors.reducing(BigDecimal.ZERO, obj -> obj.getGiveOrTakeAmount(), BigDecimal::add)));
        return userExpensesFor1Txn;
    }

    public Map<Long, List<GiveOrTakeView>> singleTxnGiveOrTakeView(long transactionId) {
        final Transaction transaction = transactionDao.findById(transactionId).get();
        if (transaction == null) return new HashMap<>();

        final Map<Long, List<GiveOrTakeView>> takeOrGiveMap = transaction.getTakeOrGiveMap();
        return takeOrGiveMap;
    }

    public Map<Long, List<GiveOrTakeView>> allTxnGiveOrTakeView() {
        final List<Transaction> all = transactionDao.findAll();
        Map<Long, List<GiveOrTakeView>> map = new HashMap<>();
        for (Transaction i : all) {
            final Map<Long, List<GiveOrTakeView>> takeOrGiveMap = i.getTakeOrGiveMap();
            takeOrGiveMap.forEach((key, value) -> {
                map.putIfAbsent(key, new LinkedList<>());
                value.forEach(x -> map.get(key).add(x));
            });
        }
        return map;
    }

    private TransactionUnit getUserTxn(Long userId, BigDecimal paidByMe, BigDecimal myShareValue) {
        return TransactionUnit.builder()
                .userId(userId)
                .paidByMe(paidByMe)
                .myShareValue(myShareValue)
                .finalValue(paidByMe.subtract(myShareValue))
                .build();
    }
}
