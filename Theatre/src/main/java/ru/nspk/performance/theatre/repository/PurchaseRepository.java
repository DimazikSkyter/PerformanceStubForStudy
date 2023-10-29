package ru.nspk.performance.theatre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nspk.performance.theatre.entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
