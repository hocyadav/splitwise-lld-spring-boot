package io.hari.demo.entity;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
public class Util {

    public static TransactionUnit getTransactionUnit(User user, BigDecimal paidByMe, BigDecimal myShareValue) {
        return TransactionUnit.builder()
                .userId(user.getId())
                .paidByMe(paidByMe)
                .myShareValue(myShareValue)
                .finalValue(paidByMe.subtract(myShareValue))
                .build();
    }

    //not working
    public static void updateGiveOrTakeMap(Transaction transaction) {
        final List<TransactionUnit> positiveTxnUnitList = transaction.getPositiveTxnUnitList();
        final List<TransactionUnit> negativeTxnUnitList = transaction.getNegativeTxnUnitList();
        final Map<Long, List<GiveOrTakeView>> takeOrGiveMap = transaction.getTakeOrGiveMap();

        transaction.getTransactionUnits().forEach(transactionUnit -> {
            final Long userId = transactionUnit.getUserId();
            takeOrGiveMap.putIfAbsent(userId, new LinkedList<>());

            final int compareTo = transactionUnit.getFinalValue().compareTo(BigDecimal.ZERO);
            if (compareTo < 0) { //than only one person has paid all bills
                positiveTxnUnitList.forEach(txnUnit -> {
                    final GiveOrTakeView giveOrTake =
                            getGiveOrTakeView(transactionUnit.getUserId(), txnUnit.getUserId(), transactionUnit.getFinalValue(), "give");
                    takeOrGiveMap.get(userId).add(giveOrTake);
                });
            } else if (compareTo > 0) {
                negativeTxnUnitList.forEach(txnUnit -> {
                    final GiveOrTakeView giveOrTake =
                            getGiveOrTakeView(transactionUnit.getUserId(), txnUnit.getUserId(), txnUnit.getFinalValue().negate(), "collect");
                    takeOrGiveMap.get(userId).add(giveOrTake);
                });
            }
        });
    }

    public static GiveOrTakeView getGiveOrTakeView(Long currentUserId, Long targetUserId, BigDecimal finalValue, String type) {
        return GiveOrTakeView.builder()
                .currentUserId(currentUserId)
                .targetUserId(targetUserId)
                .giveOrTakeAmount(finalValue)
                .giveOrTakeStr(type)
                .build();
    }

}
