package faang.school.payment_service.mapper.payment;


import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PaymentMapper {

    PaymentOperationDto toDto(PaymentOperation paymentOperation);

    @Mapping(source = "ownerId", target = "senderAccountId")
    @Mapping(source = "recipientId", target = "recipientAccountId")
    PaymentOperation toEntity(PaymentOperationDto paymentOperationDto);
}