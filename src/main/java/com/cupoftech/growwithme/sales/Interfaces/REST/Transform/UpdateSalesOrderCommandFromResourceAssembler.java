package com.cupoftech.growwithme.sales.Interfaces.REST.Transform;

import com.cupoftech.growwithme.sales.Domain.Model.Commands.UpdateSalesOrderCommand;
import com.cupoftech.growwithme.sales.Interfaces.REST.Resources.UpdateSalesOrderResource;

public class UpdateSalesOrderCommandFromResourceAssembler {
    public static UpdateSalesOrderCommand toCommandFromResource(Long salesOrderId, UpdateSalesOrderResource resource) {
        return new UpdateSalesOrderCommand(salesOrderId,resource.ruc(), resource.orderTimestamp(), resource.invoiceId());
    }
}
