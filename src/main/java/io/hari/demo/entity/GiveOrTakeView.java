package io.hari.demo.entity;

import lombok.*;

import java.math.BigDecimal;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GiveOrTakeView {
    Long currentUserId;
    Long targetUserId;
    BigDecimal giveOrTakeAmount;
    String giveOrTakeStr;
}
