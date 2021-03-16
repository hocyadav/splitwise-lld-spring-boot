package io.hari.demo.entity;

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {}, callSuper = true)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    BigDecimal total;
    String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "txn_id")
    List<TransactionUnit> transactionUnits = new ArrayList<>();//1

    //other metadata

    //below all code move to txn service

    @Transient
    Map<Long, List<GiveOrTakeView>> takeOrGiveMap = new HashMap<>();//2

    public List<TransactionUnit> getNegativeTxnUnitList() {//3
        return this.getTransactionUnits().stream()
                .filter(i -> i.getFinalValue().compareTo(BigDecimal.ZERO) < 0).collect(Collectors.toList());
    }

    public List<TransactionUnit> getPositiveTxnUnitList() {//3
        return this.getTransactionUnits().stream()
                .filter(i -> i.getFinalValue().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
    }

    public Map<Long, List<GiveOrTakeView>> getTakeOrGiveMap() {//4
        if (CollectionUtils.isEmpty(this.takeOrGiveMap.entrySet())) {
            updateGiveOrTakeMap();
        }
        return takeOrGiveMap;
    }

    private void updateGiveOrTakeMap() {//4
        this.getTransactionUnits().forEach(transactionUnit -> {
            final Long userId = transactionUnit.getUserId();
            takeOrGiveMap.putIfAbsent(userId, new LinkedList<>());

            final int compareTo = transactionUnit.getFinalValue().compareTo(BigDecimal.ZERO);
            if (compareTo < 0) { //than only one person has paid all bills
                this.getPositiveTxnUnitList().forEach(txnUnit -> {
                    final GiveOrTakeView giveOrTake =
                            getGiveOrTakeView(transactionUnit.getUserId(), txnUnit.getUserId(), transactionUnit.getFinalValue(), "give");
                    this.takeOrGiveMap.get(userId).add(giveOrTake);
                });
            } else if (compareTo > 0) {
                this.getNegativeTxnUnitList().forEach(txnUnit -> {
                    final GiveOrTakeView giveOrTake =
                            getGiveOrTakeView(transactionUnit.getUserId(), txnUnit.getUserId(), txnUnit.getFinalValue().negate(), "collect");
                    this.takeOrGiveMap.get(userId).add(giveOrTake);
                });
            }
        });
    }

    private GiveOrTakeView getGiveOrTakeView(Long currentUserId, Long targetUserId, BigDecimal finalValue, String type) {
        return GiveOrTakeView.builder()
                .currentUserId(currentUserId)
                .targetUserId(targetUserId)
                .giveOrTakeAmount(finalValue)
                .giveOrTakeStr(type)
                .build();
    }
}
