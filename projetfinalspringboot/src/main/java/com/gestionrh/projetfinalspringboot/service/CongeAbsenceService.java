package com.gestionrh.projetfinalspringboot.service;

import com.gestionrh.projetfinalspringboot.model.entity.CongeAbsence;
import com.gestionrh.projetfinalspringboot.repository.CongeAbsenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CongeAbsenceService {
    private final CongeAbsenceRepository congeAbsenceRepository;

    public List<CongeAbsence> findAll() {
        return congeAbsenceRepository.findAll();
    }

    public Optional<CongeAbsence> findById(Long id) {
        return congeAbsenceRepository.findById(id);
    }

    public List<CongeAbsence> findByEmployeId(Long employeId) {
        return congeAbsenceRepository.findByEmployeId(employeId);
    }

    public CongeAbsence save(CongeAbsence congeAbsence) {
        return congeAbsenceRepository.save(congeAbsence);
    }

    public void deleteById(Long id) {
        congeAbsenceRepository.deleteById(id);
    }
}
