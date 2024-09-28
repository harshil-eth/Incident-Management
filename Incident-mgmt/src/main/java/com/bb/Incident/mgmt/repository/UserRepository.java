package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUuid(String uuid);

    void deleteByUuid(String uuid);

    boolean existsByUsername(String username);
}
