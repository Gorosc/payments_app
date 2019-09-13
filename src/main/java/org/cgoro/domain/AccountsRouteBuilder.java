package org.cgoro.domain;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.cgoro.db.dao.AccountDAO;
import org.cgoro.exception.NotFoundException;
import org.cgoro.transform.AccountTransformBean;

import javax.persistence.NoResultException;

public class AccountsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(NotFoundException.class).handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404")).setBody().constant("Not Found");

        from("direct:getAccounts")
                .process(exchange -> {
                    AccountDAO accountDAO = (AccountDAO) exchange.getContext().getRegistry().lookupByName("accountDAO");
                    exchange.getIn().setBody(accountDAO.getAll());
                })
                .bean(AccountTransformBean.class);

        from("direct:getAccount")
                .process(exchange -> {
                    String accountId = exchange.getIn().getHeader("id", String.class);
                    AccountDAO accountDAO = (AccountDAO) exchange.getContext().getRegistry().lookupByName("accountDAO");
                    try {
                        exchange.getIn().setBody(accountDAO.find(accountId));
                    } catch (NoResultException e) {
                        log.error("Unknown Account Id");
                        throw new NotFoundException();
                    }
                })
                .bean(AccountTransformBean.class);
    }
}
