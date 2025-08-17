package com.planejao.gestao_projetos.controller;

import com.planejao.gestao_projetos.domain.StatusProjeto;
import com.planejao.gestao_projetos.dto.*;
import com.planejao.gestao_projetos.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Operation(summary = "Lista projetos com paginação e filtro por status")
    @GetMapping
    public Page<ProjectDTO> list(Pageable pageable, @RequestParam Optional<String> status) {
        Optional<StatusProjeto> statusFilter = status.map(s -> {
            try {
                return StatusProjeto.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido");
            }
        });
        return service.list(pageable, statusFilter);
    }

    @Operation(summary = "Obtém projeto por ID")
    @GetMapping("/{id}")
    public ProjectDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Cria novo projeto")
    @PostMapping
    public ProjectDTO create(@Valid @RequestBody ProjectRequestDTO dto) {
        return service.create(dto);
    }

    @Operation(summary = "Atualiza projeto")
    @PutMapping("/{id}")
    public ProjectDTO update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateDTO dto) {
        return service.update(id, dto);
    }

    @Operation(summary = "Exclui projeto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Altera status do projeto")
    @PatchMapping("/{id}/status")
    public ProjectDTO changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatusStr = body.get("status");
        if (newStatusStr == null) {
            throw new IllegalArgumentException("Status requerido");
        }
        StatusProjeto newStatus = StatusProjeto.valueOf(newStatusStr.toUpperCase());
        return service.changeStatus(id, newStatus);
    }

    @Operation(summary = "Adiciona membro ao projeto")
    @PostMapping("/{id}/members/{memberId}")
    public ProjectDTO addMember(@PathVariable Long id, @PathVariable Long memberId) {
        return service.addMember(id, memberId);
    }

    @Operation(summary = "Remove membro do projeto")
    @DeleteMapping("/{id}/members/{memberId}")
    public ProjectDTO removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        return service.removeMember(id, memberId);
    }

    @Operation(summary = "Gera relatório do portfólio")
    @GetMapping("/report")
    public ReportDTO report() {
        return service.getReport();
    }
}