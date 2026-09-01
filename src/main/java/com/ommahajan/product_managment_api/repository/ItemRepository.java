package com.ommahajan.product_managment_api.repository;

import com.ommahajan.product_managment_api.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    Page<Item> findByProductId(Integer productId, Pageable pageable);
}