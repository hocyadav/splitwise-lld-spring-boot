package io.hari.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @Author Hariom Yadav
 * @create 16-03-2021
 */
@NoRepositoryBean
public interface BaseDao<T> extends JpaRepository<T, Long> {
}
