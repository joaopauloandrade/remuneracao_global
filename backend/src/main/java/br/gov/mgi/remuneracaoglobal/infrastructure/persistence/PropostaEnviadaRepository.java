package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropostaEnviadaRepository extends JpaRepository<PropostaEnviadaEntity, Long> {

    Optional<PropostaEnviadaEntity> findFirstByCasoIdOrderByVersaoDesc(Long casoId);
}
