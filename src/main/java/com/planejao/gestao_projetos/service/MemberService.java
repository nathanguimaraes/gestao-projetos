package com.planejao.gestao_projetos.service;

import com.planejao.gestao_projetos.dto.MemberDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberService {
    private final Map<Long, MemberDTO> members = new HashMap<>();
    private long nextId = 1;

    public MemberDTO createMember(String nome, String cargo) {
        MemberDTO member = new MemberDTO(nextId, nome, cargo);
        members.put(nextId, member);
        nextId++;
        return member;
    }

    public MemberDTO getMember(Long id) {
        return members.get(id);
    }
}