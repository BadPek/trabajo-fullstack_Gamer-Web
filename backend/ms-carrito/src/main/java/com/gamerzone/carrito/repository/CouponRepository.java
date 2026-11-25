package com.gamerzone.carrito.repository;

import java.util.Optional;

import com.gamerzone.carrito.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, String> {

    Optional<Coupon> findByCodeIgnoreCaseAndActive(String code, int active);
}
