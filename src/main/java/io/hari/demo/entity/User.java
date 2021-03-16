package io.hari.demo.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {})
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    String name;

    //other metadata
}
