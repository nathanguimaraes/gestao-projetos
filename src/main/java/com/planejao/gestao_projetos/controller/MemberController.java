package com.planejao.gestao_projetos.controller;

import com.planejao.gestao_projetos.dto.MemberDTO;
import com.planejao.gestao_projetos.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @Operation(summary = "Cria novo membro")
    @PostMapping
    public MemberDTO create(@Valid @RequestBody CreateMemberRequest request) {
        return service.createMember(request.nome(), request.cargo());
    }

    @Operation(summary = "Obtém membro por ID")
    @GetMapping("/{id}")
    public MemberDTO get(@PathVariable Long id) {
        MemberDTO member = service.getMember(id);
        if (member == null) {
            throw new RuntimeException("Membro não encontrado");
        }
        return member;
    }

    @Operation(summary = "Lista todos os membros")
    @GetMapping
    public List<MemberDTO> getAll() {
        return service.getAllMembers();
    }

    @Operation(summary = "Lista membros por cargo")
    @GetMapping("/cargo/{cargo}")
    public List<MemberDTO> getByCargo(@PathVariable String cargo) {
        return service.getMembersByCargo(cargo);
    }

    @Operation(summary = "Teste - Verifica se membros estão carregados")
    @GetMapping("/test")
    public Map<String, Object> test() {
        List<MemberDTO> allMembers = service.getAllMembers();
        List<MemberDTO> gerentes = service.getMembersByCargo("gerente");
        List<MemberDTO> funcionarios = service.getMembersByCargo("funcionario");

        Map<String, Object> result = new HashMap<>();
        result.put("total", allMembers.size());
        result.put("gerentes", gerentes.size());
        result.put("funcionarios", funcionarios.size());
        result.put("allMembers", allMembers);

        return result;
    }

    public record CreateMemberRequest(String nome, String cargo) {}
}