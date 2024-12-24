package faang.school.payment_service.repository.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOperationRepository extends JpaRepository<PaymentOperation, Long> {
    Optional<PaymentOperation> findByIdAndStatus(Long id, PaymentStatus status);



    Optional<PaymentOperation> findByIdAndOperationType(Long id, PaymentOperationType type);
    @Query("SELECT p FROM PaymentOperation p " +
            "WHERE p.operationType = :type " +
            "AND p.clearScheduledAt <= :dateTime " +
            "AND p.clearScheduledAt IS NOT NULL " +
            "ORDER BY p.clearScheduledAt")
    List<PaymentOperation> findScheduledPaymentsForConfirmation(
            @Param("type") PaymentOperationType type,
            @Param("dateTime") LocalDateTime dateTime
    );
}