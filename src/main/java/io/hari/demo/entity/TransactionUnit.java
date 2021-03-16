package io.hari.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

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
public class TransactionUnit extends BaseEntity {
    Long userId;

    BigDecimal paidByMe;
    BigDecimal myShareValue;
    BigDecimal finalValue;
}
