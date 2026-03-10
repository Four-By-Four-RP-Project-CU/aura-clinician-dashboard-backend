package com.aura.clinician.provider.cu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.ScoreItem;
import com.aura.clinician.domain.CaseInputDocument;
import com.aura.clinician.provider.ScoreProvider;
import com.aura.clinician.repository.CaseInputRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CuScoreProvider implements ScoreProvider {
    private final CaseInputRepository caseInputRepository;

    @Override
    public List<ScoreItem> getScores(String caseId) {
        List<ScoreItem> scores = new ArrayList<>();
        CaseInputDocument input = caseInputRepository.findByCaseId(caseId).orElse(null);
        double uctValue = input != null && input.getScores() != null ? input.getScores().getUct() : 10.0;
        double aectValue = input != null && input.getScores() != null ? input.getScores().getAect() : 8.0;

        ScoreItem uct = new ScoreItem();
        uct.setCode("UCT");
        uct.setLabel("Urticaria Control Test");
        uct.setValue(uctValue);
        uct.setInterpretation("Partially controlled (target >= 12)");
        uct.setWarning(uctValue < 12);
        scores.add(uct);

        ScoreItem aect = new ScoreItem();
        aect.setCode("AECT");
        aect.setLabel("Angioedema Control Test");
        aect.setValue(aectValue);
        aect.setInterpretation("Moderate impact on daily life");
        aect.setWarning(aectValue < 10);
        scores.add(aect);

        return scores;
    }
}
