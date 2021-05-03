package com.norway.mastercard.ibpts.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.norway.mastercard.ibpts.dao.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

}
