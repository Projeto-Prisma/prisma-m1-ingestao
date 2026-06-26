package br.ufrpe.prisma.m1.domain.repository;

import br.ufrpe.prisma.m1.domain.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {
    Optional<Denuncia> findByProtocolo(String protocolo);
    
    List<Denuncia> findByStatus(Status status);
}