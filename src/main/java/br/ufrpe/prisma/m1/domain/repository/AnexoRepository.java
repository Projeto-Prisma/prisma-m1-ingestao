package br.ufrpe.prisma.m1.domain.repository;

import br.ufrpe.prisma.m1.domain.model.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, UUID> {
    // Método para buscar todos os anexos de uma determinada denúncia
    List<Anexo> findByDenunciaId(UUID denunciaId);
}