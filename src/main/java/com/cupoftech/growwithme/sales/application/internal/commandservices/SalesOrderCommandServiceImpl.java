package com.cupoftech.growwithme.sales.application.internal.commandservices;

import com.cupoftech.growwithme.sales.domain.model.aggregates.FarmerProductPrice;
import com.cupoftech.growwithme.sales.domain.model.aggregates.SalesOrder;
import com.cupoftech.growwithme.sales.domain.model.commands.*;
import com.cupoftech.growwithme.sales.domain.model.entities.SalesOrderItem;
import com.cupoftech.growwithme.sales.domain.model.valueobjects.InvoiceId;
import com.cupoftech.growwithme.sales.domain.services.SalesOrderCommandService;
import com.cupoftech.growwithme.sales.infrastructure.persistence.jpa.repositories.FarmerProductRepository;
import com.cupoftech.growwithme.sales.infrastructure.persistence.jpa.repositories.SalesOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SalesOrderCommandServiceImpl implements SalesOrderCommandService {
    private final SalesOrderRepository salesOrderRepository;
    private final FarmerProductRepository farmerProductRepository;

    public SalesOrderCommandServiceImpl(SalesOrderRepository salesOrderRepository, FarmerProductRepository farmerProductRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.farmerProductRepository = farmerProductRepository;
    }

    @Override
    @Transactional
    public Optional<SalesOrder> handle(CreateSalesOrderCommand command) {
        if(salesOrderRepository.existsByInvoiceId(new InvoiceId(command.invoiceId()))){
            throw new IllegalArgumentException("Invoice already exists");
        }

        var salesOrder = new SalesOrder(command.ruc(),  command.orderTimestamp(), command.invoiceId());
        try {
            salesOrder = salesOrderRepository.save(salesOrder);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving sales order: " + e.getMessage());
        }
        return Optional.of(salesOrder);
    }

    @Override
    @Transactional
    public Optional<SalesOrder>  handle(UpdateSalesOrderCommand command) {

        var result= salesOrderRepository.findById(command.SalesOrderId());
        if(result.isEmpty())
            throw new IllegalArgumentException("Sales Order not found");
        var salesOrderToUpdate = result.get();
        try {
            var salesOrderUpdate = salesOrderRepository.save(salesOrderToUpdate.updateSalesOrder(command.ruc(), command.orderTimestamp(), command.invoiceId()));
            return Optional.of(salesOrderUpdate);
        } catch (Exception e){
            throw new IllegalArgumentException("Error updating sales order: " + e.getMessage());

        }


    }

    @Override
    public void handle(DeleteSalesOrderCommand command) {
        SalesOrder salesOrder = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Sales Order not found"));
        salesOrderRepository.delete(salesOrder);
    }

    @Override
    public void handle(AddFarmerProductToSalesOrderCommand command) {
        var salesOrderOptional = salesOrderRepository.findById(command.salesOrderId());
        var farmerProductOptional = farmerProductRepository.findById(command.farmerProductPriceId());

        if(salesOrderOptional.isEmpty() || farmerProductOptional.isEmpty()) {
            throw new IllegalArgumentException("Sales Order or Farmer Product not found");
        }

        try {
            var salesOrder = salesOrderOptional.get();
            var farmerProduct = farmerProductOptional.get();
            salesOrder.addItem(farmerProduct);
            salesOrder.getLastItemSalesOrder().setSalesOrder(salesOrder);
            salesOrderRepository.save(salesOrder);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving sales order: " + e.getMessage());
        }

    }
    @Override
    public void handle(RemoveProductFromSalesOrderCommand command) {
        SalesOrder salesOrder = salesOrderRepository.findById(command.SalesOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Sales Order not found"));
        FarmerProductPrice removeProductPrice = farmerProductRepository.findById(command.farmerProductPriceId())
                .orElseThrow(() -> new IllegalArgumentException("Farmer Product Price not found"));
        SalesOrderItem itemToRemove = salesOrder.getSalesOrderItems().stream()
                .filter(item -> item.getFarmerProductPrice().equals(removeProductPrice))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sales Order Item not found"));
        salesOrder.removeItem(itemToRemove);
        salesOrderRepository.save(salesOrder);
    }

    @Override
    public void handle(PendSalesOrderCommand command) {
        salesOrderRepository.findById(command.salesOrderId()).map(salesOrder -> {
            salesOrder.pend();
            return salesOrderRepository.save(salesOrder);
        }).orElseThrow(() -> new IllegalArgumentException("Sales Order not found"));
    }

    @Override
    public void handle(ConfirmSalesOrderCommand command) {
        salesOrderRepository.findById(command.salesOrderId()).map(salesOrder -> {
            salesOrder.confirm();
            return salesOrderRepository.save(salesOrder);
        }).orElseThrow(() -> new IllegalArgumentException("Sales Order not found"));
    }

    @Override
    public void handle(RejectSalesOrderCommand command) {
        salesOrderRepository.findById(command.salesOrderId()).map(salesOrder -> {
            salesOrder.reject();
            return salesOrderRepository.save(salesOrder);
        }).orElseThrow(() -> new IllegalArgumentException("Sales Order not found"));
    }

    @Override
    public void handle(CancelSalesOrderCommand command) {
        salesOrderRepository.findById(command.salesOrderId()).map(salesOrder -> {
            salesOrder.cancel();
            return salesOrderRepository.save(salesOrder);
        }).orElseThrow(() -> new IllegalArgumentException("Sales Order not found"));
    }

}