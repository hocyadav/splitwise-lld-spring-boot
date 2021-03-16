package io.hari.demo;

import io.hari.demo.config.AppConfig;
import io.hari.demo.dao.TransactionDao;
import io.hari.demo.dao.UserDao;
import io.hari.demo.dao.TransactionUnitDao;
import io.hari.demo.entity.*;
import io.hari.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.hari.demo.entity.Util.getTransactionUnit;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    AppConfig config;

    @Autowired
    TransactionDao transactionDao;

    @Autowired
    TransactionUnitDao transactionUnitDao;

    @Autowired
    TransactionService transactionService;

    @Autowired
    UserDao userDao;

    @Override
    public void run(String... args) throws Exception {

        //todo DONE: create 4 users
        final User rishi = User.builder().name("rishi").build();
        final User rajat = User.builder().name("rajat").build();
        final User hariom = User.builder().name("hariom").build();
        final User omprakash = User.builder().name("omprakash").build();
        final User chandan = User.builder().name("chandan").build();

        userDao.save(rishi);
        userDao.save(rajat);
        userDao.save(hariom);
        userDao.save(omprakash);
        userDao.save(chandan);

        //todo :DONE create a expense 30Rs, between 3 users, paid by single user
        BigDecimal paidByMe = BigDecimal.valueOf(30);
        BigDecimal myShareValue = BigDecimal.valueOf(10);
        final TransactionUnit transactionUnit1 = getTransactionUnit(hariom, paidByMe, myShareValue);

        paidByMe = BigDecimal.ZERO;
        myShareValue = BigDecimal.valueOf(10);//same as above
        final TransactionUnit transactionUnit2 = getTransactionUnit(rajat, paidByMe, myShareValue);
        final TransactionUnit transactionUnit3 = getTransactionUnit(omprakash, paidByMe, myShareValue);

        final BigDecimal total = BigDecimal.valueOf(30);//total is independent of paidByMe and myShareValue
        Transaction transaction1 = Transaction.builder().total(total).description("kachori")
                .transactionUnits(Arrays.asList(transactionUnit1, transactionUnit2, transactionUnit3))
                .build();
        transactionService.createTransaction(transaction1);

        //todo DONE : get giveOr take View map  (used in many places like in Priority Queue etc))
        final Map<Long, List<GiveOrTakeView>> takeOrGiveMap = transactionService.singleTxnGiveOrTakeView(1L);
        System.out.println("takeOrGiveMap = " + takeOrGiveMap);

        //todo DONE: how much to give or take for above txn - single txn - global level
        final Map<Long, BigDecimal> longBigDecimalMap = transactionService.userExpensesForSingleTxn(transaction1.getId());
        System.out.println("for txn 1longBigDecimalMap = " + longBigDecimalMap);

        //todo DONE: user2 -201-> user1 + see its expenses for users
        final Transaction transaction2 = transactionService.createSettleBetween2Users(omprakash, rajat, BigDecimal.valueOf(201));
        final Map<Long, BigDecimal> longBigDecimalMap2 = transactionService.userExpensesForSingleTxn(transaction2.getId());
        System.out.println("for txn 2 longBigDecimalMap2 = " + longBigDecimalMap2);

        //todo DONE: global - for all txn find expense for each user
        final Map<Long, BigDecimal> longBigDecimalMap1 = transactionService.allTransactionUserExpenses();
        final Map<Long, BigDecimal> longBigDecimalMap4 = transactionService.allTransactionUserExpenses_usingStream();
        System.out.println("longBigDecimalMap4 = " + longBigDecimalMap4);
//        allTxnUserExpenses = {2=-211.00, 3=20.00, 4=191.00}

        //todo DONE: seattle some amount from one user to another and check the total expenses map
        transactionService.createSettleBetween2Users(rajat.getId(), omprakash.getId(), BigDecimal.valueOf(100));

        final Map<Long, BigDecimal> longBigDecimalMap3 = transactionService.allTransactionUserExpenses();
        System.out.println("longBigDecimalMap5 = " + transactionService.allTransactionUserExpenses_usingStream());
//        allTxnUserExpenses = {2=-111.00, 3=20.00, 4=91.00}

        //todo DONE: using stream find all transactions units group by user and reduce its final amount
        final Map<Long, List<TransactionUnit>> collect = transactionDao.findAll().stream()
                .flatMap(i -> i.getTransactionUnits().stream()).collect(Collectors.groupingBy(
                        i -> i.getUserId()
                ));
        System.out.println("collect = " + collect);
        final Map<Long, BigDecimal> collect1 = transactionDao.findAll().stream()
                .flatMap(i -> i.getTransactionUnits().stream()).collect(Collectors.groupingBy(
                i -> i.getUserId(),
                Collectors.reducing(BigDecimal.ZERO, i -> i.getFinalValue(), BigDecimal::add)
        ));
        System.out.println("collect1 = " + collect1); //same output as allTransactionUserExpenses()


        //todo DONE: find single txn user level expenses using stream
        final Transaction transaction = transactionDao.findById(1L).orElseGet(() -> new Transaction());
        final Map<Long, BigDecimal> collect2 = transaction.getTransactionUnits().stream().collect(Collectors.groupingBy(
                i -> i.getUserId(),
                Collectors.reducing(BigDecimal.ZERO, i -> i.getFinalValue(), BigDecimal::add)
        ));
        System.out.println("for txn 1  = " + collect2);

        final Map<Long, BigDecimal> longBigDecimalMap5 = transactionService.userExpensesForSingleTxn_usingStream(2L);
        System.out.println("for txn 2  = " + longBigDecimalMap5);

        //todo create 2 priority queue one for negative top list and another for positive top list
        PriorityQueue<TransactionUnit> priorityQueue =
                new PriorityQueue<>((a, b) -> b.getFinalValue().compareTo(a.getFinalValue()));//decreasing order

        //1. find all txn units
        final List<TransactionUnit> allTxnUnits = transactionDao.findAll().stream().filter(Objects::nonNull)
                .flatMap(txn -> txn.getTransactionUnits().stream()).collect(Collectors.toList());

        //2. add to PQ
        allTxnUnits.forEach(t -> priorityQueue.add(t));
        //3. print PQ : done
        while (!priorityQueue.isEmpty()) {
            final TransactionUnit poll = priorityQueue.poll();
            System.out.println("poll = " + poll);
        }

        //create different PQ with GiveOrTakeView as obj
        final Map<Long, List<GiveOrTakeView>> takeOrGiveMap2 = transactionService.singleTxnGiveOrTakeView(1L);
        System.out.println("takeOrGiveMa2p = " + takeOrGiveMap2);
        takeOrGiveMap2.forEach((k, v) -> {
            System.out.println("k = " + k);
            System.out.println("v = " + v);
        });

        PriorityQueue<GiveOrTakeView> pq = new PriorityQueue<>((a, b) -> b.getGiveOrTakeAmount().compareTo(a.getGiveOrTakeAmount()));
        final Map<Long, List<GiveOrTakeView>> longListMap = transactionService.allTxnGiveOrTakeView();
        System.out.println("longListMap = " + longListMap);
        final List<GiveOrTakeView> giveOrTakeViews = longListMap.entrySet().stream().flatMap(i -> i.getValue().stream()).collect(Collectors.toList());
        System.out.println("giveOrTakeViews = " + giveOrTakeViews);

        giveOrTakeViews.forEach(g -> pq.add(g));
        while (!pq.isEmpty()) {
            System.out.println("pq = " + pq.poll());
        }


    }

    private void practiceStream(List<GiveOrTakeView> collect) {
        final Map<Long, List<GiveOrTakeView>> collect2 = collect.stream()
                .collect(Collectors.groupingBy(i -> i.getCurrentUserId()));
        System.out.println("collect2 = " + collect2);

        final Map<Long, Long> collect1 = collect.stream()
                .collect(Collectors.groupingBy(i -> i.getCurrentUserId(), Collectors.counting()));
        System.out.println("collect1 = " + collect1);
    }
}
