package com.planejao.gestao_projetos.service;

import com.planejao.gestao_projetos.domain.Member;
import com.planejao.gestao_projetos.dto.MemberDTO;
import com.planejao.gestao_projetos.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    public MemberDTO createMember(String nome, String cargo) {
        logger.info("Criando membro: {} - {}", nome, cargo);
        Member member = new Member();
        member.setNome(nome);
        member.setCargo(cargo);
        Member saved = repository.save(member);
        logger.info("Membro criado com ID: {}", saved.getId());
        return toDTO(saved);
    }

    public MemberDTO getMember(Long id) {
        logger.debug("Buscando membro com ID: {}", id);
        Optional<Member> member = repository.findById(id);
        MemberDTO result = member.map(this::toDTO).orElse(null);
        if (result == null) {
            logger.warn("Membro não encontrado com ID: {}", id);
        }
        return result;
    }

    public List<MemberDTO> getAllMembers() {
        logger.debug("Buscando todos os membros");
        List<MemberDTO> members = repository.findAll().stream()
                .map(this::toDTO)
                .toList();
        logger.debug("Encontrados {} membros", members.size());
        return members;
    }

    public List<MemberDTO> getMembersByCargo(String cargo) {
        logger.debug("Buscando membros com cargo: {}", cargo);
        List<MemberDTO> members = repository.findByCargo(cargo).stream()
                .map(this::toDTO)
                .toList();
        logger.debug("Encontrados {} membros com cargo {}", members.size(), cargo);
        return members;
    }

    private MemberDTO toDTO(Member member) {
        return new MemberDTO(member.getId(), member.getNome(), member.getCargo());
    }
}