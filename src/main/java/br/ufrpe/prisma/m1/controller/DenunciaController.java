package br.ufrpe.prisma.m1.controller;

import br.ufrpe.prisma.m1.domain.dto.DenunciaDTO;
import br.ufrpe.prisma.m1.domain.dto.DenunciaResponseDTO;
import br.ufrpe.prisma.m1.domain.enums.Status;
import br.ufrpe.prisma.m1.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaService denunciaService;

    @PostMapping
    public ResponseEntity<DenunciaResponseDTO> criar(@Valid @RequestBody DenunciaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaService.criarDenuncia(dto));
    }

    @GetMapping
    public ResponseEntity<List<DenunciaResponseDTO>> listarTodas() {
        List<DenunciaResponseDTO> lista = denunciaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DenunciaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(denunciaService.buscarPorId(id));
    }

    @GetMapping("/protocolo/{protocolo}")
    public ResponseEntity<DenunciaResponseDTO> buscarPorProtocolo(@PathVariable String protocolo) {
        return ResponseEntity.ok(denunciaService.buscarPorProtocolo(protocolo));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DenunciaResponseDTO>> listarPorStatus(@PathVariable Status status) {
        return ResponseEntity.ok(denunciaService.listarPorStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable UUID id, @RequestParam Status novoStatus) {
        denunciaService.atualizarStatus(id, novoStatus);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assunto")
    public ResponseEntity<Void> confirmarAssunto(@PathVariable UUID id, @RequestParam String assuntoFinal) {
        denunciaService.confirmarAssunto(id, assuntoFinal);
        return ResponseEntity.noContent().build();
    }
}