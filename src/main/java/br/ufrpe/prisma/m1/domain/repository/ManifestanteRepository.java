package br.ufrpe.prisma.m1.domain.repository;

import br.ufrpe.prisma.m1.domain.model.Manifestante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ManifestanteRepository extends JpaRepository<Manifestante, UUID> {
}