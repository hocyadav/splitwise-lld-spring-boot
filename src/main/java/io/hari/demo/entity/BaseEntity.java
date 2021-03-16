package io.hari.demo.entity;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@Getter
@MappedSuperclass
@ToString
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @Version
//    Long version;
}
