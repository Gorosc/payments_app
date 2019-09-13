package org.cgoro.transform;

import org.cgoro.db.entity.Account;
import org.cgoro.mappers.AccountMapper;
import org.cgoro.model.AccountDTO;

import java.util.List;
import java.util.stream.Collectors;

public class AccountTransformBean {

    public List<AccountDTO> transform(List<Account> accounts) {
        return accounts.stream().map(this::transform).collect(Collectors.toList());
    }

    public AccountDTO transform(Account account) {
        return AccountMapper.INSTANCE.accountToDTO(account);
    }
}
