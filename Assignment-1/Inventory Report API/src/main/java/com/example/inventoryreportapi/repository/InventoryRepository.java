package com.example.inventoryreportapi.repository;

import com.example.inventoryreportapi.entity.Inventory;
import com.example.inventoryreportapi.entity.InventoryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface InventoryRepository  extends JpaRepository<Inventory, Integer> {

    @Query("SELECT i FROM Inventory i WHERE i.purchaseDt BETWEEN :startDate AND :endDate")
    public List<Inventory> findByPurchaseDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
        );
}
