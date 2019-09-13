package org.cgoro.mappers;

import org.cgoro.db.entity.Account;
import org.cgoro.model.AccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class );

    AccountDTO accountToDTO(Account account);
}
