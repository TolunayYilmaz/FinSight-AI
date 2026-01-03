package com.exchange.trader.repository;
import com.exchange.trader.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Role,Long> {


    @Query("SELECT r FROM Role r WHERE r.authority = :authority")
    Optional<Role> findRole(@Param("authority") String authority);
}
