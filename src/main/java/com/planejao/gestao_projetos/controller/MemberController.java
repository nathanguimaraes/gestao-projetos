package com.planejao.gestao_projetos.controller;

import com.planejao.gestao_projetos.dto.MemberDTO;
import com.planejao.gestao_projetos.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Cria membro (mock externo)")
    @PostMapping
    public MemberDTO create(@RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        String cargo = body.get("cargo");
        return memberService.createMember(nome, cargo);
    }

    @Operation(summary = "Consulta membro por ID (mock externo)")
    @GetMapping("/{id}")
    public MemberDTO get(@PathVariable Long id) {
        return memberService.getMember(id);
    }
}