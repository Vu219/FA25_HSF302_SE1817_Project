package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.CarriageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarriageTypeRepository extends JpaRepository<CarriageType, Integer> {
    boolean existsByTypeName(String typeName);

    boolean existsByTypeNameAndCarriageTypeId(String typeName, Integer carriageTypeId);
}
