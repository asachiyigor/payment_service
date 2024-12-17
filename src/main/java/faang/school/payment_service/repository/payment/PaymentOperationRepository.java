package faang.school.payment_service.repository.payment;

import faang.school.payment_service.dto.PaymentOperationDto;
import faang.school.payment_service.dto.PaymentStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentOperationRepository extends CrudRepository<PaymentOperationDto, Long> {
    Optional<PaymentOperationDto> findByIdAndStatus(Long id, PaymentStatus status);
}