package com.silverpeas.socialnetwork.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.silverpeas.socialnetwork.model.AccountId;
import com.silverpeas.socialnetwork.model.ExternalAccount;

public interface ExternalAccountDao extends JpaRepository<ExternalAccount, AccountId> {

  List<ExternalAccount> findBySilverpeasUserId(@Param("silverpeasUserId") String silverpeasUserId);
}

